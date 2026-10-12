package com.movietracker.movieservice.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Embedded document representing a cast member in a movie or TV show.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CastMember {
    private Long tmdbPersonId;
    private String name;
    private String character;
    private String profilePath;
    private Integer order;
}
