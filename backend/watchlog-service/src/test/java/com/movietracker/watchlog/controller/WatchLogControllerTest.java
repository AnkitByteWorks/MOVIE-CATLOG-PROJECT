package com.movietracker.watchlog.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.movietracker.watchlog.dto.request.CreateWatchLogRequest;
import com.movietracker.watchlog.dto.request.UpdateWatchLogRequest;
import com.movietracker.watchlog.dto.response.PagedResponse;
import com.movietracker.watchlog.dto.response.PublicUserWatchLogResponse;
import com.movietracker.watchlog.dto.response.WatchLogResponse;
import com.movietracker.watchlog.entity.MediaType;
import com.movietracker.watchlog.entity.WatchLocation;
import com.movietracker.watchlog.exception.GlobalExceptionHandler;
import com.movietracker.watchlog.security.AppUserPrincipal;
import com.movietracker.watchlog.service.WatchLogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("WatchLogController MockMvc Tests")
class WatchLogControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private WatchLogService watchLogService;

    @InjectMocks
    private WatchLogController watchLogController;

    private UUID userId;
    private AppUserPrincipal principal;
    private WatchLogResponse sampleResponse;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        userId = UUID.randomUUID();
        principal = new AppUserPrincipal(
                userId,
                "johndoe",
                "john@example.com",
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );

        HandlerMethodArgumentResolver principalResolver = new HandlerMethodArgumentResolver() {
            @Override
            public boolean supportsParameter(MethodParameter parameter) {
                return parameter.hasParameterAnnotation(AuthenticationPrincipal.class)
                        || parameter.getParameterType().equals(AppUserPrincipal.class);
            }

            @Override
            public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                          NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
                return principal;
            }
        };

        mockMvc = MockMvcBuilders.standaloneSetup(watchLogController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(
                        new PageableHandlerMethodArgumentResolver(),
                        principalResolver
                )
                .build();

        sampleResponse = new WatchLogResponse(
                UUID.randomUUID(),
                550L,
                MediaType.MOVIE,
                "Fight Club",
                "https://image.tmdb.org/t/p/w500/poster550.jpg",
                "https://image.tmdb.org/t/p/w1280/backdrop550.jpg",
                Instant.now(),
                9,
                "Masterpiece movie!",
                WatchLocation.HOME,
                0,
                Instant.now(),
                Instant.now()
        );
    }

    @Test
    @DisplayName("POST /api/v1/watchlog should return 201 Created on valid request")
    void shouldCreateWatchLog() throws Exception {
        CreateWatchLogRequest request = new CreateWatchLogRequest(
                550L,
                MediaType.MOVIE,
                Instant.now(),
                9,
                "Masterpiece movie!",
                WatchLocation.HOME
        );

        when(watchLogService.createWatchLog(any(), eq(userId), eq("johndoe"), any()))
                .thenReturn(sampleResponse);

        mockMvc.perform(post("/api/v1/watchlog")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tmdbId").value(550))
                .andExpect(jsonPath("$.title").value("Fight Club"))
                .andExpect(jsonPath("$.rating").value(9));
    }

    @Test
    @DisplayName("POST /api/v1/watchlog should return 400 Bad Request on invalid request")
    void shouldReturn400OnInvalidCreateRequest() throws Exception {
        // null tmdbId is invalid
        CreateWatchLogRequest request = new CreateWatchLogRequest(
                null,
                MediaType.MOVIE,
                Instant.now(),
                15,
                "Invalid rating",
                WatchLocation.HOME
        );

        mockMvc.perform(post("/api/v1/watchlog")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/v1/watchlog/me should return 200 OK with PagedResponse")
    void shouldGetMyHistory() throws Exception {
        PagedResponse<WatchLogResponse> pagedResponse = PagedResponse.of(
                List.of(sampleResponse), 1, 20, 1L
        );

        when(watchLogService.getMyHistory(eq(userId), eq(1), eq(20)))
                .thenReturn(pagedResponse);

        mockMvc.perform(get("/api/v1/watchlog/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].title").value("Fight Club"));
    }

    @Test
    @DisplayName("GET /api/v1/watchlog/me/recent should return 200 OK with recent watches")
    void shouldGetRecentWatches() throws Exception {
        when(watchLogService.getRecentWatches(userId)).thenReturn(List.of(sampleResponse));

        mockMvc.perform(get("/api/v1/watchlog/me/recent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Fight Club"));
    }

    @Test
    @DisplayName("GET /api/v1/watchlog/{id} should return 200 OK")
    void shouldGetWatchLogById() throws Exception {
        UUID logId = sampleResponse.id();
        when(watchLogService.getWatchLog(logId, userId)).thenReturn(sampleResponse);

        mockMvc.perform(get("/api/v1/watchlog/" + logId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(logId.toString()))
                .andExpect(jsonPath("$.title").value("Fight Club"));
    }

    @Test
    @DisplayName("PUT /api/v1/watchlog/{id} should return 200 OK on update")
    void shouldUpdateWatchLog() throws Exception {
        UUID logId = sampleResponse.id();
        UpdateWatchLogRequest request = new UpdateWatchLogRequest(10, "Updated", WatchLocation.THEATER);

        when(watchLogService.updateWatchLog(eq(logId), any(), eq(userId))).thenReturn(sampleResponse);

        mockMvc.perform(put("/api/v1/watchlog/" + logId)
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Fight Club"));
    }

    @Test
    @DisplayName("DELETE /api/v1/watchlog/{id} should return 204 No Content")
    void shouldDeleteWatchLog() throws Exception {
        UUID logId = sampleResponse.id();

        mockMvc.perform(delete("/api/v1/watchlog/" + logId))
                .andExpect(status().isNoContent());

        verify(watchLogService).deleteWatchLog(logId, userId);
    }

    @Test
    @DisplayName("POST /api/v1/watchlog/{id}/rewatch should return 200 OK")
    void shouldMarkRewatch() throws Exception {
        UUID logId = sampleResponse.id();
        when(watchLogService.markRewatch(logId, userId)).thenReturn(sampleResponse);

        mockMvc.perform(post("/api/v1/watchlog/" + logId + "/rewatch"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Fight Club"));
    }

    @Test
    @DisplayName("GET /api/v1/watchlog/user/{username} should return 200 OK")
    void shouldGetPublicHistory() throws Exception {
        PublicUserWatchLogResponse publicItem = new PublicUserWatchLogResponse(
                sampleResponse.id(),
                "johndoe",
                550L,
                MediaType.MOVIE,
                "Fight Club",
                "https://image.tmdb.org/t/p/w500/poster550.jpg",
                "https://image.tmdb.org/t/p/w1280/backdrop550.jpg",
                sampleResponse.watchedAt(),
                9,
                "Masterpiece movie!",
                WatchLocation.HOME,
                0
        );
        PagedResponse<PublicUserWatchLogResponse> pagedResponse = PagedResponse.of(
                List.of(publicItem), 1, 20, 1L
        );

        when(watchLogService.getPublicHistory("johndoe", 1, 20)).thenReturn(pagedResponse);

        mockMvc.perform(get("/api/v1/watchlog/user/johndoe"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].username").value("johndoe"));
    }
}
