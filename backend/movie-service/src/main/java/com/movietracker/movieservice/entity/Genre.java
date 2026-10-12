package com.movietracker.movieservice.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * MongoDB document storing genre classifications for movies and TV shows.
 */
@Document(collection = "genres")
@CompoundIndexes({
        @CompoundIndex(name = "tmdb_type_unique", def = "{'tmdbId': 1, 'type': 1}", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Genre {

    @Id
    private String id;
    private Integer tmdbId;
    private String name;
    private String type; // "MOVIE" | "TV"
}
