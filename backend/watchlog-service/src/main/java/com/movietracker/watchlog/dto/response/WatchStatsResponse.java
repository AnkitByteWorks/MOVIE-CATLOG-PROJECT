package com.movietracker.watchlog.dto.response;

import com.movietracker.watchlog.entity.WatchLocation;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Aggregated analytics and watching statistics profile for a user.
 */
@Schema(description = "Aggregated watching statistics profile")
public record WatchStatsResponse(
        @Schema(description = "User unique identifier")
        UUID userId,

        @Schema(description = "Username handle")
        String username,

        @Schema(description = "Total number of watch entries logged")
        long totalWatches,

        @Schema(description = "Total feature films logged")
        long totalMovies,

        @Schema(description = "Total television series logged")
        long totalTvShows,

        @Schema(description = "Cumulative minutes spent watching")
        long totalWatchTimeMinutes,

        @Schema(description = "Human-readable watch duration breakdown", example = "14d 6h 30m")
        String formattedWatchTime,

        @Schema(description = "Average score assigned across rated entries", example = "7.8")
        Double averageRating,

        @Schema(description = "Count of entries assigned a numerical rating")
        long ratedCount,

        @Schema(description = "Current consecutive days watching streak", example = "5")
        int currentStreak,

        @Schema(description = "All-time longest consecutive days watching streak", example = "12")
        int longestStreak,

        @Schema(description = "Number of items watched in the current calendar month")
        long thisMonthWatches,

        @Schema(description = "Number of items watched in the current calendar year")
        long thisYearWatches,

        @Schema(description = "Most frequently watched genres with counts")
        List<GenreStat> topGenres,

        @Schema(description = "Top cast members (reserved for future enhancement)")
        List<String> topActors,

        @Schema(description = "Breakdown of viewing locations")
        Map<WatchLocation, Long> byLocation,

        @Schema(description = "Most watched release decade", example = "2010s")
        String favoriteDecade,

        @Schema(description = "Timestamp of earliest logged watch")
        Instant firstWatch,

        @Schema(description = "Timestamp of latest logged watch")
        Instant lastWatch
) {
    public record GenreStat(String genre, long count) {}
}
