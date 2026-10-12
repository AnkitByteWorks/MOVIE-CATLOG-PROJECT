package com.movietracker.movieservice.dto.response;

import com.movietracker.movieservice.entity.CastMember;
import com.movietracker.movieservice.entity.CrewMember;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/**
 * Detailed DTO representing comprehensive TV series information.
 */
public record TvShowDetailsResponse(
        String id,
        Long tmdbId,
        String name,
        String originalName,
        String overview,
        String tagline,
        String posterUrl,
        String backdropUrl,
        LocalDate firstAirDate,
        LocalDate lastAirDate,
        Integer numberOfSeasons,
        Integer numberOfEpisodes,
        List<Integer> episodeRunTime,
        Boolean inProduction,
        Double voteAverage,
        Integer voteCount,
        Double popularity,
        String status,
        String originalLanguage,
        List<String> genres,
        List<Integer> genreIds,
        List<String> networks,
        List<String> createdBy,
        List<CastMember> cast,
        List<CrewMember> crew,
        Instant lastSyncedFromTmdb
) {
}
