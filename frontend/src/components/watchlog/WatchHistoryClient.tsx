"use client";

import * as React from "react";
import Image from "next/image";
import Link from "next/link";
import { format, parseISO } from "date-fns";
import {
  Film,
  Tv,
  Calendar,
  RotateCcw,
  Pencil,
  Trash2,
  ChevronLeft,
  ChevronRight,
  LogIn,
  Sparkles,
  Quote,
} from "lucide-react";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { RatingStars } from "./RatingStars";
import { LogWatchDialog } from "./LogWatchDialog";
import {
  useMyWatchHistory,
  useRewatch,
  useDeleteWatchLog,
} from "@/lib/hooks/useWatchlog";
import { useAuth, useIsMounted } from "@/lib/hooks/useAuth";
import { WatchLog, WatchLocation } from "@/lib/types";

const LOCATION_EMOJIS: Record<WatchLocation, string> = {
  HOME: "🛋️ Home",
  THEATER: "🎬 Theater",
  STREAMING: "📺 Streaming",
  PLANE: "✈️ Plane",
  OTHER: "🍿 Other",
};

export function WatchHistoryClient() {
  const isClient = useIsMounted();
  const { isAuthenticated: authed } = useAuth();
  const [page, setPage] = React.useState(1);
  const pageSize = 15;

  const [editingLog, setEditingLog] = React.useState<WatchLog | null>(null);
  const [editDialogOpen, setEditDialogOpen] = React.useState(false);

  const { data, isLoading } = useMyWatchHistory(page, pageSize);
  const rewatchMutation = useRewatch();
  const deleteMutation = useDeleteWatchLog();

  const handleEdit = (logItem: WatchLog) => {
    setEditingLog(logItem);
    setEditDialogOpen(true);
  };

  const handleRewatch = (logItem: WatchLog) => {
    rewatchMutation.mutate(logItem.id);
  };

  const handleDelete = (logItem: WatchLog) => {
    if (confirm(`Remove "${logItem.title}" from your watch history?`)) {
      deleteMutation.mutate(logItem.id);
    }
  };

  if (!isClient) {
    return (
      <div className="min-h-screen pt-28 pb-20 px-4 max-w-5xl mx-auto">
        <div className="h-8 w-48 bg-zinc-800 rounded animate-pulse mb-6" />
        <div className="space-y-4">
          {[1, 2, 3].map((i) => (
            <div key={i} className="h-28 bg-[#131318] rounded-xl animate-pulse" />
          ))}
        </div>
      </div>
    );
  }

  if (!authed) {
    return (
      <div className="min-h-[70vh] pt-28 pb-20 px-4 max-w-md mx-auto flex flex-col items-center justify-center text-center">
        <div className="w-16 h-16 rounded-full bg-rose-500/10 border border-rose-500/20 text-rose-500 flex items-center justify-center mb-4">
          <Film className="w-8 h-8" />
        </div>
        <h1 className="text-2xl font-serif font-bold text-white mb-2">
          Your Personal Watchlog
        </h1>
        <p className="text-sm text-muted-foreground mb-6">
          Log every movie and episode you watch, keep track of personal ratings, reviews, and viewing stats.
        </p>
        <Link href="/login?from=/watchlog">
          <Button className="bg-rose-600 hover:bg-rose-500 text-white font-medium px-6 gap-2">
            <LogIn className="w-4 h-4" />
            <span>Sign in to view Watchlog</span>
          </Button>
        </Link>
      </div>
    );
  }

  const items = data?.content || [];
  const totalElements = data?.totalElements || 0;
  const totalPages = data?.totalPages || 1;

  // Group items by Year-Month (e.g., "October 2026")
  const groupedByMonth = items.reduce((acc, item) => {
    let monthKey = "Recent";
    try {
      monthKey = format(parseISO(item.watchedAt), "MMMM yyyy");
    } catch {
      monthKey = "Recent";
    }
    if (!acc[monthKey]) {
      acc[monthKey] = [];
    }
    acc[monthKey].push(item);
    return acc;
  }, {} as Record<string, WatchLog[]>);

  return (
    <div className="min-h-screen pt-28 pb-24 px-4 sm:px-6 lg:px-8 max-w-5xl mx-auto">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-end justify-between gap-4 mb-8 pb-6 border-b border-border/20">
        <div>
          <span className="text-xs uppercase font-mono tracking-widest text-rose-400 mb-1 block">
            Diary & History
          </span>
          <h1 className="text-3xl sm:text-4xl font-serif font-bold text-white tracking-tight">
            Watch History
          </h1>
          <p className="text-xs sm:text-sm text-muted-foreground mt-1">
            {totalElements} {totalElements === 1 ? "entry" : "entries"} recorded in your cinematic timeline.
          </p>
        </div>

        <div className="flex items-center gap-2">
          <Link href="/stats">
            <Button
              variant="outline"
              size="sm"
              className="gap-1.5 border-border/40 hover:bg-white/5 text-xs text-zinc-300"
            >
              <Sparkles className="w-3.5 h-3.5 text-amber-400" />
              <span>View Analytics</span>
            </Button>
          </Link>
        </div>
      </div>

      {/* Loading Skeleton */}
      {isLoading && (
        <div className="space-y-4">
          {[1, 2, 3, 4].map((i) => (
            <div
              key={i}
              className="h-32 bg-[#131318] border border-border/20 rounded-xl p-4 flex gap-4 animate-pulse"
            >
              <div className="w-20 h-full bg-zinc-800 rounded-md" />
              <div className="flex-1 space-y-2 py-1">
                <div className="h-5 w-1/3 bg-zinc-800 rounded" />
                <div className="h-4 w-1/4 bg-zinc-800/60 rounded" />
                <div className="h-10 w-2/3 bg-zinc-800/40 rounded mt-2" />
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Empty State */}
      {!isLoading && items.length === 0 && (
        <div className="p-12 text-center rounded-2xl bg-[#131318]/60 border border-border/30 max-w-lg mx-auto">
          <div className="w-14 h-14 rounded-full bg-muted/20 flex items-center justify-center mx-auto mb-4 text-muted-foreground">
            <Film className="w-7 h-7" />
          </div>
          <h2 className="text-lg font-bold text-white mb-1">No watches logged yet</h2>
          <p className="text-xs text-muted-foreground mb-6">
            Search for your favorite movies and click &quot;Log Watch&quot; to begin building your diary!
          </p>
          <Link href="/">
            <Button className="bg-rose-600 hover:bg-rose-500 text-white text-xs">
              Explore Catalog
            </Button>
          </Link>
        </div>
      )}

      {/* Monthly Timeline Groups */}
      {!isLoading && items.length > 0 && (
        <div className="space-y-10">
          {Object.entries(groupedByMonth).map(([month, monthItems]) => (
            <div key={month} className="space-y-4">
              <div className="sticky top-20 z-10 py-1 bg-[#0a0a0f]/90 backdrop-blur-md">
                <span className="text-xs font-mono font-bold uppercase tracking-wider text-amber-400/90 border-b border-amber-400/30 pb-0.5">
                  {month}
                </span>
              </div>

              <div className="grid gap-3">
                {monthItems.map((item) => {
                  const mediaLink =
                    item.mediaType === "TV"
                      ? `/tv/${item.tmdbId}`
                      : `/movie/${item.tmdbId}`;

                  let formattedDate = "";
                  try {
                    formattedDate = format(parseISO(item.watchedAt), "EEE, MMM d, yyyy");
                  } catch {
                    formattedDate = "Recently";
                  }

                  return (
                    <div
                      key={item.id}
                      className="group relative flex flex-col sm:flex-row gap-4 p-4 rounded-xl bg-[#131318] border border-border/30 hover:border-border/60 hover:bg-[#16161d] transition-all shadow-sm"
                    >
                      {/* Artwork thumbnail */}
                      <Link
                        href={mediaLink}
                        className="relative w-20 h-28 sm:w-20 sm:h-28 rounded-lg overflow-hidden shrink-0 border border-border/30 bg-black/50 shadow-md group-hover:scale-105 transition-transform"
                      >
                        {item.posterUrl ? (
                          <Image
                            src={item.posterUrl}
                            alt={item.title}
                            fill
                            className="object-cover"
                            sizes="80px"
                          />
                        ) : (
                          <div className="w-full h-full flex items-center justify-center text-muted-foreground">
                            {item.mediaType === "TV" ? (
                              <Tv className="w-6 h-6" />
                            ) : (
                              <Film className="w-6 h-6" />
                            )}
                          </div>
                        )}
                      </Link>

                      {/* Content Info */}
                      <div className="flex-1 min-w-0 flex flex-col justify-between">
                        <div>
                          <div className="flex flex-wrap items-center gap-2 mb-1">
                            <Link
                              href={mediaLink}
                              className="text-base sm:text-lg font-serif font-bold text-white hover:text-rose-400 transition-colors line-clamp-1"
                            >
                              {item.title}
                            </Link>

                            <Badge
                              variant="outline"
                              className="text-[10px] px-1.5 py-0 border-white/10 text-muted-foreground"
                            >
                              {item.mediaType === "TV" ? "TV" : "Movie"}
                            </Badge>

                            {item.rewatchCount > 0 && (
                              <Badge className="text-[10px] px-1.5 py-0 bg-amber-500/15 text-amber-300 border border-amber-500/30 font-mono">
                                Rewatched {item.rewatchCount}x
                              </Badge>
                            )}

                            {item.watchLocation && (
                              <span className="text-[11px] text-zinc-400 flex items-center gap-1">
                                {LOCATION_EMOJIS[item.watchLocation] || item.watchLocation}
                              </span>
                            )}
                          </div>

                          {/* Date and Rating bar */}
                          <div className="flex flex-wrap items-center gap-3 text-xs text-muted-foreground mb-2">
                            <span className="flex items-center gap-1 font-mono text-[11px] text-zinc-400">
                              <Calendar className="w-3.5 h-3.5 text-rose-400/80" />
                              {formattedDate}
                            </span>

                            {item.rating && (
                              <div className="flex items-center gap-1.5">
                                <RatingStars
                                  value={item.rating}
                                  readonly
                                  size="sm"
                                  showValue
                                />
                              </div>
                            )}
                          </div>

                          {/* Review excerpt */}
                          {item.review && (
                            <div className="mt-2 p-2.5 rounded-lg bg-background/40 border border-border/20 text-xs text-zinc-300 italic relative">
                              <Quote className="w-3 h-3 text-rose-400/60 inline mr-1 -mt-1" />
                              <span>{item.review}</span>
                            </div>
                          )}
                        </div>

                        {/* Card bottom actions */}
                        <div className="flex items-center gap-2 mt-3 pt-2 border-t border-border/20 sm:border-0 sm:mt-0 sm:pt-0">
                          <Button
                            variant="ghost"
                            size="sm"
                            onClick={() => handleRewatch(item)}
                            disabled={rewatchMutation.isPending}
                            className="h-8 text-xs text-zinc-300 hover:text-white hover:bg-white/5 gap-1.5"
                            title="Log another rewatch"
                          >
                            <RotateCcw className="w-3.5 h-3.5 text-amber-400" />
                            <span>Rewatch</span>
                          </Button>

                          <Button
                            variant="ghost"
                            size="sm"
                            onClick={() => handleEdit(item)}
                            className="h-8 text-xs text-zinc-300 hover:text-white hover:bg-white/5 gap-1.5"
                          >
                            <Pencil className="w-3.5 h-3.5 text-rose-400" />
                            <span>Edit</span>
                          </Button>

                          <Button
                            variant="ghost"
                            size="sm"
                            onClick={() => handleDelete(item)}
                            disabled={deleteMutation.isPending}
                            className="h-8 text-xs text-red-400 hover:text-red-300 hover:bg-red-950/20 gap-1.5 ml-auto"
                          >
                            <Trash2 className="w-3.5 h-3.5" />
                            <span>Delete</span>
                          </Button>
                        </div>
                      </div>
                    </div>
                  );
                })}
              </div>
            </div>
          ))}

          {/* Pagination Controls */}
          {totalPages > 1 && (
            <div className="flex items-center justify-between pt-6 border-t border-border/20">
              <Button
                variant="outline"
                size="sm"
                disabled={page <= 1}
                onClick={() => setPage((p) => Math.max(1, p - 1))}
                className="gap-1 text-xs border-border/40 hover:bg-white/5"
              >
                <ChevronLeft className="w-4 h-4" />
                Previous
              </Button>

              <span className="text-xs text-muted-foreground font-mono">
                Page {page} of {totalPages}
              </span>

              <Button
                variant="outline"
                size="sm"
                disabled={page >= totalPages}
                onClick={() => setPage((p) => p + 1)}
                className="gap-1 text-xs border-border/40 hover:bg-white/5"
              >
                Next
                <ChevronRight className="w-4 h-4" />
              </Button>
            </div>
          )}
        </div>
      )}

      {/* Edit Dialog */}
      {editingLog && (
        <LogWatchDialog
          open={editDialogOpen}
          onOpenChange={(open) => {
            setEditDialogOpen(open);
            if (!open) setEditingLog(null);
          }}
          mediaInfo={{
            tmdbId: editingLog.tmdbId,
            mediaType: editingLog.mediaType,
            title: editingLog.title,
            posterUrl: editingLog.posterUrl,
            backdropUrl: editingLog.backdropUrl,
          }}
          initialData={editingLog}
        />
      )}
    </div>
  );
}
