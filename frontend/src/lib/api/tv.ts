import api from "./client";
import { filterAdultContent } from "./movies";
import type {
  TvShowSummary,
  TvShowDetails,
  PagedResponse,
} from "@/lib/types";

interface SearchTvParams {
  query: string;
  page?: number;
  size?: number;
}

export async function searchTvShows({
  query,
  page = 1,
  size = 20,
}: SearchTvParams): Promise<PagedResponse<TvShowSummary>> {
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
  const { data } = await api.get<PagedResponse<TvShowSummary>>(
    "/api/v1/tv/search",
    { params: { q: query, page: targetPage, size, include_adult: false } }
  );
  return {
    ...data,
    content: filterAdultContent(data.content),
  };
}

export async function getTrendingTvShows(
  window: "day" | "week" = "week",
  page = 1
): Promise<PagedResponse<TvShowSummary>> {
  const targetPage = Math.max(1, page);
  const { data } = await api.get<PagedResponse<TvShowSummary>>(
    "/api/v1/tv/trending",
    { params: { window, page: targetPage, include_adult: false } }
  );
  return {
    ...data,
    content: filterAdultContent(data.content),
  };
}

export async function getPopularTvShows(
  page = 1
): Promise<PagedResponse<TvShowSummary>> {
  const targetPage = Math.max(1, page);
  const { data } = await api.get<PagedResponse<TvShowSummary>>(
    "/api/v1/tv/popular",
    { params: { page: targetPage, include_adult: false } }
  );
  return {
    ...data,
    content: filterAdultContent(data.content),
  };
}

export async function getTvShowDetails(tmdbId: number): Promise<TvShowDetails> {
  const { data } = await api.get<TvShowDetails>(`/api/v1/tv/${tmdbId}`);
  return data;
}
