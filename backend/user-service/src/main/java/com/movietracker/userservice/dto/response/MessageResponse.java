package com.movietracker.userservice.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Standard response payload conveying an informational or operational message.
 */
@Schema(description = "Generic informational response message")
public record MessageResponse(
        @Schema(description = "Information or status message", example = "Operation completed successfully")
        String message
) {}
