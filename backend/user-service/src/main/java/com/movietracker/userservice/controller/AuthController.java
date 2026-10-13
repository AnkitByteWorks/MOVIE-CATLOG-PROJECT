package com.movietracker.userservice.controller;

import com.movietracker.userservice.dto.request.LoginRequest;
import com.movietracker.userservice.dto.request.RefreshTokenRequest;
import com.movietracker.userservice.dto.request.RegisterRequest;
import com.movietracker.userservice.dto.response.AuthResponse;
import com.movietracker.userservice.dto.response.MessageResponse;
import com.movietracker.userservice.dto.response.UserResponse;
import com.movietracker.userservice.entity.User;
import com.movietracker.userservice.exception.ErrorResponse;
import com.movietracker.userservice.exception.ResourceNotFoundException;
import com.movietracker.userservice.repository.UserRepository;
import com.movietracker.userservice.security.UserPrincipal;
import com.movietracker.userservice.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * REST controller exposing user registration, credential login, token rotation, and authenticated profile endpoints.
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Endpoints for user registration, authentication, token refresh, and profile retrieval")
@Validated
@Slf4j
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;

    /**
     * Registers a new user account and returns newly minted access and refresh tokens.
     *
     * @param request        registration payload
     * @param servletRequest current HTTP servlet request for client metadata
     * @return 201 Created with {@link AuthResponse}
     */
    @PostMapping("/register")
    @Operation(summary = "Register a new user", description = "Registers a new user account, creates credentials, and returns JWT access and refresh tokens")
    @ApiResponse(responseCode = "201", description = "User successfully registered",
            content = @Content(schema = @Schema(implementation = AuthResponse.class)))
    @ApiResponse(responseCode = "400", description = "Validation failure or malformed payload",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "409", description = "Username or email conflict",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody RegisterRequest request,
            HttpServletRequest servletRequest
    ) {
        String ip = extractClientIp(servletRequest);
        String userAgent = extractUserAgent(servletRequest);
        AuthResponse response = authService.register(request, ip, userAgent);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Authenticates an existing user and returns JWT tokens.
     *
     * @param request        login payload
     * @param servletRequest current HTTP servlet request for client metadata
     * @return 200 OK with {@link AuthResponse}
     */
    @PostMapping("/login")
    @Operation(summary = "Authenticate user credentials", description = "Validates username or email with password, returning JWT access and refresh tokens")
    @ApiResponse(responseCode = "200", description = "Authentication successful",
            content = @Content(schema = @Schema(implementation = AuthResponse.class)))
    @ApiResponse(responseCode = "401", description = "Invalid credentials",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "403", description = "Account is disabled",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest servletRequest
    ) {
        String ip = extractClientIp(servletRequest);
        String userAgent = extractUserAgent(servletRequest);
        AuthResponse response = authService.login(request, ip, userAgent);
        return ResponseEntity.ok(response);
    }

    /**
     * Refreshes an expired access token using a valid rotated refresh token.
     *
     * @param request        refresh request containing refresh token
     * @param servletRequest current HTTP servlet request
     * @return 200 OK with {@link AuthResponse}
     */
    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token", description = "Validates and rotates the provided refresh token, returning a fresh access token and new rotated refresh token")
    @ApiResponse(responseCode = "200", description = "Tokens rotated successfully",
            content = @Content(schema = @Schema(implementation = AuthResponse.class)))
    @ApiResponse(responseCode = "401", description = "Refresh token expired, missing, or invalid",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<AuthResponse> refresh(
            @Valid @RequestBody RefreshTokenRequest request,
            HttpServletRequest servletRequest
    ) {
        String ip = extractClientIp(servletRequest);
        String userAgent = extractUserAgent(servletRequest);
        AuthResponse response = authService.refresh(request.refreshToken(), ip, userAgent);
        return ResponseEntity.ok(response);
    }

    /**
     * Revokes the active refresh token session upon logout.
     *
     * @param request refresh request payload identifying the token to revoke
     * @return 200 OK with {@link MessageResponse}
     */
    @PostMapping("/logout")
    @Operation(summary = "Logout user", description = "Revokes and deletes the active refresh token session from Redis cache")
    @ApiResponse(responseCode = "200", description = "Successfully logged out",
            content = @Content(schema = @Schema(implementation = MessageResponse.class)))
    public ResponseEntity<MessageResponse> logout(@Valid @RequestBody RefreshTokenRequest request) {
        authService.logout(request.refreshToken());
        return ResponseEntity.ok(new MessageResponse("Logged out successfully"));
    }

    /**
     * Retrieves the profile of the currently authenticated principal.
     *
     * @param authentication current security authentication context
     * @return 200 OK with {@link UserResponse}
     */
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Get current authenticated user profile",
            description = "Retrieves public profile details for the currently authenticated user token",
            security = @SecurityRequirement(name = "BearerAuth")
    )
    @ApiResponse(responseCode = "200", description = "Profile retrieved successfully",
            content = @Content(schema = @Schema(implementation = UserResponse.class)))
    @ApiResponse(responseCode = "401", description = "Unauthenticated or invalid bearer token",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<UserResponse> getCurrentUser(Authentication authentication) {
        UUID userId = extractUserId(authentication);
        UserResponse response = authService.getCurrentUser(userId);
        return ResponseEntity.ok(response);
    }

    /**
     * Helper to resolve the authenticated user UUID from the security context principal.
     */
    private UUID extractUserId(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("User is not authenticated");
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserPrincipal up) {
            return up.getId();
        }
        if (principal instanceof UserDetails ud) {
            return userRepository.findByUsername(ud.getUsername())
                    .map(User::getId)
                    .orElseThrow(() -> new ResourceNotFoundException("User", "username", ud.getUsername()));
        }
        throw new AccessDeniedException("Unrecognized principal type in security context");
    }

    /**
     * Helper to extract client remote IP address, taking into account potential reverse proxies.
     */
    private String extractClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr() != null ? request.getRemoteAddr() : "127.0.0.1";
    }

    /**
     * Helper to extract client User-Agent header safely.
     */
    private String extractUserAgent(HttpServletRequest request) {
        String header = request.getHeader("User-Agent");
        return (header != null && !header.isBlank()) ? header : "Unknown";
    }
}
