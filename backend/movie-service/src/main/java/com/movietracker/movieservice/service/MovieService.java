package com.movietracker.movieservice.service;

import com.movietracker.movieservice.dto.response.MovieDetailsResponse;
import com.movietracker.movieservice.dto.response.MovieSummaryResponse;
import com.movietracker.movieservice.dto.response.PagedResponse;

import java.util.List;

/**
 * Service contract for Movie catalog operations, TMDB synchronization, and discovery.
 */
public interface MovieService {

    PagedResponse<MovieSummaryResponse> searchMovies(String query, int page);

    MovieDetailsResponse getMovieDetails(Long tmdbId);

    PagedResponse<MovieSummaryResponse> getTrending(String window, int page);

    PagedResponse<MovieSummaryResponse> getTrendingIndia(int page);

    PagedResponse<MovieSummaryResponse> getTrendingGlobal(int page);

    java.util.Map<String, Object> syncTrendingNow();

    List<MovieSummaryResponse> getRecommendations(Long tmdbId);

    PagedResponse<MovieSummaryResponse> getByGenre(String genreName, int page);

    PagedResponse<MovieSummaryResponse> getPopular(int page);

    List<MovieSummaryResponse> getRecentlyViewed(int limit);
}
