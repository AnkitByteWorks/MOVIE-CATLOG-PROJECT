package com.movietracker.movieservice.service.impl;

import com.movietracker.movieservice.config.AppProperties;
import com.movietracker.movieservice.dto.response.PagedResponse;
import com.movietracker.movieservice.dto.response.TvShowDetailsResponse;
import com.movietracker.movieservice.dto.response.TvShowSummaryResponse;
import com.movietracker.movieservice.entity.TvShow;
import com.movietracker.movieservice.exception.ResourceNotFoundException;
import com.movietracker.movieservice.mapper.MovieDtoMapper;
import com.movietracker.movieservice.repository.TvShowRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
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
@DisplayName("TvShowServiceImpl Unit Tests")
class TvShowServiceImplTest {

    @Mock
    private TvShowRepository tvShowRepository;

    @Mock
    private CachedMovieLookupService cachedLookupService;

    private AppProperties appProperties;
    private MovieDtoMapper movieDtoMapper;
    private TvShowServiceImpl tvShowService;

    @BeforeEach
    void setUp() {
        appProperties = new AppProperties();
        movieDtoMapper = new MovieDtoMapper(appProperties);
        tvShowService = new TvShowServiceImpl(
                tvShowRepository,
                movieDtoMapper,
                cachedLookupService
        );
    }

    @Test
    @DisplayName("searchTv should query MongoDB text index and return mapped TV series")
    void searchTv_shouldQueryMongo() {
        String query = "Breaking Bad";
        int page = 1;

        when(cachedLookupService.getCachedTvSearch(eq(query), eq(page), any()))
                .thenAnswer(invocation -> {
                    Supplier<PagedResponse<TvShowSummaryResponse>> supplier = invocation.getArgument(2);
                    return supplier.get();
                });

        TvShow tvShow = TvShow.builder()
                .tmdbId(1396L)
                .name("Breaking Bad")
                .overview("Chemistry teacher turns cook")
                .firstAirDate(LocalDate.of(2008, 1, 20))
                .voteAverage(8.9)
                .voteCount(14000)
                .genres(List.of("Drama", "Crime"))
                .build();

        when(tvShowRepository.searchByText(eq(query), eq(PageRequest.of(0, 20))))
                .thenReturn(new PageImpl<>(List.of(tvShow), PageRequest.of(0, 20), 1));

        PagedResponse<TvShowSummaryResponse> result = tvShowService.searchTv(query, page);

        assertThat(result).isNotNull();
        assertThat(result.content()).hasSize(1);
        assertThat(result.content().get(0).name()).isEqualTo("Breaking Bad");
        verify(tvShowRepository, times(1)).searchByText(query, PageRequest.of(0, 20));
    }

    @Test
    @DisplayName("searchTv cache hit should not call MongoDB")
    void searchTv_cacheHit_shouldNotCallMongo() {
        String query = "Dark";
        int page = 1;

        TvShowSummaryResponse cached = new TvShowSummaryResponse(
                70523L, "Dark", "Time travel mystery", null, null,
                LocalDate.of(2017, 12, 1), 8.5, 6000, List.of("Mystery"), "TV"
        );
        PagedResponse<TvShowSummaryResponse> cachedResponse = PagedResponse.of(List.of(cached), 1, 20, 1);

        when(cachedLookupService.getCachedTvSearch(eq(query), eq(page), any()))
                .thenReturn(cachedResponse);

        PagedResponse<TvShowSummaryResponse> result = tvShowService.searchTv(query, page);

        assertThat(result).isNotNull();
        assertThat(result.content()).hasSize(1);
        assertThat(result.content().get(0).name()).isEqualTo("Dark");
        verify(tvShowRepository, never()).searchByText(any(), any());
    }

    @Test
    @DisplayName("getTvDetails should return entity from MongoDB")
    void getTvDetails_existingId_shouldReturnDetails() {
        Long tmdbId = 1396L;

        when(cachedLookupService.getCachedTvDetails(eq(tmdbId), any()))
                .thenAnswer(invocation -> {
                    Supplier<TvShowDetailsResponse> supplier = invocation.getArgument(1);
                    return supplier.get();
                });

        TvShow tv = TvShow.builder()
                .tmdbId(tmdbId)
                .name("Breaking Bad")
                .numberOfSeasons(5)
                .numberOfEpisodes(62)
                .build();

        when(tvShowRepository.findByTmdbId(tmdbId)).thenReturn(Optional.of(tv));

        TvShowDetailsResponse response = tvShowService.getTvDetails(tmdbId);

        assertThat(response).isNotNull();
        assertThat(response.name()).isEqualTo("Breaking Bad");
        verify(tvShowRepository, times(1)).findByTmdbId(tmdbId);
    }

    @Test
    @DisplayName("getTvDetails not found should throw ResourceNotFoundException")
    void getTvDetails_notFound_shouldThrowException() {
        Long tmdbId = 999999L;

        when(cachedLookupService.getCachedTvDetails(eq(tmdbId), any()))
                .thenAnswer(invocation -> {
                    Supplier<TvShowDetailsResponse> supplier = invocation.getArgument(1);
                    return supplier.get();
                });

        when(tvShowRepository.findByTmdbId(tmdbId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> tvShowService.getTvDetails(tmdbId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("getTrendingTv should query MongoDB sorted by popularity")
    void getTrendingTv_shouldQueryMongo() {
        TvShow tv = TvShow.builder().tmdbId(1396L).name("Breaking Bad").popularity(200.0).build();
        when(tvShowRepository.findAllByOrderByPopularityDesc(PageRequest.of(0, 20)))
                .thenReturn(new PageImpl<>(List.of(tv), PageRequest.of(0, 20), 1));

        PagedResponse<TvShowSummaryResponse> response = tvShowService.getTrendingTv("day", 1);

        assertThat(response).isNotNull();
        assertThat(response.content()).hasSize(1);
        assertThat(response.content().get(0).name()).isEqualTo("Breaking Bad");
    }
}
