package com.movietracker.watchlog.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/**
 * JPA entity representing an individual Movie or TV watch record.
 */
@Entity
@Table(name = "watch_logs", indexes = {
        @Index(name = "idx_user_watched", columnList = "user_id, watched_at"),
        @Index(name = "idx_user_tmdb", columnList = "user_id, tmdb_id"),
        @Index(name = "idx_tmdb", columnList = "tmdb_id"),
        @Index(name = "idx_username_public", columnList = "username, watched_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WatchLog extends BaseEntity {

    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;

    @Column(name = "username", nullable = false, length = 50)
    private String username;

    @Column(name = "tmdb_id", nullable = false)
    private Long tmdbId;

    @Enumerated(EnumType.STRING)
    @Column(name = "media_type", nullable = false, length = 10)
    private MediaType mediaType;

    @Column(name = "title", nullable = false, length = 500)
    private String title;

    @Column(name = "poster_path", length = 500)
    private String posterPath;

    @Column(name = "backdrop_path", length = 500)
    private String backdropPath;

    @Column(name = "watched_at", nullable = false)
    private Instant watchedAt;

    @Column(name = "rating")
    private Integer rating;

    @Column(name = "review", columnDefinition = "TEXT")
    private String review;

    @Enumerated(EnumType.STRING)
    @Column(name = "watch_location", length = 20)
    private WatchLocation watchLocation;

    @Column(name = "rewatch_count", nullable = false)
    @Builder.Default
    private Integer rewatchCount = 0;

    @Column(name = "runtime_minutes")
    private Integer runtimeMinutes;

    @Column(name = "genres", length = 500)
    private String genres;

    @Column(name = "release_year")
    private Integer releaseYear;
}
