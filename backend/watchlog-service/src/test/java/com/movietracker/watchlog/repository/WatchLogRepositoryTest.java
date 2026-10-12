package com.movietracker.watchlog.repository;

import com.movietracker.watchlog.config.JpaAuditingConfig;
import com.movietracker.watchlog.entity.MediaType;
import com.movietracker.watchlog.entity.WatchLocation;
import com.movietracker.watchlog.entity.WatchLog;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(JpaAuditingConfig.class)
@ActiveProfiles("test")
@DisplayName("WatchLogRepository DataJpaTest")
class WatchLogRepositoryTest {

    @Autowired
    private WatchLogRepository watchLogRepository;

    private UUID userId;
    private WatchLog savedMovie;
    private WatchLog savedTv;

    @BeforeEach
    void setUp() {
        watchLogRepository.deleteAll();

        userId = UUID.randomUUID();

        savedMovie = WatchLog.builder()
                .userId(userId)
                .username("movielover")
                .tmdbId(550L)
                .title("Fight Club")
                .posterPath("/pB8BM7pdSp6B6Ih7QZ4DrQ3PmJK.jpg")
                .backdropPath("/backdrop550.jpg")
                .mediaType(MediaType.MOVIE)
                .watchedAt(Instant.now())
                .rating(9)
                .review("Great psychological thriller")
                .watchLocation(WatchLocation.HOME)
                .rewatchCount(0)
                .runtimeMinutes(139)
                .genres("Drama, Thriller")
                .releaseYear(1999)
                .build();

        savedTv = WatchLog.builder()
                .userId(userId)
                .username("movielover")
                .tmdbId(1396L)
                .title("Breaking Bad")
                .posterPath("/ggFHVNu6YYI5L9pCfOacjizRGt.jpg")
                .backdropPath("/backdrop1396.jpg")
                .mediaType(MediaType.TV)
                .watchedAt(Instant.now().minus(1, ChronoUnit.DAYS))
                .rating(10)
                .review("Absolute masterpiece")
                .watchLocation(WatchLocation.STREAMING)
                .rewatchCount(1)
                .runtimeMinutes(47)
                .genres("Drama, Crime")
                .releaseYear(2008)
                .build();

        watchLogRepository.save(savedMovie);
        watchLogRepository.save(savedTv);
    }

    @Test
    @DisplayName("Should find watch log by userId, tmdbId and mediaType")
    void shouldFindByUserIdAndTmdbIdAndMediaType() {
        Optional<WatchLog> found = watchLogRepository.findByUserIdAndTmdbIdAndMediaType(
                userId, 550L, MediaType.MOVIE
        );
        assertThat(found).isPresent();
        assertThat(found.get().getTitle()).isEqualTo("Fight Club");
        assertThat(found.get().getRuntimeMinutes()).isEqualTo(139);
    }

    @Test
    @DisplayName("Should check existence by userId, tmdbId and mediaType")
    void shouldCheckExistsByUserIdAndTmdbIdAndMediaType() {
        boolean exists = watchLogRepository.existsByUserIdAndTmdbIdAndMediaType(
                userId, 550L, MediaType.MOVIE
        );
        boolean notExists = watchLogRepository.existsByUserIdAndTmdbIdAndMediaType(
                userId, 999999L, MediaType.MOVIE
        );

        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    @DisplayName("Should fetch user watch logs ordered by watchedAt desc")
    void shouldFindByUserIdOrderByWatchedAtDesc() {
        Page<WatchLog> page = watchLogRepository.findByUserIdOrderByWatchedAtDesc(
                userId, PageRequest.of(0, 10)
        );

        assertThat(page.getTotalElements()).isEqualTo(2);
        assertThat(page.getContent().get(0).getTitle()).isEqualTo("Fight Club");
        assertThat(page.getContent().get(1).getTitle()).isEqualTo("Breaking Bad");
    }

    @Test
    @DisplayName("Should fetch logs by username ordered by watchedAt desc")
    void shouldFindByUsernameOrderByWatchedAtDesc() {
        Page<WatchLog> logs = watchLogRepository.findByUsernameOrderByWatchedAtDesc(
                "movielover", PageRequest.of(0, 10)
        );

        assertThat(logs.getTotalElements()).isEqualTo(2);
        assertThat(logs.getContent().get(0).getTitle()).isEqualTo("Fight Club");
    }

    @Test
    @DisplayName("Should count user logs by media type")
    void shouldCountByUserAndType() {
        long movieCount = watchLogRepository.countByUserAndType(userId, MediaType.MOVIE);
        long tvCount = watchLogRepository.countByUserAndType(userId, MediaType.TV);

        assertThat(movieCount).isEqualTo(1L);
        assertThat(tvCount).isEqualTo(1L);
    }

    @Test
    @DisplayName("Should sum and count ratings for user")
    void shouldSumAndCountRatings() {
        long sum = watchLogRepository.sumRatingForUser(userId);
        long count = watchLogRepository.countRatedForUser(userId);

        assertThat(sum).isEqualTo(19L); // 9 + 10
        assertThat(count).isEqualTo(2L);
    }

    @Test
    @DisplayName("Should fetch all user logs for stats")
    void shouldFindAllByUserForStats() {
        List<WatchLog> logs = watchLogRepository.findAllByUserForStats(userId);

        assertThat(logs).hasSize(2);
        assertThat(logs.get(0).getTitle()).isEqualTo("Fight Club");
    }
}
