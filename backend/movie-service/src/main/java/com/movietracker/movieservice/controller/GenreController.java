package com.movietracker.movieservice.controller;

import com.movietracker.movieservice.entity.Genre;
import com.movietracker.movieservice.repository.GenreRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for retrieving available Movie & TV genres.
 */
@RestController
@RequestMapping("/api/v1/genres")
@RequiredArgsConstructor
@Tag(name = "Genres", description = "Endpoints for exploring movie and television categories")
public class GenreController {

    private final GenreRepository genreRepository;

    @Operation(summary = "List all genres", description = "Optionally filter genres by type ('MOVIE' or 'TV')")
    @GetMapping
    public ResponseEntity<List<Genre>> getGenres(
            @Parameter(description = "Optional genre type filter: MOVIE or TV")
            @RequestParam(value = "type", required = false) String type
    ) {
        if (type != null && !type.isBlank()) {
            return ResponseEntity.ok(genreRepository.findByType(type.toUpperCase()));
        }
        return ResponseEntity.ok(genreRepository.findAll());
    }
}
