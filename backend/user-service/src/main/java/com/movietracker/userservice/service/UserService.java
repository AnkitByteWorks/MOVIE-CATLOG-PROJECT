package com.movietracker.userservice.service;

import com.movietracker.userservice.dto.request.ChangePasswordRequest;
import com.movietracker.userservice.dto.request.UpdateProfileRequest;
import com.movietracker.userservice.dto.response.FollowStateResponse;
import com.movietracker.userservice.dto.response.PublicUserResponse;
import com.movietracker.userservice.dto.response.UserProfileResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

/**
 * Service interface governing user profile management, security operations, and the social graph.
 */
public interface UserService {

    /**
     * Retrieves the profile of the currently authenticated owner.
     *
     * @param currentUserId owner user UUID
     * @return full user profile response
     */
    UserProfileResponse getMyProfile(UUID currentUserId);

    /**
     * Updates optional profile details for the authenticated user.
     *
     * @param currentUserId owner user UUID
     * @param request       profile update payload
     * @return updated user profile response
     */
    UserProfileResponse updateProfile(UUID currentUserId, UpdateProfileRequest request);

    /**
     * Modifies the authenticated user's password, invalidating existing sessions.
     *
     * @param currentUserId owner user UUID
     * @param request       password change payload
     */
    void changePassword(UUID currentUserId, ChangePasswordRequest request);

    /**
     * Retrieves public profile metrics and metadata by username handle.
     *
     * @param username target username
     * @return public profile view
     */
    PublicUserResponse getPublicProfile(String username);

    /**
     * Resolves directional follow state and counter metrics between current user and target user.
     *
     * @param currentUserId  calling user UUID, or null if unauthenticated
     * @param targetUsername target username handle
     * @return follow state response
     */
    FollowStateResponse getFollowState(UUID currentUserId, String targetUsername);

    /**
     * Establishes a directional follow relationship from the current user to the target user.
     *
     * @param currentUserId  initiating user UUID
     * @param targetUsername target username handle
     * @return updated follow state
     */
    FollowStateResponse follow(UUID currentUserId, String targetUsername);

    /**
     * Removes an established follow relationship from the current user to the target user.
     *
     * @param currentUserId  initiating user UUID
     * @param targetUsername target username handle
     * @return updated follow state
     */
    FollowStateResponse unfollow(UUID currentUserId, String targetUsername);

    /**
     * Retrieves a paginated list of accounts following the specified user.
     *
     * @param username target username handle
     * @param pageable pagination parameters
     * @return page of follower public profiles
     */
    Page<PublicUserResponse> getFollowers(String username, Pageable pageable);

    /**
     * Retrieves a paginated list of accounts that the specified user is following.
     *
     * @param username target username handle
     * @param pageable pagination parameters
     * @return page of followed public profiles
     */
    Page<PublicUserResponse> getFollowing(String username, Pageable pageable);

    /**
     * Searches for user accounts matching a query string across usernames.
     *
     * @param query    search query term
     * @param pageable pagination parameters
     * @return page of matching public profiles
     */
    Page<PublicUserResponse> searchUsers(String query, Pageable pageable);
}
