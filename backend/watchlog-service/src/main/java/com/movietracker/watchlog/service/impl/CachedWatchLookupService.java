package com.movietracker.watchlog.service.impl;

import com.movietracker.watchlog.dto.response.WatchLogResponse;
import com.movietracker.watchlog.dto.response.WatchStatsResponse;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * Dedicated caching layer for watch history lists and user watch statistics.
 * Avoids Spring self-invocation proxy bypass problems.
 */
@Service
public class CachedWatchLookupService {

    @Cacheable(value = "watchRecent", key = "#userId.toString()")
    public List<WatchLogResponse> getRecentForUser(UUID userId, Supplier<List<WatchLogResponse>> loader) {
        return loader.get();
    }

    @Cacheable(value = "watchStats", key = "#userId.toString()")
    public WatchStatsResponse getStats(UUID userId, Supplier<WatchStatsResponse> loader) {
        return loader.get();
    }

    @Caching(evict = {
            @CacheEvict(value = "watchRecent", key = "#userId.toString()"),
            @CacheEvict(value = "watchStats", key = "#userId.toString()")
    })
    public void evictUser(UUID userId) {
        // Cache entries for recent watches and calculated stats are purged
    }
}
