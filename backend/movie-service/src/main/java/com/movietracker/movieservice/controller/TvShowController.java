package com.movietracker.movieservice.controller;

import com.movietracker.movieservice.dto.response.PagedResponse;
import com.movietracker.movieservice.dto.response.TvShowDetailsResponse;
import com.movietracker.movieservice.dto.response.TvShowSummaryResponse;
import com.movietracker.movieservice.service.TvShowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller exposing endpoints for Television series.
 */
@RestController
@RequestMapping("/api/v1/tv")
@RequiredArgsConstructor
@Validated
@Tag(name = "TV Shows", description = "Endpoints for TV series catalog, search, and details")
public class TvShowController {

    private final TvShowService tvShowService;

    @Operation(summary = "Search TV shows", description = "Searches TV series by title")
    @ApiResponse(responseCode = "200", description = "Paginated list of TV show matches")
    @ApiResponse(responseCode = "400", description = "Missing or blank search query")
    @GetMapping("/search")
    public ResponseEntity<PagedResponse<TvShowSummaryResponse>> searchTv(
            @RequestParam("q") @NotBlank(message = "Query parameter 'q' must not be blank") String query,
            @RequestParam(value = "page", defaultValue = "1") int page
    ) {
        return ResponseEntity.ok(tvShowService.searchTv(query, page));
    }

    @Operation(summary = "Get trending TV shows", description = "Retrieves trending TV series for day or week")
    @ApiResponse(responseCode = "200", description = "Paginated list of trending TV series")
    @ApiResponse(responseCode = "400", description = "Invalid window parameter")
    @GetMapping("/trending")
    public ResponseEntity<PagedResponse<TvShowSummaryResponse>> getTrendingTv(
            @RequestParam(value = "window", defaultValue = "day")
            @Pattern(regexp = "^(day|week)$", message = "Window must be either 'day' or 'week'") String window,
            @RequestParam(value = "page", defaultValue = "1") int page
    ) {
        return ResponseEntity.ok(tvShowService.getTrendingTv(window, page));
    }

    @Operation(summary = "Get popular TV shows", description = "Retrieves list of popular TV series")
    @ApiResponse(responseCode = "200", description = "Paginated list of popular TV series")
    @GetMapping("/popular")
    public ResponseEntity<PagedResponse<TvShowSummaryResponse>> getPopularTv(
            @RequestParam(value = "page", defaultValue = "1") int page
    ) {
        return ResponseEntity.ok(tvShowService.getPopularTv(page));
    }

    @Operation(summary = "Get TV show details", description = "Fetches complete details and cast for a TV series")
    @ApiResponse(responseCode = "200", description = "Detailed TV show profile")
    @ApiResponse(responseCode = "404", description = "TV show not found")
    @GetMapping("/{tmdbId}")
    public ResponseEntity<TvShowDetailsResponse> getTvDetails(
            @Parameter(description = "TMDB TV Show ID")
            @PathVariable("tmdbId") Long tmdbId
    ) {
        return ResponseEntity.ok(tvShowService.getTvDetails(tmdbId));
    }
}
