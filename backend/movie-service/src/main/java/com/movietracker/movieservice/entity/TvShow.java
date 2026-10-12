package com.movietracker.movieservice.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * MongoDB document representing a Television Series in the catalog.
 */
@Document(collection = "tv_shows")
@CompoundIndexes({
        @CompoundIndex(name = "name_text_overview_text", def = "{'name':'text', 'overview':'text'}"),
        @CompoundIndex(name = "tmdbId_1", def = "{'tmdbId': 1}", unique = true, sparse = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TvShow {

    @Id
    private String id;

    @Indexed(unique = true, sparse = true)
    private Long tmdbId;

    private String name;
    private String originalName;
    private String overview;
    private String tagline;
    private String posterPath;
    private String backdropPath;
    private LocalDate firstAirDate;
    private LocalDate lastAirDate;
    private Integer numberOfSeasons;
    private Integer numberOfEpisodes;

    @Builder.Default
    private List<Integer> episodeRunTime = new ArrayList<>();

    private Boolean inProduction;
    private Double voteAverage;
    private Integer voteCount;
    private Double popularity;
    private String status;
    private String originalLanguage;

    @Builder.Default
    private List<String> genres = new ArrayList<>();

    @Builder.Default
    private List<Integer> genreIds = new ArrayList<>();

    @Builder.Default
    private List<String> networks = new ArrayList<>();

    @Builder.Default
    private List<String> createdBy = new ArrayList<>();

    @Builder.Default
    private List<CastMember> cast = new ArrayList<>();

    @Builder.Default
    private List<CrewMember> crew = new ArrayList<>();

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    private Instant lastSyncedFromTmdb;
}
