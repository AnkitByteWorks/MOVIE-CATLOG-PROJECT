package com.movietracker.userservice.service;

import java.time.Instant;
import java.util.UUID;

/**
 * Data payload stored in Redis representing an active refresh token session.
 *
 * @param tokenId   unique token identifier (UUID format)
 * @param userId    associated user entity UUID
 * @param username  associated user handle
 * @param expiresAt token expiration timestamp
 * @param issuedAt  token generation timestamp
 * @param userAgent client User-Agent header from issuance request
 * @param ip        client remote IP address from issuance request
 */
public record RefreshTokenData(
        String tokenId,
        UUID userId,
        String username,
        Instant expiresAt,
        Instant issuedAt,
        String userAgent,
        String ip
) {
    /**
     * Backward-compatible constructor when tokenId is omitted in payload.
     */
    public RefreshTokenData(
            UUID userId,
            String username,
            Instant expiresAt,
            Instant issuedAt,
            String userAgent,
            String ip
    ) {
        this(null, userId, username, expiresAt, issuedAt, userAgent, ip);
    }
}
