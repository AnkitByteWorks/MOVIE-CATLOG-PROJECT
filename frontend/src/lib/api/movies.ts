import api from "./client";
import type {
  MovieSummary,
  MovieDetails,
  PagedResponse,
  Genre,
} from "@/lib/types";

export function filterAdultContent<T extends { adult?: boolean }>(
  items: T[]
): T[] {
  if (!Array.isArray(items)) return [];
  return items.filter((item) => !item.adult);
}

interface SearchParams {
  query: string;
  page?: number;
  size?: number;
}

export async function searchMovies({
  query,
  page = 1,
  size = 20,
}: SearchParams): Promise<PagedResponse<MovieSummary>> {
  if (!query.trim()) {
    return {
      content: [],
      page: 1,
      size,
      totalElements: 0,
      totalPages: 0,
      last: true,
    };
  }
  const targetPage = Math.max(1, page);
  const { data } = await api.get<PagedResponse<MovieSummary>>(
    "/api/v1/movies/search",
    { params: { q: query, page: targetPage, size, include_adult: false } }
  );
  return {
    ...data,
    content: filterAdultContent(data.content),
  };
}

export async function getTrendingMovies(
  window: "day" | "week" | "india" | "global" = "week",
  page = 1
): Promise<PagedResponse<MovieSummary>> {
  const targetPage = Math.max(1, page);
  const { data } = await api.get<PagedResponse<MovieSummary>>(
    "/api/v1/movies/trending",
    {
      params: { window, page: targetPage, include_adult: false },
    }
  );
  return {
    ...data,
    content: filterAdultContent(data.content),
  };
}

export async function getTrendingIndia(
  page = 1
): Promise<PagedResponse<MovieSummary>> {
  const targetPage = Math.max(1, page);
  const { data } = await api.get<PagedResponse<MovieSummary>>(
    "/api/v1/movies/trending/india",
    {
      params: { page: targetPage, include_adult: false },
    }
  );
  return {
    ...data,
    content: filterAdultContent(data.content),
  };
}

export async function getTrendingGlobal(
  page = 1
): Promise<PagedResponse<MovieSummary>> {
  const targetPage = Math.max(1, page);
  const { data } = await api.get<PagedResponse<MovieSummary>>(
    "/api/v1/movies/trending/global",
    {
      params: { page: targetPage, include_adult: false },
    }
  );
  return {
    ...data,
    content: filterAdultContent(data.content),
  };
}

export async function triggerTrendingSync(): Promise<Record<string, unknown>> {
  const { data } = await api.post<Record<string, unknown>>(
    "/api/v1/movies/trending/sync"
  );
  return data;
}

export async function getPopularMovies(
  page = 1
): Promise<PagedResponse<MovieSummary>> {
  const targetPage = Math.max(1, page);
  const { data } = await api.get<PagedResponse<MovieSummary>>(
    "/api/v1/movies/popular",
    {
      params: { page: targetPage, include_adult: false },
    }
  );
  return {
    ...data,
    content: filterAdultContent(data.content),
  };
}

export async function getMoviesByGenre(
  genre: string,
  page = 1
): Promise<PagedResponse<MovieSummary>> {
  const targetPage = Math.max(1, page);
  const { data } = await api.get<PagedResponse<MovieSummary>>(
    `/api/v1/movies/genre/${encodeURIComponent(genre)}`,
    { params: { page: targetPage, include_adult: false } }
  );
  return {
    ...data,
    content: filterAdultContent(data.content),
  };
}

export async function getMovieDetails(tmdbId: number): Promise<MovieDetails> {
  const { data } = await api.get<MovieDetails>(`/api/v1/movies/${tmdbId}`);
  return data;
}

export async function getMovieRecommendations(
  tmdbId: number
): Promise<MovieSummary[]> {
  const { data } = await api.get<MovieSummary[]>(
    `/api/v1/movies/${tmdbId}/recommendations`
  );
  return filterAdultContent(data);
}

export async function getGenres(type?: "MOVIE" | "TV"): Promise<Genre[]> {
  const { data } = await api.get<Genre[]>("/api/v1/genres", {
    params: type ? { type } : undefined,
  });
  return data;
}
