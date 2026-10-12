package com.movietracker.watchlog.controller;

import com.movietracker.watchlog.dto.response.WatchStatsResponse;
import com.movietracker.watchlog.security.AppUserPrincipal;
import com.movietracker.watchlog.service.WatchStatsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller delivering viewing analytics and user statistics.
 */
@RestController
@RequestMapping("/api/v1/watchlog/stats")
@RequiredArgsConstructor
@Validated
@Tag(name = "Watch Stats", description = "Endpoints for user watch time analytics, streaks, and viewing patterns")
public class WatchStatsController {

    private final WatchStatsService watchStatsService;

    @Operation(
            summary = "Get authenticated user's watch statistics",
            description = "Computes or returns cached watching statistics including total watch time, streaks, and favorite genres",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponse(responseCode = "200", description = "Watching analytics returned")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    @GetMapping("/me")
    public ResponseEntity<WatchStatsResponse> getMyStats(
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        return ResponseEntity.ok(watchStatsService.getMyStats(principal.id()));
    }

    @Operation(
            summary = "Get public watch statistics by username",
            description = "Retrieves public watching statistics for any user handle"
    )
    @ApiResponse(responseCode = "200", description = "Public statistics returned")
    @ApiResponse(responseCode = "404", description = "User not found")
    @ApiResponse(responseCode = "503", description = "Downstream user-service unavailable")
    @GetMapping("/{username}")
    public ResponseEntity<WatchStatsResponse> getPublicStats(
            @Parameter(description = "Username handle")
            @PathVariable("username") String username
    ) {
        return ResponseEntity.ok(watchStatsService.getPublicStats(username));
    }
}
