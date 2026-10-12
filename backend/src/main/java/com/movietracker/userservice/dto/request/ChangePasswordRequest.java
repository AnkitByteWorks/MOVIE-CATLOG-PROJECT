package com.movietracker.userservice.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Request payload for changing user password.
 */
@Schema(description = "Request payload for changing user password")
public record ChangePasswordRequest(
        @Schema(description = "Current account password", example = "OldPassw0rd!")
        @NotBlank(message = "Current password is required")
        String currentPassword,

        @Schema(description = "New account password containing upper, lower, digit, and special characters", example = "NewPassw0rd!")
        @NotBlank(message = "New password is required")
        @Size(min = 8, max = 72, message = "Password must be between 8 and 72 characters")
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^\\w\\s]).+$",
                message = "Password must contain upper, lower, digit, special"
        )
        String newPassword
) {
}
