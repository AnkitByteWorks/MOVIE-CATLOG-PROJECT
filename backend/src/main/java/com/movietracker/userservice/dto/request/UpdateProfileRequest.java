package com.movietracker.userservice.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

/**
 * Request payload for updating editable profile fields.
 */
@Schema(description = "Request payload for updating user profile fields")
public record UpdateProfileRequest(
        @Schema(description = "Display name", example = "John Smith")
        @Size(max = 100, message = "Display name cannot exceed 100 characters")
        String displayName,

        @Schema(description = "User biography", example = "Film enthusiast and reviewer.")
        @Size(max = 200, message = "Bio cannot exceed 200 characters")
        String bio,

        @Schema(description = "Avatar URL", example = "https://example.com/avatar.jpg")
        @Size(max = 500, message = "Avatar URL cannot exceed 500 characters")
        String avatarUrl,

        @Schema(description = "Geographic location", example = "New York, USA")
        @Size(max = 100, message = "Location cannot exceed 100 characters")
        String location,

        @Schema(description = "Website URL", example = "https://johnsmith.com")
        @Size(max = 200, message = "Website cannot exceed 200 characters")
        String website
) {
}
