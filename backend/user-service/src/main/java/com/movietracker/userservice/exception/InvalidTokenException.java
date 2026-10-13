package com.movietracker.userservice.exception;

/**
 * Exception thrown when a provided authentication or verification token is malformed, expired, or invalid.
 */
public class InvalidTokenException extends RuntimeException {

    /**
     * Constructs a new exception with the specified diagnostic message.
     *
     * @param message detail message describing token validation failure
     */
    public InvalidTokenException(String message) {
        super(message);
    }
}
