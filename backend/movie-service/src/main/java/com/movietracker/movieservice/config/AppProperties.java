package com.movietracker.movieservice.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Strongly typed configuration properties for movie-service.
 */
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private Cache cache = new Cache();
    private Image image = new Image();
    private Trending trending = new Trending();

    public Cache cache() {
        return cache;
    }

    public Image image() {
        return image;
    }

    public Trending trending() {
        return trending;
    }

    @Getter
    @Setter
    public static class Image {
        private String baseUrl = "https://image.tmdb.org/t/p";
        private String posterSize = "w500";
        private String backdropSize = "w1280";

        /**
         * Resolves full poster image URL from relative path.
         */
        public String buildPosterUrl(String posterPath) {
            if (posterPath == null || posterPath.isBlank()) {
                return null;
            }
            String cleanPath = posterPath.startsWith("/") ? posterPath : "/" + posterPath;
            return baseUrl + "/" + posterSize + cleanPath;
        }

        /**
         * Resolves full backdrop image URL from relative path.
         */
        public String buildBackdropUrl(String backdropPath) {
            if (backdropPath == null || backdropPath.isBlank()) {
                return null;
            }
            String cleanPath = backdropPath.startsWith("/") ? backdropPath : "/" + backdropPath;
            return baseUrl + "/" + backdropSize + cleanPath;
        }
    }

    @Getter
    @Setter
    public static class Cache {
        private Map<String, Duration> ttls = new HashMap<>();

        public Duration getTtl(String cacheName, Duration defaultTtl) {
            return ttls.getOrDefault(cacheName, defaultTtl);
        }
    }

    @Getter
    @Setter
    public static class Trending {
        private String syncCron = "0 0 4 * * ?";
        private String externalFeedUrl = "";
        private int externalFeedTimeoutSeconds = 3;
        private List<String> languagesIndia = List.of("hi", "te", "ta", "ml", "kn", "pa", "bn");
        private int minVotesGlobal = 0;
        private int cutoffYearIndia = 2024;
        private int cutoffYearGlobal = 2024;
    }
}
