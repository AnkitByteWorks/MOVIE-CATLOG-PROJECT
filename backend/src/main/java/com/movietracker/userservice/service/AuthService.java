package com.movietracker.userservice.service;

import com.movietracker.userservice.dto.request.LoginRequest;
import com.movietracker.userservice.dto.request.RegisterRequest;
import com.movietracker.userservice.dto.response.AuthResponse;
import com.movietracker.userservice.dto.response.UserResponse;

import java.util.UUID;

/**
 * Service contract defining identity, authentication, session tokens, and profile retrieval operations.
 */
public interface AuthService {

    /**
     * Registers a new user account, creates persistent credentials, and issues initial tokens.
     *
     * @param req       registration details including username, email, and password
     * @param ip        client IP address
     * @param userAgent client device user agent
     * @return authentication response containing tokens and profile
     */
    AuthResponse register(RegisterRequest req, String ip, String userAgent);

    /**
     * Authenticates an existing user via credentials and issues new access and refresh tokens.
     *
     * @param req       login credentials
     * @param ip        client IP address
     * @param userAgent client device user agent
     * @return authentication response containing tokens and profile
     */
    AuthResponse login(LoginRequest req, String ip, String userAgent);

    /**
     * Rotates an existing refresh token and generates a fresh access token.
     *
     * @param refreshTokenJwt valid signed refresh token JWT
     * @param ip              client IP address
     * @param userAgent       client device user agent
     * @return authentication response containing new tokens and profile
     */
    AuthResponse refresh(String refreshTokenJwt, String ip, String userAgent);

    /**
     * Revokes the active refresh token session in Redis upon logout.
     *
     * @param refreshTokenJwt refresh token to invalidate
     */
    void logout(String refreshTokenJwt);

    /**
     * Retrieves the current user's profile by their unique identifier.
     *
     * @param userId unique user UUID
     * @return safe public user profile
     */
    UserResponse getCurrentUser(UUID userId);
}
