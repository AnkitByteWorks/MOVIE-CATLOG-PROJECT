package com.movietracker.movieservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Health and diagnostics controller for movie-service.
 */
@RestController
@RequestMapping("/api/v1/health")
@RequiredArgsConstructor
@Tag(name = "Health", description = "Service health diagnostics and catalog status")
public class HealthController {

    private final MongoTemplate mongoTemplate;

    @Operation(summary = "Service health check", description = "Basic liveness ping for movie-service")
    @GetMapping
    public ResponseEntity<Map<String, Object>> getHealth() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "movie-service");
        health.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(health);
    }

    @Operation(summary = "Catalog status and dataset metrics", description = "Returns document counts and Kaggle dataset metadata")
    @GetMapping("/catalog")
    public ResponseEntity<Map<String, Object>> getCatalogHealth() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");

        Map<String, Object> catalog = new HashMap<>();
        catalog.put("movies", mongoTemplate.estimatedCount("movies"));
        catalog.put("tvShows", mongoTemplate.estimatedCount("tv_shows"));
        catalog.put("genres", mongoTemplate.estimatedCount("genres"));
        response.put("catalog", catalog);

        Document metadata = mongoTemplate.findOne(new Query(), Document.class, "catalog_metadata");
        if (metadata != null) {
            metadata.remove("_id");
            response.put("lastImport", metadata);
        }

        return ResponseEntity.ok(response);
    }
}
