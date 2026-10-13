package com.movietracker.userservice.exception;

import lombok.Getter;

/**
 * Exception thrown when attempting to create a user that conflicts with an existing account.
 */
@Getter
public class UserAlreadyExistsException extends RuntimeException {

    private final String field;
    private final String value;

    /**
     * Constructs a new exception indicating an account conflict.
     *
     * @param field conflicting attribute field name (e.g. "email" or "username")
     * @param value conflicting attribute value
     */
    public UserAlreadyExistsException(String field, String value) {
        super(String.format("User already exists with %s: '%s'", field, value));
        this.field = field;
        this.value = value;
    }
}
