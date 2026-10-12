package com.movietracker.movieservice.controller;

import org.bson.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("HealthController Tests")
class HealthControllerTest {

    @Mock
    private MongoTemplate mongoTemplate;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        HealthController controller = new HealthController(mongoTemplate);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    @DisplayName("GET /api/v1/health should return UP status")
    void getHealth_shouldReturnUp() throws Exception {
        mockMvc.perform(get("/api/v1/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.service").value("movie-service"));
    }

    @Test
    @DisplayName("GET /api/v1/health/catalog should return catalog document counts and metadata")
    void getCatalogHealth_shouldReturnCatalogCounts() throws Exception {
        when(mongoTemplate.estimatedCount("movies")).thenReturn(1508074L);
        when(mongoTemplate.estimatedCount("tv_shows")).thenReturn(164705L);
        when(mongoTemplate.estimatedCount("genres")).thenReturn(35L);

        Document doc = new Document("type", "kaggle_import")
                .append("moviesCount", 1508074)
                .append("tvShowsCount", 164705)
                .append("status", "COMPLETED");
        when(mongoTemplate.findOne(any(Query.class), eq(Document.class), eq("catalog_metadata")))
                .thenReturn(doc);

        mockMvc.perform(get("/api/v1/health/catalog"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.catalog.movies").value(1508074))
                .andExpect(jsonPath("$.catalog.tvShows").value(164705))
                .andExpect(jsonPath("$.catalog.genres").value(35))
                .andExpect(jsonPath("$.lastImport.type").value("kaggle_import"));
    }
}
