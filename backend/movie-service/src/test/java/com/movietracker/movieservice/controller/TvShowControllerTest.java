package com.movietracker.movieservice.controller;

import com.movietracker.movieservice.dto.response.PagedResponse;
import com.movietracker.movieservice.dto.response.TvShowDetailsResponse;
import com.movietracker.movieservice.dto.response.TvShowSummaryResponse;
import com.movietracker.movieservice.exception.GlobalExceptionHandler;
import com.movietracker.movieservice.exception.ResourceNotFoundException;
import com.movietracker.movieservice.service.TvShowService;
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
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("TvShowController Tests")
class TvShowControllerTest {

    @Mock
    private TvShowService tvShowService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        TvShowController controller = new TvShowController(tvShowService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("GET /api/v1/tv/search with query should return 200 OK")
    void searchTv_shouldReturn200() throws Exception {
        TvShowSummaryResponse tv = new TvShowSummaryResponse(
                1396L, "Breaking Bad", "Overview", "/poster.jpg", "/backdrop.jpg",
                LocalDate.of(2008, 1, 20), 8.9, 14000, List.of("Drama"), "TV"
        );
        PagedResponse<TvShowSummaryResponse> paged = PagedResponse.of(List.of(tv), 1, 20, 1);

        when(tvShowService.searchTv(eq("Breaking Bad"), anyInt())).thenReturn(paged);

        mockMvc.perform(get("/api/v1/tv/search").param("q", "Breaking Bad").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Breaking Bad"));
    }

    @Test
    @DisplayName("GET /api/v1/tv/{id} should return 200 for existing show")
    void getTvDetails_shouldReturn200() throws Exception {
        TvShowDetailsResponse details = new TvShowDetailsResponse(
                "mongo-tv-1", 1396L, "Breaking Bad", "Breaking Bad", "Overview", "Tagline",
                "/poster.jpg", "/backdrop.jpg", LocalDate.of(2008, 1, 20), null,
                5, 62, List.of(47), false, 8.9, 14000, 150.0, "Ended", "en",
                List.of("Drama"), List.of(18), List.of("AMC"), List.of(), Collections.emptyList(),
                Collections.emptyList(), null
        );

        when(tvShowService.getTvDetails(1396L)).thenReturn(details);

        mockMvc.perform(get("/api/v1/tv/1396").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Breaking Bad"));
    }

    @Test
    @DisplayName("GET /api/v1/tv/{id} should return 404 when not found")
    void getTvDetails_notFound_shouldReturn404() throws Exception {
        when(tvShowService.getTvDetails(999999L)).thenThrow(new ResourceNotFoundException("TvShow", "tmdbId", 999999L));

        mockMvc.perform(get("/api/v1/tv/999999").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
}
