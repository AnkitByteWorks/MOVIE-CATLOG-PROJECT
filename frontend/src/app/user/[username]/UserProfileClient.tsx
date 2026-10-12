"use client";

import * as React from "react";
import Image from "next/image";
import Link from "next/link";
import { format, parseISO } from "date-fns";
import {
  User,
  Film,
  Flame,
  Share2,
} from "lucide-react";
import { toast } from "sonner";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { RatingStars } from "@/components/watchlog/RatingStars";
import {
  useUserStats,
  useUserWatchHistory,
} from "@/lib/hooks/useWatchlog";

export function UserProfileClient({ username }: { username: string }) {
  const { data: stats } = useUserStats(username);
  const { data: history, isLoading: loadingHistory } = useUserWatchHistory(
    username,
    1,
    20
  );

  const handleShare = () => {
    if (typeof window !== "undefined") {
      navigator.clipboard.writeText(window.location.href);
      toast.success("Profile link copied to clipboard");
    }
  };

  const logs = history?.content || [];
  const totalWatched =
    stats?.totalWatches ??
    (stats?.totalMovies ?? 0) + (stats?.totalTvShows ?? 0);

  return (
    <div className="min-h-screen pt-28 pb-24 px-4 sm:px-6 lg:px-8 max-w-5xl mx-auto space-y-8">
      {/* Profile Header */}
      <div className="p-6 sm:p-8 rounded-2xl bg-[#131318] border border-border/30 relative overflow-hidden shadow-xl">
        <div className="flex flex-col sm:flex-row items-center sm:items-start gap-6 relative z-10 text-center sm:text-left">
          <div className="w-20 h-20 sm:w-24 sm:h-24 rounded-full bg-gradient-to-tr from-rose-600 to-amber-500 p-1 shrink-0 shadow-lg shadow-rose-950/50">
            <div className="w-full h-full rounded-full bg-[#131318] flex items-center justify-center text-white">
              <User className="w-10 h-10 text-rose-400" />
            </div>
          </div>

          <div className="flex-1 min-w-0">
            <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2">
              <div>
                <span className="text-[11px] font-mono uppercase tracking-widest text-rose-400">
                  Public Cinephile Profile
                </span>
                <h1 className="text-2xl sm:text-3xl font-serif font-bold text-white tracking-tight">
                  @{username}
                </h1>
              </div>

              <Button
                variant="outline"
                size="sm"
                onClick={handleShare}
                className="gap-1.5 text-xs border-border/40 hover:bg-white/5 text-zinc-300 self-center sm:self-auto"
              >
                <Share2 className="w-3.5 h-3.5" />
                <span>Share Profile</span>
              </Button>
            </div>

            {/* Quick Stats Summary */}
            <div className="grid grid-cols-2 sm:grid-cols-4 gap-3 mt-6 pt-6 border-t border-border/20">
              <div className="space-y-0.5">
                <span className="text-[10px] text-muted-foreground uppercase tracking-wider">
                  Total Logged
                </span>
                <p className="text-xl font-serif font-bold text-white">
                  {totalWatched}
                </p>
              </div>

              <div className="space-y-0.5">
                <span className="text-[10px] text-muted-foreground uppercase tracking-wider">
                  Watch Time
                </span>
                <p className="text-xl font-serif font-bold text-amber-400">
                  {stats?.formattedWatchTime || stats?.totalWatchTimeFormatted || "0m"}
                </p>
              </div>

              <div className="space-y-0.5">
                <span className="text-[10px] text-muted-foreground uppercase tracking-wider">
                  Avg Rating
                </span>
                <p className="text-xl font-serif font-bold text-white">
                  {stats?.averageRating ? `${stats.averageRating.toFixed(1)}/10` : "—"}
                </p>
              </div>

              <div className="space-y-0.5">
                <span className="text-[10px] text-muted-foreground uppercase tracking-wider">
                  Streak
                </span>
                <p className="text-xl font-serif font-bold text-orange-400 flex items-center gap-1">
                  <Flame className="w-4 h-4 fill-orange-400" />
                  {stats?.currentStreak ?? stats?.watchStreak ?? 0}d
                </p>
              </div>
            </div>

            {/* Top Genres Pills */}
            {stats?.topGenres && stats.topGenres.length > 0 && (
              <div className="flex flex-wrap items-center gap-1.5 mt-5">
                <span className="text-[11px] text-muted-foreground mr-1">
                  Favorite genres:
                </span>
                {stats.topGenres.slice(0, 4).map((g) => (
                  <Badge
                    key={g.genre}
                    variant="outline"
                    className="text-[10px] border-rose-500/30 text-rose-300 bg-rose-950/20"
                  >
                    {g.genre} ({g.count})
                  </Badge>
                ))}
              </div>
            )}
          </div>
        </div>
      </div>

      {/* User's Watch History Feed */}
      <div className="space-y-4">
        <h2 className="text-xl font-serif font-bold text-white flex items-center gap-2">
          <Film className="w-5 h-5 text-rose-500" />
          Recent Watches
        </h2>

        {loadingHistory && (
          <div className="space-y-3">
            {[1, 2, 3].map((i) => (
              <div
                key={i}
                className="h-24 bg-[#131318] rounded-xl border border-border/20 animate-pulse"
              />
            ))}
          </div>
        )}

        {!loadingHistory && logs.length === 0 && (
          <div className="p-8 text-center rounded-xl bg-[#131318] border border-border/20 text-muted-foreground">
            <p className="text-sm">No watches publicly recorded yet for @{username}.</p>
          </div>
        )}

        {!loadingHistory && logs.length > 0 && (
          <div className="grid gap-3">
            {logs.map((item) => {
              const link =
                item.mediaType === "TV"
                  ? `/tv/${item.tmdbId}`
                  : `/movie/${item.tmdbId}`;

              let dateFormatted = "";
              try {
                dateFormatted = format(parseISO(item.watchedAt), "MMM d, yyyy");
              } catch {
                dateFormatted = "Recently";
              }

              return (
                <div
                  key={item.id}
                  className="flex gap-4 p-3.5 rounded-xl bg-[#131318] border border-border/30 hover:border-border/60 transition-colors"
                >
                  <Link
                    href={link}
                    className="relative w-14 h-20 rounded-md overflow-hidden shrink-0 border border-border/20 bg-black/40"
                  >
                    {item.posterUrl ? (
                      <Image
                        src={item.posterUrl}
                        alt={item.title}
                        fill
                        className="object-cover"
                        sizes="56px"
                      />
                    ) : (
                      <div className="w-full h-full flex items-center justify-center text-muted-foreground">
                        <Film className="w-5 h-5" />
                      </div>
                    )}
                  </Link>

                  <div className="flex-1 min-w-0 flex flex-col justify-between py-0.5">
                    <div>
                      <div className="flex items-center gap-2">
                        <Link
                          href={link}
                          className="text-base font-serif font-bold text-white hover:text-rose-400 transition-colors truncate"
                        >
                          {item.title}
                        </Link>
                        {item.rewatchCount > 0 && (
                          <span className="text-[10px] font-mono px-1.5 py-0.2 rounded bg-amber-500/10 text-amber-300 border border-amber-500/20">
                            x{item.rewatchCount + 1}
                          </span>
                        )}
                      </div>

                      <div className="flex items-center gap-3 text-xs text-muted-foreground mt-1">
                        <span className="font-mono text-[11px] text-zinc-400">
                          {dateFormatted}
                        </span>
                        {item.rating && (
                          <RatingStars
                            value={item.rating}
                            readonly
                            size="sm"
                            showValue
                          />
                        )}
                      </div>
                    </div>

                    {item.review && (
                      <p className="text-xs text-zinc-300 line-clamp-1 italic mt-1">
                        &ldquo;{item.review}&rdquo;
                      </p>
                    )}
                  </div>
                </div>
              );
            })}
          </div>
        )}
      </div>
    </div>
  );
}
