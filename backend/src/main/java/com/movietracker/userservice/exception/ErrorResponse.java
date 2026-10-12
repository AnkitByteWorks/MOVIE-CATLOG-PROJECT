package com.movietracker.userservice.exception;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.Collections;
import java.util.Map;

/**
 * Standardized API error payload returned when an HTTP request fails.
 *
 * @param timestamp   instant when the error occurred
 * @param status      HTTP status code
 * @param error       HTTP status reason phrase
 * @param message     human-readable diagnostic description of the failure
 * @param path        request URI path that produced the error
 * @param fieldErrors map of field-level validation errors (if applicable)
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

    /**
     * Constructs an {@link ErrorResponse} instance with empty field validation errors.
     *
     * @param status  HTTP status code
     * @param error   HTTP status reason phrase
     * @param message failure description
     * @param path    request URI path
     * @return constructed {@link ErrorResponse} instance
     */
    public static ErrorResponse of(int status, String error, String message, String path) {
        return new ErrorResponse(Instant.now(), status, error, message, path, Collections.emptyMap());
    }

    /**
     * Constructs an {@link ErrorResponse} instance with detailed field validation errors.
     *
     * @param status      HTTP status code
     * @param error       HTTP status reason phrase
     * @param message     failure description
     * @param path        request URI path
     * @param fieldErrors map of field names to error messages
     * @return constructed {@link ErrorResponse} instance
     */
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
