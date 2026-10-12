package com.movietracker.watchlog.controller;

import com.movietracker.watchlog.dto.request.CreateWatchLogRequest;
import com.movietracker.watchlog.dto.request.UpdateWatchLogRequest;
import com.movietracker.watchlog.dto.response.PagedResponse;
import com.movietracker.watchlog.dto.response.PublicUserWatchLogResponse;
import com.movietracker.watchlog.dto.response.WatchLogResponse;
import com.movietracker.watchlog.security.AppUserPrincipal;
import com.movietracker.watchlog.service.WatchLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * REST controller managing personal and public movie &amp; television watch log entries.
 */
@RestController
@RequestMapping("/api/v1/watchlog")
@RequiredArgsConstructor
@Validated
@Tag(name = "Watch Log", description = "Endpoints for logging movie and TV watches, history queries, and rewatches")
public class WatchLogController {

    private final WatchLogService watchLogService;

    @Operation(
            summary = "Create a watch log entry",
            description = "Logs a movie or TV viewing for the authenticated user, synchronizes metadata from movie-service, and streams a Kafka event",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponse(responseCode = "201", description = "Watch log entry created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request payload")
    @ApiResponse(responseCode = "401", description = "Unauthorized - Bearer token required")
    @ApiResponse(responseCode = "409", description = "Conflict - Title already logged by user")
    @ApiResponse(responseCode = "503", description = "Downstream movie-service unavailable")
    @PostMapping
    public ResponseEntity<WatchLogResponse> createWatchLog(
            @Valid @RequestBody CreateWatchLogRequest request,
            @AuthenticationPrincipal AppUserPrincipal principal,
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        WatchLogResponse response = watchLogService.createWatchLog(
                request,
                principal.id(),
                principal.username(),
                authHeader
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(
            summary = "Get authenticated user's watch history",
            description = "Retrieves a paginated list of watching entries for the authenticated user, ordered from most recent to oldest",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponse(responseCode = "200", description = "Paginated watch history returned")
    @GetMapping("/me")
    public ResponseEntity<PagedResponse<WatchLogResponse>> getMyHistory(
            @AuthenticationPrincipal AppUserPrincipal principal,
            @Parameter(description = "Page number (1-indexed)")
            @RequestParam(value = "page", defaultValue = "1") int page,
            @Parameter(description = "Page size")
            @RequestParam(value = "size", defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(watchLogService.getMyHistory(principal.id(), page, size));
    }

    @Operation(
            summary = "Get authenticated user's recent watches",
            description = "Retrieves the top 10 most recent watches for the authenticated user, cached in Redis",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponse(responseCode = "200", description = "Recent watches returned")
    @GetMapping("/me/recent")
    public ResponseEntity<List<WatchLogResponse>> getMyRecentWatches(
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        return ResponseEntity.ok(watchLogService.getRecentWatches(principal.id()));
    }

    @Operation(
            summary = "Get public watch history of a user",
            description = "Publicly viewable watch history for any registered user handle"
    )
    @ApiResponse(responseCode = "200", description = "Public watch history returned")
    @GetMapping("/user/{username}")
    public ResponseEntity<PagedResponse<PublicUserWatchLogResponse>> getPublicHistory(
            @PathVariable("username") String username,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(watchLogService.getPublicHistory(username, page, size));
    }

    @Operation(
            summary = "Get who watched this title",
            description = "Community feed of users who logged a specific movie or TV show by TMDB ID"
    )
    @ApiResponse(responseCode = "200", description = "Watch history for title returned")
    @GetMapping("/movie/{tmdbId}")
    public ResponseEntity<PagedResponse<WatchLogResponse>> getWhoWatchedThisMovie(
            @PathVariable("tmdbId") Long tmdbId,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(watchLogService.getWhoWatchedThisMovie(tmdbId, page, size));
    }

    @Operation(
            summary = "Get watch log by ID",
            description = "Retrieves a specific watch log entry (owner only)",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponse(responseCode = "200", description = "Watch log details returned")
    @ApiResponse(responseCode = "403", description = "Forbidden - Not owner of this log")
    @ApiResponse(responseCode = "404", description = "Watch log not found")
    @GetMapping("/{id}")
    public ResponseEntity<WatchLogResponse> getWatchLog(
            @PathVariable("id") UUID id,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        return ResponseEntity.ok(watchLogService.getWatchLog(id, principal.id()));
    }

    @Operation(
            summary = "Update an existing watch log",
            description = "Allows updating rating, review, or watch location on an owned watch log entry",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponse(responseCode = "200", description = "Watch log updated")
    @ApiResponse(responseCode = "403", description = "Forbidden - Not owner")
    @ApiResponse(responseCode = "404", description = "Watch log not found")
    @PutMapping("/{id}")
    public ResponseEntity<WatchLogResponse> updateWatchLog(
            @PathVariable("id") UUID id,
            @Valid @RequestBody UpdateWatchLogRequest request,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        return ResponseEntity.ok(watchLogService.updateWatchLog(id, request, principal.id()));
    }

    @Operation(
            summary = "Delete a watch log",
            description = "Removes a watch log entry (owner only) and publishes a deletion event",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponse(responseCode = "204", description = "Watch log successfully deleted")
    @ApiResponse(responseCode = "403", description = "Forbidden - Not owner")
    @ApiResponse(responseCode = "404", description = "Watch log not found")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> deleteWatchLog(
            @PathVariable("id") UUID id,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        watchLogService.deleteWatchLog(id, principal.id());
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Mark a rewatch on an existing log",
            description = "Increments the rewatch counter, refreshes watchedAt to the current timestamp, and publishes a rewatch event",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponse(responseCode = "200", description = "Rewatch recorded")
    @ApiResponse(responseCode = "403", description = "Forbidden - Not owner")
    @ApiResponse(responseCode = "404", description = "Watch log not found")
    @PostMapping("/{id}/rewatch")
    public ResponseEntity<WatchLogResponse> markRewatch(
            @PathVariable("id") UUID id,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        return ResponseEntity.ok(watchLogService.markRewatch(id, principal.id()));
    }
}
