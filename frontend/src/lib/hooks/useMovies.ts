"use client";

import { useQuery, keepPreviousData } from "@tanstack/react-query";
import {
  getTrendingMovies,
  getTrendingIndia,
  getTrendingGlobal,
  getPopularMovies,
  searchMovies,
  getMovieDetails,
  getMovieRecommendations,
  getMoviesByGenre,
} from "@/lib/api/movies";

export function useTrendingMovies(
  window: "day" | "week" | "india" | "global" = "week",
  page: number = 1
) {
  return useQuery({
    queryKey: ["movies", "trending", window, page],
    queryFn: () => getTrendingMovies(window, page),
    staleTime: 1000 * 60 * 10,
    placeholderData: keepPreviousData,
  });
}

export function useTrendingIndia(page: number = 1) {
  return useQuery({
    queryKey: ["movies", "trending", "india", page],
    queryFn: () => getTrendingIndia(page),
    staleTime: 1000 * 60 * 10,
    placeholderData: keepPreviousData,
  });
}

export function useTrendingGlobal(page: number = 1) {
  return useQuery({
    queryKey: ["movies", "trending", "global", page],
    queryFn: () => getTrendingGlobal(page),
    staleTime: 1000 * 60 * 10,
    placeholderData: keepPreviousData,
  });
}

export function usePopularMovies(page: number = 1) {
  return useQuery({
    queryKey: ["movies", "popular", page],
    queryFn: () => getPopularMovies(page),
    staleTime: 1000 * 60 * 10,
    placeholderData: keepPreviousData,
  });
}

export function useMovieSearch(query: string, page: number = 1) {
  return useQuery({
    queryKey: ["movies", "search", query, page],
    queryFn: () => searchMovies({ query, page }),
    enabled: query.trim().length >= 2,
    placeholderData: keepPreviousData,
    staleTime: 1000 * 60 * 5,
  });
}

export function useMovieDetails(tmdbId: number) {
  return useQuery({
    queryKey: ["movies", "details", tmdbId],
    queryFn: () => getMovieDetails(tmdbId),
    enabled: !!tmdbId && tmdbId > 0,
    staleTime: 1000 * 60 * 30,
  });
}

export function useMovieRecommendations(tmdbId: number) {
  return useQuery({
    queryKey: ["movies", "recommendations", tmdbId],
    queryFn: () => getMovieRecommendations(tmdbId),
    enabled: !!tmdbId && tmdbId > 0,
    staleTime: 1000 * 60 * 15,
  });
}

export function useMoviesByGenre(genre: string, page: number = 1) {
  return useQuery({
    queryKey: ["movies", "genre", genre, page],
    queryFn: () => getMoviesByGenre(genre, page),
    enabled: !!genre && genre.trim().length > 0,
    placeholderData: keepPreviousData,
    staleTime: 1000 * 60 * 10,
  });
}
