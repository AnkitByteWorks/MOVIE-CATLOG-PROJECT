package com.movietracker.movieservice.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Embedded document representing a crew member in a production.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CrewMember {
    private Long tmdbPersonId;
    private String name;
    private String job;
    private String department;
    private String profilePath;
}
