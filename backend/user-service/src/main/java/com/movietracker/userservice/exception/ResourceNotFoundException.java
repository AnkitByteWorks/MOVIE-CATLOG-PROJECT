package com.movietracker.userservice.exception;

import lombok.Getter;

/**
 * Exception thrown when a requested resource cannot be located.
 */
@Getter
public class ResourceNotFoundException extends RuntimeException {

    private final String resourceName;
    private final String fieldName;
    private final Object fieldValue;

    /**
     * Constructs a new exception indicating that a specific resource was not found.
     *
     * @param resourceName name of the missing resource type (e.g. "User")
     * @param fieldName    identifying field name (e.g. "id" or "email")
     * @param fieldValue   identifying field value used for the lookup
     */
    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s not found with %s: '%s'", resourceName, fieldName, fieldValue));
        this.resourceName = resourceName;
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
    }
}
