package com.movietracker.userservice.event;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain event payload dispatched to Kafka when significant user lifecycle actions occur.
 *
 * @param eventId    unique event identifier
 * @param eventType  classification of the event ("USER_REGISTERED" or "USER_LOGGED_IN")
 * @param userId     unique user UUID
 * @param username   username handle
 * @param email      user email address
 * @param occurredAt timestamp when the event transpired
 * @param ip         client IP address
 * @param userAgent  client device / browser User-Agent
 */
public record UserEvent(
        UUID eventId,
        String eventType,
        UUID userId,
        String username,
        String email,
        Instant occurredAt,
        String ip,
        String userAgent
) {}
