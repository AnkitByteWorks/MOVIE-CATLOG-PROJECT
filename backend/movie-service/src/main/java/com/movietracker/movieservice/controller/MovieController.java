package com.movietracker.movieservice.controller;

import com.movietracker.movieservice.dto.response.MovieDetailsResponse;
import com.movietracker.movieservice.dto.response.MovieSummaryResponse;
import com.movietracker.movieservice.dto.response.PagedResponse;
import com.movietracker.movieservice.service.MovieService;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * REST controller exposing endpoints for Movie discovery, search, and catalog details.
 */
@RestController
@RequestMapping("/api/v1/movies")
@RequiredArgsConstructor
@Validated
@Tag(name = "Movies", description = "Endpoints for searching movies, querying details, trending lists, and recommendations")
public class MovieController {

    private final MovieService movieService;

    @Operation(summary = "Search movies by title/query", description = "Executes multi-tier cached search against TMDB with MongoDB offline fallback")
    @ApiResponse(responseCode = "200", description = "Paginated list of movie matches")
    @ApiResponse(responseCode = "400", description = "Missing or blank search query")
    @GetMapping("/search")
    public ResponseEntity<PagedResponse<MovieSummaryResponse>> searchMovies(
            @Parameter(description = "Search query string", required = true)
            @RequestParam("q") @NotBlank(message = "Query parameter 'q' must not be blank") String query,
            @Parameter(description = "Page number (1-indexed)")
            @RequestParam(value = "page", defaultValue = "1") int page
    ) {
        return ResponseEntity.ok(movieService.searchMovies(query, page));
    }

    @Operation(summary = "Get trending movies", description = "Retrieves trending movies for day, week, india, or global")
    @ApiResponse(responseCode = "200", description = "Paginated list of trending movies")
    @ApiResponse(responseCode = "400", description = "Invalid window parameter")
    @GetMapping("/trending")
    public ResponseEntity<PagedResponse<MovieSummaryResponse>> getTrending(
            @Parameter(description = "Time window or region: 'day', 'week', 'india', or 'global'")
            @RequestParam(value = "window", defaultValue = "day")
            @Pattern(regexp = "^(day|week|india|global)$", message = "Window must be 'day', 'week', 'india', or 'global'") String window,
            @Parameter(description = "Page number")
            @RequestParam(value = "page", defaultValue = "1") int page
    ) {
        return ResponseEntity.ok(movieService.getTrending(window, page));
    }

    @Operation(summary = "Get trending movies in India", description = "Retrieves top trending Indian movies across regional cinema (Hindi, Telugu, Tamil, Malayalam, Kannada, Punjabi, Bengali)")
    @ApiResponse(responseCode = "200", description = "Paginated list of trending Indian movies")
    @GetMapping("/trending/india")
    public ResponseEntity<PagedResponse<MovieSummaryResponse>> getTrendingIndia(
            @Parameter(description = "Page number")
            @RequestParam(value = "page", defaultValue = "1") int page
    ) {
        return ResponseEntity.ok(movieService.getTrendingIndia(page));
    }

    @Operation(summary = "Get trending movies globally", description = "Retrieves top trending movies worldwide with high ratings and popularity")
    @ApiResponse(responseCode = "200", description = "Paginated list of global trending movies")
    @GetMapping("/trending/global")
    public ResponseEntity<PagedResponse<MovieSummaryResponse>> getTrendingGlobal(
            @Parameter(description = "Page number")
            @RequestParam(value = "page", defaultValue = "1") int page
    ) {
        return ResponseEntity.ok(movieService.getTrendingGlobal(page));
    }

    @Operation(summary = "Trigger trending movies synchronization", description = "Fetches external feeds or recomputes top trending from database and updates Redis cache")
    @ApiResponse(responseCode = "200", description = "Sync result metadata")
    @PostMapping("/trending/sync")
    public ResponseEntity<Map<String, Object>> syncTrending() {
        return ResponseEntity.ok(movieService.syncTrendingNow());
    }

    @Operation(summary = "Get popular movies", description = "Retrieves list of popular movies")
    @ApiResponse(responseCode = "200", description = "Paginated list of popular movies")
    @GetMapping("/popular")
    public ResponseEntity<PagedResponse<MovieSummaryResponse>> getPopular(
            @RequestParam(value = "page", defaultValue = "1") int page
    ) {
        return ResponseEntity.ok(movieService.getPopular(page));
    }

    @Operation(summary = "Get movies by genre", description = "Filters movies by genre name")
    @ApiResponse(responseCode = "200", description = "Paginated list of movies belonging to the genre")
    @GetMapping("/genre/{genre}")
    public ResponseEntity<PagedResponse<MovieSummaryResponse>> getByGenre(
            @Parameter(description = "Genre name (e.g. Action, Drama)")
            @PathVariable("genre") String genre,
            @RequestParam(value = "page", defaultValue = "1") int page
    ) {
        return ResponseEntity.ok(movieService.getByGenre(genre, page));
    }

    @Operation(summary = "Get comprehensive movie details", description = "Fetches complete movie details, cast, and crew by TMDB ID")
    @ApiResponse(responseCode = "200", description = "Detailed movie profile")
    @ApiResponse(responseCode = "404", description = "Movie not found")
    @GetMapping("/{tmdbId}")
    public ResponseEntity<MovieDetailsResponse> getMovieDetails(
            @Parameter(description = "TMDB Movie ID")
            @PathVariable("tmdbId") Long tmdbId
    ) {
        return ResponseEntity.ok(movieService.getMovieDetails(tmdbId));
    }

    @Operation(summary = "Get movie recommendations", description = "Returns top recommended movies based on a TMDB movie ID")
    @ApiResponse(responseCode = "200", description = "List of recommended movies")
    @GetMapping("/{tmdbId}/recommendations")
    public ResponseEntity<List<MovieSummaryResponse>> getRecommendations(
            @Parameter(description = "TMDB Movie ID")
            @PathVariable("tmdbId") Long tmdbId
    ) {
        return ResponseEntity.ok(movieService.getRecommendations(tmdbId));
    }

    @Operation(summary = "Get recently viewed / updated movies", description = "Returns catalog entries ordered by recent activity")
    @ApiResponse(responseCode = "200", description = "List of recently viewed movies")
    @GetMapping("/recent")
    public ResponseEntity<List<MovieSummaryResponse>> getRecentlyViewed(
            @RequestParam(value = "limit", defaultValue = "10") int limit
    ) {
        return ResponseEntity.ok(movieService.getRecentlyViewed(limit));
    }
}
