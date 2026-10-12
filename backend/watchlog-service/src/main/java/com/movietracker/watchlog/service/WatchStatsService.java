package com.movietracker.watchlog.service;

import com.movietracker.watchlog.dto.response.WatchStatsResponse;

import java.util.UUID;

/**
 * Service contract for computing and retrieving user watching statistics and analytics.
 */
public interface WatchStatsService {

    WatchStatsResponse getMyStats(UUID userId);

    WatchStatsResponse getPublicStats(String username);
}
