package com.movietracker.userservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Controller exposing lightweight service availability and health status endpoints.
 */
@Tag(name = "Health", description = "Endpoints for liveness, readiness, and runtime metadata checks.")
@RestController
@RequestMapping("/api/v1/health")
public class HealthController {

    private final String applicationName;
    private final String applicationVersion;
    private final Environment environment;

    /**
     * Constructs a new {@link HealthController} with environment and build property bindings.
     *
     * @param applicationName    configured Spring application name
     * @param applicationVersion application version string
     * @param environment        current Spring runtime environment
     */
    public HealthController(
            @Value("${spring.application.name:user-service}") String applicationName,
            @Value("${info.app.version:0.0.1-SNAPSHOT}") String applicationVersion,
            Environment environment
    ) {
        this.applicationName = applicationName;
        this.applicationVersion = applicationVersion;
        this.environment = environment;
    }

    /**
     * Returns comprehensive application metadata and health status.
     *
     * @return map containing service name, status, version, timestamp, and active profiles
     */
    @Operation(summary = "Service health details", description = "Provides service name, status, version, timestamp, and active profiles.")
    @GetMapping
    public ResponseEntity<Map<String, Object>> getHealth() {
        String[] activeProfiles = environment.getActiveProfiles();
        String profile = activeProfiles.length == 0 ? "default" : Arrays.toString(activeProfiles);

        Map<String, Object> health = new LinkedHashMap<>();
        health.put("service", applicationName);
        health.put("status", "UP");
        health.put("version", applicationVersion);
        health.put("timestamp", Instant.now());
        health.put("profile", profile);

        return ResponseEntity.ok(health);
    }

    /**
     * Simple ping-pong endpoint for quick connectivity verification.
     *
     * @return "pong" literal string
     */
    @Operation(summary = "Ping probe", description = "Returns a simple pong string to verify basic HTTP reachability.")
    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("pong");
    }
}
