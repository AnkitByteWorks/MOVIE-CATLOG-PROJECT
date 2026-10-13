package com.movietracker.userservice.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

/**
 * Publicly visible user profile view.
 */
@Schema(description = "Public user profile visible to any user or visitor")
public record PublicUserResponse(
        @Schema(description = "Unique user identifier", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID id,

        @Schema(description = "Unique username handle", example = "john_doe")
        String username,

        @Schema(description = "Display name", example = "John Doe")
        String displayName,

        @Schema(description = "Short biography", example = "Film critic and cinephile")
        String bio,

        @Schema(description = "Avatar image URL", example = "https://example.com/avatar.jpg")
        String avatarUrl,

        @Schema(description = "Geographic location", example = "San Francisco, CA")
        String location,

        @Schema(description = "Personal website or portfolio URL", example = "https://johndoe.com")
        String website,

        @Schema(description = "Number of followers", example = "42")
        Integer followerCount,

        @Schema(description = "Number of accounts followed", example = "15")
        Integer followingCount,

        @Schema(description = "Timestamp when the account was registered")
        Instant createdAt
) {
}
