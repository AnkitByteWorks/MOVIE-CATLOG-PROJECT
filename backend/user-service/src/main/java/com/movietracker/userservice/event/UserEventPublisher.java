package com.movietracker.userservice.event;

import com.movietracker.userservice.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

/**
 * Service producing user lifecycle domain events onto the {@code user-events} Kafka topic.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserEventPublisher {

    public static final String USER_EVENTS_TOPIC = "user-events";
    public static final String EVENT_TYPE_REGISTERED = "USER_REGISTERED";
    public static final String EVENT_TYPE_LOGGED_IN = "USER_LOGGED_IN";
    public static final String EVENT_TYPE_FOLLOWED = "USER_FOLLOWED";
    public static final String EVENT_TYPE_UNFOLLOWED = "USER_UNFOLLOWED";
    public static final String EVENT_TYPE_PROFILE_UPDATED = "USER_PROFILE_UPDATED";
    public static final String EVENT_TYPE_PASSWORD_CHANGED = "USER_PASSWORD_CHANGED";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * Publishes a {@link UserEvent} to the Kafka topic partitioned by the user identifier.
     *
     * @param event domain event to publish
     */
    public void publish(UserEvent event) {
        String key = event.userId() != null ? event.userId().toString() : event.username();
        kafkaTemplate.send(USER_EVENTS_TOPIC, key, event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish {} event for user '{}': {}",
                                event.eventType(), event.username(), ex.getMessage(), ex);
                    } else {
                        log.info("Published {} event for user '{}' to partition {} with offset {}",
                                event.eventType(),
                                event.username(),
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset());
                    }
                });
    }

    /**
     * Publishes a {@link SocialEvent} to the Kafka topic partitioned by the actor identifier.
     *
     * @param event social domain event to publish
     */
    public void publishSocial(SocialEvent event) {
        String key = event.actorId() != null ? event.actorId().toString() : event.actorUsername();
        kafkaTemplate.send(USER_EVENTS_TOPIC, key, event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish {} event for actor '{}': {}",
                                event.eventType(), event.actorUsername(), ex.getMessage(), ex);
                    } else {
                        log.info("Published {} event for actor '{}' (target: '{}') to partition {} with offset {}",
                                event.eventType(),
                                event.actorUsername(),
                                event.targetUsername(),
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset());
                    }
                });
    }

    /**
     * Publishes a user registered lifecycle event.
     *
     * @param user      persisted user entity
     * @param ip        client IP address
     * @param userAgent client device user agent
     */
    public void publishRegistered(User user, String ip, String userAgent) {
        UserEvent event = new UserEvent(
                UUID.randomUUID(),
                EVENT_TYPE_REGISTERED,
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                Instant.now(),
                ip,
                userAgent
        );
        publish(event);
    }

    /**
     * Publishes a user authenticated login lifecycle event.
     *
     * @param user      authenticated user entity
     * @param ip        client IP address
     * @param userAgent client device user agent
     */
    public void publishLoggedIn(User user, String ip, String userAgent) {
        UserEvent event = new UserEvent(
                UUID.randomUUID(),
                EVENT_TYPE_LOGGED_IN,
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                Instant.now(),
                ip,
                userAgent
        );
        publish(event);
    }

    /**
     * Publishes a user followed social event.
     *
     * @param actor  user initiating the follow
     * @param target user being followed
     */
    public void publishFollowed(User actor, User target) {
        SocialEvent event = new SocialEvent(
                UUID.randomUUID(),
                EVENT_TYPE_FOLLOWED,
                actor.getId(),
                actor.getUsername(),
                target.getId(),
                target.getUsername(),
                Instant.now()
        );
        publishSocial(event);
    }

    /**
     * Publishes a user unfollowed social event.
     *
     * @param actor  user initiating the unfollow
     * @param target user being unfollowed
     */
    public void publishUnfollowed(User actor, User target) {
        SocialEvent event = new SocialEvent(
                UUID.randomUUID(),
                EVENT_TYPE_UNFOLLOWED,
                actor.getId(),
                actor.getUsername(),
                target.getId(),
                target.getUsername(),
                Instant.now()
        );
        publishSocial(event);
    }

    /**
     * Publishes a profile updated lifecycle event.
     *
     * @param user user whose profile was modified
     */
    public void publishProfileUpdated(User user) {
        SocialEvent event = new SocialEvent(
                UUID.randomUUID(),
                EVENT_TYPE_PROFILE_UPDATED,
                user.getId(),
                user.getUsername(),
                user.getId(),
                user.getUsername(),
                Instant.now()
        );
        publishSocial(event);
    }

    /**
     * Publishes a password changed security event.
     *
     * @param user user whose password was changed
     */
    public void publishPasswordChanged(User user) {
        SocialEvent event = new SocialEvent(
                UUID.randomUUID(),
                EVENT_TYPE_PASSWORD_CHANGED,
                user.getId(),
                user.getUsername(),
                user.getId(),
                user.getUsername(),
                Instant.now()
        );
        publishSocial(event);
    }
}
