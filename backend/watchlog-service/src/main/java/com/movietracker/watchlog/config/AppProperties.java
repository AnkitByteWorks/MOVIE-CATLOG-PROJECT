package com.movietracker.watchlog.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;
import java.util.List;

/**
 * Strongly typed configuration properties for watchlog-service.
 */
@Validated
@ConfigurationProperties(prefix = "app")
public record AppProperties(
        Jwt jwt,
        Cors cors,
        Clients clients,
        Stats stats
) {
    public record Jwt(String secret, Duration accessTokenTtl) {
        public Jwt {
            if (accessTokenTtl == null) {
                accessTokenTtl = Duration.ofMinutes(15);
            }
        }
    }

    public record Cors(
            List<String> allowedOrigins,
            List<String> allowedMethods,
            List<String> allowedHeaders,
            Boolean allowCredentials
    ) {
    }

    public record Clients(
            String userServiceUrl,
            String movieServiceUrl
    ) {
    }

    public record Stats(
            Duration cacheTtl
    ) {
        public Stats {
            if (cacheTtl == null) {
                cacheTtl = Duration.ofMinutes(15);
            }
        }
    }
}
