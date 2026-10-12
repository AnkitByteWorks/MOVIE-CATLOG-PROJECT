"use client";

import { useSyncExternalStore } from "react";
import { toast } from "sonner";

export function useWatchlist(tmdbId: number) {
  const subscribe = (callback: () => void) => {
    if (typeof window === "undefined") return () => {};
    window.addEventListener("storage", callback);
    window.addEventListener("watchlist-updated", callback);
    return () => {
      window.removeEventListener("storage", callback);
      window.removeEventListener("watchlist-updated", callback);
    };
  };

  const getSnapshot = () => {
    if (typeof window === "undefined" || !tmdbId) return false;
    return localStorage.getItem(`watchlist:${tmdbId}`) === "1";
  };

  const getServerSnapshot = () => false;

  const inWatchlist = useSyncExternalStore(subscribe, getSnapshot, getServerSnapshot);

  const toggleWatchlist = (title: string) => {
    if (typeof window === "undefined" || !tmdbId) return;
    if (inWatchlist) {
      localStorage.removeItem(`watchlist:${tmdbId}`);
      toast.info(`"${title}" removed from watchlist`);
    } else {
      localStorage.setItem(`watchlist:${tmdbId}`, "1");
      toast.success(`"${title}" added to watchlist ✓`);
    }
    window.dispatchEvent(new Event("watchlist-updated"));
  };

  return { inWatchlist, toggleWatchlist };
}
