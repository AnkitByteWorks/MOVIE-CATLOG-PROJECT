package com.movietracker.userservice.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * Request payload for authenticating an existing user.
 */
@Schema(description = "Request payload for user authentication")
public record LoginRequest(
        @Schema(description = "Registered username or primary email address", example = "john_doe", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Username or email is required")
        String usernameOrEmail,

        @Schema(description = "Account password", example = "Passw0rd!", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Password is required")
        String password
) {}
