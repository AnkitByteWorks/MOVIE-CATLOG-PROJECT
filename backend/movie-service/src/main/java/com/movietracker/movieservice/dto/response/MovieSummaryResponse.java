package com.movietracker.movieservice.dto.response;

import java.time.LocalDate;
import java.util.List;

/**
 * Lightweight DTO representing a movie summary for search, trending, and lists.
 */
public record MovieSummaryResponse(
        Long tmdbId,
        String title,
        String overview,
        String posterUrl,
        String backdropUrl,
        LocalDate releaseDate,
        Double voteAverage,
        Integer voteCount,
        List<String> genres,
        String mediaType
) {
    public MovieSummaryResponse {
        if (mediaType == null) {
            mediaType = "MOVIE";
        }
    }
}
