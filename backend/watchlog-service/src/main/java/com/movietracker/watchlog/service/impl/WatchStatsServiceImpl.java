package com.movietracker.watchlog.service.impl;

import com.movietracker.watchlog.client.UserServiceClient;
import com.movietracker.watchlog.dto.external.UserResponse;
import com.movietracker.watchlog.dto.response.WatchStatsResponse;
import com.movietracker.watchlog.entity.MediaType;
import com.movietracker.watchlog.entity.WatchLocation;
import com.movietracker.watchlog.entity.WatchLog;
import com.movietracker.watchlog.exception.ExternalServiceException;
import com.movietracker.watchlog.exception.ResourceNotFoundException;
import com.movietracker.watchlog.repository.WatchLogRepository;
import com.movietracker.watchlog.service.WatchStatsService;
import com.movietracker.watchlog.util.DurationFormatter;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Production implementation of {@link WatchStatsService} computing aggregated user analytics
 * backed by Redis multi-tier caching.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WatchStatsServiceImpl implements WatchStatsService {

    private final WatchLogRepository watchLogRepository;
    private final UserServiceClient userServiceClient;
    private final CachedWatchLookupService cachedLookupService;

    @Override
    @Transactional(readOnly = true)
    public WatchStatsResponse getMyStats(UUID userId) {
        log.debug("Fetching watching statistics for authenticated user '{}'", userId);
        return cachedLookupService.getStats(userId, () -> computeStats(userId, null));
    }

    @Override
    @Transactional(readOnly = true)
    @CircuitBreaker(name = "userService", fallbackMethod = "fallbackPublicStats")
    public WatchStatsResponse getPublicStats(String username) {
        log.debug("Fetching public watching statistics for username '{}'", username);
        UserResponse user = userServiceClient.getPublicProfile(username);
        if (user == null || user.id() == null) {
            throw new ResourceNotFoundException("User", "username", username);
        }
        return cachedLookupService.getStats(user.id(), () -> computeStats(user.id(), username));
    }

    public WatchStatsResponse fallbackPublicStats(String username, Throwable t) {
        if (t instanceof ResourceNotFoundException) {
            throw (ResourceNotFoundException) t;
        }
        log.warn("User service unavailable when fetching public profile for '{}': {}", username, t.getMessage());
        throw new ExternalServiceException("User service is temporarily unavailable. Unable to resolve user statistics.");
    }

    public WatchStatsResponse computeStats(UUID userId, String knownUsername) {
        log.info("Computing fresh watching analytics for user '{}'", userId);

        List<WatchLog> allLogs = watchLogRepository.findAllByUserForStats(userId);
        String resolvedUsername = knownUsername != null ? knownUsername :
                (allLogs.isEmpty() ? "unknown" : allLogs.getFirst().getUsername());

        if (allLogs.isEmpty()) {
            return new WatchStatsResponse(
                    userId,
                    resolvedUsername,
                    0, 0, 0, 0, "0m",
                    null, 0, 0, 0, 0, 0,
                    Collections.emptyList(),
                    Collections.emptyList(),
                    Collections.emptyMap(),
                    null, null, null
            );
        }

        long totalWatches = allLogs.size();
        long totalMovies = allLogs.stream().filter(l -> l.getMediaType() == MediaType.MOVIE).count();
        long totalTvShows = allLogs.stream().filter(l -> l.getMediaType() == MediaType.TV).count();

        long totalWatchTimeMinutes = allLogs.stream()
                .filter(l -> l.getRuntimeMinutes() != null && l.getRuntimeMinutes() > 0)
                .mapToLong(l -> (long) l.getRuntimeMinutes() * (l.getRewatchCount() != null ? l.getRewatchCount() + 1 : 1))
                .sum();

        String formattedWatchTime = DurationFormatter.formatMinutes(totalWatchTimeMinutes);

        long sumRating = watchLogRepository.sumRatingForUser(userId);
        long countRated = watchLogRepository.countRatedForUser(userId);
        Double averageRating = countRated > 0
                ? Math.round((double) sumRating / countRated * 10.0) / 10.0
                : null;

        // Streak computation
        List<LocalDate> distinctDates = watchLogRepository.findDistinctWatchDates(userId);
        int currentStreak = calculateCurrentStreak(distinctDates);
        int longestStreak = calculateLongestStreak(distinctDates);

        // Date-based metrics (Current UTC Month / Year)
        LocalDate now = LocalDate.now(ZoneOffset.UTC);
        int currentYear = now.getYear();
        int currentMonth = now.getMonthValue();

        long thisMonthWatches = allLogs.stream()
                .filter(l -> {
                    LocalDate d = l.getWatchedAt().atZone(ZoneOffset.UTC).toLocalDate();
                    return d.getYear() == currentYear && d.getMonthValue() == currentMonth;
                })
                .count();

        long thisYearWatches = allLogs.stream()
                .filter(l -> l.getWatchedAt().atZone(ZoneOffset.UTC).toLocalDate().getYear() == currentYear)
                .count();

        // Top genres
        Map<String, Long> genreCounts = new HashMap<>();
        for (WatchLog log : allLogs) {
            if (log.getGenres() != null && !log.getGenres().isBlank()) {
                Arrays.stream(log.getGenres().split(","))
                        .map(String::trim)
                        .filter(g -> !g.isBlank())
                        .forEach(g -> genreCounts.put(g, genreCounts.getOrDefault(g, 0L) + 1));
            }
        }

        List<WatchStatsResponse.GenreStat> topGenres = genreCounts.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                .limit(5)
                .map(e -> new WatchStatsResponse.GenreStat(e.getKey(), e.getValue()))
                .toList();

        // Location breakdown
        Map<WatchLocation, Long> byLocation = new EnumMap<>(WatchLocation.class);
        for (WatchLog log : allLogs) {
            if (log.getWatchLocation() != null) {
                byLocation.put(log.getWatchLocation(), byLocation.getOrDefault(log.getWatchLocation(), 0L) + 1);
            }
        }

        // Favorite decade
        Map<String, Long> decadeCounts = new HashMap<>();
        for (WatchLog log : allLogs) {
            if (log.getReleaseYear() != null && log.getReleaseYear() > 1900) {
                int decade = (log.getReleaseYear() / 10) * 10;
                String decadeKey = decade + "s";
                decadeCounts.put(decadeKey, decadeCounts.getOrDefault(decadeKey, 0L) + 1);
            }
        }

        String favoriteDecade = decadeCounts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);

        // First and last watch timestamps
        Instant firstWatch = allLogs.stream()
                .map(WatchLog::getWatchedAt)
                .min(Comparator.naturalOrder())
                .orElse(null);

        Instant lastWatch = allLogs.stream()
                .map(WatchLog::getWatchedAt)
                .max(Comparator.naturalOrder())
                .orElse(null);

        return new WatchStatsResponse(
                userId,
                resolvedUsername,
                totalWatches,
                totalMovies,
                totalTvShows,
                totalWatchTimeMinutes,
                formattedWatchTime,
                averageRating,
                countRated,
                currentStreak,
                longestStreak,
                thisMonthWatches,
                thisYearWatches,
                topGenres,
                Collections.emptyList(), // topActors placeholder
                byLocation,
                favoriteDecade,
                firstWatch,
                lastWatch
        );
    }

    public int calculateCurrentStreak(List<LocalDate> sortedDates) {
        if (sortedDates == null || sortedDates.isEmpty()) {
            return 0;
        }

        Set<LocalDate> dateSet = new HashSet<>(sortedDates);
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        LocalDate pointer = today;

        // If nothing logged today, streak may be active from yesterday
        if (!dateSet.contains(pointer)) {
            pointer = today.minusDays(1);
            if (!dateSet.contains(pointer)) {
                return 0;
            }
        }

        int streak = 0;
        while (dateSet.contains(pointer)) {
            streak++;
            pointer = pointer.minusDays(1);
        }

        return streak;
    }

    public int calculateLongestStreak(List<LocalDate> sortedDates) {
        if (sortedDates == null || sortedDates.isEmpty()) {
            return 0;
        }

        List<LocalDate> ordered = sortedDates.stream()
                .distinct()
                .sorted(Comparator.reverseOrder())
                .toList();

        int maxStreak = 1;
        int currentRun = 1;

        for (int i = 0; i < ordered.size() - 1; i++) {
            LocalDate current = ordered.get(i);
            LocalDate next = ordered.get(i + 1);

            if (current.minusDays(1).equals(next)) {
                currentRun++;
                maxStreak = Math.max(maxStreak, currentRun);
            } else {
                currentRun = 1;
            }
        }

        return maxStreak;
    }
}
