package com.movietracker.watchlog.service.impl;

import com.movietracker.watchlog.client.MovieServiceClient;
import com.movietracker.watchlog.dto.external.MovieDetailsResponse;
import com.movietracker.watchlog.dto.request.CreateWatchLogRequest;
import com.movietracker.watchlog.dto.request.UpdateWatchLogRequest;
import com.movietracker.watchlog.dto.response.PagedResponse;
import com.movietracker.watchlog.dto.response.PublicUserWatchLogResponse;
import com.movietracker.watchlog.dto.response.WatchLogResponse;
import com.movietracker.watchlog.entity.MediaType;
import com.movietracker.watchlog.entity.WatchLocation;
import com.movietracker.watchlog.entity.WatchLog;
import com.movietracker.watchlog.event.WatchEventPublisher;
import com.movietracker.watchlog.exception.ResourceNotFoundException;
import com.movietracker.watchlog.exception.WatchLogAlreadyExistsException;
import com.movietracker.watchlog.mapper.WatchLogMapper;
import com.movietracker.watchlog.repository.WatchLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("WatchLogServiceImpl Unit Tests")
class WatchLogServiceImplTest {

    @Mock
    private WatchLogRepository watchLogRepository;

    @Mock
    private MovieServiceClient movieServiceClient;

    @Mock
    private WatchLogMapper watchLogMapper;

    @Mock
    private WatchEventPublisher watchEventPublisher;

    @Mock
    private CachedWatchLookupService cachedLookupService;

    @InjectMocks
    private WatchLogServiceImpl watchLogService;

    private UUID userId;
    private String username;
    private WatchLog sampleLog;
    private WatchLogResponse sampleResponse;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        username = "johndoe";

        sampleLog = WatchLog.builder()
                .userId(userId)
                .username(username)
                .tmdbId(550L)
                .mediaType(MediaType.MOVIE)
                .title("Fight Club")
                .posterPath("/poster550.jpg")
                .backdropPath("/backdrop550.jpg")
                .watchedAt(Instant.now())
                .rating(9)
                .review("Great movie!")
                .watchLocation(WatchLocation.HOME)
                .rewatchCount(0)
                .runtimeMinutes(139)
                .genres("Drama, Thriller")
                .releaseYear(1999)
                .build();
        sampleLog.setId(UUID.randomUUID());

        sampleResponse = new WatchLogResponse(
                sampleLog.getId(),
                550L,
                MediaType.MOVIE,
                "Fight Club",
                "https://image.tmdb.org/t/p/w500/poster550.jpg",
                "https://image.tmdb.org/t/p/w1280/backdrop550.jpg",
                sampleLog.getWatchedAt(),
                9,
                "Great movie!",
                WatchLocation.HOME,
                0,
                Instant.now(),
                Instant.now()
        );
    }

    @Test
    @DisplayName("Should create watch log successfully when title not already logged")
    void shouldCreateWatchLogSuccessfully() {
        CreateWatchLogRequest request = new CreateWatchLogRequest(
                550L,
                MediaType.MOVIE,
                Instant.now(),
                9,
                "Great movie!",
                WatchLocation.HOME
        );

        MovieDetailsResponse movieDetails = new MovieDetailsResponse(
                550L,
                "Fight Club",
                "Overview text",
                "/poster550.jpg",
                "/backdrop550.jpg",
                LocalDate.of(1999, 10, 15),
                8.4,
                139,
                List.of("Drama", "Thriller"),
                "MOVIE"
        );

        when(watchLogRepository.existsByUserIdAndTmdbIdAndMediaType(userId, 550L, MediaType.MOVIE))
                .thenReturn(false);
        when(movieServiceClient.getMovieDetails(550L)).thenReturn(movieDetails);
        when(watchLogRepository.save(any(WatchLog.class))).thenReturn(sampleLog);
        when(watchLogMapper.toResponse(any(WatchLog.class))).thenReturn(sampleResponse);

        WatchLogResponse result = watchLogService.createWatchLog(request, userId, username, "Bearer token");

        assertThat(result).isNotNull();
        assertThat(result.tmdbId()).isEqualTo(550L);
        assertThat(result.title()).isEqualTo("Fight Club");

        verify(watchEventPublisher).publishWatched(any(WatchLog.class));
        verify(cachedLookupService).evictUser(userId);
    }

    @Test
    @DisplayName("Should throw WatchLogAlreadyExistsException if movie is already logged")
    void shouldThrowWhenWatchLogAlreadyExists() {
        CreateWatchLogRequest request = new CreateWatchLogRequest(
                550L,
                MediaType.MOVIE,
                Instant.now(),
                9,
                "Duplicate log",
                WatchLocation.HOME
        );

        when(watchLogRepository.existsByUserIdAndTmdbIdAndMediaType(userId, 550L, MediaType.MOVIE))
                .thenReturn(true);

        assertThatThrownBy(() -> watchLogService.createWatchLog(request, userId, username, "Bearer token"))
                .isInstanceOf(WatchLogAlreadyExistsException.class);

        verify(watchLogRepository, never()).save(any());
        verify(watchEventPublisher, never()).publishWatched(any());
    }

    @Test
    @DisplayName("Should update watch log when user is the owner")
    void shouldUpdateWatchLogWhenOwner() {
        UUID logId = sampleLog.getId();
        UpdateWatchLogRequest updateRequest = new UpdateWatchLogRequest(
                10,
                "Updated review: Masterpiece",
                WatchLocation.THEATER
        );

        when(watchLogRepository.findById(logId)).thenReturn(Optional.of(sampleLog));
        when(watchLogRepository.save(any(WatchLog.class))).thenReturn(sampleLog);
        when(watchLogMapper.toResponse(any(WatchLog.class))).thenReturn(sampleResponse);

        WatchLogResponse result = watchLogService.updateWatchLog(logId, updateRequest, userId);

        assertThat(result).isNotNull();
        verify(watchEventPublisher).publishUpdated(any(WatchLog.class));
        verify(cachedLookupService).evictUser(userId);
    }

    @Test
    @DisplayName("Should throw AccessDeniedException when updating another user's watch log")
    void shouldThrowAccessDeniedWhenUpdatingOthersLog() {
        UUID logId = sampleLog.getId();
        UUID otherUserId = UUID.randomUUID();
        UpdateWatchLogRequest updateRequest = new UpdateWatchLogRequest(
                10, "Unauthorized update", WatchLocation.THEATER
        );

        when(watchLogRepository.findById(logId)).thenReturn(Optional.of(sampleLog));

        assertThatThrownBy(() -> watchLogService.updateWatchLog(logId, updateRequest, otherUserId))
                .isInstanceOf(AccessDeniedException.class);

        verify(watchLogRepository, never()).save(any());
        verify(watchEventPublisher, never()).publishUpdated(any());
    }

    @Test
    @DisplayName("Should delete watch log when user is the owner")
    void shouldDeleteWatchLogWhenOwner() {
        UUID logId = sampleLog.getId();

        when(watchLogRepository.findById(logId)).thenReturn(Optional.of(sampleLog));

        watchLogService.deleteWatchLog(logId, userId);

        verify(watchLogRepository).delete(sampleLog);
        verify(watchEventPublisher).publishDeleted(sampleLog);
        verify(cachedLookupService).evictUser(userId);
    }

    @Test
    @DisplayName("Should throw AccessDeniedException when deleting another user's watch log")
    void shouldThrowAccessDeniedWhenDeletingOthersLog() {
        UUID logId = sampleLog.getId();
        UUID otherUserId = UUID.randomUUID();

        when(watchLogRepository.findById(logId)).thenReturn(Optional.of(sampleLog));

        assertThatThrownBy(() -> watchLogService.deleteWatchLog(logId, otherUserId))
                .isInstanceOf(AccessDeniedException.class);

        verify(watchLogRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Should mark rewatch and increment rewatch count")
    void shouldMarkRewatchSuccessfully() {
        UUID logId = sampleLog.getId();

        when(watchLogRepository.findById(logId)).thenReturn(Optional.of(sampleLog));
        when(watchLogRepository.save(any(WatchLog.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(watchLogMapper.toResponse(any(WatchLog.class))).thenReturn(sampleResponse);

        WatchLogResponse result = watchLogService.markRewatch(logId, userId);

        assertThat(result).isNotNull();
        assertThat(sampleLog.getRewatchCount()).isEqualTo(1);
        verify(watchEventPublisher).publishRewatched(any(WatchLog.class));
        verify(cachedLookupService).evictUser(userId);
    }

    @Test
    @DisplayName("Should fetch user watch history with pagination")
    void shouldGetMyHistory() {
        Page<WatchLog> page = new PageImpl<>(List.of(sampleLog));
        when(watchLogRepository.findByUserIdOrderByWatchedAtDesc(eq(userId), any(Pageable.class)))
                .thenReturn(page);
        when(watchLogMapper.toResponse(sampleLog)).thenReturn(sampleResponse);

        PagedResponse<WatchLogResponse> history = watchLogService.getMyHistory(userId, 1, 10);

        assertThat(history).isNotNull();
        assertThat(history.content()).hasSize(1);
        assertThat(history.totalElements()).isEqualTo(1L);
        assertThat(history.page()).isEqualTo(1);
    }

    @Test
    @DisplayName("Should fetch public history by username")
    void shouldGetPublicHistory() {
        PublicUserWatchLogResponse publicResponse = new PublicUserWatchLogResponse(
                sampleLog.getId(),
                username,
                550L,
                MediaType.MOVIE,
                "Fight Club",
                "https://image.tmdb.org/t/p/w500/poster550.jpg",
                "https://image.tmdb.org/t/p/w1280/backdrop550.jpg",
                sampleLog.getWatchedAt(),
                9,
                "Great movie!",
                WatchLocation.HOME,
                0
        );

        Page<WatchLog> page = new PageImpl<>(List.of(sampleLog));
        when(watchLogRepository.findByUsernameOrderByWatchedAtDesc(eq(username), any(Pageable.class)))
                .thenReturn(page);
        when(watchLogMapper.toPublicResponse(sampleLog)).thenReturn(publicResponse);

        PagedResponse<PublicUserWatchLogResponse> history = watchLogService.getPublicHistory(username, 1, 10);

        assertThat(history).isNotNull();
        assertThat(history.content()).hasSize(1);
        assertThat(history.content().get(0).username()).isEqualTo(username);
    }
}
