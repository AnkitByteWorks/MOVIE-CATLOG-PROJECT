package com.movietracker.watchlog.exception;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.Collections;
import java.util.Map;

/**
 * Standardized API error payload matching Movie Tracker platform standard.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path,
        Map<String, String> fieldErrors
) {
    public static ErrorResponse of(int status, String error, String message, String path) {
        return new ErrorResponse(Instant.now(), status, error, message, path, Collections.emptyMap());
    }

    public static ErrorResponse withFieldErrors(int status, String error, String message, String path, Map<String, String> fieldErrors) {
        return new ErrorResponse(
                Instant.now(),
                status,
                error,
                message,
                path,
                fieldErrors != null ? Collections.unmodifiableMap(fieldErrors) : Collections.emptyMap()
        );
    }
}
