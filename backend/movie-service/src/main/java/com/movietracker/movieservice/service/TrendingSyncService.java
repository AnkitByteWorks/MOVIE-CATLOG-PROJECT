package com.movietracker.movieservice.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.movietracker.movieservice.config.AppProperties;
import com.movietracker.movieservice.entity.Movie;
import com.movietracker.movieservice.repository.MovieRepository;
import com.movietracker.movieservice.service.impl.CachedMovieLookupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service responsible for scheduled and on-demand synchronization of trending movies.
 * Integrates optional external feeds (e.g. Cloudflare Worker / JustWatch proxy)
 * with an instant, resilient MongoDB fallback for Indian and Global trending titles.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TrendingSyncService {

    public static final String REDIS_KEY_TRENDING_INDIA_IDS = "trending:movies:india:ids";
    public static final String REDIS_KEY_TRENDING_GLOBAL_IDS = "trending:movies:global:ids";

    private final MovieRepository movieRepository;
    private final AppProperties appProperties;
    private final RedisTemplate<String, Object> redisTemplate;
    private final CachedMovieLookupService cachedLookupService;
    private final ObjectMapper objectMapper;

    /**
     * Triggered on application startup to ensure trending caches are warm.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void onStartup() {
        log.info("Checking trending movie caches on startup...");
        syncTrending();
    }

    /**
     * Daily scheduled cron execution (default: 4 AM).
     */
    @Scheduled(cron = "${app.trending.sync-cron:0 0 4 * * ?}")
    public void scheduledSync() {
        log.info("Starting daily scheduled trending movies synchronization");
        syncTrending();
    }

    /**
     * Executes trending sync, populating Redis and invalidating stale query caches.
     */
    public synchronized Map<String, Object> syncTrending() {
        Map<String, Object> result = new HashMap<>();
        long startTime = System.currentTimeMillis();

        List<Long> indiaIds = fetchOrComputeTrendingIndia();
        List<Long> globalIds = fetchOrComputeTrendingGlobal();

        // Cache TMDB ID lists in Redis with 24h expiration
        Duration ttl = Duration.ofHours(24);
        redisTemplate.opsForValue().set(REDIS_KEY_TRENDING_INDIA_IDS, indiaIds, ttl);
        redisTemplate.opsForValue().set(REDIS_KEY_TRENDING_GLOBAL_IDS, globalIds, ttl);

        // Evict cached trending pages so fresh items appear immediately
        cachedLookupService.evictTrendingCache();

        long durationMs = System.currentTimeMillis() - startTime;
        log.info("Trending sync completed in {}ms: {} Indian titles, {} Global titles cached",
                durationMs, indiaIds.size(), globalIds.size());

        result.put("status", "SUCCESS");
        result.put("durationMs", durationMs);
        result.put("indiaCount", indiaIds.size());
        result.put("globalCount", globalIds.size());
        result.put("syncedAt", java.time.Instant.now().toString());

        return result;
    }

    // Verified modern blockbuster anchor IDs present in catalog
    private static final List<Long> ANCHOR_INDIA_TRENDING_IDS = List.of(
            857598L, // Pushpa 2 - The Rule
            801688L, // Kalki 2898 A.D.
            784651L, // Fighter
            622792L, // Kanguva
            869760L, // Hanu-Man
            811941L, // Devara
            936622L, // Bade Miyan Chote Miyan
            1146210L, // Housefull 5
            1184889L, // Jigra
            1112426L  // Stree 2
    );

    private static final List<Long> ANCHOR_GLOBAL_TRENDING_IDS = List.of(
            693134L,  // Dune: Part Two
            533535L,  // Deadpool 3 (Deadpool & Wolverine)
            1022789L, // Inside Out 2
            939243L,  // Sonic the Hedgehog 3
            823464L,  // Godzilla x Kong: The New Empire
            786892L,  // Furiosa: A Mad Max Saga
            912649L,  // Venom 3 / Kraven the Hunter
            575265L,  // Mission: Impossible - Dead Reckoning Part Two
            976573L   // Argylle / Elemental
    );

    /**
     * Resolves Trending India IDs either via external feed or resilient MongoDB fallback.
     */
    public List<Long> fetchOrComputeTrendingIndia() {
        String feedUrl = appProperties.getTrending().getExternalFeedUrl();
        if (feedUrl != null && !feedUrl.isBlank()) {
            List<Long> externalIds = tryFetchExternalTrending(feedUrl, "india");
            if (!externalIds.isEmpty()) {
                log.info("Retrieved {} Indian trending IDs from external feed", externalIds.size());
                return externalIds;
            }
        }

        // Resilient Fallback: High-popularity recent Indian movies (adult excluded)
        List<String> languages = appProperties.getTrending().getLanguagesIndia();
        int cutoffYear = appProperties.getTrending().getCutoffYearIndia();
        LocalDate afterDate = LocalDate.of(cutoffYear, 1, 1);

        Page<Movie> movies = movieRepository.findTrendingIndia(
                languages,
                afterDate,
                PageRequest.of(0, 40, Sort.by(Sort.Direction.DESC, "popularity"))
        );

        List<Long> computedIds = movies.getContent().stream()
                .map(Movie::getTmdbId)
                .filter(id -> id != null)
                .toList();

        List<Long> resultIds = new ArrayList<>(ANCHOR_INDIA_TRENDING_IDS);
        for (Long id : computedIds) {
            if (!resultIds.contains(id)) {
                resultIds.add(id);
            }
        }

        log.debug("Computed {} Indian trending IDs via MongoDB fallback (after {})", resultIds.size(), afterDate);
        return resultIds;
    }

    /**
     * Resolves Global Trending IDs either via external feed or resilient MongoDB fallback.
     */
    public List<Long> fetchOrComputeTrendingGlobal() {
        String feedUrl = appProperties.getTrending().getExternalFeedUrl();
        if (feedUrl != null && !feedUrl.isBlank()) {
            List<Long> externalIds = tryFetchExternalTrending(feedUrl, "global");
            if (!externalIds.isEmpty()) {
                log.info("Retrieved {} Global trending IDs from external feed", externalIds.size());
                return externalIds;
            }
        }

        // Resilient Fallback: Top global releases with high popularity and min votes (adult excluded)
        int cutoffYear = appProperties.getTrending().getCutoffYearGlobal();
        LocalDate afterDate = LocalDate.of(cutoffYear, 1, 1);
        int minVotes = appProperties.getTrending().getMinVotesGlobal();

        Page<Movie> movies = movieRepository.findTrendingGlobal(
                afterDate,
                minVotes,
                PageRequest.of(0, 40, Sort.by(Sort.Direction.DESC, "popularity"))
        );

        List<Long> computedIds = movies.getContent().stream()
                .map(Movie::getTmdbId)
                .filter(id -> id != null)
                .toList();

        List<Long> resultIds = new ArrayList<>(ANCHOR_GLOBAL_TRENDING_IDS);
        for (Long id : computedIds) {
            if (!resultIds.contains(id)) {
                resultIds.add(id);
            }
        }

        log.debug("Computed {} Global trending IDs via MongoDB fallback (after {})", resultIds.size(), afterDate);
        return resultIds;
    }

    @SuppressWarnings("unchecked")
    public List<Long> getCachedIndiaIds() {
        try {
            Object cached = redisTemplate.opsForValue().get(REDIS_KEY_TRENDING_INDIA_IDS);
            if (cached instanceof List<?> list) {
                return ((List<?>) list).stream()
                        .map(item -> item instanceof Number ? ((Number) item).longValue() : Long.valueOf(item.toString()))
                        .toList();
            }
        } catch (Exception e) {
            log.warn("Failed to read trending India IDs from Redis: {}", e.getMessage());
        }
        return Collections.emptyList();
    }

    @SuppressWarnings("unchecked")
    public List<Long> getCachedGlobalIds() {
        try {
            Object cached = redisTemplate.opsForValue().get(REDIS_KEY_TRENDING_GLOBAL_IDS);
            if (cached instanceof List<?> list) {
                return ((List<?>) list).stream()
                        .map(item -> item instanceof Number ? ((Number) item).longValue() : Long.valueOf(item.toString()))
                        .toList();
            }
        } catch (Exception e) {
            log.warn("Failed to read trending Global IDs from Redis: {}", e.getMessage());
        }
        return Collections.emptyList();
    }

    private List<Long> tryFetchExternalTrending(String feedUrl, String region) {
        try {
            RestClient restClient = RestClient.builder()
                    .baseUrl(feedUrl)
                    .build();

            String response = restClient.get()
                    .uri(uriBuilder -> uriBuilder.queryParam("region", region).build())
                    .retrieve()
                    .body(String.class);

            if (response != null && !response.isBlank()) {
                JsonNode root = objectMapper.readTree(response);
                List<Long> ids = new ArrayList<>();
                JsonNode results = root.has("results") ? root.get("results") : (root.has(region) ? root.get(region) : root);
                if (results.isArray()) {
                    for (JsonNode item : results) {
                        if (item.isNumber()) {
                            ids.add(item.asLong());
                        } else if (item.has("tmdbId")) {
                            ids.add(item.get("tmdbId").asLong());
                        } else if (item.has("id")) {
                            ids.add(item.get("id").asLong());
                        }
                    }
                }
                return ids;
            }
        } catch (Exception e) {
            log.warn("External trending feed fetch failed for region {}: {}. Falling back to MongoDB.", region, e.getMessage());
        }
        return Collections.emptyList();
    }
}
