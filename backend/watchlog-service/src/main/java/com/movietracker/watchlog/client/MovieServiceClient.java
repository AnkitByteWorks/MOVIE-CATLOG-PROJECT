package com.movietracker.watchlog.client;

import com.movietracker.watchlog.config.FeignConfig;
import com.movietracker.watchlog.dto.external.MovieDetailsResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Feign client communication contract for movie-service.
 */
@FeignClient(
        name = "movie-service",
        url = "${app.clients.movie-service-url}",
        configuration = FeignConfig.class
)
public interface MovieServiceClient {

    @GetMapping("/api/v1/movies/{tmdbId}")
    MovieDetailsResponse getMovieDetails(@PathVariable("tmdbId") Long tmdbId);

    @GetMapping("/api/v1/tv/{tmdbId}")
    MovieDetailsResponse getTvDetails(@PathVariable("tmdbId") Long tmdbId);
}
