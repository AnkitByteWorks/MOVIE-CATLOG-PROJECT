package com.movietracker.movieservice.service.impl;

import com.movietracker.movieservice.config.AppProperties;
import com.movietracker.movieservice.dto.response.MovieDetailsResponse;
import com.movietracker.movieservice.dto.response.MovieSummaryResponse;
import com.movietracker.movieservice.dto.response.PagedResponse;
import com.movietracker.movieservice.entity.Movie;
import com.movietracker.movieservice.exception.ResourceNotFoundException;
import com.movietracker.movieservice.mapper.MovieDtoMapper;
import com.movietracker.movieservice.repository.MovieRepository;
import com.movietracker.movieservice.service.TrendingSyncService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("MovieServiceImpl Unit Tests")
class MovieServiceImplTest {

    @Mock
    private MovieRepository movieRepository;

    @Mock
    private CachedMovieLookupService cachedLookupService;

    @Mock
    private TrendingSyncService trendingSyncService;

    private AppProperties appProperties;
    private MovieDtoMapper movieDtoMapper;
    private MovieServiceImpl movieService;

    @BeforeEach
    void setUp() {
        appProperties = new AppProperties();
        movieDtoMapper = new MovieDtoMapper(appProperties);
        movieService = new MovieServiceImpl(
                movieRepository,
                movieDtoMapper,
                cachedLookupService,
                trendingSyncService
        );
    }

    @Test
    @DisplayName("searchMovies cache miss should query MongoDB text index and return mapped page")
    void searchMovies_cacheMiss_shouldQueryMongo() {
        String query = "Inception";
        int page = 1;

        when(cachedLookupService.getCachedMovieSearch(eq(query), eq(page), any()))
                .thenAnswer(invocation -> {
                    Supplier<PagedResponse<MovieSummaryResponse>> supplier = invocation.getArgument(2);
                    return supplier.get();
                });

        Movie movie = Movie.builder()
                .tmdbId(27205L)
                .title("Inception")
                .overview("Mind heist")
                .posterPath("/poster.jpg")
                .backdropPath("/backdrop.jpg")
                .releaseDate(LocalDate.of(2010, 7, 15))
                .voteAverage(8.4)
                .voteCount(35000)
                .genres(List.of("Action", "Sci-Fi"))
                .build();

        when(movieRepository.searchByText(eq(query), eq(PageRequest.of(0, 20))))
                .thenReturn(new PageImpl<>(List.of(movie), PageRequest.of(0, 20), 1));

        PagedResponse<MovieSummaryResponse> result = movieService.searchMovies(query, page);

        assertThat(result).isNotNull();
        assertThat(result.content()).hasSize(1);
        assertThat(result.content().get(0).title()).isEqualTo("Inception");
        assertThat(result.content().get(0).posterUrl()).contains("/poster.jpg");
        verify(movieRepository, times(1)).searchByText(query, PageRequest.of(0, 20));
    }

    @Test
    @DisplayName("searchMovies cache hit should return cached result without calling MongoDB")
    void searchMovies_cacheHit_shouldNotCallMongo() {
        String query = "Interstellar";
        int page = 1;

        MovieSummaryResponse cachedItem = new MovieSummaryResponse(
                157336L, "Interstellar", "Space voyage", "http://image.url", null,
                LocalDate.of(2014, 11, 5), 8.4, 30000, List.of("Sci-Fi"), "MOVIE"
        );
        PagedResponse<MovieSummaryResponse> cachedResponse = PagedResponse.of(List.of(cachedItem), 1, 20, 1);

        when(cachedLookupService.getCachedMovieSearch(eq(query), eq(page), any()))
                .thenReturn(cachedResponse);

        PagedResponse<MovieSummaryResponse> result = movieService.searchMovies(query, page);

        assertThat(result).isNotNull();
        assertThat(result.content()).hasSize(1);
        assertThat(result.content().get(0).title()).isEqualTo("Interstellar");
        verify(movieRepository, never()).searchByText(any(), any());
    }

    @Test
    @DisplayName("searchMovies with empty query should return empty paged response")
    void searchMovies_emptyQuery_shouldReturnEmpty() {
        PagedResponse<MovieSummaryResponse> result = movieService.searchMovies("", 1);
        assertThat(result).isNotNull();
        assertThat(result.content()).isEmpty();
        verify(movieRepository, never()).searchByText(any(), any());
    }

    @Test
    @DisplayName("getMovieDetails should query MongoDB and return mapped details")
    void getMovieDetails_existingId_shouldReturnDetails() {
        Long tmdbId = 27205L;

        when(cachedLookupService.getCachedMovieDetails(eq(tmdbId), any()))
                .thenAnswer(invocation -> {
                    Supplier<MovieDetailsResponse> supplier = invocation.getArgument(1);
                    return supplier.get();
                });

        Movie movie = Movie.builder()
                .tmdbId(tmdbId)
                .title("Inception")
                .overview("Dream within a dream")
                .voteAverage(8.4)
                .build();

        when(movieRepository.findByTmdbId(tmdbId)).thenReturn(Optional.of(movie));

        MovieDetailsResponse response = movieService.getMovieDetails(tmdbId);

        assertThat(response).isNotNull();
        assertThat(response.title()).isEqualTo("Inception");
        verify(movieRepository, times(1)).findByTmdbId(tmdbId);
    }

    @Test
    @DisplayName("getMovieDetails not found should throw ResourceNotFoundException")
    void getMovieDetails_notFound_shouldThrowException() {
        Long tmdbId = 999999L;

        when(cachedLookupService.getCachedMovieDetails(eq(tmdbId), any()))
                .thenAnswer(invocation -> {
                    Supplier<MovieDetailsResponse> supplier = invocation.getArgument(1);
                    return supplier.get();
                });

        when(movieRepository.findByTmdbId(tmdbId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> movieService.getMovieDetails(tmdbId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("getTrending should query MongoDB by popularity descending")
    void getTrending_shouldReturnPopularMovies() {
        int page = 1;
        when(cachedLookupService.getCachedTrendingMovies(eq("day"), eq(page), any()))
                .thenAnswer(invocation -> {
                    Supplier<PagedResponse<MovieSummaryResponse>> supplier = invocation.getArgument(2);
                    return supplier.get();
                });

        Movie movie = Movie.builder().tmdbId(155L).title("The Dark Knight").popularity(150.0).build();
        when(movieRepository.findAllByOrderByPopularityDesc(PageRequest.of(0, 20)))
                .thenReturn(new PageImpl<>(List.of(movie), PageRequest.of(0, 20), 1));

        PagedResponse<MovieSummaryResponse> result = movieService.getTrending("day", page);

        assertThat(result).isNotNull();
        assertThat(result.content()).hasSize(1);
        assertThat(result.content().get(0).title()).isEqualTo("The Dark Knight");
    }

    @Test
    @DisplayName("getRecommendations should query similar movies by genre excluding current movie")
    void getRecommendations_withGenres_shouldReturnSimilarMovies() {
        Long tmdbId = 27205L;

        when(cachedLookupService.getCachedRecommendations(eq(tmdbId), any()))
                .thenAnswer(invocation -> {
                    Supplier<List<MovieSummaryResponse>> supplier = invocation.getArgument(1);
                    return supplier.get();
                });

        Movie movie = Movie.builder()
                .tmdbId(tmdbId)
                .title("Inception")
                .genres(List.of("Action", "Sci-Fi"))
                .build();
        when(movieRepository.findByTmdbId(tmdbId)).thenReturn(Optional.of(movie));

        Movie rec = Movie.builder().tmdbId(157336L).title("Interstellar").build();
        when(movieRepository.findByGenresInAndTmdbIdNot(eq(List.of("Action", "Sci-Fi")), eq(tmdbId), eq(PageRequest.of(0, 20))))
                .thenReturn(new PageImpl<>(List.of(rec)));

        List<MovieSummaryResponse> recs = movieService.getRecommendations(tmdbId);

        assertThat(recs).hasSize(1);
        assertThat(recs.get(0).title()).isEqualTo("Interstellar");
    }
}
