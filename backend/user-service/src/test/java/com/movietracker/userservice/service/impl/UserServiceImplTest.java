package com.movietracker.userservice.service.impl;

import com.movietracker.userservice.dto.request.ChangePasswordRequest;
import com.movietracker.userservice.dto.request.UpdateProfileRequest;
import com.movietracker.userservice.dto.response.FollowStateResponse;
import com.movietracker.userservice.dto.response.PublicUserResponse;
import com.movietracker.userservice.dto.response.UserProfileResponse;
import com.movietracker.userservice.entity.FollowRelation;
import com.movietracker.userservice.entity.Role;
import com.movietracker.userservice.entity.User;
import com.movietracker.userservice.event.UserEventPublisher;
import com.movietracker.userservice.exception.BusinessException;
import com.movietracker.userservice.exception.ResourceNotFoundException;
import com.movietracker.userservice.mapper.UserMapper;
import com.movietracker.userservice.repository.FollowRelationRepository;
import com.movietracker.userservice.repository.UserRepository;
import com.movietracker.userservice.service.RefreshTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserServiceImpl Unit Tests")
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private FollowRelationRepository followRelationRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private CachedUserLookupService cachedUserLookupService;

    @Mock
    private UserEventPublisher userEventPublisher;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RefreshTokenService refreshTokenService;

    @InjectMocks
    private UserServiceImpl userService;

    private User currentUser;
    private User targetUser;
    private UUID currentUserId;
    private UUID targetUserId;
    private UserProfileResponse profileResponse;
    private PublicUserResponse publicResponse;

    @BeforeEach
    void setUp() {
        currentUserId = UUID.randomUUID();
        targetUserId = UUID.randomUUID();

        currentUser = User.builder()
                .username("john_doe")
                .email("john@example.com")
                .password("encodedPassword123")
                .displayName("John Doe")
                .role(Role.USER)
                .enabled(true)
                .followerCount(0)
                .followingCount(0)
                .build();
        currentUser.setId(currentUserId);

        targetUser = User.builder()
                .username("jane_doe")
                .email("jane@example.com")
                .password("encodedPassword456")
                .displayName("Jane Doe")
                .role(Role.USER)
                .enabled(true)
                .followerCount(5)
                .followingCount(2)
                .build();
        targetUser.setId(targetUserId);

        profileResponse = new UserProfileResponse(
                currentUserId,
                "john_doe",
                "john@example.com",
                "John Doe",
                "Bio text",
                "https://avatar.png",
                "NYC",
                "https://site.com",
                0,
                0,
                "USER",
                true,
                Instant.now(),
                null
        );

        publicResponse = new PublicUserResponse(
                targetUserId,
                "jane_doe",
                "Jane Doe",
                "Bio text",
                "https://avatar.png",
                "LA",
                "https://site.com",
                5,
                2,
                Instant.now()
        );
    }

    @Test
    @DisplayName("getMyProfile should return UserProfileResponse when user exists")
    void shouldGetMyProfile() {
        when(userRepository.findById(currentUserId)).thenReturn(Optional.of(currentUser));
        when(userMapper.toProfileResponse(currentUser)).thenReturn(profileResponse);

        UserProfileResponse response = userService.getMyProfile(currentUserId);

        assertThat(response).isNotNull();
        assertThat(response.username()).isEqualTo("john_doe");
        verify(userRepository).findById(currentUserId);
    }

    @Test
    @DisplayName("getMyProfile should throw ResourceNotFoundException when user does not exist")
    void shouldThrowWhenUserNotFoundInGetMyProfile() {
        when(userRepository.findById(currentUserId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getMyProfile(currentUserId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User");
    }

    @Test
    @DisplayName("updateProfile should update fields, save, evict cache, and publish event")
    void shouldUpdateProfileSuccessfully() {
        UpdateProfileRequest request = new UpdateProfileRequest(
                "John Updated",
                "New bio",
                "https://newavatar.png",
                "Chicago",
                "https://newsite.com"
        );

        when(userRepository.findById(currentUserId)).thenReturn(Optional.of(currentUser));
        when(userRepository.save(currentUser)).thenReturn(currentUser);
        when(userMapper.toProfileResponse(currentUser)).thenReturn(profileResponse);

        UserProfileResponse response = userService.updateProfile(currentUserId, request);

        assertThat(response).isNotNull();
        assertThat(currentUser.getDisplayName()).isEqualTo("John Updated");
        assertThat(currentUser.getBio()).isEqualTo("New bio");
        assertThat(currentUser.getAvatarUrl()).isEqualTo("https://newavatar.png");
        assertThat(currentUser.getLocation()).isEqualTo("Chicago");
        assertThat(currentUser.getWebsite()).isEqualTo("https://newsite.com");

        verify(userRepository).save(currentUser);
        verify(cachedUserLookupService).evict("john_doe");
        verify(userEventPublisher).publishProfileUpdated(currentUser);
    }

    @Test
    @DisplayName("changePassword should encode password, save, revoke all tokens, and publish event")
    void shouldChangePasswordSuccessfully() {
        ChangePasswordRequest request = new ChangePasswordRequest("OldPass123!", "NewPass123!");

        when(userRepository.findById(currentUserId)).thenReturn(Optional.of(currentUser));
        when(passwordEncoder.matches("OldPass123!", "encodedPassword123")).thenReturn(true);
        when(passwordEncoder.encode("NewPass123!")).thenReturn("encodedNewPassword");
        when(userRepository.save(currentUser)).thenReturn(currentUser);

        userService.changePassword(currentUserId, request);

        assertThat(currentUser.getPassword()).isEqualTo("encodedNewPassword");
        verify(userRepository).save(currentUser);
        verify(refreshTokenService).revokeAllForUser(currentUserId);
        verify(userEventPublisher).publishPasswordChanged(currentUser);
    }

    @Test
    @DisplayName("changePassword should throw BadCredentialsException when current password does not match")
    void shouldThrowWhenCurrentPasswordIncorrect() {
        ChangePasswordRequest request = new ChangePasswordRequest("WrongPass123!", "NewPass123!");

        when(userRepository.findById(currentUserId)).thenReturn(Optional.of(currentUser));
        when(passwordEncoder.matches("WrongPass123!", "encodedPassword123")).thenReturn(false);

        assertThatThrownBy(() -> userService.changePassword(currentUserId, request))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("Current password is incorrect");

        verify(userRepository, never()).save(any());
        verify(refreshTokenService, never()).revokeAllForUser(any());
        verify(userEventPublisher, never()).publishPasswordChanged(any());
    }

    @Test
    @DisplayName("getPublicProfile should delegate to CachedUserLookupService")
    void shouldGetPublicProfileFromCacheService() {
        when(cachedUserLookupService.getPublicProfile("jane_doe")).thenReturn(publicResponse);

        PublicUserResponse response = userService.getPublicProfile("jane_doe");

        assertThat(response).isNotNull();
        assertThat(response.username()).isEqualTo("jane_doe");
        verify(cachedUserLookupService).getPublicProfile("jane_doe");
    }

    @Test
    @DisplayName("getFollowState for unauthenticated user should return following false and followedBy false")
    void shouldGetFollowStateUnauthenticated() {
        when(userRepository.findByUsernameIgnoreCase("jane_doe")).thenReturn(Optional.of(targetUser));

        FollowStateResponse response = userService.getFollowState(null, "jane_doe");

        assertThat(response.username()).isEqualTo("jane_doe");
        assertThat(response.following()).isFalse();
        assertThat(response.followedBy()).isFalse();
        assertThat(response.followerCount()).isEqualTo(5);
        assertThat(response.followingCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("getFollowState for authenticated user should check repository relations")
    void shouldGetFollowStateAuthenticated() {
        when(userRepository.findByUsernameIgnoreCase("jane_doe")).thenReturn(Optional.of(targetUser));
        when(followRelationRepository.existsByFollowerIdAndFollowingId(currentUserId, targetUserId)).thenReturn(true);
        when(followRelationRepository.existsByFollowerIdAndFollowingId(targetUserId, currentUserId)).thenReturn(false);

        FollowStateResponse response = userService.getFollowState(currentUserId, "jane_doe");

        assertThat(response.following()).isTrue();
        assertThat(response.followedBy()).isFalse();
    }

    @Test
    @DisplayName("follow should save relation, update counters, evict cache, and publish event")
    void shouldFollowUserSuccessfully() {
        when(userRepository.findById(currentUserId)).thenReturn(Optional.of(currentUser));
        when(userRepository.findByUsernameIgnoreCase("jane_doe")).thenReturn(Optional.of(targetUser));
        when(followRelationRepository.existsByFollowerIdAndFollowingId(currentUserId, targetUserId)).thenReturn(false);
        when(followRelationRepository.existsByFollowerIdAndFollowingId(targetUserId, currentUserId)).thenReturn(false);

        FollowStateResponse response = userService.follow(currentUserId, "jane_doe");

        assertThat(response.following()).isTrue();
        assertThat(currentUser.getFollowingCount()).isEqualTo(1);
        assertThat(targetUser.getFollowerCount()).isEqualTo(6);

        verify(followRelationRepository).save(any(FollowRelation.class));
        verify(userRepository).save(currentUser);
        verify(userRepository).save(targetUser);
        verify(cachedUserLookupService).evict("john_doe");
        verify(cachedUserLookupService).evict("jane_doe");
        verify(userEventPublisher).publishFollowed(currentUser, targetUser);
    }

    @Test
    @DisplayName("follow should throw BusinessException when attempting self-follow")
    void shouldThrowOnSelfFollow() {
        when(userRepository.findById(currentUserId)).thenReturn(Optional.of(currentUser));
        when(userRepository.findByUsernameIgnoreCase("john_doe")).thenReturn(Optional.of(currentUser));

        assertThatThrownBy(() -> userService.follow(currentUserId, "john_doe"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("You cannot follow yourself");

        verify(followRelationRepository, never()).save(any());
    }

    @Test
    @DisplayName("follow should be idempotent when already following")
    void shouldBeIdempotentOnDuplicateFollow() {
        when(userRepository.findById(currentUserId)).thenReturn(Optional.of(currentUser));
        when(userRepository.findByUsernameIgnoreCase("jane_doe")).thenReturn(Optional.of(targetUser));
        when(followRelationRepository.existsByFollowerIdAndFollowingId(currentUserId, targetUserId)).thenReturn(true);
        when(followRelationRepository.existsByFollowerIdAndFollowingId(targetUserId, currentUserId)).thenReturn(true);

        FollowStateResponse response = userService.follow(currentUserId, "jane_doe");

        assertThat(response.following()).isTrue();
        assertThat(response.followedBy()).isTrue();
        verify(followRelationRepository, never()).save(any());
        verify(userEventPublisher, never()).publishFollowed(any(), any());
    }

    @Test
    @DisplayName("unfollow should delete relation, decrement counters, evict cache, and publish event")
    void shouldUnfollowUserSuccessfully() {
        FollowRelation relation = FollowRelation.builder().follower(currentUser).following(targetUser).build();

        when(userRepository.findById(currentUserId)).thenReturn(Optional.of(currentUser));
        when(userRepository.findByUsernameIgnoreCase("jane_doe")).thenReturn(Optional.of(targetUser));
        when(followRelationRepository.findByFollowerIdAndFollowingId(currentUserId, targetUserId))
                .thenReturn(Optional.of(relation));
        when(followRelationRepository.existsByFollowerIdAndFollowingId(targetUserId, currentUserId))
                .thenReturn(false);

        currentUser.setFollowingCount(1);
        targetUser.setFollowerCount(6);

        FollowStateResponse response = userService.unfollow(currentUserId, "jane_doe");

        assertThat(response.following()).isFalse();
        assertThat(currentUser.getFollowingCount()).isEqualTo(0);
        assertThat(targetUser.getFollowerCount()).isEqualTo(5);

        verify(followRelationRepository).delete(relation);
        verify(userRepository).save(currentUser);
        verify(userRepository).save(targetUser);
        verify(cachedUserLookupService).evict("john_doe");
        verify(cachedUserLookupService).evict("jane_doe");
        verify(userEventPublisher).publishUnfollowed(currentUser, targetUser);
    }

    @Test
    @DisplayName("unfollow should be idempotent when relation does not exist")
    void shouldBeIdempotentWhenNotFollowing() {
        when(userRepository.findById(currentUserId)).thenReturn(Optional.of(currentUser));
        when(userRepository.findByUsernameIgnoreCase("jane_doe")).thenReturn(Optional.of(targetUser));
        when(followRelationRepository.findByFollowerIdAndFollowingId(currentUserId, targetUserId))
                .thenReturn(Optional.empty());
        when(followRelationRepository.existsByFollowerIdAndFollowingId(targetUserId, currentUserId))
                .thenReturn(false);

        FollowStateResponse response = userService.unfollow(currentUserId, "jane_doe");

        assertThat(response.following()).isFalse();
        verify(followRelationRepository, never()).delete(any());
        verify(userEventPublisher, never()).publishUnfollowed(any(), any());
    }

    @Test
    @DisplayName("getFollowers, getFollowing, and searchUsers should return mapped pages")
    void shouldReturnMappedPages() {
        PageRequest pageable = PageRequest.of(0, 10);
        Page<User> userPage = new PageImpl<>(Collections.singletonList(targetUser), pageable, 1);

        when(userRepository.findByUsernameIgnoreCase("jane_doe")).thenReturn(Optional.of(targetUser));
        when(followRelationRepository.findFollowers(targetUserId, pageable)).thenReturn(userPage);
        when(followRelationRepository.findFollowing(targetUserId, pageable)).thenReturn(userPage);
        when(userRepository.findByUsernameContainingIgnoreCase("jane", pageable)).thenReturn(userPage);
        when(userMapper.toPublicResponse(targetUser)).thenReturn(publicResponse);

        Page<PublicUserResponse> followers = userService.getFollowers("jane_doe", pageable);
        assertThat(followers.getContent()).hasSize(1);

        Page<PublicUserResponse> following = userService.getFollowing("jane_doe", pageable);
        assertThat(following.getContent()).hasSize(1);

        Page<PublicUserResponse> search = userService.searchUsers("jane", pageable);
        assertThat(search.getContent()).hasSize(1);
    }
}
