package com.movietracker.watchlog.service.impl;

import com.movietracker.watchlog.client.MovieServiceClient;
import com.movietracker.watchlog.dto.external.MovieDetailsResponse;
import com.movietracker.watchlog.dto.request.CreateWatchLogRequest;
import com.movietracker.watchlog.dto.request.UpdateWatchLogRequest;
import com.movietracker.watchlog.dto.response.PagedResponse;
import com.movietracker.watchlog.dto.response.PublicUserWatchLogResponse;
import com.movietracker.watchlog.dto.response.WatchLogResponse;
import com.movietracker.watchlog.entity.MediaType;
import com.movietracker.watchlog.entity.WatchLog;
import com.movietracker.watchlog.event.WatchEventPublisher;
import com.movietracker.watchlog.exception.ExternalServiceException;
import com.movietracker.watchlog.exception.ResourceNotFoundException;
import com.movietracker.watchlog.exception.WatchLogAlreadyExistsException;
import com.movietracker.watchlog.mapper.WatchLogMapper;
import com.movietracker.watchlog.repository.WatchLogRepository;
import com.movietracker.watchlog.service.WatchLogService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Production implementation of {@link WatchLogService} with MySQL persistence,
 * circuit breaker resilience, Redis caching, and Kafka event publishing.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WatchLogServiceImpl implements WatchLogService {

    private final WatchLogRepository watchLogRepository;
    private final MovieServiceClient movieServiceClient;
    private final WatchLogMapper watchLogMapper;
    private final WatchEventPublisher watchEventPublisher;
    private final CachedWatchLookupService cachedLookupService;

    @Override
    @Transactional
    public WatchLogResponse createWatchLog(CreateWatchLogRequest req, UUID userId, String username, String authHeader) {
        log.info("Creating watch log for user '{}' (tmdbId={}, mediaType={})", username, req.tmdbId(), req.mediaType());

        if (watchLogRepository.existsByUserIdAndTmdbIdAndMediaType(userId, req.tmdbId(), req.mediaType())) {
            throw new WatchLogAlreadyExistsException(userId, req.tmdbId(), req.mediaType());
        }

        MovieDetailsResponse details = fetchMovieDetails(req.tmdbId(), req.mediaType());

        String genresJoined = (details.genres() != null && !details.genres().isEmpty())
                ? String.join(",", details.genres())
                : null;

        Integer releaseYear = details.releaseDate() != null ? details.releaseDate().getYear() : null;

        WatchLog watchLog = WatchLog.builder()
                .userId(userId)
                .username(username)
                .tmdbId(req.tmdbId())
                .mediaType(req.mediaType())
                .title(details.title() != null ? details.title() : "Untitled")
                .posterPath(details.posterUrl())
                .backdropPath(details.backdropUrl())
                .watchedAt(req.watchedAt())
                .rating(req.rating())
                .review(req.review())
                .watchLocation(req.watchLocation())
                .rewatchCount(0)
                .runtimeMinutes(details.runtime())
                .genres(genresJoined)
                .releaseYear(releaseYear)
                .build();

        WatchLog saved = watchLogRepository.save(watchLog);
        log.info("Watch log saved successfully with ID '{}'", saved.getId());

        // Publish Kafka event and evict cached user state
        watchEventPublisher.publishWatched(saved);
        cachedLookupService.evictUser(userId);

        return watchLogMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public WatchLogResponse updateWatchLog(UUID id, UpdateWatchLogRequest req, UUID userId) {
        log.info("Updating watch log '{}' for user '{}'", id, userId);

        WatchLog watchLog = watchLogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("WatchLog", "id", id));

        if (!watchLog.getUserId().equals(userId)) {
            log.warn("User '{}' unauthorized to modify watch log '{}'", userId, id);
            throw new AccessDeniedException("You do not have permission to modify this watch log");
        }

        if (req.rating() != null) {
            watchLog.setRating(req.rating());
        }
        if (req.review() != null) {
            watchLog.setReview(req.review());
        }
        if (req.watchLocation() != null) {
            watchLog.setWatchLocation(req.watchLocation());
        }

        WatchLog updated = watchLogRepository.save(watchLog);
        log.info("Watch log '{}' updated successfully", id);

        watchEventPublisher.publishUpdated(updated);
        cachedLookupService.evictUser(userId);

        return watchLogMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public void deleteWatchLog(UUID id, UUID userId) {
        log.info("Deleting watch log '{}' for user '{}'", id, userId);

        WatchLog watchLog = watchLogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("WatchLog", "id", id));

        if (!watchLog.getUserId().equals(userId)) {
            log.warn("User '{}' unauthorized to delete watch log '{}'", userId, id);
            throw new AccessDeniedException("You do not have permission to delete this watch log");
        }

        watchLogRepository.delete(watchLog);
        log.info("Watch log '{}' deleted successfully", id);

        watchEventPublisher.publishDeleted(watchLog);
        cachedLookupService.evictUser(userId);
    }

    @Override
    @Transactional
    public WatchLogResponse markRewatch(UUID id, UUID userId) {
        log.info("Marking rewatch for log '{}' for user '{}'", id, userId);

        WatchLog watchLog = watchLogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("WatchLog", "id", id));

        if (!watchLog.getUserId().equals(userId)) {
            log.warn("User '{}' unauthorized to mark rewatch on '{}'", userId, id);
            throw new AccessDeniedException("You do not have permission to rewatch this log");
        }

        watchLog.setRewatchCount(watchLog.getRewatchCount() + 1);
        watchLog.setWatchedAt(Instant.now());

        WatchLog saved = watchLogRepository.save(watchLog);
        log.info("Watch log '{}' rewatched (total count: {})", id, saved.getRewatchCount());

        watchEventPublisher.publishRewatched(saved);
        cachedLookupService.evictUser(userId);

        return watchLogMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public WatchLogResponse getWatchLog(UUID id, UUID userId) {
        WatchLog watchLog = watchLogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("WatchLog", "id", id));

        if (!watchLog.getUserId().equals(userId)) {
            throw new AccessDeniedException("You do not have permission to view this watch log");
        }

        return watchLogMapper.toResponse(watchLog);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<WatchLogResponse> getMyHistory(UUID userId, int page, int size) {
        int validPage = Math.max(1, page);
        int validSize = Math.max(1, Math.min(size, 100));
        Pageable pageable = PageRequest.of(validPage - 1, validSize);

        Page<WatchLog> pageData = watchLogRepository.findByUserIdOrderByWatchedAtDesc(userId, pageable);
        List<WatchLogResponse> responses = pageData.getContent().stream()
                .map(watchLogMapper::toResponse)
                .toList();

        return PagedResponse.of(responses, validPage, validSize, pageData.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<PublicUserWatchLogResponse> getPublicHistory(String username, int page, int size) {
        int validPage = Math.max(1, page);
        int validSize = Math.max(1, Math.min(size, 100));
        Pageable pageable = PageRequest.of(validPage - 1, validSize);

        Page<WatchLog> pageData = watchLogRepository.findByUsernameOrderByWatchedAtDesc(username, pageable);
        List<PublicUserWatchLogResponse> responses = pageData.getContent().stream()
                .map(watchLogMapper::toPublicResponse)
                .toList();

        return PagedResponse.of(responses, validPage, validSize, pageData.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public List<WatchLogResponse> getRecentWatches(UUID userId) {
        return cachedLookupService.getRecentForUser(userId, () ->
                watchLogRepository.findTop10ByUserIdOrderByWatchedAtDesc(userId).stream()
                        .map(watchLogMapper::toResponse)
                        .toList()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<WatchLogResponse> getWhoWatchedThisMovie(Long tmdbId, int page, int size) {
        int validPage = Math.max(1, page);
        int validSize = Math.max(1, Math.min(size, 100));
        Pageable pageable = PageRequest.of(validPage - 1, validSize);

        Page<WatchLog> pageData = watchLogRepository.findByTmdbIdOrderByWatchedAtDesc(tmdbId, pageable);
        List<WatchLogResponse> responses = pageData.getContent().stream()
                .map(watchLogMapper::toResponse)
                .toList();

        return PagedResponse.of(responses, validPage, validSize, pageData.getTotalElements());
    }

    @CircuitBreaker(name = "movieService", fallbackMethod = "fallbackMovieDetails")
    public MovieDetailsResponse fetchMovieDetails(Long tmdbId, MediaType mediaType) {
        if (mediaType == MediaType.TV) {
            return movieServiceClient.getTvDetails(tmdbId);
        }
        return movieServiceClient.getMovieDetails(tmdbId);
    }

    public MovieDetailsResponse fallbackMovieDetails(Long tmdbId, MediaType mediaType, Throwable t) {
        log.warn("Movie service unavailable for tmdbId={}, mediaType={}: {}", tmdbId, mediaType, t.getMessage());
        throw new ExternalServiceException("Movie catalog service is temporarily unavailable. Please retry later.");
    }
}
