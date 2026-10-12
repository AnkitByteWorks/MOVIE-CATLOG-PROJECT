package com.movietracker.userservice.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Authentication response returned upon successful registration, login, or token refresh.
 */
@Schema(description = "Authentication response containing issued tokens and user profile")
public record AuthResponse(
        @Schema(description = "Signed JWT access token")
        String accessToken,

        @Schema(description = "Signed JWT refresh token")
        String refreshToken,

        @Schema(description = "Token type identifier", example = "Bearer")
        String tokenType,

        @Schema(description = "Access token lifespan in seconds", example = "900")
        long expiresIn,

        @Schema(description = "Authenticated user profile details")
        UserResponse user
) {}
