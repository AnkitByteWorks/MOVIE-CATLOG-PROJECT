package com.movietracker.userservice.exception;

import lombok.Getter;

/**
 * Generic domain exception representing business rule violations.
 */
@Getter
public class BusinessException extends RuntimeException {

    private final String errorCode;

    /**
     * Constructs a new business exception with a default error code and message.
     *
     * @param message explanatory message describing the business rule violation
     */
    public BusinessException(String message) {
        super(message);
        this.errorCode = "BUSINESS_ERROR";
    }

    /**
     * Constructs a new business exception with an error code and message.
     *
     * @param errorCode unique identifier for the specific business failure
     * @param message   explanatory message describing the business rule violation
     */
    public BusinessException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}
