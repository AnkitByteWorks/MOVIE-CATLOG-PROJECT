package com.movietracker.userservice.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Follow relationship state and counter metrics between current user and target user.
 */
@Schema(description = "Follow status and follower counts between current user and a target user")
public record FollowStateResponse(
        @Schema(description = "Username of the target user", example = "jane_doe")
        String username,

        @Schema(description = "Whether the current user is following the target user", example = "true")
        boolean following,

        @Schema(description = "Whether the target user is following the current user", example = "false")
        boolean followedBy,

        @Schema(description = "Total follower count of the target user", example = "120")
        int followerCount,

        @Schema(description = "Total following count of the target user", example = "75")
        int followingCount
) {
}
