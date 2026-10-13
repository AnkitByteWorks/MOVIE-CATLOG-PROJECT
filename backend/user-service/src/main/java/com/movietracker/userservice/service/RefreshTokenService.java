package com.movietracker.userservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.movietracker.userservice.config.AppProperties;
import com.movietracker.userservice.entity.User;
import com.movietracker.userservice.exception.InvalidTokenException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

/**
 * Service managing Redis-backed refresh token persistence, validation, rotation, and revocation.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenService {

    private static final String KEY_PREFIX = "refresh:token:";

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;
    private final AppProperties appProperties;

    /**
     * Issues a new refresh token session stored in Redis with configured TTL.
     *
     * @param user      authenticated user entity
     * @param userAgent client device/browser User-Agent header
     * @param ip        client network IP address
     * @return generated refresh token identifier (UUID format)
     */
    public String createRefreshToken(User user, String userAgent, String ip) {
        String tokenId = UUID.randomUUID().toString();
        Instant now = Instant.now();
        Duration ttl = appProperties.jwt().refreshTokenTtl();
        Instant expiresAt = now.plus(ttl);

        RefreshTokenData tokenData = new RefreshTokenData(
                tokenId,
                user.getId(),
                user.getUsername(),
                expiresAt,
                now,
                userAgent,
                ip
        );

        String key = KEY_PREFIX + tokenId;
        try {
            String json = objectMapper.writeValueAsString(tokenData);
            stringRedisTemplate.opsForValue().set(key, json, ttl);
            log.debug("Stored refresh token in Redis for user '{}' with TTL {}", user.getUsername(), ttl);
            return tokenId;
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize refresh token data for user: {}", user.getUsername(), e);
            throw new IllegalStateException("Error serializing refresh token metadata", e);
        }
    }

    /**
     * Validates an existing refresh token, revokes it, issues a rotated token, and saves the new token to Redis.
     *
     * @param oldTokenId previous refresh token UUID identifier
     * @param userAgent  client device/browser User-Agent header
     * @param ip         client network IP address
     * @return newly rotated {@link RefreshTokenData} session containing the new tokenId
     */
    public RefreshTokenData validateAndRotate(String oldTokenId, String userAgent, String ip) {
        String oldKey = KEY_PREFIX + oldTokenId;
        String json = stringRedisTemplate.opsForValue().get(oldKey);

        if (json == null || json.isBlank()) {
            log.warn("Attempted to validate missing or expired refresh token: {}", oldTokenId);
            throw new InvalidTokenException("Refresh token is expired or revoked");
        }

        RefreshTokenData oldData;
        try {
            oldData = objectMapper.readValue(json, RefreshTokenData.class);
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize refresh token data for token: {}", oldTokenId, e);
            throw new InvalidTokenException("Malformed refresh token metadata");
        }

        if (oldData.expiresAt() != null && oldData.expiresAt().isBefore(Instant.now())) {
            stringRedisTemplate.delete(oldKey);
            log.warn("Refresh token expired at {} for user '{}'", oldData.expiresAt(), oldData.username());
            throw new InvalidTokenException("Refresh token has expired");
        }

        // Revoke the old token (rotation policy)
        stringRedisTemplate.delete(oldKey);

        // Issue and persist rotated token
        String newTokenId = UUID.randomUUID().toString();
        Instant now = Instant.now();
        Duration ttl = appProperties.jwt().refreshTokenTtl();
        Instant expiresAt = now.plus(ttl);

        RefreshTokenData newData = new RefreshTokenData(
                newTokenId,
                oldData.userId(),
                oldData.username(),
                expiresAt,
                now,
                userAgent,
                ip
        );

        String newKey = KEY_PREFIX + newTokenId;
        try {
            String newJson = objectMapper.writeValueAsString(newData);
            stringRedisTemplate.opsForValue().set(newKey, newJson, ttl);
            log.debug("Successfully rotated refresh token for user '{}'", oldData.username());
            return newData;
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize rotated refresh token data for user: {}", oldData.username(), e);
            throw new IllegalStateException("Error serializing rotated refresh token metadata", e);
        }
    }

    /**
     * Revokes a refresh token by deleting it from Redis.
     *
     * @param tokenId refresh token UUID identifier to revoke
     */
    public void revoke(String tokenId) {
        if (tokenId != null && !tokenId.isBlank()) {
            stringRedisTemplate.delete(KEY_PREFIX + tokenId);
            log.debug("Revoked refresh token: {}", tokenId);
        }
    }

    /**
     * Revokes all active refresh tokens associated with a given user ID.
     *
     * @param userId unique user identifier
     */
    public void revokeAllForUser(UUID userId) {
        log.info("Revoke all refresh tokens requested for user ID: {}", userId);
        if (userId == null) {
            return;
        }

        ScanOptions scanOptions = ScanOptions.scanOptions()
                .match(KEY_PREFIX + "*")
                .count(100)
                .build();

        try (var cursor = stringRedisTemplate.scan(scanOptions)) {
            while (cursor.hasNext()) {
                String key = cursor.next();
                String json = stringRedisTemplate.opsForValue().get(key);
                if (json != null && !json.isBlank()) {
                    try {
                        RefreshTokenData data = objectMapper.readValue(json, RefreshTokenData.class);
                        if (userId.equals(data.userId())) {
                            stringRedisTemplate.delete(key);
                            log.debug("Revoked refresh token '{}' for user ID: {}", key, userId);
                        }
                    } catch (JsonProcessingException e) {
                        log.warn("Failed to parse token data during revokeAllForUser for key: {}", key, e);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error occurred while scanning keys to revoke all tokens for user ID: {}", userId, e);
        }
    }
}
