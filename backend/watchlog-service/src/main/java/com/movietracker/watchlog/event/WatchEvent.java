package com.movietracker.watchlog.event;

import com.movietracker.watchlog.entity.MediaType;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain event published to Kafka when a movie or television watch action occurs.
 */
public record WatchEvent(
        UUID eventId,
        String eventType,
        UUID userId,
        String username,
        Long tmdbId,
        MediaType mediaType,
        String title,
        Integer rating,
        Instant watchedAt,
        Instant occurredAt,
        String watchLocation
) {
    public static WatchEvent of(
            String eventType,
            UUID userId,
            String username,
            Long tmdbId,
            MediaType mediaType,
            String title,
            Integer rating,
            Instant watchedAt,
            String watchLocation
    ) {
        return new WatchEvent(
                UUID.randomUUID(),
                eventType,
                userId,
                username,
                tmdbId,
                mediaType,
                title,
                rating,
                watchedAt,
                Instant.now(),
                watchLocation
        );
    }
}
