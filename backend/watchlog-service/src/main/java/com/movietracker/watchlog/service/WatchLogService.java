package com.movietracker.watchlog.service;

import com.movietracker.watchlog.dto.request.CreateWatchLogRequest;
import com.movietracker.watchlog.dto.request.UpdateWatchLogRequest;
import com.movietracker.watchlog.dto.response.PagedResponse;
import com.movietracker.watchlog.dto.response.PublicUserWatchLogResponse;
import com.movietracker.watchlog.dto.response.WatchLogResponse;

import java.util.List;
import java.util.UUID;

/**
 * Service contract for creating, mutating, and querying watch logs.
 */
public interface WatchLogService {

    WatchLogResponse createWatchLog(CreateWatchLogRequest req, UUID userId, String username, String authHeader);

    WatchLogResponse updateWatchLog(UUID id, UpdateWatchLogRequest req, UUID userId);

    void deleteWatchLog(UUID id, UUID userId);

    WatchLogResponse markRewatch(UUID id, UUID userId);

    WatchLogResponse getWatchLog(UUID id, UUID userId);

    PagedResponse<WatchLogResponse> getMyHistory(UUID userId, int page, int size);

    PagedResponse<PublicUserWatchLogResponse> getPublicHistory(String username, int page, int size);

    List<WatchLogResponse> getRecentWatches(UUID userId);

    PagedResponse<WatchLogResponse> getWhoWatchedThisMovie(Long tmdbId, int page, int size);
}
