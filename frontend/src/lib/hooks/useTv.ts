"use client";

import { useQuery, keepPreviousData } from "@tanstack/react-query";
import {
  getTrendingTvShows,
  getPopularTvShows,
  searchTvShows,
  getTvShowDetails,
} from "@/lib/api/tv";

export function useTrendingTvShows(
  window: "day" | "week" = "week",
  page: number = 1
) {
  return useQuery({
    queryKey: ["tv", "trending", window, page],
    queryFn: () => getTrendingTvShows(window, page),
    staleTime: 1000 * 60 * 10,
    placeholderData: keepPreviousData,
  });
}

export function usePopularTvShows(page: number = 1) {
  return useQuery({
    queryKey: ["tv", "popular", page],
    queryFn: () => getPopularTvShows(page),
    staleTime: 1000 * 60 * 10,
    placeholderData: keepPreviousData,
  });
}

export function useTvSearch(query: string, page: number = 1) {
  return useQuery({
    queryKey: ["tv", "search", query, page],
    queryFn: () => searchTvShows({ query, page }),
    enabled: query.trim().length >= 2,
    placeholderData: keepPreviousData,
    staleTime: 1000 * 60 * 5,
  });
}

export function useTvShowDetails(tmdbId: number) {
  return useQuery({
    queryKey: ["tv", "details", tmdbId],
    queryFn: () => getTvShowDetails(tmdbId),
    enabled: !!tmdbId && tmdbId > 0,
    staleTime: 1000 * 60 * 30,
  });
}
