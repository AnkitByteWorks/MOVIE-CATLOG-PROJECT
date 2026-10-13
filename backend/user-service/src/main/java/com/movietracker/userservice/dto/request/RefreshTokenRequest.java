package com.movietracker.userservice.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * Request payload for refreshing an expired access token using a valid refresh token.
 */
@Schema(description = "Request payload for refreshing JWT access tokens")
public record RefreshTokenRequest(
        @Schema(description = "JWT refresh token previously issued to client", example = "eyJhbGciOiJIUzI1NiIsIn...", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Refresh token is required")
        String refreshToken
) {}
