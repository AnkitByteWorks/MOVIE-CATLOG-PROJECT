package com.movietracker.userservice.service.impl;

import com.movietracker.userservice.config.AppProperties;
import com.movietracker.userservice.dto.request.LoginRequest;
import com.movietracker.userservice.dto.request.RegisterRequest;
import com.movietracker.userservice.dto.response.AuthResponse;
import com.movietracker.userservice.dto.response.UserResponse;
import com.movietracker.userservice.entity.Role;
import com.movietracker.userservice.entity.User;
import com.movietracker.userservice.event.UserEventPublisher;
import com.movietracker.userservice.exception.InvalidTokenException;
import com.movietracker.userservice.exception.UserAlreadyExistsException;
import com.movietracker.userservice.mapper.UserMapper;
import com.movietracker.userservice.repository.UserRepository;
import com.movietracker.userservice.security.CustomUserDetailsService;
import com.movietracker.userservice.security.JwtService;
import com.movietracker.userservice.security.UserPrincipal;
import com.movietracker.userservice.service.RefreshTokenData;
import com.movietracker.userservice.service.RefreshTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
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
@DisplayName("AuthServiceImpl Unit Tests")
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private CustomUserDetailsService customUserDetailsService;

    @Mock
    private UserMapper userMapper;

    @Mock
    private UserEventPublisher userEventPublisher;

    @Mock
    private AppProperties appProperties;

    @InjectMocks
    private AuthServiceImpl authService;

    private User sampleUser;
    private UserPrincipal samplePrincipal;
    private UserResponse sampleUserResponse;
    private AppProperties.Jwt jwtProperties;

    @BeforeEach
    void setUp() {
        UUID userId = UUID.randomUUID();
        sampleUser = User.builder()
                .username("john_doe")
                .email("john@example.com")
                .password("encoded_pass")
                .displayName("John Doe")
                .role(Role.USER)
                .enabled(true)
                .emailVerified(false)
                .build();
        sampleUser.setId(userId);

        samplePrincipal = new UserPrincipal(
                userId,
                "john_doe",
                "john@example.com",
                "encoded_pass",
                true,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );

        sampleUserResponse = new UserResponse(
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

        jwtProperties = new AppProperties.Jwt(
                "abcdefghijklmnopqrstuvwxyz1234567890ABCDEFGHIJKLMNOPQRSTUVWXYZ12",
                Duration.ofMinutes(15),
                Duration.ofDays(7)
        );
    }

    @Test
    @DisplayName("Should successfully register a new user, save entity, publish event, and return tokens")
    void shouldRegisterUserSuccessfully() {
        RegisterRequest req = new RegisterRequest("john_doe", "john@example.com", "Passw0rd!", "John Doe");

        when(userRepository.existsByUsername(req.username())).thenReturn(false);
        when(userRepository.existsByEmail(req.email())).thenReturn(false);
        when(passwordEncoder.encode(req.password())).thenReturn("encoded_pass");
        when(userRepository.save(any(User.class))).thenReturn(sampleUser);
        when(customUserDetailsService.loadUserByUsername(sampleUser.getUsername())).thenReturn(samplePrincipal);
        when(appProperties.jwt()).thenReturn(jwtProperties);
        when(jwtService.generateAccessToken(eq(samplePrincipal), eq(sampleUser.getId()))).thenReturn("mock-access-token");
        when(refreshTokenService.createRefreshToken(eq(sampleUser), anyString(), anyString())).thenReturn("mock-token-id");
        when(jwtService.generateRefreshTokenJwt(eq("mock-token-id"), eq(samplePrincipal), eq(sampleUser.getId()))).thenReturn("mock-refresh-token");
        when(userMapper.toResponse(sampleUser)).thenReturn(sampleUserResponse);

        AuthResponse response = authService.register(req, "127.0.0.1", "JUnit-Agent");

        assertThat(response).isNotNull();
        assertThat(response.accessToken()).isEqualTo("mock-access-token");
        assertThat(response.refreshToken()).isEqualTo("mock-refresh-token");
        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(response.expiresIn()).isEqualTo(900L);
        assertThat(response.user()).isEqualTo(sampleUserResponse);

        verify(userRepository).save(any(User.class));
        verify(userEventPublisher).publishRegistered(sampleUser, "127.0.0.1", "JUnit-Agent");
    }

    @Test
    @DisplayName("Should throw UserAlreadyExistsException when registering duplicate username")
    void shouldThrowWhenUsernameAlreadyExists() {
        RegisterRequest req = new RegisterRequest("john_doe", "john@example.com", "Passw0rd!", "John Doe");
        when(userRepository.existsByUsername(req.username())).thenReturn(true);

        assertThatThrownBy(() -> authService.register(req, "127.0.0.1", "JUnit-Agent"))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining("username");

        verify(userRepository, never()).save(any(User.class));
        verify(userEventPublisher, never()).publishRegistered(any(), any(), any());
    }

    @Test
    @DisplayName("Should authenticate user with valid credentials, update lastLoginAt, and return tokens")
    void shouldLoginSuccessfully() {
        LoginRequest req = new LoginRequest("john_doe", "Passw0rd!");

        when(userRepository.findByUsernameOrEmail("john_doe", "john_doe")).thenReturn(Optional.of(sampleUser));
        when(passwordEncoder.matches("Passw0rd!", "encoded_pass")).thenReturn(true);
        when(userRepository.save(sampleUser)).thenReturn(sampleUser);
        when(customUserDetailsService.loadUserByUsername(sampleUser.getUsername())).thenReturn(samplePrincipal);
        when(appProperties.jwt()).thenReturn(jwtProperties);
        when(jwtService.generateAccessToken(eq(samplePrincipal), eq(sampleUser.getId()))).thenReturn("mock-access-token");
        when(refreshTokenService.createRefreshToken(eq(sampleUser), anyString(), anyString())).thenReturn("mock-token-id");
        when(jwtService.generateRefreshTokenJwt(eq("mock-token-id"), eq(samplePrincipal), eq(sampleUser.getId()))).thenReturn("mock-refresh-token");
        when(userMapper.toResponse(sampleUser)).thenReturn(sampleUserResponse);

        AuthResponse response = authService.login(req, "127.0.0.1", "JUnit-Agent");

        assertThat(response).isNotNull();
        assertThat(response.accessToken()).isEqualTo("mock-access-token");
        assertThat(response.refreshToken()).isEqualTo("mock-refresh-token");
        assertThat(sampleUser.getLastLoginAt()).isNotNull();

        verify(userRepository).save(sampleUser);
        verify(userEventPublisher).publishLoggedIn(sampleUser, "127.0.0.1", "JUnit-Agent");
    }

    @Test
    @DisplayName("Should throw AuthenticationException when password does not match")
    void shouldThrowWhenPasswordDoesNotMatch() {
        LoginRequest req = new LoginRequest("john_doe", "WrongPass!");

        when(userRepository.findByUsernameOrEmail("john_doe", "john_doe")).thenReturn(Optional.of(sampleUser));
        when(passwordEncoder.matches("WrongPass!", "encoded_pass")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(req, "127.0.0.1", "JUnit-Agent"))
                .isInstanceOf(AuthenticationException.class)
                .hasMessageContaining("Invalid credentials");

        verify(userRepository, never()).save(sampleUser);
        verify(userEventPublisher, never()).publishLoggedIn(any(), any(), any());
    }

    @Test
    @DisplayName("Should rotate valid refresh token, issue new access token, and return response")
    void shouldRefreshValidToken() {
        String refreshJwt = "valid-refresh-jwt";
        String oldTokenId = UUID.randomUUID().toString();
        String newTokenId = UUID.randomUUID().toString();

        RefreshTokenData rotatedData = new RefreshTokenData(
                newTokenId,
                sampleUser.getId(),
                sampleUser.getUsername(),
                Instant.now().plus(Duration.ofDays(7)),
                Instant.now(),
                "JUnit-Agent",
                "127.0.0.1"
        );

        when(jwtService.isRefreshTokenValid(refreshJwt)).thenReturn(true);
        when(jwtService.extractTokenId(refreshJwt)).thenReturn(oldTokenId);
        when(refreshTokenService.validateAndRotate(oldTokenId, "JUnit-Agent", "127.0.0.1")).thenReturn(rotatedData);
        when(userRepository.findById(sampleUser.getId())).thenReturn(Optional.of(sampleUser));
        when(customUserDetailsService.loadUserByUsername(sampleUser.getUsername())).thenReturn(samplePrincipal);
        when(appProperties.jwt()).thenReturn(jwtProperties);
        when(jwtService.generateAccessToken(eq(samplePrincipal), eq(sampleUser.getId()))).thenReturn("new-access-token");
        when(jwtService.generateRefreshTokenJwt(eq(newTokenId), eq(samplePrincipal), eq(sampleUser.getId()))).thenReturn("new-refresh-token");
        when(userMapper.toResponse(sampleUser)).thenReturn(sampleUserResponse);

        AuthResponse response = authService.refresh(refreshJwt, "127.0.0.1", "JUnit-Agent");

        assertThat(response).isNotNull();
        assertThat(response.accessToken()).isEqualTo("new-access-token");
        assertThat(response.refreshToken()).isEqualTo("new-refresh-token");
    }

    @Test
    @DisplayName("Should throw InvalidTokenException when refresh token JWT is invalid")
    void shouldThrowWhenRefreshTokenJwtInvalid() {
        String invalidRefreshJwt = "invalid-jwt";
        when(jwtService.isRefreshTokenValid(invalidRefreshJwt)).thenReturn(false);

        assertThatThrownBy(() -> authService.refresh(invalidRefreshJwt, "127.0.0.1", "JUnit-Agent"))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessageContaining("Invalid refresh token");

        verify(refreshTokenService, never()).validateAndRotate(any(), any(), any());
    }
}
