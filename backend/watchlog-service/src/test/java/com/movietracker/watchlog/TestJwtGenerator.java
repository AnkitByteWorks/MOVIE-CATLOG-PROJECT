package com.movietracker.watchlog;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

public class TestJwtGenerator {
    public static void main(String[] args) {
        String secret = "dev-only-secret-please-replace-in-production-min-256-bits-abcdefghij";
        UUID userId = UUID.fromString("00000000-0000-0000-0000-000000000001");
        String username = "moviebuff";
        String email = "moviebuff@example.com";

        String token = Jwts.builder()
                .subject(username)
                .claim("uid", userId.toString())
                .claim("email", email)
                .claim("role", "ROLE_USER")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 86400000L * 7)) // 7 days
                .signWith(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)))
                .compact();

        System.out.println("TEST_JWT:" + token);
    }
}
