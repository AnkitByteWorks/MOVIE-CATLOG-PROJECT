package com.movietracker.movieservice.repository;

import com.movietracker.movieservice.entity.TvShow;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data MongoDB repository for TvShow documents.
 */
@Repository
public interface TvShowRepository extends MongoRepository<TvShow, String> {

    Optional<TvShow> findByTmdbId(Long tmdbId);

    boolean existsByTmdbId(Long tmdbId);

    @Query("{ '$text': { '$search': ?0 } }")
    Page<TvShow> searchByText(String query, Pageable pageable);

    @Query("{ 'genres': { $in: ?0 } }")
    Page<TvShow> findByGenresIn(List<String> genres, Pageable pageable);

    @Query("{ 'genres': { $in: ?0 }, 'tmdbId': { $ne: ?1 } }")
    Page<TvShow> findByGenresInAndTmdbIdNot(List<String> genres, Long tmdbId, Pageable pageable);

    @Query("{ 'tmdbId': { $in: ?0 } }")
    List<TvShow> findByTmdbIdIn(List<Long> tmdbIds);

    Page<TvShow> findByPopularityGreaterThan(Double threshold, Pageable pageable);

    List<TvShow> findTop20ByOrderByPopularityDesc();

    Page<TvShow> findAllByOrderByPopularityDesc(Pageable pageable);
}
