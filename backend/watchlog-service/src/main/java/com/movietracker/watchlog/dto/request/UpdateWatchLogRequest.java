package com.movietracker.watchlog.dto.request;

import com.movietracker.watchlog.entity.WatchLocation;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

/**
 * Request payload for updating rating, review, or location of an existing watch log entry.
 */
@Schema(description = "Payload to update an existing watch log entry")
public record UpdateWatchLogRequest(
        @Schema(description = "Rating out of 10", example = "8", minimum = "1", maximum = "10")
        @Min(value = 1, message = "Rating must be at least 1")
        @Max(value = 10, message = "Rating cannot exceed 10")
        Integer rating,

        @Schema(description = "Updated user review", example = "Rewatched in IMAX, still fantastic")
        @Size(max = 5000, message = "Review cannot exceed 5000 characters")
        String review,

        @Schema(description = "Updated viewing location", example = "STREAMING")
        WatchLocation watchLocation
) {
}
