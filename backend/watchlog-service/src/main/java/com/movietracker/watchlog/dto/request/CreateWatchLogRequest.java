package com.movietracker.watchlog.dto.request;

import com.movietracker.watchlog.entity.MediaType;
import com.movietracker.watchlog.entity.WatchLocation;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.time.Instant;

/**
 * Request payload for recording a new movie or TV watch log entry.
 */
@Schema(description = "Payload to record a new watch log entry")
public record CreateWatchLogRequest(
        @Schema(description = "TMDB ID of the title", example = "872585", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "tmdbId must not be null")
        Long tmdbId,

        @Schema(description = "Media type (MOVIE or TV)", example = "MOVIE", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "mediaType must not be null")
        MediaType mediaType,

        @Schema(description = "Timestamp when the title was watched", example = "2026-10-01T20:00:00Z", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "watchedAt must not be null")
        @PastOrPresent(message = "watchedAt cannot be in the future")
        Instant watchedAt,

        @Schema(description = "Rating out of 10", example = "9", minimum = "1", maximum = "10")
        @Min(value = 1, message = "Rating must be at least 1")
        @Max(value = 10, message = "Rating cannot exceed 10")
        Integer rating,

        @Schema(description = "User review or notes", example = "Nolan at his best")
        @Size(max = 5000, message = "Review cannot exceed 5000 characters")
        String review,

        @Schema(description = "Viewing location", example = "THEATER")
        WatchLocation watchLocation
) {
}
