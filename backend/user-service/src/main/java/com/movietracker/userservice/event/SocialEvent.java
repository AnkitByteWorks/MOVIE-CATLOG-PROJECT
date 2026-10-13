package com.movietracker.userservice.event;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain event payload dispatched to Kafka for social relationship and profile lifecycle changes.
 */
public record SocialEvent(
        UUID eventId,
        String eventType,
        UUID actorId,
        String actorUsername,
        UUID targetId,
        String targetUsername,
        Instant occurredAt
) {
}
