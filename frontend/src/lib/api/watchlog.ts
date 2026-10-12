import { watchlogApi } from "./client";
import {
  WatchLog,
  CreateWatchLogRequest,
  UpdateWatchLogRequest,
  WatchStats,
  PagedResponse,
} from "../types";

function normalizeStats(stats: Partial<WatchStats> | null | undefined): WatchStats {
  if (!stats) {
    return {
      totalMovies: 0,
      totalTvShows: 0,
      totalWatchTimeMinutes: 0,
      formattedWatchTime: "0m",
      totalWatchTimeFormatted: "0m",
      averageRating: null,
      longestStreak: 0,
      currentStreak: 0,
      watchStreak: 0,
      thisMonthWatches: 0,
      thisYearWatches: 0,
      topGenres: [],
      topActors: [],
      byLocation: {},
    };
  }
  const thisMonthObj = stats.thisMonth as { movies?: number } | undefined;
  const thisYearObj = stats.thisYear as { movies?: number } | undefined;
  const formattedTime = (stats.formattedWatchTime || stats.totalWatchTimeFormatted || "0m") as string;
  const streak = (stats.currentStreak ?? stats.watchStreak ?? 0) as number;

  return {
    ...stats,
    totalMovies: stats.totalMovies ?? 0,
    totalTvShows: stats.totalTvShows ?? 0,
    totalWatchTimeMinutes: stats.totalWatchTimeMinutes ?? 0,
    formattedWatchTime: formattedTime,
    totalWatchTimeFormatted: formattedTime,
    averageRating: stats.averageRating ?? null,
    longestStreak: stats.longestStreak ?? 0,
    currentStreak: streak,
    watchStreak: streak,
    thisMonthWatches: (stats.thisMonthWatches ?? thisMonthObj?.movies ?? 0) as number,
    thisYearWatches: (stats.thisYearWatches ?? thisYearObj?.movies ?? 0) as number,
    topGenres: stats.topGenres || [],
    topActors: stats.topActors || [],
    byLocation: stats.byLocation || {},
  };
}

export async function createWatchLog(data: CreateWatchLogRequest): Promise<WatchLog> {
  const response = await watchlogApi.post<WatchLog>("/api/v1/watchlog", data);
  return response.data;
}

export async function getMyWatchHistory(page = 1, size = 20): Promise<PagedResponse<WatchLog>> {
  const response = await watchlogApi.get<PagedResponse<WatchLog>>("/api/v1/watchlog/me", {
    params: { page, size },
  });
  return response.data;
}

export async function getMyRecentWatches(): Promise<WatchLog[]> {
  const response = await watchlogApi.get<WatchLog[]>("/api/v1/watchlog/me/recent");
  return response.data;
}

export async function getUserWatchHistory(
  username: string,
  page = 1,
  size = 20
): Promise<PagedResponse<WatchLog>> {
  const response = await watchlogApi.get<PagedResponse<WatchLog>>(
    `/api/v1/watchlog/user/${encodeURIComponent(username)}`,
    {
      params: { page, size },
    }
  );
  return response.data;
}

export async function getMovieWatchHistory(
  tmdbId: number,
  page = 1,
  size = 20
): Promise<PagedResponse<WatchLog>> {
  const response = await watchlogApi.get<PagedResponse<WatchLog>>(
    `/api/v1/watchlog/movie/${tmdbId}`,
    {
      params: { page, size },
    }
  );
  return response.data;
}

export async function getWatchLogById(id: string): Promise<WatchLog> {
  const response = await watchlogApi.get<WatchLog>(`/api/v1/watchlog/${id}`);
  return response.data;
}

export async function updateWatchLog(
  id: string,
  data: UpdateWatchLogRequest
): Promise<WatchLog> {
  const response = await watchlogApi.put<WatchLog>(`/api/v1/watchlog/${id}`, data);
  return response.data;
}

export async function deleteWatchLog(id: string): Promise<void> {
  await watchlogApi.delete(`/api/v1/watchlog/${id}`);
}

export async function rewatchWatchLog(id: string): Promise<WatchLog> {
  const response = await watchlogApi.post<WatchLog>(`/api/v1/watchlog/${id}/rewatch`);
  return response.data;
}

export async function getMyStats(): Promise<WatchStats> {
  const response = await watchlogApi.get<WatchStats>("/api/v1/watchlog/stats/me");
  return normalizeStats(response.data);
}

export async function getUserStats(username: string): Promise<WatchStats> {
  const response = await watchlogApi.get<WatchStats>(
    `/api/v1/watchlog/stats/${encodeURIComponent(username)}`
  );
  return normalizeStats(response.data);
}
