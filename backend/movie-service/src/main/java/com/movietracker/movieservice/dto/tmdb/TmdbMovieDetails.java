package com.movietracker.movieservice.dto.tmdb;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TmdbMovieDetails(
        Long id,
        @JsonProperty("imdb_id") String imdbId,
        String title,
        @JsonProperty("original_title") String originalTitle,
        String overview,
        String tagline,
        @JsonProperty("poster_path") String posterPath,
        @JsonProperty("backdrop_path") String backdropPath,
        @JsonProperty("release_date") String releaseDate,
        Integer runtime,
        @JsonProperty("vote_average") Double voteAverage,
        @JsonProperty("vote_count") Integer voteCount,
        Double popularity,
        String status,
        @JsonProperty("original_language") String originalLanguage,
        List<TmdbGenre> genres,
        Long budget,
        Long revenue,
        String homepage,
        Boolean adult,
        TmdbCredits credits,
        @JsonProperty("production_companies") List<TmdbProductionCompany> productionCompanies
) {
}
