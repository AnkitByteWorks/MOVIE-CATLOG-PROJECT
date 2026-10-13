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
import com.movietracker.userservice.exception.ResourceNotFoundException;
import com.movietracker.userservice.exception.UserAlreadyExistsException;
import com.movietracker.userservice.mapper.UserMapper;
import com.movietracker.userservice.repository.UserRepository;
import com.movietracker.userservice.security.CustomUserDetailsService;
import com.movietracker.userservice.security.JwtService;
import com.movietracker.userservice.service.AuthService;
import com.movietracker.userservice.service.RefreshTokenData;
import com.movietracker.userservice.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

/**
 * Production-grade implementation of {@link AuthService} orchestrating credential checks,
 * password hashing, JWT creation, Redis token rotation, and asynchronous Kafka domain events.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final CustomUserDetailsService customUserDetailsService;
    private final UserMapper userMapper;
    private final UserEventPublisher userEventPublisher;
    private final AppProperties appProperties;

    /**
     * {@inheritDoc}
     */
    @Override
    public AuthResponse register(RegisterRequest req, String ip, String userAgent) {
        log.info("Processing user registration for username '{}' and email '{}'", req.username(), req.email());

        if (userRepository.existsByUsername(req.username())) {
            log.warn("Registration rejected: Username '{}' already taken", req.username());
            throw new UserAlreadyExistsException("username", req.username());
        }

        if (userRepository.existsByEmail(req.email())) {
            log.warn("Registration rejected: Email '{}' already registered", req.email());
            throw new UserAlreadyExistsException("email", req.email());
        }

        String displayName = (req.displayName() != null && !req.displayName().isBlank())
                ? req.displayName()
                : req.username();

        User user = User.builder()
                .username(req.username())
                .email(req.email())
                .password(passwordEncoder.encode(req.password()))
                .displayName(displayName)
                .role(Role.USER)
                .enabled(true)
                .emailVerified(false)
                .build();

        User savedUser = userRepository.save(user);
        log.info("Successfully registered user '{}' with ID {}", savedUser.getUsername(), savedUser.getId());

        // Dispatch domain lifecycle event to Kafka
        userEventPublisher.publishRegistered(savedUser, ip, userAgent);

        return buildAuthResponse(savedUser, ip, userAgent);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AuthResponse login(LoginRequest req, String ip, String userAgent) {
        log.info("Authenticating user identifier: {}", req.usernameOrEmail());

        User user = userRepository.findByUsernameOrEmail(req.usernameOrEmail(), req.usernameOrEmail())
                .orElseThrow(() -> {
                    log.warn("Login attempt failed: Identifier '{}' not found", req.usernameOrEmail());
                    return new BadCredentialsException("Invalid credentials");
                });

        if (!user.isEnabled()) {
            log.warn("Login attempt rejected: Account for user '{}' is disabled", user.getUsername());
            throw new DisabledException("Account is disabled");
        }

        if (!passwordEncoder.matches(req.password(), user.getPassword())) {
            log.warn("Login attempt failed: Incorrect password for user '{}'", user.getUsername());
            throw new BadCredentialsException("Invalid credentials");
        }

        user.setLastLoginAt(Instant.now());
        userRepository.save(user);

        log.info("User '{}' authenticated successfully", user.getUsername());

        // Dispatch domain lifecycle event to Kafka
        userEventPublisher.publishLoggedIn(user, ip, userAgent);

        return buildAuthResponse(user, ip, userAgent);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AuthResponse refresh(String refreshTokenJwt, String ip, String userAgent) {
        log.debug("Processing refresh token rotation request");

        if (!jwtService.isRefreshTokenValid(refreshTokenJwt)) {
            log.warn("Invalid or expired refresh token presented for rotation");
            throw new InvalidTokenException("Invalid refresh token");
        }

        String jti = jwtService.extractTokenId(refreshTokenJwt);
        if (jti == null || jti.isBlank()) {
            throw new InvalidTokenException("Refresh token is missing identifier claim");
        }

        RefreshTokenData rotatedData = refreshTokenService.validateAndRotate(jti, userAgent, ip);

        User user = userRepository.findById(rotatedData.userId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", rotatedData.userId()));

        if (!user.isEnabled()) {
            throw new DisabledException("Account is disabled");
        }

        UserDetails userDetails = customUserDetailsService.loadUserByUsername(user.getUsername());
        String newAccessToken = jwtService.generateAccessToken(userDetails, user.getId());
        String newRefreshTokenJwt = jwtService.generateRefreshTokenJwt(rotatedData.tokenId(), userDetails, user.getId());

        log.debug("Successfully refreshed tokens for user '{}'", user.getUsername());

        return new AuthResponse(
                newAccessToken,
                newRefreshTokenJwt,
                "Bearer",
                appProperties.jwt().accessTokenTtl().toSeconds(),
                userMapper.toResponse(user)
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void logout(String refreshTokenJwt) {
        if (refreshTokenJwt != null && !refreshTokenJwt.isBlank()) {
            try {
                String jti = jwtService.extractTokenId(refreshTokenJwt);
                if (jti != null) {
                    refreshTokenService.revoke(jti);
                    log.info("Successfully revoked refresh token session during logout");
                }
            } catch (Exception e) {
                log.debug("Notice during logout token revocation: {}", e.getMessage());
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        return userMapper.toResponse(user);
    }

    /**
     * Private helper to build a full authentication response containing access and refresh tokens.
     */
    private AuthResponse buildAuthResponse(User user, String ip, String userAgent) {
        UserDetails details = customUserDetailsService.loadUserByUsername(user.getUsername());
        String accessToken = jwtService.generateAccessToken(details, user.getId());
        String tokenId = refreshTokenService.createRefreshToken(user, userAgent, ip);
        String refreshTokenJwt = jwtService.generateRefreshTokenJwt(tokenId, details, user.getId());

        return new AuthResponse(
                accessToken,
                refreshTokenJwt,
                "Bearer",
                appProperties.jwt().accessTokenTtl().toSeconds(),
                userMapper.toResponse(user)
        );
    }
}
