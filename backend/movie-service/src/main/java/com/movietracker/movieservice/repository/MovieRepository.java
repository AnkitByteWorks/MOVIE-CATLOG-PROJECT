package com.movietracker.movieservice.repository;

import com.movietracker.movieservice.entity.Movie;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Spring Data MongoDB repository for Movie documents.
 */
@Repository
public interface MovieRepository extends MongoRepository<Movie, String> {

    Optional<Movie> findByTmdbId(Long tmdbId);

    boolean existsByTmdbId(Long tmdbId);

    @Query("{ '$text': { '$search': ?0 }, 'adult': { $ne: true } }")
    Page<Movie> searchByText(String query, Pageable pageable);

    @Query("{ 'genres': { $in: ?0 }, 'adult': { $ne: true } }")
    Page<Movie> findByGenresIn(List<String> genres, Pageable pageable);

    @Query("{ 'genres': { $in: ?0 }, 'tmdbId': { $ne: ?1 }, 'adult': { $ne: true } }")
    Page<Movie> findByGenresInAndTmdbIdNot(List<String> genres, Long tmdbId, Pageable pageable);

    @Query("{ 'tmdbId': { $in: ?0 }, 'adult': { $ne: true } }")
    List<Movie> findByTmdbIdIn(List<Long> tmdbIds);

    @Query("{ 'tmdbId': { $in: ?0 }, 'adult': { $ne: true } }")
    List<Movie> findByTmdbIdInAndAdultNotTrue(List<Long> tmdbIds);

    @Query("{ 'popularity': { $gt: ?0 }, 'adult': { $ne: true } }")
    Page<Movie> findByPopularityGreaterThan(Double threshold, Pageable pageable);

    @Query("{ 'adult': { $ne: true } }")
    List<Movie> findTop20ByOrderByPopularityDesc();

    @Query("{ 'adult': { $ne: true } }")
    Page<Movie> findAllByOrderByPopularityDesc(Pageable pageable);

    @Query("{ 'originalLanguage': { $in: ?0 }, 'adult': { $ne: true }, 'releaseDate': { $gte: ?1 }, 'posterPath': { $ne: null } }")
    Page<Movie> findTrendingIndia(List<String> languages, LocalDate releaseDateAfter, Pageable pageable);

    @Query("{ 'adult': { $ne: true }, 'releaseDate': { $gte: ?0 }, 'voteCount': { $gte: ?1 }, 'posterPath': { $ne: null } }")
    Page<Movie> findTrendingGlobal(LocalDate releaseDateAfter, int minVotes, Pageable pageable);
}
