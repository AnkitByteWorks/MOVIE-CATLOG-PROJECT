package com.movietracker.movieservice.mapper;

import com.movietracker.movieservice.dto.tmdb.TmdbCast;
import com.movietracker.movieservice.dto.tmdb.TmdbCrew;
import com.movietracker.movieservice.dto.tmdb.TmdbGenre;
import com.movietracker.movieservice.dto.tmdb.TmdbMovieDetails;
import com.movietracker.movieservice.dto.tmdb.TmdbMovieSummary;
import com.movietracker.movieservice.dto.tmdb.TmdbTvDetails;
import com.movietracker.movieservice.dto.tmdb.TmdbTvSummary;
import com.movietracker.movieservice.entity.CastMember;
import com.movietracker.movieservice.entity.CrewMember;
import com.movietracker.movieservice.entity.Movie;
import com.movietracker.movieservice.entity.TvShow;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.List;

/**
 * MapStruct mapper for converting raw TMDB DTOs to MongoDB entity documents.
 */
@Mapper(componentModel = "spring")
public interface TmdbToMovieMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tmdbId", source = "id")
    @Mapping(target = "releaseDate", source = "releaseDate", qualifiedByName = "stringToLocalDate")
    @Mapping(target = "genres", source = "genres", qualifiedByName = "tmdbGenresToStrings")
    @Mapping(target = "cast", source = "credits.cast", qualifiedByName = "mapCastTop20")
    @Mapping(target = "crew", source = "credits.crew", qualifiedByName = "mapCrewTop10")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "lastSyncedFromTmdb", ignore = true)
    @Mapping(target = "addedByUserId", ignore = true)
    @Mapping(target = "genreIds", ignore = true)
    Movie toMovie(TmdbMovieDetails details);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tmdbId", source = "id")
    @Mapping(target = "releaseDate", source = "releaseDate", qualifiedByName = "stringToLocalDate")
    @Mapping(target = "tagline", ignore = true)
    @Mapping(target = "runtime", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "genres", ignore = true)
    @Mapping(target = "imdbId", ignore = true)
    @Mapping(target = "homepage", ignore = true)
    @Mapping(target = "budget", ignore = true)
    @Mapping(target = "revenue", ignore = true)
    @Mapping(target = "cast", ignore = true)
    @Mapping(target = "crew", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "lastSyncedFromTmdb", ignore = true)
    @Mapping(target = "addedByUserId", ignore = true)
    Movie toMovieFromSummary(TmdbMovieSummary summary);

    @Mapping(target = "tmdbPersonId", source = "id")
    CastMember toCastMember(TmdbCast cast);

    @Mapping(target = "tmdbPersonId", source = "id")
    CrewMember toCrewMember(TmdbCrew crew);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tmdbId", source = "id")
    @Mapping(target = "firstAirDate", source = "firstAirDate", qualifiedByName = "stringToLocalDate")
    @Mapping(target = "lastAirDate", source = "lastAirDate", qualifiedByName = "stringToLocalDate")
    @Mapping(target = "genres", source = "genres", qualifiedByName = "tmdbGenresToStrings")
    @Mapping(target = "cast", source = "credits.cast", qualifiedByName = "mapCastTop20")
    @Mapping(target = "crew", source = "credits.crew", qualifiedByName = "mapCrewTop10")
    @Mapping(target = "networks", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "genreIds", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "lastSyncedFromTmdb", ignore = true)
    TvShow toTvShow(TmdbTvDetails details);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tmdbId", source = "id")
    @Mapping(target = "firstAirDate", source = "firstAirDate", qualifiedByName = "stringToLocalDate")
    @Mapping(target = "lastAirDate", ignore = true)
    @Mapping(target = "tagline", ignore = true)
    @Mapping(target = "numberOfSeasons", ignore = true)
    @Mapping(target = "numberOfEpisodes", ignore = true)
    @Mapping(target = "episodeRunTime", ignore = true)
    @Mapping(target = "inProduction", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "genres", ignore = true)
    @Mapping(target = "networks", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "cast", ignore = true)
    @Mapping(target = "crew", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "lastSyncedFromTmdb", ignore = true)
    TvShow toTvShowFromSummary(TmdbTvSummary summary);

    @Named("stringToLocalDate")
    default LocalDate stringToLocalDate(String date) {
        if (date == null || date.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(date);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    @Named("tmdbGenresToStrings")
    default List<String> tmdbGenresToStrings(List<TmdbGenre> genres) {
        if (genres == null) {
            return Collections.emptyList();
        }
        return genres.stream().map(TmdbGenre::name).toList();
    }

    @Named("mapCastTop20")
    default List<CastMember> mapCastTop20(List<TmdbCast> cast) {
        if (cast == null) {
            return Collections.emptyList();
        }
        return cast.stream()
                .limit(20)
                .map(this::toCastMember)
                .toList();
    }

    @Named("mapCrewTop10")
    default List<CrewMember> mapCrewTop10(List<TmdbCrew> crew) {
        if (crew == null) {
            return Collections.emptyList();
        }
        return crew.stream()
                .limit(10)
                .map(this::toCrewMember)
                .toList();
    }
}
