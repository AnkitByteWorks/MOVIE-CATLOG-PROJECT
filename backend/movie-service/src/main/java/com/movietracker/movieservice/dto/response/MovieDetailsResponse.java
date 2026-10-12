package com.movietracker.movieservice.dto.response;

import com.movietracker.movieservice.entity.CastMember;
import com.movietracker.movieservice.entity.CrewMember;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/**
 * Detailed DTO representing comprehensive movie information.
 */
public record MovieDetailsResponse(
        String id,
        Long tmdbId,
        String title,
        String originalTitle,
        String overview,
        String tagline,
        String posterUrl,
        String backdropUrl,
        LocalDate releaseDate,
        Integer runtime,
        Double voteAverage,
        Integer voteCount,
        Double popularity,
        String status,
        String originalLanguage,
        List<String> genres,
        List<Integer> genreIds,
        String imdbId,
        String homepage,
        Long budget,
        Long revenue,
        Boolean adult,
        List<CastMember> cast,
        List<CrewMember> crew,
        Instant lastSyncedFromTmdb
) {
}
