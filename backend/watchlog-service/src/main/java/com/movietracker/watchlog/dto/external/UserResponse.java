package com.movietracker.watchlog.dto.external;

import java.time.Instant;
import java.util.UUID;

/**
 * Mirror of user-service PublicUserResponse DTO for Feign client communication.
 */
public record UserResponse(
        UUID id,
        String username,
        String displayName,
        String bio,
        String avatarUrl,
        String location,
        String website,
        Integer followerCount,
        Integer followingCount,
        Instant createdAt
) {
}
