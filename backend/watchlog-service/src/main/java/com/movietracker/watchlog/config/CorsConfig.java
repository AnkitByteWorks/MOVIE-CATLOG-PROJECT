package com.movietracker.watchlog.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Cross-Origin Resource Sharing (CORS) configuration based on externalized application properties.
 */
@Configuration
@RequiredArgsConstructor
public class CorsConfig {

    private final AppProperties appProperties;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        AppProperties.Cors corsProps = appProperties.cors();

        if (corsProps != null) {
            config.setAllowedOrigins(corsProps.allowedOrigins() != null ? corsProps.allowedOrigins() : List.of("*"));
            config.setAllowedMethods(corsProps.allowedMethods() != null ? corsProps.allowedMethods() : List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
            config.setAllowedHeaders(corsProps.allowedHeaders() != null ? corsProps.allowedHeaders() : List.of("*"));
            config.setAllowCredentials(Boolean.TRUE.equals(corsProps.allowCredentials()));
        } else {
            config.setAllowedOriginPatterns(List.of("*"));
            config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
            config.setAllowedHeaders(List.of("*"));
            config.setAllowCredentials(true);
        }

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
