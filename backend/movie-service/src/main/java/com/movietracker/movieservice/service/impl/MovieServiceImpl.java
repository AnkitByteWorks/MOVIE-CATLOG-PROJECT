package com.movietracker.movieservice.service.impl;

import com.movietracker.movieservice.dto.response.MovieDetailsResponse;
import com.movietracker.movieservice.dto.response.MovieSummaryResponse;
import com.movietracker.movieservice.dto.response.PagedResponse;
import com.movietracker.movieservice.entity.Movie;
import com.movietracker.movieservice.exception.ResourceNotFoundException;
import com.movietracker.movieservice.mapper.MovieDtoMapper;
import com.movietracker.movieservice.repository.MovieRepository;
import com.movietracker.movieservice.service.MovieService;
import com.movietracker.movieservice.service.TrendingSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Production implementation of MovieService powered by MongoDB text search
 * and multi-tier Redis caching.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MovieServiceImpl implements MovieService {

    private final MovieRepository movieRepository;
    private final MovieDtoMapper movieDtoMapper;
    private final CachedMovieLookupService cachedLookupService;
    private final TrendingSyncService trendingSyncService;

    @Override
    public PagedResponse<MovieSummaryResponse> searchMovies(String query, int page) {
        if (query == null || query.isBlank()) {
            return PagedResponse.of(Collections.emptyList(), page, 20, 0);
        }
        int validPage = Math.max(1, page);
        return cachedLookupService.getCachedMovieSearch(query, validPage, () -> executeSearch(query, validPage));
    }

    private PagedResponse<MovieSummaryResponse> executeSearch(String query, int page) {
        log.debug("Executing MongoDB text search for '{}' (page {})", query, page);
        Pageable pageable = PageRequest.of(page - 1, 20);
        Page<Movie> results = movieRepository.searchByText(query, pageable);

        List<MovieSummaryResponse> items = results.getContent().stream()
                .map(movieDtoMapper::toSummaryResponse)
                .toList();

        return PagedResponse.of(items, page, 20, results.getTotalElements());
    }

    @Override
    public MovieDetailsResponse getMovieDetails(Long tmdbId) {
        if (tmdbId == null) {
            throw new ResourceNotFoundException("Movie", "tmdbId", "null");
        }
        return cachedLookupService.getCachedMovieDetails(tmdbId, () -> executeGetMovieDetails(tmdbId));
    }

    private MovieDetailsResponse executeGetMovieDetails(Long tmdbId) {
        log.debug("Looking up movie details in MongoDB for tmdbId {}", tmdbId);
        Movie movie = movieRepository.findByTmdbId(tmdbId)
                .orElseThrow(() -> new ResourceNotFoundException("Movie", "tmdbId", tmdbId));
        return movieDtoMapper.toDetailsResponse(movie);
    }

    @Override
    public PagedResponse<MovieSummaryResponse> getTrending(String window, int page) {
        if ("india".equalsIgnoreCase(window)) {
            return getTrendingIndia(page);
        }
        return getTrendingGlobal(page);
    }

    @Override
    public PagedResponse<MovieSummaryResponse> getTrendingIndia(int page) {
        int validPage = Math.max(1, page);
        return cachedLookupService.getCachedTrendingIndia(validPage, () -> executeGetTrendingIndia(validPage));
    }

    private PagedResponse<MovieSummaryResponse> executeGetTrendingIndia(int page) {
        log.debug("Fetching Indian trending movies (page {})", page);
        List<Long> cachedIds = trendingSyncService.getCachedIndiaIds();
        if (cachedIds.isEmpty()) {
            cachedIds = trendingSyncService.fetchOrComputeTrendingIndia();
        }

        int pageSize = 20;
        int fromIndex = (page - 1) * pageSize;
        if (fromIndex >= cachedIds.size()) {
            return PagedResponse.of(Collections.emptyList(), page, pageSize, cachedIds.size());
        }
        int toIndex = Math.min(fromIndex + pageSize, cachedIds.size());
        List<Long> pageIds = cachedIds.subList(fromIndex, toIndex);

        List<Movie> movies = movieRepository.findByTmdbIdInAndAdultNotTrue(pageIds);
        Map<Long, Movie> movieMap = movies.stream()
                .filter(m -> m.getTmdbId() != null)
                .collect(Collectors.toMap(Movie::getTmdbId, m -> m, (a, b) -> a));

        List<MovieSummaryResponse> items = pageIds.stream()
                .map(movieMap::get)
                .filter(Objects::nonNull)
                .map(movieDtoMapper::toSummaryResponse)
                .toList();

        return PagedResponse.of(items, page, pageSize, cachedIds.size());
    }

    @Override
    public PagedResponse<MovieSummaryResponse> getTrendingGlobal(int page) {
        int validPage = Math.max(1, page);
        return cachedLookupService.getCachedTrendingGlobal(validPage, () -> executeGetTrendingGlobal(validPage));
    }

    private PagedResponse<MovieSummaryResponse> executeGetTrendingGlobal(int page) {
        log.debug("Fetching Global trending movies (page {})", page);
        List<Long> cachedIds = trendingSyncService.getCachedGlobalIds();
        if (cachedIds.isEmpty()) {
            cachedIds = trendingSyncService.fetchOrComputeTrendingGlobal();
        }

        int pageSize = 20;
        int fromIndex = (page - 1) * pageSize;
        if (fromIndex >= cachedIds.size()) {
            return PagedResponse.of(Collections.emptyList(), page, pageSize, cachedIds.size());
        }
        int toIndex = Math.min(fromIndex + pageSize, cachedIds.size());
        List<Long> pageIds = cachedIds.subList(fromIndex, toIndex);

        List<Movie> movies = movieRepository.findByTmdbIdInAndAdultNotTrue(pageIds);
        Map<Long, Movie> movieMap = movies.stream()
                .filter(m -> m.getTmdbId() != null)
                .collect(Collectors.toMap(Movie::getTmdbId, m -> m, (a, b) -> a));

        List<MovieSummaryResponse> items = pageIds.stream()
                .map(movieMap::get)
                .filter(Objects::nonNull)
                .map(movieDtoMapper::toSummaryResponse)
                .toList();

        return PagedResponse.of(items, page, pageSize, cachedIds.size());
    }

    @Override
    public Map<String, Object> syncTrendingNow() {
        return trendingSyncService.syncTrending();
    }

    @Override
    public List<MovieSummaryResponse> getRecommendations(Long tmdbId) {
        if (tmdbId == null) {
            return Collections.emptyList();
        }
        return cachedLookupService.getCachedRecommendations(tmdbId, () -> executeGetRecommendations(tmdbId));
    }

    private List<MovieSummaryResponse> executeGetRecommendations(Long tmdbId) {
        log.debug("Fetching recommendations from MongoDB for tmdbId {}", tmdbId);
        Optional<Movie> movieOpt = movieRepository.findByTmdbId(tmdbId);

        if (movieOpt.isPresent() && movieOpt.get().getGenres() != null && !movieOpt.get().getGenres().isEmpty()) {
            Page<Movie> related = movieRepository.findByGenresInAndTmdbIdNot(
                    movieOpt.get().getGenres(), tmdbId, PageRequest.of(0, 20));
            if (related.hasContent()) {
                return related.getContent().stream()
                        .map(movieDtoMapper::toSummaryResponse)
                        .toList();
            }
        }

        return movieRepository.findTop20ByOrderByPopularityDesc().stream()
                .filter(m -> !m.getTmdbId().equals(tmdbId))
                .limit(20)
                .map(movieDtoMapper::toSummaryResponse)
                .toList();
    }

    @Override
    public PagedResponse<MovieSummaryResponse> getByGenre(String genreName, int page) {
        int pageIndex = Math.max(0, page - 1);
        List<String> targetGenres;
        String normalized = genreName != null ? genreName.trim() : "";
        if ("Kids".equalsIgnoreCase(normalized) || "Children".equalsIgnoreCase(normalized) || "Child shows".equalsIgnoreCase(normalized)) {
            targetGenres = List.of("Animation", "Family");
        } else if ("Romance".equalsIgnoreCase(normalized) || "Romantic".equalsIgnoreCase(normalized)) {
            targetGenres = List.of("Romance");
        } else if ("Crime".equalsIgnoreCase(normalized)) {
            targetGenres = List.of("Crime", "Mystery");
        } else if ("Sci-Fi".equalsIgnoreCase(normalized) || "Science Fiction".equalsIgnoreCase(normalized)) {
            targetGenres = List.of("Science Fiction");
        } else {
            targetGenres = List.of(normalized);
        }

        Page<Movie> movies = movieRepository.findByGenresIn(
                targetGenres,
                PageRequest.of(pageIndex, 20, Sort.by(Sort.Direction.DESC, "popularity"))
        );
        List<MovieSummaryResponse> dtos = movies.getContent().stream()
                .map(movieDtoMapper::toSummaryResponse)
                .toList();
        return PagedResponse.of(dtos, page, 20, movies.getTotalElements());
    }

    @Override
    public PagedResponse<MovieSummaryResponse> getPopular(int page) {
        int pageIndex = Math.max(0, page - 1);
        Page<Movie> movies = movieRepository.findAllByOrderByPopularityDesc(PageRequest.of(pageIndex, 20));
        List<MovieSummaryResponse> dtos = movies.getContent().stream()
                .map(movieDtoMapper::toSummaryResponse)
                .toList();
        return PagedResponse.of(dtos, page, 20, movies.getTotalElements());
    }

    @Override
    public List<MovieSummaryResponse> getRecentlyViewed(int limit) {
        int cappedLimit = Math.min(Math.max(1, limit), 50);
        Page<Movie> movies = movieRepository.findAll(PageRequest.of(0, cappedLimit, Sort.by(Sort.Direction.DESC, "updatedAt")));
        return movies.getContent().stream()
                .map(movieDtoMapper::toSummaryResponse)
                .toList();
    }
}
