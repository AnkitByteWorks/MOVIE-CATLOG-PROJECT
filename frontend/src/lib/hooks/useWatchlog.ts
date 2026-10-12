"use client";

import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { toast } from "sonner";
import {
  createWatchLog,
  getMyWatchHistory,
  getMyRecentWatches,
  getUserWatchHistory,
  getMovieWatchHistory,
  getWatchLogById,
  updateWatchLog,
  deleteWatchLog,
  rewatchWatchLog,
  getMyStats,
  getUserStats,
} from "../api/watchlog";
import {
  CreateWatchLogRequest,
  UpdateWatchLogRequest,
  WatchLog,
  WatchStats,
  PagedResponse,
} from "../types";
import { isAuthenticated } from "../auth";

export function useMyWatchHistory(page = 1, size = 20) {
  const authed = typeof window !== "undefined" && isAuthenticated();
  return useQuery<PagedResponse<WatchLog>>({
    queryKey: ["watchlog", "me", page, size],
    queryFn: () => getMyWatchHistory(page, size),
    enabled: authed,
    staleTime: 1000 * 60, // 1 minute
  });
}

export function useMyRecentWatches() {
  const authed = typeof window !== "undefined" && isAuthenticated();
  return useQuery<WatchLog[]>({
    queryKey: ["watchlog", "me", "recent"],
    queryFn: () => getMyRecentWatches(),
    enabled: authed,
    staleTime: 1000 * 60,
  });
}

export function useUserWatchHistory(username: string, page = 1, size = 20) {
  return useQuery<PagedResponse<WatchLog>>({
    queryKey: ["watchlog", "user", username, page, size],
    queryFn: () => getUserWatchHistory(username, page, size),
    enabled: !!username,
    staleTime: 1000 * 60,
  });
}

export function useMovieWatchHistory(tmdbId: number, page = 1, size = 20) {
  return useQuery<PagedResponse<WatchLog>>({
    queryKey: ["watchlog", "movie", tmdbId, page, size],
    queryFn: () => getMovieWatchHistory(tmdbId, page, size),
    enabled: !!tmdbId,
    staleTime: 1000 * 60,
  });
}

export function useWatchLogById(id: string) {
  const authed = typeof window !== "undefined" && isAuthenticated();
  return useQuery<WatchLog>({
    queryKey: ["watchlog", "detail", id],
    queryFn: () => getWatchLogById(id),
    enabled: authed && !!id,
  });
}

export function useMyStats() {
  const authed = typeof window !== "undefined" && isAuthenticated();
  return useQuery<WatchStats>({
    queryKey: ["watchlog-stats", "me"],
    queryFn: () => getMyStats(),
    enabled: authed,
    staleTime: 1000 * 60,
  });
}

export function useUserStats(username: string) {
  return useQuery<WatchStats>({
    queryKey: ["watchlog-stats", "user", username],
    queryFn: () => getUserStats(username),
    enabled: !!username,
    staleTime: 1000 * 60,
  });
}

export function useCreateWatchLog() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (data: CreateWatchLogRequest) => createWatchLog(data),
    onSuccess: (data) => {
      toast.success(`Logged "${data.title}" to your watch history!`);
      queryClient.invalidateQueries({ queryKey: ["watchlog"] });
      queryClient.invalidateQueries({ queryKey: ["watchlog-stats"] });
    },
    onError: (error: unknown) => {
      const axiosErr = error as { response?: { status?: number; data?: { message?: string } } };
      const status = axiosErr.response?.status;
      if (status === 409) {
        toast.error("Already logged! Use 'Rewatch' to log another view.");
      } else if (status === 401) {
        toast.error("Please log in first to record your watch.");
      } else {
        toast.error(axiosErr.response?.data?.message || "Failed to log watch.");
      }
    },
  });
}

export function useUpdateWatchLog() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ id, data }: { id: string; data: UpdateWatchLogRequest }) =>
      updateWatchLog(id, data),
    onSuccess: () => {
      toast.success("Watch log updated successfully!");
      queryClient.invalidateQueries({ queryKey: ["watchlog"] });
      queryClient.invalidateQueries({ queryKey: ["watchlog-stats"] });
    },
    onError: (error: unknown) => {
      const axiosErr = error as { response?: { data?: { message?: string } } };
      toast.error(axiosErr.response?.data?.message || "Failed to update watch log.");
    },
  });
}

export function useDeleteWatchLog() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (id: string) => deleteWatchLog(id),
    onSuccess: () => {
      toast.success("Watch log removed from history.");
      queryClient.invalidateQueries({ queryKey: ["watchlog"] });
      queryClient.invalidateQueries({ queryKey: ["watchlog-stats"] });
    },
    onError: (error: unknown) => {
      const axiosErr = error as { response?: { data?: { message?: string } } };
      toast.error(axiosErr.response?.data?.message || "Failed to delete watch log.");
    },
  });
}

export function useRewatch() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (id: string) => rewatchWatchLog(id),
    onSuccess: (data) => {
      toast.success(`Rewatch logged! Total watches: ${(data.rewatchCount || 0) + 1}`);
      queryClient.invalidateQueries({ queryKey: ["watchlog"] });
      queryClient.invalidateQueries({ queryKey: ["watchlog-stats"] });
    },
    onError: (error: unknown) => {
      const axiosErr = error as { response?: { data?: { message?: string } } };
      toast.error(axiosErr.response?.data?.message || "Failed to record rewatch.");
    },
  });
}

export function useMovieWatchState(tmdbId: number, mediaType: "MOVIE" | "TV" = "MOVIE") {
  const authed = typeof window !== "undefined" && isAuthenticated();
  const { data: recent, isLoading: loadingRecent } = useMyRecentWatches();
  const { data: history, isLoading: loadingHistory } = useMyWatchHistory(1, 100);

  const found =
    recent?.find(
      (w) => w.tmdbId === tmdbId && (!mediaType || w.mediaType === mediaType)
    ) ||
    history?.content?.find(
      (w) => w.tmdbId === tmdbId && (!mediaType || w.mediaType === mediaType)
    );

  return {
    isWatched: !!found,
    watchLog: found,
    rewatchCount: found ? (found.rewatchCount || 0) : 0,
    isLoading: authed ? loadingRecent && loadingHistory : false,
  };
}
