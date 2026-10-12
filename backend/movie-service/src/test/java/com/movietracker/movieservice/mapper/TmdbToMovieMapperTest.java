package com.movietracker.movieservice.mapper;

import com.movietracker.movieservice.dto.tmdb.TmdbCast;
import com.movietracker.movieservice.dto.tmdb.TmdbCredits;
import com.movietracker.movieservice.dto.tmdb.TmdbCrew;
import com.movietracker.movieservice.dto.tmdb.TmdbGenre;
import com.movietracker.movieservice.dto.tmdb.TmdbMovieDetails;
import com.movietracker.movieservice.dto.tmdb.TmdbMovieSummary;
import com.movietracker.movieservice.entity.CastMember;
import com.movietracker.movieservice.entity.CrewMember;
import com.movietracker.movieservice.entity.Movie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("TmdbToMovieMapper Unit Tests")
class TmdbToMovieMapperTest {

    private final TmdbToMovieMapper mapper = Mappers.getMapper(TmdbToMovieMapper.class);

    @Test
    @DisplayName("Should map TmdbMovieDetails to Movie entity correctly")
    void shouldMapMovieDetailsToEntity() {
        TmdbCast cast = new TmdbCast(101L, "Leonardo DiCaprio", "Cobb", "/cobb.jpg", 0);
        TmdbCrew crew = new TmdbCrew(201L, "Christopher Nolan", "Director", "Directing", "/nolan.jpg");
        TmdbCredits credits = new TmdbCredits(List.of(cast), List.of(crew));
        TmdbGenre genre = new TmdbGenre(28, "Action");

        TmdbMovieDetails details = new TmdbMovieDetails(
                27205L,
                "tt1375666",
                "Inception",
                "Inception",
                "A mind-bending thriller",
                "Your mind is the scene of the crime",
                "/poster.jpg",
                "/backdrop.jpg",
                "2010-07-15",
                148,
                8.4,
                36000,
                120.5,
                "Released",
                "en",
                List.of(genre),
                160000000L,
                825532764L,
                "https://inception.com",
                false,
                credits,
                null
        );

        Movie movie = mapper.toMovie(details);

        assertThat(movie).isNotNull();
        assertThat(movie.getTmdbId()).isEqualTo(27205L);
        assertThat(movie.getTitle()).isEqualTo("Inception");
        assertThat(movie.getReleaseDate()).isEqualTo(LocalDate.of(2010, 7, 15));
        assertThat(movie.getRuntime()).isEqualTo(148);
        assertThat(movie.getGenres()).containsExactly("Action");
        assertThat(movie.getCast()).hasSize(1);
        assertThat(movie.getCast().get(0).getName()).isEqualTo("Leonardo DiCaprio");
        assertThat(movie.getCrew()).hasSize(1);
        assertThat(movie.getCrew().get(0).getName()).isEqualTo("Christopher Nolan");
    }

    @Test
    @DisplayName("Should map TmdbMovieSummary to Movie entity correctly")
    void shouldMapMovieSummaryToEntity() {
        TmdbMovieSummary summary = new TmdbMovieSummary(
                155L,
                "The Dark Knight",
                "The Dark Knight",
                "Batman battles Joker",
                "/darkknight.jpg",
                "/backdrop.jpg",
                "2008-07-16",
                8.5,
                32000,
                135.0,
                false,
                "en",
                List.of(28, 80)
        );

        Movie movie = mapper.toMovieFromSummary(summary);

        assertThat(movie).isNotNull();
        assertThat(movie.getTmdbId()).isEqualTo(155L);
        assertThat(movie.getTitle()).isEqualTo("The Dark Knight");
        assertThat(movie.getReleaseDate()).isEqualTo(LocalDate.of(2008, 7, 16));
        assertThat(movie.getVoteAverage()).isEqualTo(8.5);
    }

    @Test
    @DisplayName("Should handle null and invalid date formats gracefully")
    void shouldHandleNullAndInvalidDates() {
        assertThat(mapper.stringToLocalDate(null)).isNull();
        assertThat(mapper.stringToLocalDate("")).isNull();
        assertThat(mapper.stringToLocalDate("invalid-date")).isNull();
        assertThat(mapper.stringToLocalDate("2026-10-04")).isEqualTo(LocalDate.of(2026, 10, 4));
    }
}
