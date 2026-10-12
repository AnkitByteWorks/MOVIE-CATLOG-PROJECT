package com.movietracker.userservice.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

/**
 * Detailed user profile returned to the authenticated owner.
 */
@Schema(description = "Full user profile details returned to the authenticated owner")
public record UserProfileResponse(
        @Schema(description = "Unique user identifier", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID id,

        @Schema(description = "Unique username handle", example = "john_doe")
        String username,

        @Schema(description = "User primary email address", example = "john@example.com")
        String email,

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

        @Schema(description = "Assigned user role", example = "USER")
        String role,

        @Schema(description = "Whether the user's email has been verified", example = "true")
        boolean emailVerified,

        @Schema(description = "Timestamp when the account was registered")
        Instant createdAt,

        @Schema(description = "Timestamp of the user's most recent login")
        Instant lastLoginAt
) {
}
