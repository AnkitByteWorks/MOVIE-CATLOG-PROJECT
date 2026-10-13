package com.movietracker.userservice.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

/**
 * Public profile data representation of a user account.
 * <p>
 * Excludes sensitive credentials such as hashed passwords.
 */
@Schema(description = "Safe public view of a user profile")
public record UserResponse(
        @Schema(description = "Unique user identifier", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID id,

        @Schema(description = "Unique username", example = "john_doe")
        String username,

        @Schema(description = "Primary email address", example = "john.doe@example.com")
        String email,

        @Schema(description = "Friendly display name", example = "John Doe")
        String displayName,

        @Schema(description = "Assigned user role", example = "USER")
        String role,

        @Schema(description = "Whether the user account is active", example = "true")
        boolean enabled,

        @Schema(description = "Whether the email address has been verified", example = "false")
        boolean emailVerified,

        @Schema(description = "Timestamp when the account was registered")
        Instant createdAt,

        @Schema(description = "Timestamp of the most recent successful login")
        Instant lastLoginAt
) {}
