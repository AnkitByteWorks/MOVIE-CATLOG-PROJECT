package com.movietracker.watchlog.repository;

import com.movietracker.watchlog.entity.MediaType;
import com.movietracker.watchlog.entity.WatchLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for {@link WatchLog} entities.
 */
@Repository
public interface WatchLogRepository extends JpaRepository<WatchLog, UUID> {

    Page<WatchLog> findByUserIdOrderByWatchedAtDesc(UUID userId, Pageable pageable);

    Page<WatchLog> findByUsernameOrderByWatchedAtDesc(String username, Pageable pageable);

    boolean existsByUserIdAndTmdbIdAndMediaType(UUID userId, Long tmdbId, MediaType mediaType);

    Optional<WatchLog> findByUserIdAndTmdbIdAndMediaType(UUID userId, Long tmdbId, MediaType mediaType);

    Page<WatchLog> findByTmdbIdOrderByWatchedAtDesc(Long tmdbId, Pageable pageable);

    List<WatchLog> findTop10ByUserIdOrderByWatchedAtDesc(UUID userId);

    @Query("SELECT COUNT(w) FROM WatchLog w WHERE w.userId = :userId AND w.mediaType = :type")
    long countByUserAndType(@Param("userId") UUID userId, @Param("type") MediaType type);

    @Query("SELECT COALESCE(SUM(w.rating), 0) FROM WatchLog w WHERE w.userId = :userId AND w.rating IS NOT NULL")
    long sumRatingForUser(@Param("userId") UUID userId);

    @Query("SELECT COUNT(w) FROM WatchLog w WHERE w.userId = :userId AND w.rating IS NOT NULL")
    long countRatedForUser(@Param("userId") UUID userId);

    @Query("SELECT w FROM WatchLog w WHERE w.userId = :userId AND w.watchedAt BETWEEN :from AND :to ORDER BY w.watchedAt DESC")
    List<WatchLog> findByUserAndDateRange(@Param("userId") UUID userId, @Param("from") Instant from, @Param("to") Instant to);

    @Query("SELECT DISTINCT CAST(w.watchedAt AS LocalDate) FROM WatchLog w WHERE w.userId = :userId ORDER BY CAST(w.watchedAt AS LocalDate) DESC")
    List<LocalDate> findDistinctWatchDates(@Param("userId") UUID userId);

    @Query("SELECT w FROM WatchLog w WHERE w.userId = :userId ORDER BY w.watchedAt DESC")
    List<WatchLog> findAllByUserForStats(@Param("userId") UUID userId);
}
