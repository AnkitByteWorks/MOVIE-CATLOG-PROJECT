package com.movietracker.movieservice.dto.response;

import java.util.List;

/**
 * DTO representing trending movies response.
 */
public record TrendingResponse(
        String window,
        List<MovieSummaryResponse> movies
) {
}
