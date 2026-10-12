package com.movietracker.watchlog.exception;

import com.movietracker.watchlog.entity.MediaType;

import java.util.UUID;

/**
 * Exception thrown when a user attempts to log an identical title they have already recorded.
 */
public class WatchLogAlreadyExistsException extends RuntimeException {

    public WatchLogAlreadyExistsException(UUID userId, Long tmdbId, MediaType mediaType) {
        super(String.format("Watch log already exists for user '%s', %s id '%d'. Use rewatch instead.", userId, mediaType, tmdbId));
    }

    public WatchLogAlreadyExistsException(String message) {
        super(message);
    }
}
