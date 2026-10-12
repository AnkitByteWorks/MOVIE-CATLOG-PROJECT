package com.movietracker.watchlog.dto.response;

import com.movietracker.watchlog.entity.MediaType;
import com.movietracker.watchlog.entity.WatchLocation;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

/**
 * Publicly viewable summary of a user's watch log entry.
 */
@Schema(description = "Public user watch log entry")
public record PublicUserWatchLogResponse(
        @Schema(description = "Unique watch log ID")
        UUID id,

        @Schema(description = "Username who logged the entry")
        String username,

        @Schema(description = "TMDB ID of the title")
        Long tmdbId,

        @Schema(description = "Media type (MOVIE or TV)")
        MediaType mediaType,

        @Schema(description = "Title name")
        String title,

        @Schema(description = "Full URL to poster artwork")
        String posterUrl,

        @Schema(description = "Full URL to backdrop artwork")
        String backdropUrl,

        @Schema(description = "Instant when the title was watched")
        Instant watchedAt,

        @Schema(description = "User rating out of 10")
        Integer rating,

        @Schema(description = "Review text")
        String review,

        @Schema(description = "Viewing location")
        WatchLocation watchLocation,

        @Schema(description = "Number of times rewatched")
        Integer rewatchCount
) {
}
