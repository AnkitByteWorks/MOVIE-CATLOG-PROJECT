package com.movietracker.userservice.controller;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * End-to-end integration test suite using Testcontainers.
 * <p>
 * Evaluates the full web layer, MySQL persistence, Redis token caching, and Kafka domain events.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DisplayName("AuthController Integration Tests")
class AuthControllerIT {

    // TODO: Configure full Testcontainers suite with MySQLContainer, RedisContainer, and KafkaContainer
    // in Step 5/6 once cross-service network orchestration is finalized.

    @Test
    @Disabled("Integration environment container orchestration scheduled for Step 5/6")
    void fullAuthFlowIntegrationTest() {
        // Step 1: POST /register -> 201 Created with tokens
        // Step 2: POST /login with same credentials -> 200 OK
        // Step 3: GET /me with Bearer token -> 200 OK
        // Step 4: POST /refresh -> 200 OK with rotated tokens
        // Step 5: POST /logout -> 200 OK and Redis key revoked
    }
}
