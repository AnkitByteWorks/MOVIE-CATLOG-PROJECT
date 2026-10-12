package com.movietracker.watchlog.controller;

import com.movietracker.watchlog.dto.response.WatchStatsResponse;
import com.movietracker.watchlog.exception.GlobalExceptionHandler;
import com.movietracker.watchlog.security.AppUserPrincipal;
import com.movietracker.watchlog.service.WatchStatsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
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

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("WatchStatsController MockMvc Tests")
class WatchStatsControllerTest {

    private MockMvc mockMvc;

    @Mock
    private WatchStatsService watchStatsService;

    @InjectMocks
    private WatchStatsController watchStatsController;

    private UUID userId;
    private AppUserPrincipal principal;
    private WatchStatsResponse sampleStats;

    @BeforeEach
    void setUp() {
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

        mockMvc = MockMvcBuilders.standaloneSetup(watchStatsController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(principalResolver)
                .build();

        sampleStats = new WatchStatsResponse(
                userId,
                "johndoe",
                15,
                12,
                3,
                1800L,
                "1d 6h 0m",
                8.7,
                15,
                4,
                7,
                5,
                15,
                List.of(new WatchStatsResponse.GenreStat("Drama", 8L)),
                Collections.emptyList(),
                Collections.emptyMap(),
                "2010s",
                Instant.now(),
                Instant.now()
        );
    }

    @Test
    @DisplayName("GET /api/v1/watchlog/stats/me should return 200 OK and user stats")
    void shouldGetMyStats() throws Exception {
        when(watchStatsService.getMyStats(userId)).thenReturn(sampleStats);

        mockMvc.perform(get("/api/v1/watchlog/stats/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(userId.toString()))
                .andExpect(jsonPath("$.username").value("johndoe"))
                .andExpect(jsonPath("$.totalWatches").value(15))
                .andExpect(jsonPath("$.formattedWatchTime").value("1d 6h 0m"))
                .andExpect(jsonPath("$.currentStreak").value(4));
    }

    @Test
    @DisplayName("GET /api/v1/watchlog/stats/{username} should return 200 OK and public stats")
    void shouldGetPublicStats() throws Exception {
        when(watchStatsService.getPublicStats("johndoe")).thenReturn(sampleStats);

        mockMvc.perform(get("/api/v1/watchlog/stats/johndoe"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("johndoe"))
                .andExpect(jsonPath("$.totalWatches").value(15));
    }
}
