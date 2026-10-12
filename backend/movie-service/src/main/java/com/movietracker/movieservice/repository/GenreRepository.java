package com.movietracker.movieservice.repository;

import com.movietracker.movieservice.entity.Genre;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data MongoDB repository for Genre documents.
 */
@Repository
public interface GenreRepository extends MongoRepository<Genre, String> {

    Optional<Genre> findByTmdbIdAndType(Integer tmdbId, String type);

    List<Genre> findByType(String type);

    boolean existsByTmdbIdAndType(Integer tmdbId, String type);
}
