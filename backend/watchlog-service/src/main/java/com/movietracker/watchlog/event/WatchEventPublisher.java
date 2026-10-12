package com.movietracker.watchlog.event;

import com.movietracker.watchlog.entity.WatchLog;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Service responsible for publishing domain watch events to Kafka topics for asynchronous processing.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WatchEventPublisher {

    public static final String WATCH_EVENTS_TOPIC = "watch-events";

    private final KafkaTemplate<String, WatchEvent> kafkaTemplate;

    public void publish(WatchEvent event) {
        String key = event.userId() != null ? event.userId().toString() : UUID.randomUUID().toString();
        log.info("Publishing {} event to topic '{}' for user '{}' (tmdbId={})",
                event.eventType(), WATCH_EVENTS_TOPIC, event.username(), event.tmdbId());

        kafkaTemplate.send(WATCH_EVENTS_TOPIC, key, event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish {} event for user '{}': {}",
                                event.eventType(), event.username(), ex.getMessage());
                    } else {
                        log.debug("{} event published successfully to partition {}",
                                event.eventType(), result.getRecordMetadata().partition());
                    }
                });
    }

    public void publishWatched(WatchLog logEntry) {
        publish(WatchEvent.of(
                "MOVIE_WATCHED",
                logEntry.getUserId(),
                logEntry.getUsername(),
                logEntry.getTmdbId(),
                logEntry.getMediaType(),
                logEntry.getTitle(),
                logEntry.getRating(),
                logEntry.getWatchedAt(),
                logEntry.getWatchLocation() != null ? logEntry.getWatchLocation().name() : null
        ));
    }

    public void publishRewatched(WatchLog logEntry) {
        publish(WatchEvent.of(
                "MOVIE_REWATCHED",
                logEntry.getUserId(),
                logEntry.getUsername(),
                logEntry.getTmdbId(),
                logEntry.getMediaType(),
                logEntry.getTitle(),
                logEntry.getRating(),
                logEntry.getWatchedAt(),
                logEntry.getWatchLocation() != null ? logEntry.getWatchLocation().name() : null
        ));
    }

    public void publishUpdated(WatchLog logEntry) {
        publish(WatchEvent.of(
                "WATCH_UPDATED",
                logEntry.getUserId(),
                logEntry.getUsername(),
                logEntry.getTmdbId(),
                logEntry.getMediaType(),
                logEntry.getTitle(),
                logEntry.getRating(),
                logEntry.getWatchedAt(),
                logEntry.getWatchLocation() != null ? logEntry.getWatchLocation().name() : null
        ));
    }

    public void publishDeleted(WatchLog logEntry) {
        publish(WatchEvent.of(
                "WATCH_DELETED",
                logEntry.getUserId(),
                logEntry.getUsername(),
                logEntry.getTmdbId(),
                logEntry.getMediaType(),
                logEntry.getTitle(),
                logEntry.getRating(),
                logEntry.getWatchedAt(),
                logEntry.getWatchLocation() != null ? logEntry.getWatchLocation().name() : null
        ));
    }
}
