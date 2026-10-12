package com.movietracker.watchlog.dto.external;

import java.time.LocalDate;
import java.util.List;

/**
 * Mirror of movie-service MovieDetailsResponse DTO for Feign client communication.
 */
public record MovieDetailsResponse(
        Long tmdbId,
        String title,
        String overview,
        String posterUrl,
        String backdropUrl,
        LocalDate releaseDate,
        Double voteAverage,
        Integer runtime,
        List<String> genres,
        String mediaType
) {
}
