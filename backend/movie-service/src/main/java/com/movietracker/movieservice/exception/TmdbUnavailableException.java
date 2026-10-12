package com.movietracker.movieservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when TMDB external API is unavailable or circuit is open and no cache exists.
 */
@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
public class TmdbUnavailableException extends RuntimeException {

    public TmdbUnavailableException(String message) {
        super(message);
    }

    public TmdbUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
