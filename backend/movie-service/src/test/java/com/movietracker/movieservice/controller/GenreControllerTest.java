package com.movietracker.movieservice.controller;

import com.movietracker.movieservice.entity.Genre;
import com.movietracker.movieservice.repository.GenreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("GenreController Tests")
class GenreControllerTest {

    @Mock
    private GenreRepository genreRepository;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        GenreController controller = new GenreController(genreRepository);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    @DisplayName("GET /api/v1/genres should return all genres")
    void getGenres_shouldReturnAll() throws Exception {
        Genre g1 = Genre.builder().tmdbId(28).name("Action").type("MOVIE").build();
        Genre g2 = Genre.builder().tmdbId(10765).name("Sci-Fi & Fantasy").type("TV").build();

        when(genreRepository.findAll()).thenReturn(List.of(g1, g2));

        mockMvc.perform(get("/api/v1/genres"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Action"));
    }

    @Test
    @DisplayName("GET /api/v1/genres?type=MOVIE should return filtered genres")
    void getGenres_withType_shouldFilter() throws Exception {
        Genre g1 = Genre.builder().tmdbId(28).name("Action").type("MOVIE").build();

        when(genreRepository.findByType("MOVIE")).thenReturn(List.of(g1));

        mockMvc.perform(get("/api/v1/genres").param("type", "MOVIE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].type").value("MOVIE"));
    }
}
