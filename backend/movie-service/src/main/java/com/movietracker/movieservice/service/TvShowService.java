package com.movietracker.movieservice.service;

import com.movietracker.movieservice.dto.response.PagedResponse;
import com.movietracker.movieservice.dto.response.TvShowDetailsResponse;
import com.movietracker.movieservice.dto.response.TvShowSummaryResponse;

/**
 * Service contract for TV Show catalog operations.
 */
public interface TvShowService {

    PagedResponse<TvShowSummaryResponse> searchTv(String query, int page);

    TvShowDetailsResponse getTvDetails(Long tmdbId);

    PagedResponse<TvShowSummaryResponse> getTrendingTv(String window, int page);

    PagedResponse<TvShowSummaryResponse> getPopularTv(int page);
}
