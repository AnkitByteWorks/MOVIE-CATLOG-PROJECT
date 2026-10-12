package com.movietracker.movieservice.service.impl;

import com.movietracker.movieservice.dto.response.PagedResponse;
import com.movietracker.movieservice.dto.response.TvShowDetailsResponse;
import com.movietracker.movieservice.dto.response.TvShowSummaryResponse;
import com.movietracker.movieservice.entity.TvShow;
import com.movietracker.movieservice.exception.ResourceNotFoundException;
import com.movietracker.movieservice.mapper.MovieDtoMapper;
import com.movietracker.movieservice.repository.TvShowRepository;
import com.movietracker.movieservice.service.TvShowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * Service implementation for TV Show catalog operations powered by MongoDB text search
 * and Redis caching.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TvShowServiceImpl implements TvShowService {

    private final TvShowRepository tvShowRepository;
    private final MovieDtoMapper movieDtoMapper;
    private final CachedMovieLookupService cachedLookupService;

    @Override
    public PagedResponse<TvShowSummaryResponse> searchTv(String query, int page) {
        if (query == null || query.isBlank()) {
            return PagedResponse.of(Collections.emptyList(), page, 20, 0);
        }
        int validPage = Math.max(1, page);
        return cachedLookupService.getCachedTvSearch(query, validPage, () -> executeSearchTv(query, validPage));
    }

    private PagedResponse<TvShowSummaryResponse> executeSearchTv(String query, int page) {
        log.debug("Executing MongoDB text search for TV show '{}' (page {})", query, page);
        Pageable pageable = PageRequest.of(page - 1, 20);
        Page<TvShow> results = tvShowRepository.searchByText(query, pageable);

        List<TvShowSummaryResponse> items = results.getContent().stream()
                .map(movieDtoMapper::toTvSummaryResponse)
                .toList();

        return PagedResponse.of(items, page, 20, results.getTotalElements());
    }

    @Override
    public TvShowDetailsResponse getTvDetails(Long tmdbId) {
        if (tmdbId == null) {
            throw new ResourceNotFoundException("TvShow", "tmdbId", "null");
        }
        return cachedLookupService.getCachedTvDetails(tmdbId, () -> executeGetTvDetails(tmdbId));
    }

    private TvShowDetailsResponse executeGetTvDetails(Long tmdbId) {
        log.debug("Looking up TV show details in MongoDB for tmdbId {}", tmdbId);
        TvShow tvShow = tvShowRepository.findByTmdbId(tmdbId)
                .orElseThrow(() -> new ResourceNotFoundException("TvShow", "tmdbId", tmdbId));
        return movieDtoMapper.toTvDetailsResponse(tvShow);
    }

    @Override
    public PagedResponse<TvShowSummaryResponse> getTrendingTv(String window, int page) {
        int validPage = Math.max(1, page);
        Pageable pageable = PageRequest.of(validPage - 1, 20);
        Page<TvShow> topTv = tvShowRepository.findAllByOrderByPopularityDesc(pageable);

        List<TvShowSummaryResponse> items = topTv.getContent().stream()
                .map(movieDtoMapper::toTvSummaryResponse)
                .toList();

        return PagedResponse.of(items, validPage, 20, topTv.getTotalElements());
    }

    @Override
    public PagedResponse<TvShowSummaryResponse> getPopularTv(int page) {
        int pageIndex = Math.max(0, page - 1);
        Page<TvShow> tv = tvShowRepository.findAllByOrderByPopularityDesc(PageRequest.of(pageIndex, 20));
        List<TvShowSummaryResponse> dtos = tv.getContent().stream()
                .map(movieDtoMapper::toTvSummaryResponse)
                .toList();
        return PagedResponse.of(dtos, page, 20, tv.getTotalElements());
    }
}
