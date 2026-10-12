package com.movietracker.userservice.service.impl;

import com.movietracker.userservice.dto.request.ChangePasswordRequest;
import com.movietracker.userservice.dto.request.UpdateProfileRequest;
import com.movietracker.userservice.dto.response.FollowStateResponse;
import com.movietracker.userservice.dto.response.PublicUserResponse;
import com.movietracker.userservice.dto.response.UserProfileResponse;
import com.movietracker.userservice.entity.FollowRelation;
import com.movietracker.userservice.entity.User;
import com.movietracker.userservice.event.UserEventPublisher;
import com.movietracker.userservice.exception.BusinessException;
import com.movietracker.userservice.exception.ResourceNotFoundException;
import com.movietracker.userservice.mapper.UserMapper;
import com.movietracker.userservice.repository.FollowRelationRepository;
import com.movietracker.userservice.repository.UserRepository;
import com.movietracker.userservice.service.RefreshTokenService;
import com.movietracker.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

/**
 * Production implementation of {@link UserService} managing profiles, credentials, and social follow relations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final FollowRelationRepository followRelationRepository;
    private final UserMapper userMapper;
    private final CachedUserLookupService cachedUserLookupService;
    private final UserEventPublisher userEventPublisher;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public UserProfileResponse getMyProfile(UUID currentUserId) {
        log.debug("Retrieving authenticated profile for user ID: {}", currentUserId);
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", currentUserId));
        return userMapper.toProfileResponse(user);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public UserProfileResponse updateProfile(UUID currentUserId, UpdateProfileRequest request) {
        log.info("Updating profile details for user ID: {}", currentUserId);
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", currentUserId));

        if (request.displayName() != null) {
            user.setDisplayName(request.displayName().trim());
        }
        if (request.bio() != null) {
            user.setBio(request.bio().trim());
        }
        if (request.avatarUrl() != null) {
            user.setAvatarUrl(request.avatarUrl().trim());
        }
        if (request.location() != null) {
            user.setLocation(request.location().trim());
        }
        if (request.website() != null) {
            user.setWebsite(request.website().trim());
        }

        User updatedUser = userRepository.save(user);

        // Evict public profile cache in Redis
        cachedUserLookupService.evict(updatedUser.getUsername());

        // Publish domain update event to Kafka
        userEventPublisher.publishProfileUpdated(updatedUser);

        log.info("Profile updated successfully for user '{}'", updatedUser.getUsername());
        return userMapper.toProfileResponse(updatedUser);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void changePassword(UUID currentUserId, ChangePasswordRequest request) {
        log.info("Processing password change request for user ID: {}", currentUserId);
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", currentUserId));

        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            log.warn("Password change rejected: Current password mismatch for user '{}'", user.getUsername());
            throw new BadCredentialsException("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);

        // Invalidate all active refresh token sessions for security
        refreshTokenService.revokeAllForUser(user.getId());

        // Publish password changed event to Kafka
        userEventPublisher.publishPasswordChanged(user);

        log.info("Password changed and active sessions revoked successfully for user '{}'", user.getUsername());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PublicUserResponse getPublicProfile(String username) {
        return cachedUserLookupService.getPublicProfile(username);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public FollowStateResponse getFollowState(UUID currentUserId, String targetUsername) {
        User targetUser = userRepository.findByUsernameIgnoreCase(targetUsername)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", targetUsername));

        int followerCount = targetUser.getFollowerCount() != null ? targetUser.getFollowerCount() : 0;
        int followingCount = targetUser.getFollowingCount() != null ? targetUser.getFollowingCount() : 0;

        if (currentUserId == null) {
            return new FollowStateResponse(targetUser.getUsername(), false, false, followerCount, followingCount);
        }

        boolean following = followRelationRepository.existsByFollowerIdAndFollowingId(currentUserId, targetUser.getId());
        boolean followedBy = followRelationRepository.existsByFollowerIdAndFollowingId(targetUser.getId(), currentUserId);

        return new FollowStateResponse(targetUser.getUsername(), following, followedBy, followerCount, followingCount);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public FollowStateResponse follow(UUID currentUserId, String targetUsername) {
        log.info("User ID {} attempting to follow '{}'", currentUserId, targetUsername);
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", currentUserId));

        User targetUser = userRepository.findByUsernameIgnoreCase(targetUsername)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", targetUsername));

        if (currentUser.getId().equals(targetUser.getId())) {
            log.warn("Self-follow attempt rejected for user '{}'", currentUser.getUsername());
            throw new BusinessException("You cannot follow yourself");
        }

        boolean alreadyFollowing = followRelationRepository.existsByFollowerIdAndFollowingId(
                currentUser.getId(),
                targetUser.getId()
        );

        if (!alreadyFollowing) {
            FollowRelation relation = FollowRelation.builder()
                    .follower(currentUser)
                    .following(targetUser)
                    .build();
            followRelationRepository.save(relation);

            int currentFollowing = currentUser.getFollowingCount() != null ? currentUser.getFollowingCount() : 0;
            currentUser.setFollowingCount(currentFollowing + 1);

            int targetFollowers = targetUser.getFollowerCount() != null ? targetUser.getFollowerCount() : 0;
            targetUser.setFollowerCount(targetFollowers + 1);

            userRepository.save(currentUser);
            userRepository.save(targetUser);

            // Evict cached profiles for both users
            cachedUserLookupService.evict(currentUser.getUsername());
            cachedUserLookupService.evict(targetUser.getUsername());

            // Dispatch social event to Kafka
            userEventPublisher.publishFollowed(currentUser, targetUser);

            log.info("User '{}' now follows '{}'", currentUser.getUsername(), targetUser.getUsername());
        } else {
            log.debug("User '{}' already follows '{}' (idempotent follow request)",
                    currentUser.getUsername(), targetUser.getUsername());
        }

        boolean followedBy = followRelationRepository.existsByFollowerIdAndFollowingId(
                targetUser.getId(),
                currentUser.getId()
        );

        return new FollowStateResponse(
                targetUser.getUsername(),
                true,
                followedBy,
                targetUser.getFollowerCount() != null ? targetUser.getFollowerCount() : 0,
                targetUser.getFollowingCount() != null ? targetUser.getFollowingCount() : 0
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public FollowStateResponse unfollow(UUID currentUserId, String targetUsername) {
        log.info("User ID {} attempting to unfollow '{}'", currentUserId, targetUsername);
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", currentUserId));

        User targetUser = userRepository.findByUsernameIgnoreCase(targetUsername)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", targetUsername));

        if (currentUser.getId().equals(targetUser.getId())) {
            log.warn("Self-unfollow attempt rejected for user '{}'", currentUser.getUsername());
            throw new BusinessException("You cannot unfollow yourself");
        }

        Optional<FollowRelation> existingRelation = followRelationRepository.findByFollowerIdAndFollowingId(
                currentUser.getId(),
                targetUser.getId()
        );

        if (existingRelation.isPresent()) {
            followRelationRepository.delete(existingRelation.get());

            int currentFollowing = currentUser.getFollowingCount() != null ? currentUser.getFollowingCount() : 0;
            currentUser.setFollowingCount(Math.max(0, currentFollowing - 1));

            int targetFollowers = targetUser.getFollowerCount() != null ? targetUser.getFollowerCount() : 0;
            targetUser.setFollowerCount(Math.max(0, targetFollowers - 1));

            userRepository.save(currentUser);
            userRepository.save(targetUser);

            // Evict cached profiles for both users
            cachedUserLookupService.evict(currentUser.getUsername());
            cachedUserLookupService.evict(targetUser.getUsername());

            // Dispatch social event to Kafka
            userEventPublisher.publishUnfollowed(currentUser, targetUser);

            log.info("User '{}' unfollowed '{}'", currentUser.getUsername(), targetUser.getUsername());
        } else {
            log.debug("User '{}' was not following '{}' (idempotent unfollow request)",
                    currentUser.getUsername(), targetUser.getUsername());
        }

        boolean followedBy = followRelationRepository.existsByFollowerIdAndFollowingId(
                targetUser.getId(),
                currentUser.getId()
        );

        return new FollowStateResponse(
                targetUser.getUsername(),
                false,
                followedBy,
                targetUser.getFollowerCount() != null ? targetUser.getFollowerCount() : 0,
                targetUser.getFollowingCount() != null ? targetUser.getFollowingCount() : 0
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public Page<PublicUserResponse> getFollowers(String username, Pageable pageable) {
        log.debug("Fetching followers for user '{}'", username);
        User targetUser = userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

        return followRelationRepository.findFollowers(targetUser.getId(), pageable)
                .map(userMapper::toPublicResponse);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public Page<PublicUserResponse> getFollowing(String username, Pageable pageable) {
        log.debug("Fetching accounts followed by user '{}'", username);
        User targetUser = userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

        return followRelationRepository.findFollowing(targetUser.getId(), pageable)
                .map(userMapper::toPublicResponse);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public Page<PublicUserResponse> searchUsers(String query, Pageable pageable) {
        log.debug("Searching users matching query: '{}'", query);
        String searchTerm = query != null ? query.trim() : "";
        return userRepository.findByUsernameContainingIgnoreCase(searchTerm, pageable)
                .map(userMapper::toPublicResponse);
    }
}
