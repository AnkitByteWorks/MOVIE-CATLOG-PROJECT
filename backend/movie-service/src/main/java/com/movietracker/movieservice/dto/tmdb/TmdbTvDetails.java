package com.movietracker.movieservice.dto.tmdb;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TmdbTvDetails(
        Long id,
        String name,
        @JsonProperty("original_name") String originalName,
        String overview,
        String tagline,
        @JsonProperty("poster_path") String posterPath,
        @JsonProperty("backdrop_path") String backdropPath,
        @JsonProperty("first_air_date") String firstAirDate,
        @JsonProperty("last_air_date") String lastAirDate,
        @JsonProperty("number_of_seasons") Integer numberOfSeasons,
        @JsonProperty("number_of_episodes") Integer numberOfEpisodes,
        @JsonProperty("episode_run_time") List<Integer> episodeRunTime,
        @JsonProperty("in_production") Boolean inProduction,
        @JsonProperty("vote_average") Double voteAverage,
        @JsonProperty("vote_count") Integer voteCount,
        Double popularity,
        String status,
        @JsonProperty("original_language") String originalLanguage,
        List<TmdbGenre> genres,
        TmdbCredits credits
) {
}
