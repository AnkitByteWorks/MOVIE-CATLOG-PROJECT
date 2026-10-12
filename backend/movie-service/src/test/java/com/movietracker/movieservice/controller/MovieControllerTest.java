package com.movietracker.movieservice.controller;

import com.movietracker.movieservice.dto.response.MovieDetailsResponse;
import com.movietracker.movieservice.dto.response.MovieSummaryResponse;
import com.movietracker.movieservice.dto.response.PagedResponse;
import com.movietracker.movieservice.exception.GlobalExceptionHandler;
import com.movietracker.movieservice.exception.ResourceNotFoundException;
import com.movietracker.movieservice.service.MovieService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("MovieController MockMvc Tests")
class MovieControllerTest {

    @Mock
    private MovieService movieService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        MovieController controller = new MovieController(movieService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("GET /api/v1/movies/search with query should return 200 OK")
    void searchMovies_validQuery_shouldReturn200() throws Exception {
        MovieSummaryResponse movie = new MovieSummaryResponse(
                27205L, "Inception", "Overview", "/poster.jpg", "/backdrop.jpg",
                LocalDate.of(2010, 7, 15), 8.4, 35000, List.of("Sci-Fi"), "MOVIE"
        );
        PagedResponse<MovieSummaryResponse> page = PagedResponse.of(List.of(movie), 1, 20, 1);

        when(movieService.searchMovies(eq("Inception"), anyInt())).thenReturn(page);

        mockMvc.perform(get("/api/v1/movies/search")
                        .param("q", "Inception")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].tmdbId").value(27205))
                .andExpect(jsonPath("$.content[0].title").value("Inception"));
    }

    @Test
    @DisplayName("GET /api/v1/movies/search without query parameter should return 400 Bad Request")
    void searchMovies_missingQuery_shouldReturn400() throws Exception {
        mockMvc.perform(get("/api/v1/movies/search")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/v1/movies/{id} for existing movie should return 200 OK")
    void getMovieDetails_existingId_shouldReturn200() throws Exception {
        MovieDetailsResponse details = new MovieDetailsResponse(
                "mongo-123", 27205L, "Inception", "Inception", "Tagline", "Overview",
                "http://poster.url", "http://backdrop.url", LocalDate.of(2010, 7, 15),
                148, 8.4, 35000, 120.0, "Released", "en", List.of("Sci-Fi"), List.of(878),
                "tt1375666", "http://inception.com", 160000000L, 825532764L, false,
                List.of(), List.of(), null
        );

        when(movieService.getMovieDetails(27205L)).thenReturn(details);

        mockMvc.perform(get("/api/v1/movies/27205")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tmdbId").value(27205))
                .andExpect(jsonPath("$.title").value("Inception"));
    }

    @Test
    @DisplayName("GET /api/v1/movies/{id} for non-existent movie should return 404 Not Found")
    void getMovieDetails_nonExistentId_shouldReturn404() throws Exception {
        when(movieService.getMovieDetails(999999L))
                .thenThrow(new ResourceNotFoundException("Movie", "tmdbId", 999999L));

        mockMvc.perform(get("/api/v1/movies/999999")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("GET /api/v1/movies/popular should return 200 OK")
    void getPopular_shouldReturn200() throws Exception {
        PagedResponse<MovieSummaryResponse> page = PagedResponse.of(List.of(), 1, 20, 0);
        when(movieService.getPopular(1)).thenReturn(page);

        mockMvc.perform(get("/api/v1/movies/popular")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0));
    }
}
