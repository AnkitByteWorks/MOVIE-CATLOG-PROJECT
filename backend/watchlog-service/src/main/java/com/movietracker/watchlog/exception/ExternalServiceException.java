package com.movietracker.watchlog.exception;

/**
 * Exception thrown when a downstream microservice call fails or circuit breaker trips.
 */
public class ExternalServiceException extends RuntimeException {

    public ExternalServiceException(String message) {
        super(message);
    }

    public ExternalServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
