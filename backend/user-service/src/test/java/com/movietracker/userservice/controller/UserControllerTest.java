package com.movietracker.userservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.movietracker.userservice.dto.request.ChangePasswordRequest;
import com.movietracker.userservice.dto.request.UpdateProfileRequest;
import com.movietracker.userservice.dto.response.FollowStateResponse;
import com.movietracker.userservice.dto.response.PublicUserResponse;
import com.movietracker.userservice.dto.response.UserProfileResponse;
import com.movietracker.userservice.exception.GlobalExceptionHandler;
import com.movietracker.userservice.repository.UserRepository;
import com.movietracker.userservice.security.AppUserPrincipal;
import com.movietracker.userservice.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.Collections;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserController MockMvc Tests")
class UserControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserController userController;

    private UUID userId;
    private UsernamePasswordAuthenticationToken auth;
    private UserProfileResponse profileResponse;
    private PublicUserResponse publicResponse;
    private FollowStateResponse followStateResponse;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(userController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();

        userId = UUID.randomUUID();
        AppUserPrincipal principal = new AppUserPrincipal(
                userId,
                "john_doe",
                "john@example.com",
                "hashedPass",
                true,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
        auth = UsernamePasswordAuthenticationToken.authenticated(
                principal,
                null,
                principal.getAuthorities()
        );

        profileResponse = new UserProfileResponse(
                userId,
                "john_doe",
                "john@example.com",
                "John Doe",
                "Bio text",
                "https://avatar.png",
                "NYC",
                "https://site.com",
                10,
                5,
                "USER",
                true,
                Instant.now(),
                null
        );

        publicResponse = new PublicUserResponse(
                userId,
                "john_doe",
                "John Doe",
                "Bio text",
                "https://avatar.png",
                "NYC",
                "https://site.com",
                10,
                5,
                Instant.now()
        );

        followStateResponse = new FollowStateResponse(
                "jane_doe",
                true,
                false,
                100,
                50
        );
    }

    @Test
    @DisplayName("GET /api/v1/users/me should return 200 OK and UserProfileResponse")
    void shouldGetMyProfile() throws Exception {
        when(userService.getMyProfile(userId)).thenReturn(profileResponse);

        mockMvc.perform(get("/api/v1/users/me")
                        .principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.username").value("john_doe"))
                .andExpect(jsonPath("$.email").value("john@example.com"))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    @DisplayName("PATCH /api/v1/users/me should return 200 OK with updated profile")
    void shouldUpdateProfile() throws Exception {
        UpdateProfileRequest request = new UpdateProfileRequest("New Name", "New Bio", null, "London", null);

        when(userService.updateProfile(eq(userId), any(UpdateProfileRequest.class))).thenReturn(profileResponse);

        mockMvc.perform(patch("/api/v1/users/me")
                        .principal(auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("john_doe"));
    }

    @Test
    @DisplayName("PATCH /api/v1/users/me with invalid fields should return 400 Bad Request")
    void shouldReturn400OnUpdateProfileValidationError() throws Exception {
        String longBio = "A".repeat(201); // max is 200
        UpdateProfileRequest request = new UpdateProfileRequest(null, longBio, null, null, null);

        mockMvc.perform(patch("/api/v1/users/me")
                        .principal(auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.bio").isNotEmpty());
    }

    @Test
    @DisplayName("POST /api/v1/users/me/change-password should return 200 OK")
    void shouldChangePassword() throws Exception {
        ChangePasswordRequest request = new ChangePasswordRequest("OldP@ssw0rd1", "NewP@ssw0rd2");

        mockMvc.perform(post("/api/v1/users/me/change-password")
                        .principal(auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Password changed successfully"));

        verify(userService).changePassword(eq(userId), any(ChangePasswordRequest.class));
    }

    @Test
    @DisplayName("POST /api/v1/users/me/change-password with invalid new password should return 400")
    void shouldReturn400OnChangePasswordValidationError() throws Exception {
        ChangePasswordRequest request = new ChangePasswordRequest("OldP@ssw0rd1", "simple");

        mockMvc.perform(post("/api/v1/users/me/change-password")
                        .principal(auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.newPassword").isNotEmpty());
    }

    @Test
    @DisplayName("GET /api/v1/users/{username} should return 200 OK and PublicUserResponse")
    void shouldGetPublicProfile() throws Exception {
        when(userService.getPublicProfile("john_doe")).thenReturn(publicResponse);

        mockMvc.perform(get("/api/v1/users/john_doe"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("john_doe"))
                .andExpect(jsonPath("$.followerCount").value(10));
    }

    @Test
    @DisplayName("GET /api/v1/users/{username}/follow-state should return 200 OK")
    void shouldGetFollowState() throws Exception {
        when(userService.getFollowState(userId, "jane_doe")).thenReturn(followStateResponse);

        mockMvc.perform(get("/api/v1/users/jane_doe/follow-state")
                        .principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("jane_doe"))
                .andExpect(jsonPath("$.following").value(true))
                .andExpect(jsonPath("$.followerCount").value(100));
    }

    @Test
    @DisplayName("POST /api/v1/users/{username}/follow should return 200 OK")
    void shouldFollowUser() throws Exception {
        when(userService.follow(userId, "jane_doe")).thenReturn(followStateResponse);

        mockMvc.perform(post("/api/v1/users/jane_doe/follow")
                        .principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.following").value(true));
    }

    @Test
    @DisplayName("DELETE /api/v1/users/{username}/follow should return 200 OK")
    void shouldUnfollowUser() throws Exception {
        FollowStateResponse unfollowResponse = new FollowStateResponse("jane_doe", false, false, 99, 50);
        when(userService.unfollow(userId, "jane_doe")).thenReturn(unfollowResponse);

        mockMvc.perform(delete("/api/v1/users/jane_doe/follow")
                        .principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.following").value(false));
    }

    @Test
    @DisplayName("GET /api/v1/users/{username}/followers should return 200 OK with page")
    void shouldGetFollowers() throws Exception {
        when(userService.getFollowers(eq("john_doe"), any()))
                .thenReturn(new PageImpl<>(Collections.singletonList(publicResponse), PageRequest.of(0, 20), 1));

        mockMvc.perform(get("/api/v1/users/john_doe/followers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].username").value("john_doe"));
    }

    @Test
    @DisplayName("GET /api/v1/users/{username}/following should return 200 OK with page")
    void shouldGetFollowing() throws Exception {
        when(userService.getFollowing(eq("john_doe"), any()))
                .thenReturn(new PageImpl<>(Collections.singletonList(publicResponse), PageRequest.of(0, 20), 1));

        mockMvc.perform(get("/api/v1/users/john_doe/following"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].username").value("john_doe"));
    }

    @Test
    @DisplayName("GET /api/v1/users/search should return 200 OK with page")
    void shouldSearchUsers() throws Exception {
        when(userService.searchUsers(eq("john"), any()))
                .thenReturn(new PageImpl<>(Collections.singletonList(publicResponse), PageRequest.of(0, 20), 1));

        mockMvc.perform(get("/api/v1/users/search?q=john"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].username").value("john_doe"));
    }
}
