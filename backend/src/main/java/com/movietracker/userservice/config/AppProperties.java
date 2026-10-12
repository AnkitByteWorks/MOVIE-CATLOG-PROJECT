package com.movietracker.userservice.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;
import java.util.List;

/**
 * Strongly-typed application configuration properties bound to the {@code app} prefix.
 * <p>
 * Encapsulates settings for JWT issuance and verification, Cross-Origin Resource
 * Sharing (CORS) policies, and TMDB integration configuration.
 *
 * @param jwt  JWT token generation and verification properties
 * @param cors Cross-Origin Resource Sharing configurations
 * @param tmdb External TMDB service integration settings
 */
@Validated
@ConfigurationProperties(prefix = "app")
public record AppProperties(
        @NotNull @Valid Jwt jwt,
        @NotNull @Valid Cors cors,
        @NotNull @Valid Tmdb tmdb
) {

    /**
     * JSON Web Token (JWT) configuration settings.
     *
     * @param secret          HMAC secret key used for signing and verifying tokens
     * @param accessTokenTtl  time-to-live duration for short-lived access tokens
     * @param refreshTokenTtl time-to-live duration for long-lived refresh tokens
     */
    public record Jwt(
            @NotBlank String secret,
            @NotNull Duration accessTokenTtl,
            @NotNull Duration refreshTokenTtl
    ) {}

    /**
     * Cross-Origin Resource Sharing (CORS) security configuration settings.
     *
     * @param allowedOrigins   list of permitted client origin URLs
     * @param allowedMethods   list of HTTP methods permitted for CORS requests
     * @param allowedHeaders   list of request headers accepted from clients
     * @param allowCredentials whether cookies and authentication credentials can be shared
     */
    public record Cors(
            @NotEmpty List<String> allowedOrigins,
            @NotEmpty List<String> allowedMethods,
            @NotEmpty List<String> allowedHeaders,
            @NotNull Boolean allowCredentials
    ) {}

    /**
     * TMDB (The Movie Database) client integration configuration.
     *
     * @param baseUrl base URL of the TMDB API
     * @param apiKey  API authentication key for TMDB service calls
     */
    public record Tmdb(
            @NotBlank String baseUrl,
            @NotBlank String apiKey
    ) {}
}
