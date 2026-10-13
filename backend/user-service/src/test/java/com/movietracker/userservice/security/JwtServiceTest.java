package com.movietracker.userservice.security;

import com.movietracker.userservice.config.AppProperties;
import com.movietracker.userservice.exception.InvalidTokenException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.Duration;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("JwtService Unit Tests")
class JwtServiceTest {

    private static final String TEST_SECRET = "abcdefghijklmnopqrstuvwxyz1234567890ABCDEFGHIJKLMNOPQRSTUVWXYZ12"; // 64 chars

    private AppProperties appProperties;
    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        appProperties = new AppProperties(
                new AppProperties.Jwt(TEST_SECRET, Duration.ofMinutes(15), Duration.ofDays(7)),
                new AppProperties.Cors(List.of("http://localhost:3000"), List.of("GET"), List.of("*"), true),
                new AppProperties.Tmdb("https://api.themoviedb.org/3", "key")
        );
        jwtService = new JwtService(appProperties);
    }

    @Test
    @DisplayName("Should generate and parse access token with correct username, userId, and role")
    void shouldGenerateAndParseAccessToken() {
        UUID userId = UUID.randomUUID();
        UserPrincipal principal = new UserPrincipal(
                userId,
                "john_doe",
                "john@example.com",
                "hashedPass",
                true,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );

        String token = jwtService.generateAccessToken(principal);

        assertThat(token).isNotBlank();
        assertThat(jwtService.extractUsername(token)).isEqualTo("john_doe");
        assertThat(jwtService.extractUserId(token)).isEqualTo(userId);
        assertThat(jwtService.extractTokenType(token)).isEqualTo("access");
        assertThat(jwtService.extractExpiration(token)).isAfter(new Date());
        assertThat(jwtService.isTokenValid(token, principal)).isTrue();
    }

    @Test
    @DisplayName("Should throw InvalidTokenException when token is expired")
    void shouldThrowInvalidTokenExceptionWhenTokenExpired() {
        // Create service with 1ms TTL
        AppProperties expiredProperties = new AppProperties(
                new AppProperties.Jwt(TEST_SECRET, Duration.ofMillis(1), Duration.ofMillis(1)),
                appProperties.cors(),
                appProperties.tmdb()
        );
        JwtService shortLivedService = new JwtService(expiredProperties);

        UUID userId = UUID.randomUUID();
        UserPrincipal principal = new UserPrincipal(
                userId,
                "john_doe",
                "john@example.com",
                "hashedPass",
                true,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );

        String token = shortLivedService.generateAccessToken(principal);

        // Allow token to expire
        try {
            Thread.sleep(15);
        } catch (InterruptedException ignored) {}

        assertThatThrownBy(() -> shortLivedService.extractUsername(token))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessageContaining("Token expired");

        assertThat(shortLivedService.isTokenValid(token, principal)).isFalse();
    }

    @Test
    @DisplayName("Should throw InvalidTokenException when token is tampered with")
    void shouldThrowInvalidTokenExceptionWhenTokenTampered() {
        UUID userId = UUID.randomUUID();
        UserPrincipal principal = new UserPrincipal(
                userId,
                "john_doe",
                "john@example.com",
                "hashedPass",
                true,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );

        String token = jwtService.generateAccessToken(principal);
        String tamperedToken = token + "corrupted";

        assertThatThrownBy(() -> jwtService.extractUsername(tamperedToken))
                .isInstanceOf(InvalidTokenException.class);

        assertThat(jwtService.isTokenValid(tamperedToken, principal)).isFalse();
    }

    @Test
    @DisplayName("Should generate and validate refresh token JWT")
    void shouldGenerateAndValidateRefreshTokenJwt() {
        UUID userId = UUID.randomUUID();
        String tokenId = UUID.randomUUID().toString();
        UserPrincipal principal = new UserPrincipal(
                userId,
                "john_doe",
                "john@example.com",
                "hashedPass",
                true,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );

        String refreshJwt = jwtService.generateRefreshTokenJwt(tokenId, principal);

        assertThat(refreshJwt).isNotBlank();
        assertThat(jwtService.extractTokenId(refreshJwt)).isEqualTo(tokenId);
        assertThat(jwtService.extractTokenType(refreshJwt)).isEqualTo("refresh");
        assertThat(jwtService.isRefreshTokenValid(refreshJwt)).isTrue();
    }
}
