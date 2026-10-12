package com.movietracker.movieservice.mapper;

import com.movietracker.movieservice.config.AppProperties;
import com.movietracker.movieservice.dto.response.MovieDetailsResponse;
import com.movietracker.movieservice.dto.response.MovieSummaryResponse;
import com.movietracker.movieservice.dto.response.TvShowDetailsResponse;
import com.movietracker.movieservice.dto.response.TvShowSummaryResponse;
import com.movietracker.movieservice.dto.tmdb.TmdbMovieSummary;
import com.movietracker.movieservice.dto.tmdb.TmdbTvSummary;
import com.movietracker.movieservice.entity.Genre;
import com.movietracker.movieservice.entity.Movie;
import com.movietracker.movieservice.entity.TvShow;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Mapper service producing outward-facing API DTOs with fully resolved image URLs.
 */
@Component
@RequiredArgsConstructor
public class MovieDtoMapper {

    private final AppProperties appProperties;

    public MovieSummaryResponse toSummaryResponse(Movie movie) {
        if (movie == null) {
            return null;
        }
        return new MovieSummaryResponse(
                movie.getTmdbId(),
                movie.getTitle(),
                movie.getOverview(),
                appProperties.getImage().buildPosterUrl(movie.getPosterPath()),
                appProperties.getImage().buildBackdropUrl(movie.getBackdropPath()),
                movie.getReleaseDate(),
                movie.getVoteAverage(),
                movie.getVoteCount(),
                movie.getGenres() != null ? movie.getGenres() : Collections.emptyList(),
                "MOVIE"
        );
    }

    public MovieSummaryResponse toSummaryResponse(TmdbMovieSummary summary, Map<Integer, String> genreMap) {
        if (summary == null) {
            return null;
        }
        LocalDate releaseDate = parseDate(summary.releaseDate());
        List<String> genreNames = Collections.emptyList();
        if (summary.genreIds() != null && genreMap != null) {
            genreNames = summary.genreIds().stream()
                    .filter(genreMap::containsKey)
                    .map(genreMap::get)
                    .toList();
        }

        return new MovieSummaryResponse(
                summary.id(),
                summary.title(),
                summary.overview(),
                appProperties.getImage().buildPosterUrl(summary.posterPath()),
                appProperties.getImage().buildBackdropUrl(summary.backdropPath()),
                releaseDate,
                summary.voteAverage(),
                summary.voteCount(),
                genreNames,
                "MOVIE"
        );
    }

    public MovieDetailsResponse toDetailsResponse(Movie movie) {
        if (movie == null) {
            return null;
        }
        return new MovieDetailsResponse(
                movie.getId(),
                movie.getTmdbId(),
                movie.getTitle(),
                movie.getOriginalTitle(),
                movie.getOverview(),
                movie.getTagline(),
                appProperties.getImage().buildPosterUrl(movie.getPosterPath()),
                appProperties.getImage().buildBackdropUrl(movie.getBackdropPath()),
                movie.getReleaseDate(),
                movie.getRuntime(),
                movie.getVoteAverage(),
                movie.getVoteCount(),
                movie.getPopularity(),
                movie.getStatus(),
                movie.getOriginalLanguage(),
                movie.getGenres() != null ? movie.getGenres() : Collections.emptyList(),
                movie.getGenreIds() != null ? movie.getGenreIds() : Collections.emptyList(),
                movie.getImdbId(),
                movie.getHomepage(),
                movie.getBudget(),
                movie.getRevenue(),
                movie.getAdult(),
                movie.getCast() != null ? movie.getCast() : Collections.emptyList(),
                movie.getCrew() != null ? movie.getCrew() : Collections.emptyList(),
                movie.getLastSyncedFromTmdb()
        );
    }

    public TvShowSummaryResponse toTvSummaryResponse(TvShow tvShow) {
        if (tvShow == null) {
            return null;
        }
        return new TvShowSummaryResponse(
                tvShow.getTmdbId(),
                tvShow.getName(),
                tvShow.getOverview(),
                appProperties.getImage().buildPosterUrl(tvShow.getPosterPath()),
                appProperties.getImage().buildBackdropUrl(tvShow.getBackdropPath()),
                tvShow.getFirstAirDate(),
                tvShow.getVoteAverage(),
                tvShow.getVoteCount(),
                tvShow.getGenres() != null ? tvShow.getGenres() : Collections.emptyList(),
                "TV"
        );
    }

    public TvShowSummaryResponse toTvSummaryResponse(TmdbTvSummary summary, Map<Integer, String> genreMap) {
        if (summary == null) {
            return null;
        }
        LocalDate firstAirDate = parseDate(summary.firstAirDate());
        List<String> genreNames = Collections.emptyList();
        if (summary.genreIds() != null && genreMap != null) {
            genreNames = summary.genreIds().stream()
                    .filter(genreMap::containsKey)
                    .map(genreMap::get)
                    .toList();
        }

        return new TvShowSummaryResponse(
                summary.id(),
                summary.name(),
                summary.overview(),
                appProperties.getImage().buildPosterUrl(summary.posterPath()),
                appProperties.getImage().buildBackdropUrl(summary.backdropPath()),
                firstAirDate,
                summary.voteAverage(),
                summary.voteCount(),
                genreNames,
                "TV"
        );
    }

    public TvShowDetailsResponse toTvDetailsResponse(TvShow tvShow) {
        if (tvShow == null) {
            return null;
        }
        return new TvShowDetailsResponse(
                tvShow.getId(),
                tvShow.getTmdbId(),
                tvShow.getName(),
                tvShow.getOriginalName(),
                tvShow.getOverview(),
                tvShow.getTagline(),
                appProperties.getImage().buildPosterUrl(tvShow.getPosterPath()),
                appProperties.getImage().buildBackdropUrl(tvShow.getBackdropPath()),
                tvShow.getFirstAirDate(),
                tvShow.getLastAirDate(),
                tvShow.getNumberOfSeasons(),
                tvShow.getNumberOfEpisodes(),
                tvShow.getEpisodeRunTime() != null ? tvShow.getEpisodeRunTime() : Collections.emptyList(),
                tvShow.getInProduction(),
                tvShow.getVoteAverage(),
                tvShow.getVoteCount(),
                tvShow.getPopularity(),
                tvShow.getStatus(),
                tvShow.getOriginalLanguage(),
                tvShow.getGenres() != null ? tvShow.getGenres() : Collections.emptyList(),
                tvShow.getGenreIds() != null ? tvShow.getGenreIds() : Collections.emptyList(),
                tvShow.getNetworks() != null ? tvShow.getNetworks() : Collections.emptyList(),
                tvShow.getCreatedBy() != null ? tvShow.getCreatedBy() : Collections.emptyList(),
                tvShow.getCast() != null ? tvShow.getCast() : Collections.emptyList(),
                tvShow.getCrew() != null ? tvShow.getCrew() : Collections.emptyList(),
                tvShow.getLastSyncedFromTmdb()
        );
    }

    private LocalDate parseDate(String date) {
        if (date == null || date.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(date);
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}
