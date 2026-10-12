package com.movietracker.watchlog.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

/**
 * Health and liveness probe controller.
 */
@RestController
@RequestMapping("/api/v1/health")
@Tag(name = "Health", description = "Service health and diagnostics endpoints")
public class HealthController {

    @Operation(summary = "Health probe", description = "Returns service name and uptime status")
    @GetMapping
    public ResponseEntity<Map<String, Object>> checkHealth() {
        return ResponseEntity.ok(Map.of(
                "service", "watchlog-service",
                "status", "UP",
                "timestamp", Instant.now().toString()
        ));
    }

    @Operation(summary = "Ping probe", description = "Simple connectivity echo")
    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("pong");
    }
}
