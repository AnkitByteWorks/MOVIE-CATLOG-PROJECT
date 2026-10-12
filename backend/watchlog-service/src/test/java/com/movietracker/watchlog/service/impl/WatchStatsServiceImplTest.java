package com.movietracker.watchlog.service.impl;

import com.movietracker.watchlog.client.UserServiceClient;
import com.movietracker.watchlog.dto.external.UserResponse;
import com.movietracker.watchlog.dto.response.WatchStatsResponse;
import com.movietracker.watchlog.entity.MediaType;
import com.movietracker.watchlog.entity.WatchLocation;
import com.movietracker.watchlog.entity.WatchLog;
import com.movietracker.watchlog.exception.ResourceNotFoundException;
import com.movietracker.watchlog.repository.WatchLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("WatchStatsServiceImpl Unit Tests")
class WatchStatsServiceImplTest {

    @Mock
    private WatchLogRepository watchLogRepository;

    @Mock
    private UserServiceClient userServiceClient;

    @Mock
    private CachedWatchLookupService cachedLookupService;

    @InjectMocks
    private WatchStatsServiceImpl watchStatsService;

    private UUID userId;
    private String username;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        username = "cinephile";
    }

    @Test
    @DisplayName("Should return stats through cached lookup service for getMyStats")
    void shouldGetMyStatsViaCache() {
        WatchStatsResponse expected = new WatchStatsResponse(
                userId, username, 10, 8, 2, 1200L, "20h 0m",
                8.5, 10, 3, 5, 2, 10,
                Collections.emptyList(), Collections.emptyList(),
                Collections.emptyMap(), "2010s", Instant.now(), Instant.now()
        );

        when(cachedLookupService.getStats(eq(userId), any())).thenReturn(expected);

        WatchStatsResponse result = watchStatsService.getMyStats(userId);

        assertThat(result).isNotNull();
        assertThat(result.totalWatches()).isEqualTo(10);
        assertThat(result.formattedWatchTime()).isEqualTo("20h 0m");
    }

    @Test
    @DisplayName("Should resolve public user profile and return stats for getPublicStats")
    void shouldGetPublicStats() {
        UserResponse userResponse = new UserResponse(
                userId, username, "Cinephile User", "Bio", "https://avatar.png",
                "NYC", "https://site.com", 10, 5, Instant.now()
        );

        WatchStatsResponse expected = new WatchStatsResponse(
                userId, username, 5, 4, 1, 600L, "10h 0m",
                9.0, 5, 2, 4, 1, 5,
                Collections.emptyList(), Collections.emptyList(),
                Collections.emptyMap(), "2020s", Instant.now(), Instant.now()
        );

        when(userServiceClient.getPublicProfile(username)).thenReturn(userResponse);
        when(cachedLookupService.getStats(eq(userId), any())).thenReturn(expected);

        WatchStatsResponse result = watchStatsService.getPublicStats(username);

        assertThat(result).isNotNull();
        assertThat(result.username()).isEqualTo(username);
        assertThat(result.totalWatches()).isEqualTo(5);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when user profile not found for public stats")
    void shouldThrowWhenPublicUserNotFound() {
        when(userServiceClient.getPublicProfile("unknown")).thenReturn(null);

        assertThatThrownBy(() -> watchStatsService.getPublicStats("unknown"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User");
    }

    @Test
    @DisplayName("Should compute stats correctly from repository logs")
    void shouldComputeStatsCorrectly() {
        Instant now = Instant.now();

        WatchLog log1 = WatchLog.builder()
                .userId(userId)
                .username(username)
                .tmdbId(101L)
                .mediaType(MediaType.MOVIE)
                .runtimeMinutes(120)
                .rating(8)
                .genres("Action, Sci-Fi")
                .releaseYear(2021)
                .watchLocation(WatchLocation.THEATER)
                .rewatchCount(0)
                .watchedAt(now)
                .build();

        WatchLog log2 = WatchLog.builder()
                .userId(userId)
                .username(username)
                .tmdbId(102L)
                .mediaType(MediaType.MOVIE)
                .runtimeMinutes(100)
                .rating(9)
                .genres("Action, Drama")
                .releaseYear(1995)
                .watchLocation(WatchLocation.HOME)
                .rewatchCount(1) // 100 * (1 + 1) = 200 min
                .watchedAt(now.minusSeconds(86400))
                .build();

        WatchLog log3 = WatchLog.builder()
                .userId(userId)
                .username(username)
                .tmdbId(201L)
                .mediaType(MediaType.TV)
                .runtimeMinutes(45)
                .rating(7)
                .genres("Drama")
                .releaseYear(2018)
                .watchLocation(WatchLocation.STREAMING)
                .rewatchCount(0)
                .watchedAt(now.minusSeconds(86400 * 2))
                .build();

        List<WatchLog> allLogs = List.of(log1, log2, log3);

        when(watchLogRepository.findAllByUserForStats(userId)).thenReturn(allLogs);
        when(watchLogRepository.sumRatingForUser(userId)).thenReturn(24L); // 8 + 9 + 7
        when(watchLogRepository.countRatedForUser(userId)).thenReturn(3L);

        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        List<LocalDate> distinctDates = List.of(today, today.minusDays(1), today.minusDays(2));
        when(watchLogRepository.findDistinctWatchDates(userId)).thenReturn(distinctDates);

        WatchStatsResponse stats = watchStatsService.computeStats(userId, username);

        assertThat(stats.totalWatches()).isEqualTo(3);
        assertThat(stats.totalMovies()).isEqualTo(2);
        assertThat(stats.totalTvShows()).isEqualTo(1);
        // total minutes: 120 + (100 * 2) + 45 = 365
        assertThat(stats.totalWatchTimeMinutes()).isEqualTo(365L);
        assertThat(stats.formattedWatchTime()).isEqualTo("6h 5m");
        assertThat(stats.averageRating()).isEqualTo(8.0);
        assertThat(stats.ratedCount()).isEqualTo(3);
        assertThat(stats.currentStreak()).isEqualTo(3);
        assertThat(stats.longestStreak()).isEqualTo(3);
        assertThat(stats.topGenres()).isNotEmpty();
        assertThat(stats.byLocation()).containsEntry(WatchLocation.THEATER, 1L);
        assertThat(stats.byLocation()).containsEntry(WatchLocation.HOME, 1L);
        assertThat(stats.byLocation()).containsEntry(WatchLocation.STREAMING, 1L);
    }

    @Test
    @DisplayName("Should return empty stats when user has zero watch logs")
    void shouldReturnEmptyStatsWhenNoLogs() {
        when(watchLogRepository.findAllByUserForStats(userId)).thenReturn(Collections.emptyList());

        WatchStatsResponse stats = watchStatsService.computeStats(userId, username);

        assertThat(stats.totalWatches()).isEqualTo(0);
        assertThat(stats.totalMovies()).isEqualTo(0);
        assertThat(stats.totalTvShows()).isEqualTo(0);
        assertThat(stats.totalWatchTimeMinutes()).isEqualTo(0L);
        assertThat(stats.formattedWatchTime()).isEqualTo("0m");
        assertThat(stats.currentStreak()).isEqualTo(0);
        assertThat(stats.longestStreak()).isEqualTo(0);
    }

    @Test
    @DisplayName("Should calculate current streak accurately")
    void shouldCalculateCurrentStreak() {
        LocalDate today = LocalDate.now(ZoneOffset.UTC);

        // 3 consecutive days including today -> streak 3
        int streak3 = watchStatsService.calculateCurrentStreak(List.of(today, today.minusDays(1), today.minusDays(2)));
        assertThat(streak3).isEqualTo(3);

        // Active from yesterday -> streak 2
        int streak2 = watchStatsService.calculateCurrentStreak(List.of(today.minusDays(1), today.minusDays(2)));
        assertThat(streak2).isEqualTo(2);

        // Broken streak (gap yesterday) -> streak 0
        int streak0 = watchStatsService.calculateCurrentStreak(List.of(today.minusDays(3), today.minusDays(4)));
        assertThat(streak0).isEqualTo(0);

        // Empty list -> 0
        assertThat(watchStatsService.calculateCurrentStreak(Collections.emptyList())).isEqualTo(0);
    }

    @Test
    @DisplayName("Should calculate longest streak accurately")
    void shouldCalculateLongestStreak() {
        LocalDate d = LocalDate.of(2026, 1, 1);
        List<LocalDate> dates = List.of(
                d, d.plusDays(1), d.plusDays(2), d.plusDays(3), // 4 days run
                d.plusDays(10), d.plusDays(11)                  // 2 days run
        );

        int longest = watchStatsService.calculateLongestStreak(dates);
        assertThat(longest).isEqualTo(4);

        assertThat(watchStatsService.calculateLongestStreak(Collections.emptyList())).isEqualTo(0);
    }
}
