package com.movietracker.movieservice.dto.tmdb;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TmdbCrew(
        Long id,
        String name,
        String job,
        String department,
        @JsonProperty("profile_path") String profilePath
) {
}
