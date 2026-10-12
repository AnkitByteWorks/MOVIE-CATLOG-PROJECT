package com.movietracker.userservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.movietracker.userservice.dto.request.LoginRequest;
import com.movietracker.userservice.dto.request.RefreshTokenRequest;
import com.movietracker.userservice.dto.request.RegisterRequest;
import com.movietracker.userservice.dto.response.AuthResponse;
import com.movietracker.userservice.dto.response.UserResponse;
import com.movietracker.userservice.exception.GlobalExceptionHandler;
import com.movietracker.userservice.repository.UserRepository;
import com.movietracker.userservice.security.UserPrincipal;
import com.movietracker.userservice.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.Collections;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthController MockMvc Tests")
class AuthControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private AuthService authService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AuthController authController;

    private UUID userId;
    private UserResponse userResponse;
    private AuthResponse authResponse;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(authController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        userId = UUID.randomUUID();
        userResponse = new UserResponse(
                userId,
                "john_doe",
                "john@example.com",
                "John Doe",
                "USER",
                true,
                false,
                Instant.now(),
                null
        );

        authResponse = new AuthResponse(
                "mock-access-token",
                "mock-refresh-token",
                "Bearer",
                900L,
                userResponse
        );
    }

    @Test
    @DisplayName("POST /api/v1/auth/register should return 201 Created and AuthResponse")
    void shouldRegisterUser() throws Exception {
        RegisterRequest req = new RegisterRequest("john_doe", "john@example.com", "Passw0rd!", "John Doe");

        when(authService.register(any(RegisterRequest.class), anyString(), anyString())).thenReturn(authResponse);

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").value("mock-access-token"))
                .andExpect(jsonPath("$.refreshToken").value("mock-refresh-token"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.user.username").value("john_doe"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/login should return 200 OK and AuthResponse")
    void shouldLoginUser() throws Exception {
        LoginRequest req = new LoginRequest("john_doe", "Passw0rd!");

        when(authService.login(any(LoginRequest.class), anyString(), anyString())).thenReturn(authResponse);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("mock-access-token"))
                .andExpect(jsonPath("$.user.email").value("john@example.com"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/refresh should return 200 OK and rotated AuthResponse")
    void shouldRefreshToken() throws Exception {
        RefreshTokenRequest req = new RefreshTokenRequest("valid-refresh-token");

        when(authService.refresh(eq("valid-refresh-token"), anyString(), anyString())).thenReturn(authResponse);

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("mock-access-token"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/logout should return 200 OK with MessageResponse")
    void shouldLogoutUser() throws Exception {
        RefreshTokenRequest req = new RefreshTokenRequest("token-to-revoke");

        mockMvc.perform(post("/api/v1/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Logged out successfully"));

        verify(authService).logout("token-to-revoke");
    }

    @Test
    @DisplayName("GET /api/v1/auth/me should return 200 OK with UserResponse")
    void shouldGetCurrentUserProfile() throws Exception {
        UserPrincipal principal = new UserPrincipal(
                userId,
                "john_doe",
                "john@example.com",
                "hashedPass",
                true,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
        UsernamePasswordAuthenticationToken authentication = UsernamePasswordAuthenticationToken.authenticated(
                principal,
                null,
                principal.getAuthorities()
        );

        when(authService.getCurrentUser(userId)).thenReturn(userResponse);

        mockMvc.perform(get("/api/v1/auth/me")
                        .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.username").value("john_doe"))
                .andExpect(jsonPath("$.email").value("john@example.com"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/register with invalid payload should return 400 Bad Request")
    void shouldReturn400OnValidationFailure() throws Exception {
        // Missing password and invalid email
        RegisterRequest invalidReq = new RegisterRequest("ab", "invalid-email", "", "");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.fieldErrors").isNotEmpty());
    }
}
