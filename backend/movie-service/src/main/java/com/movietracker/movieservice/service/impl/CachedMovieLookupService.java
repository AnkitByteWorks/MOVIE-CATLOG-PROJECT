package com.movietracker.movieservice.service.impl;

import com.movietracker.movieservice.dto.response.MovieDetailsResponse;
import com.movietracker.movieservice.dto.response.MovieSummaryResponse;
import com.movietracker.movieservice.dto.response.PagedResponse;
import com.movietracker.movieservice.dto.response.TvShowDetailsResponse;
import com.movietracker.movieservice.dto.response.TvShowSummaryResponse;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.Supplier;

/**
 * Dedicated cache layer for Movie and TV service queries.
 * Avoids self-invocation proxy bypass issues.
 */
@Service
public class CachedMovieLookupService {

    @Cacheable(value = "movieSearch", key = "'movie:search:' + #query.toLowerCase() + ':page:' + #page")
    public PagedResponse<MovieSummaryResponse> getCachedMovieSearch(String query, int page, Supplier<PagedResponse<MovieSummaryResponse>> loader) {
        return loader.get();
    }

    @Cacheable(value = "movieDetails", key = "'movie:details:' + #tmdbId")
    public MovieDetailsResponse getCachedMovieDetails(Long tmdbId, Supplier<MovieDetailsResponse> loader) {
        return loader.get();
    }

    @Cacheable(value = "trending", key = "'movie:trending:' + #window + ':page:' + #page")
    public PagedResponse<MovieSummaryResponse> getCachedTrendingMovies(String window, int page, Supplier<PagedResponse<MovieSummaryResponse>> loader) {
        return loader.get();
    }

    @Cacheable(value = "trending", key = "'movie:trending:india:page:' + #page")
    public PagedResponse<MovieSummaryResponse> getCachedTrendingIndia(int page, Supplier<PagedResponse<MovieSummaryResponse>> loader) {
        return loader.get();
    }

    @Cacheable(value = "trending", key = "'movie:trending:global:page:' + #page")
    public PagedResponse<MovieSummaryResponse> getCachedTrendingGlobal(int page, Supplier<PagedResponse<MovieSummaryResponse>> loader) {
        return loader.get();
    }

    @org.springframework.cache.annotation.CacheEvict(value = "trending", allEntries = true)
    public void evictTrendingCache() {
    }

    @Cacheable(value = "recommendations", key = "'movie:recommendations:' + #tmdbId")
    public List<MovieSummaryResponse> getCachedRecommendations(Long tmdbId, Supplier<List<MovieSummaryResponse>> loader) {
        return loader.get();
    }

    @Cacheable(value = "tvSearch", key = "'tv:search:' + #query.toLowerCase() + ':page:' + #page")
    public PagedResponse<TvShowSummaryResponse> getCachedTvSearch(String query, int page, Supplier<PagedResponse<TvShowSummaryResponse>> loader) {
        return loader.get();
    }

    @Cacheable(value = "tvDetails", key = "'tv:details:' + #tmdbId")
    public TvShowDetailsResponse getCachedTvDetails(Long tmdbId, Supplier<TvShowDetailsResponse> loader) {
        return loader.get();
    }
}
