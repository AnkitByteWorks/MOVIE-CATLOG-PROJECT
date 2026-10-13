package com.movietracker.userservice.security;

import com.movietracker.userservice.config.AppProperties;
import com.movietracker.userservice.exception.InvalidTokenException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecurityException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Service managing JWT generation, cryptographic signing, claims extraction, and validity verification.
 */
@Service
@Slf4j
public class JwtService {

    private final AppProperties appProperties;
    private final SecretKey secretKey;

    /**
     * Initializes the JWT service and validates that the secret key meets minimum cryptographic requirements.
     *
     * @param appProperties application configuration properties containing JWT settings
     */
    public JwtService(AppProperties appProperties) {
        this.appProperties = appProperties;
        String secret = appProperties.jwt().secret();
        if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalArgumentException("JWT secret key must be at least 256 bits (32 bytes) long");
        }
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Generates a signed access token for the authenticated user details.
     *
     * @param userDetails principal details of the authenticated user
     * @return signed compact JWT access token string
     */
    public String generateAccessToken(UserDetails userDetails) {
        UUID userId = null;
        if (userDetails instanceof UserPrincipal up) {
            userId = up.getId();
        }
        return generateAccessToken(userDetails, userId);
    }

    /**
     * Generates a signed access token with explicit user UUID.
     *
     * @param userDetails principal details of the authenticated user
     * @param userId      unique identifier of the user
     * @return signed compact JWT access token string
     */
    public String generateAccessToken(UserDetails userDetails, UUID userId) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + appProperties.jwt().accessTokenTtl().toMillis());

        String authority = userDetails.getAuthorities().stream()
                .findFirst()
                .map(GrantedAuthority::getAuthority)
                .orElse("ROLE_USER");

        JwtBuilder builder = Jwts.builder()
                .subject(userDetails.getUsername())
                .claim("role", authority)
                .claim("type", "access")
                .issuedAt(now)
                .expiration(expiration);

        if (userId != null) {
            builder.claim("uid", userId.toString());
        }

        return builder.signWith(secretKey).compact();
    }

    /**
     * Generates a signed refresh token JWT containing a unique token ID claim.
     *
     * @param tokenId unique token identifier UUID
     * @param user    principal user details
     * @return signed compact JWT refresh token string
     */
    public String generateRefreshTokenJwt(String tokenId, UserDetails user) {
        UUID userId = null;
        if (user instanceof UserPrincipal up) {
            userId = up.getId();
        }
        return generateRefreshTokenJwt(tokenId, user, userId);
    }

    /**
     * Generates a signed refresh token JWT containing a unique token ID claim and explicit user UUID.
     *
     * @param tokenId unique token identifier UUID
     * @param user    principal user details
     * @param userId  unique identifier of the user
     * @return signed compact JWT refresh token string
     */
    public String generateRefreshTokenJwt(String tokenId, UserDetails user, UUID userId) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + appProperties.jwt().refreshTokenTtl().toMillis());

        JwtBuilder builder = Jwts.builder()
                .subject(user.getUsername())
                .id(tokenId)
                .claim("type", "refresh")
                .issuedAt(now)
                .expiration(expiration);

        if (userId != null) {
            builder.claim("uid", userId.toString());
        }

        return builder.signWith(secretKey).compact();
    }

    /**
     * Extracts the username subject claim from the given JWT token.
     *
     * @param token encoded JWT token
     * @return subject username
     */
    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    /**
     * Extracts the token identifier claim (jti) from the given JWT token.
     *
     * @param token encoded JWT token
     * @return token identifier string
     */
    public String extractTokenId(String token) {
        return extractAllClaims(token).getId();
    }

    /**
     * Extracts the custom token type claim from the given JWT token.
     *
     * @param token encoded JWT token
     * @return token type string (e.g., "access" or "refresh")
     */
    public String extractTokenType(String token) {
        return extractAllClaims(token).get("type", String.class);
    }

    /**
     * Extracts the user UUID claim from the given JWT token.
     *
     * @param token encoded JWT token
     * @return user UUID if present, null otherwise
     */
    public UUID extractUserId(String token) {
        String uid = extractAllClaims(token).get("uid", String.class);
        return uid != null ? UUID.fromString(uid) : null;
    }

    /**
     * Extracts the expiration date claim from the given JWT token.
     *
     * @param token encoded JWT token
     * @return token expiration date
     */
    public Date extractExpiration(String token) {
        return extractAllClaims(token).getExpiration();
    }

    /**
     * Validates whether an access token is authentic, unexpired, and matches the expected user.
     *
     * @param token       encoded JWT token
     * @param userDetails user details to match against
     * @return true if valid and authentic; false otherwise
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            Claims claims = extractAllClaims(token);
            String username = claims.getSubject();
            Date expiration = claims.getExpiration();
            String type = claims.get("type", String.class);

            return username != null
                    && username.equals(userDetails.getUsername())
                    && expiration != null
                    && expiration.after(new Date())
                    && "access".equals(type);
        } catch (InvalidTokenException e) {
            log.debug("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Validates whether a refresh token JWT has valid signature, is unexpired, and is of type refresh.
     *
     * @param token encoded refresh token JWT
     * @return true if valid; false otherwise
     */
    public boolean isRefreshTokenValid(String token) {
        try {
            Claims claims = extractAllClaims(token);
            Date expiration = claims.getExpiration();
            String type = claims.get("type", String.class);
            String jti = claims.getId();

            return jti != null
                    && expiration != null
                    && expiration.after(new Date())
                    && "refresh".equals(type);
        } catch (InvalidTokenException e) {
            log.debug("Refresh token validation failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Cryptographically parses and extracts all claims from a JWT token with rigorous error handling.
     *
     * @param token encoded JWT token string
     * @return parsed JWT Claims
     */
    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            log.debug("JWT token is expired: {}", e.getMessage());
            throw new InvalidTokenException("Token expired");
        } catch (MalformedJwtException e) {
            log.debug("JWT token is malformed: {}", e.getMessage());
            throw new InvalidTokenException("Malformed JWT token");
        } catch (SecurityException e) {
            log.debug("JWT signature verification failed: {}", e.getMessage());
            throw new InvalidTokenException("Invalid JWT signature");
        } catch (UnsupportedJwtException e) {
            log.debug("JWT token is unsupported: {}", e.getMessage());
            throw new InvalidTokenException("Unsupported JWT token");
        } catch (IllegalArgumentException | JwtException e) {
            log.debug("Invalid JWT token: {}", e.getMessage());
            throw new InvalidTokenException("Invalid token: " + e.getMessage());
        }
    }
}
