package com.movietracker.movieservice.dto.response;

import java.time.LocalDate;
import java.util.List;

/**
 * Lightweight DTO representing a TV show summary for search and listings.
 */
public record TvShowSummaryResponse(
        Long tmdbId,
        String name,
        String overview,
        String posterUrl,
        String backdropUrl,
        LocalDate firstAirDate,
        Double voteAverage,
        Integer voteCount,
        List<String> genres,
        String mediaType
) {
    public TvShowSummaryResponse {
        if (mediaType == null) {
            mediaType = "TV";
        }
    }
}
