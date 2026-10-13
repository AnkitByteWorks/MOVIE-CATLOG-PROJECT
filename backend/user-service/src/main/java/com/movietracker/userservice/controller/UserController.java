package com.movietracker.userservice.controller;

import com.movietracker.userservice.dto.request.ChangePasswordRequest;
import com.movietracker.userservice.dto.request.UpdateProfileRequest;
import com.movietracker.userservice.dto.response.FollowStateResponse;
import com.movietracker.userservice.dto.response.MessageResponse;
import com.movietracker.userservice.dto.response.PublicUserResponse;
import com.movietracker.userservice.dto.response.UserProfileResponse;
import com.movietracker.userservice.entity.User;
import com.movietracker.userservice.exception.ErrorResponse;
import com.movietracker.userservice.repository.UserRepository;
import com.movietracker.userservice.security.AppUserPrincipal;
import com.movietracker.userservice.security.UserPrincipal;
import com.movietracker.userservice.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * REST controller exposing user profile management and social follow graph operations.
 */
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "Endpoints for user profile management, user search, and social graph interactions")
@Validated
@Slf4j
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;

    /**
     * Retrieves the profile of the currently authenticated user.
     *
     * @param authentication current security context
     * @return 200 OK with {@link UserProfileResponse}
     */
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Get current authenticated profile",
            description = "Retrieves full profile details for the authenticated caller",
            security = @SecurityRequirement(name = "BearerAuth")
    )
    @ApiResponse(responseCode = "200", description = "Profile retrieved successfully",
            content = @Content(schema = @Schema(implementation = UserProfileResponse.class)))
    @ApiResponse(responseCode = "401", description = "Unauthenticated caller",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<UserProfileResponse> getMyProfile(Authentication authentication) {
        UUID currentUserId = requireCurrentUserId(authentication);
        UserProfileResponse response = userService.getMyProfile(currentUserId);
        return ResponseEntity.ok(response);
    }

    /**
     * Updates optional profile details for the authenticated user.
     *
     * @param authentication current security context
     * @param request        update fields
     * @return 200 OK with {@link UserProfileResponse}
     */
    @PatchMapping("/me")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Update user profile",
            description = "Partially updates bio, location, website, displayName, or avatarUrl for authenticated caller",
            security = @SecurityRequirement(name = "BearerAuth")
    )
    @ApiResponse(responseCode = "200", description = "Profile updated successfully",
            content = @Content(schema = @Schema(implementation = UserProfileResponse.class)))
    @ApiResponse(responseCode = "400", description = "Invalid payload field constraints",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<UserProfileResponse> updateProfile(
            Authentication authentication,
            @Valid @RequestBody UpdateProfileRequest request
    ) {
        UUID currentUserId = requireCurrentUserId(authentication);
        UserProfileResponse response = userService.updateProfile(currentUserId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Modifies the authenticated user's account password.
     *
     * @param authentication current security context
     * @param request        password change payload
     * @return 200 OK with {@link MessageResponse}
     */
    @PostMapping("/me/change-password")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Change password",
            description = "Updates password and invalidates all active refresh tokens for the authenticated caller",
            security = @SecurityRequirement(name = "BearerAuth")
    )
    @ApiResponse(responseCode = "200", description = "Password changed successfully",
            content = @Content(schema = @Schema(implementation = MessageResponse.class)))
    @ApiResponse(responseCode = "400", description = "Current password incorrect or new password validation failure",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<MessageResponse> changePassword(
            Authentication authentication,
            @Valid @RequestBody ChangePasswordRequest request
    ) {
        UUID currentUserId = requireCurrentUserId(authentication);
        userService.changePassword(currentUserId, request);
        return ResponseEntity.ok(new MessageResponse("Password changed successfully"));
    }

    /**
     * Searches users by case-insensitive partial username match.
     *
     * @param query    search query string
     * @param pageable pagination parameters
     * @return 200 OK with page of {@link PublicUserResponse}
     */
    @GetMapping("/search")
    @Operation(summary = "Search users", description = "Searches user profiles by partial username match")
    @ApiResponse(responseCode = "200", description = "Matching users retrieved successfully")
    public ResponseEntity<Page<PublicUserResponse>> searchUsers(
            @RequestParam(name = "q", defaultValue = "") String query,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        Page<PublicUserResponse> page = userService.searchUsers(query, pageable);
        return ResponseEntity.ok(page);
    }

    /**
     * Retrieves the public profile of a user by username handle.
     *
     * @param username target username handle
     * @return 200 OK with {@link PublicUserResponse}
     */
    @GetMapping("/{username}")
    @Operation(summary = "Get public user profile", description = "Retrieves public profile information and metrics by username")
    @ApiResponse(responseCode = "200", description = "Public profile retrieved successfully",
            content = @Content(schema = @Schema(implementation = PublicUserResponse.class)))
    @ApiResponse(responseCode = "404", description = "User not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<PublicUserResponse> getPublicProfile(@PathVariable String username) {
        PublicUserResponse response = userService.getPublicProfile(username);
        return ResponseEntity.ok(response);
    }

    /**
     * Resolves directional follow state and follower counters between current caller and target user.
     *
     * @param username       target username handle
     * @param authentication security authentication context (optional)
     * @return 200 OK with {@link FollowStateResponse}
     */
    @GetMapping("/{username}/follow-state")
    @Operation(summary = "Get follow state", description = "Checks whether the caller follows the target user and vice versa")
    @ApiResponse(responseCode = "200", description = "Follow state resolved successfully",
            content = @Content(schema = @Schema(implementation = FollowStateResponse.class)))
    @ApiResponse(responseCode = "404", description = "User not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<FollowStateResponse> getFollowState(
            @PathVariable String username,
            Authentication authentication
    ) {
        UUID currentUserId = resolveCurrentUserId(authentication);
        FollowStateResponse response = userService.getFollowState(currentUserId, username);
        return ResponseEntity.ok(response);
    }

    /**
     * Follows the target user.
     *
     * @param username       target username to follow
     * @param authentication security authentication context
     * @return 200 OK with {@link FollowStateResponse}
     */
    @PostMapping("/{username}/follow")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Follow user",
            description = "Establishes a directional follow relation from caller to target user",
            security = @SecurityRequirement(name = "BearerAuth")
    )
    @ApiResponse(responseCode = "200", description = "Target user followed successfully",
            content = @Content(schema = @Schema(implementation = FollowStateResponse.class)))
    @ApiResponse(responseCode = "400", description = "Attempting to self-follow",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Target user not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<FollowStateResponse> follow(
            @PathVariable String username,
            Authentication authentication
    ) {
        UUID currentUserId = requireCurrentUserId(authentication);
        FollowStateResponse response = userService.follow(currentUserId, username);
        return ResponseEntity.ok(response);
    }

    /**
     * Unfollows the target user.
     *
     * @param username       target username to unfollow
     * @param authentication security authentication context
     * @return 200 OK with {@link FollowStateResponse}
     */
    @DeleteMapping("/{username}/follow")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Unfollow user",
            description = "Removes directional follow relation from caller to target user",
            security = @SecurityRequirement(name = "BearerAuth")
    )
    @ApiResponse(responseCode = "200", description = "Target user unfollowed successfully",
            content = @Content(schema = @Schema(implementation = FollowStateResponse.class)))
    @ApiResponse(responseCode = "400", description = "Attempting to self-unfollow",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Target user not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<FollowStateResponse> unfollow(
            @PathVariable String username,
            Authentication authentication
    ) {
        UUID currentUserId = requireCurrentUserId(authentication);
        FollowStateResponse response = userService.unfollow(currentUserId, username);
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves paginated list of accounts following the target user.
     *
     * @param username target username
     * @param pageable pagination parameters
     * @return 200 OK with page of {@link PublicUserResponse}
     */
    @GetMapping("/{username}/followers")
    @Operation(summary = "Get user followers", description = "Retrieves paginated followers of target user")
    @ApiResponse(responseCode = "200", description = "Followers retrieved successfully")
    @ApiResponse(responseCode = "404", description = "User not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<Page<PublicUserResponse>> getFollowers(
            @PathVariable String username,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        Page<PublicUserResponse> page = userService.getFollowers(username, pageable);
        return ResponseEntity.ok(page);
    }

    /**
     * Retrieves paginated list of accounts that the target user is following.
     *
     * @param username target username
     * @param pageable pagination parameters
     * @return 200 OK with page of {@link PublicUserResponse}
     */
    @GetMapping("/{username}/following")
    @Operation(summary = "Get accounts followed by user", description = "Retrieves paginated list of accounts followed by target user")
    @ApiResponse(responseCode = "200", description = "Following accounts retrieved successfully")
    @ApiResponse(responseCode = "404", description = "User not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<Page<PublicUserResponse>> getFollowing(
            @PathVariable String username,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        Page<PublicUserResponse> page = userService.getFollowing(username, pageable);
        return ResponseEntity.ok(page);
    }

    /**
     * Helper to resolve the authenticated caller UUID safely, returning null if unauthenticated.
     */
    private UUID resolveCurrentUserId(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof AppUserPrincipal aup) {
            return aup.id();
        }
        if (principal instanceof UserPrincipal up) {
            return up.getId();
        }
        if (principal instanceof UserDetails ud) {
            return userRepository.findByUsername(ud.getUsername())
                    .map(User::getId)
                    .orElse(null);
        }
        return null;
    }

    /**
     * Helper to require that the caller is authenticated and return their user UUID.
     */
    private UUID requireCurrentUserId(Authentication authentication) {
        UUID userId = resolveCurrentUserId(authentication);
        if (userId == null) {
            throw new AccessDeniedException("User is not authenticated");
        }
        return userId;
    }
}
