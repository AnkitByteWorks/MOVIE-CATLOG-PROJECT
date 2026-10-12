"use client";

import * as React from "react";
import Link from "next/link";
import {
  Film,
  Clock,
  Star,
  Flame,
  Trophy,
  Calendar,
  Compass,
  PieChart as PieChartIcon,
  BarChart3,
  ArrowUpRight,
  LogIn,
} from "lucide-react";
import {
  ResponsiveContainer,
  BarChart,
  Bar,
  XAxis,
  YAxis,
  Tooltip,
  PieChart,
  Pie,
  Cell,
} from "recharts";
import { motion } from "framer-motion";
import { Button } from "@/components/ui/button";
import { Card, CardHeader, CardTitle, CardContent } from "@/components/ui/card";
import { useMyStats, useMyWatchHistory } from "@/lib/hooks/useWatchlog";
import { useAuth, useIsMounted } from "@/lib/hooks/useAuth";
import { cn } from "@/lib/utils";

const LOCATION_COLORS: Record<string, string> = {
  HOME: "#3b82f6", // blue
  THEATER: "#e11d48", // rose
  STREAMING: "#8b5cf6", // purple
  PLANE: "#06b6d4", // cyan
  OTHER: "#f59e0b", // amber
};

const LOCATION_LABELS: Record<string, string> = {
  HOME: "At Home",
  THEATER: "Theater",
  STREAMING: "Streaming",
  PLANE: "Airplane",
  OTHER: "Other",
};

export function StatsClient() {
  const mounted = useIsMounted();
  const { isAuthenticated: authed, user: currentUser } = useAuth();

  const { data: stats, isLoading: loadingStats } = useMyStats();
  const { data: recentHistory } = useMyWatchHistory(1, 100);

  if (!mounted) {
    return (
      <div className="min-h-screen pt-28 pb-20 px-4 max-w-6xl mx-auto">
        <div className="h-10 w-64 bg-zinc-800 rounded animate-pulse mb-8" />
        <div className="grid grid-cols-2 sm:grid-cols-4 gap-4">
          {[1, 2, 3, 4].map((i) => (
            <div key={i} className="h-28 bg-[#131318] rounded-xl animate-pulse" />
          ))}
        </div>
      </div>
    );
  }

  if (!authed) {
    return (
      <div className="min-h-[70vh] pt-28 pb-20 px-4 max-w-md mx-auto flex flex-col items-center justify-center text-center">
        <div className="w-16 h-16 rounded-full bg-amber-500/10 border border-amber-500/20 text-amber-500 flex items-center justify-center mb-4">
          <BarChart3 className="w-8 h-8" />
        </div>
        <h1 className="text-2xl font-serif font-bold text-white mb-2">
          Watch Analytics & Insights
        </h1>
        <p className="text-sm text-muted-foreground mb-6">
          Track your hours, favorite genres, viewing streaks, and watch patterns over time.
        </p>
        <Link href="/login?from=/stats">
          <Button className="bg-rose-600 hover:bg-rose-500 text-white font-medium px-6 gap-2">
            <LogIn className="w-4 h-4" />
            <span>Sign in to view Stats</span>
          </Button>
        </Link>
      </div>
    );
  }

  if (loadingStats) {
    return (
      <div className="min-h-screen pt-28 pb-24 px-4 sm:px-6 lg:px-8 max-w-6xl mx-auto space-y-6">
        <div className="h-10 w-72 bg-zinc-800 rounded animate-pulse" />
        <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
          {[1, 2, 3, 4].map((i) => (
            <div key={i} className="h-32 bg-[#131318] border border-border/20 rounded-xl animate-pulse" />
          ))}
        </div>
      </div>
    );
  }

  const totalWatches =
    stats?.totalWatches ??
    (stats?.totalMovies ?? 0) + (stats?.totalTvShows ?? 0);

  // Parse location data for PieChart
  const locationData = Object.entries(stats?.byLocation || {})
    .filter(([, count]) => (count as number) > 0)
    .map(([loc, count]) => ({
      name: LOCATION_LABELS[loc] || loc,
      rawKey: loc,
      value: count as number,
      color: LOCATION_COLORS[loc] || "#94a3b8",
    }));

  // Genre data formatted for BarChart
  const genreData = (stats?.topGenres || []).slice(0, 7).map((g) => ({
    name: g.genre,
    count: g.count,
  }));

  // Activity Heatmap generation (Last 16 weeks / ~112 days)
  const today = new Date();
  const daysGrid: { dateStr: string; count: number; date: Date }[] = [];
  const logDatesMap: Record<string, number> = {};

  if (recentHistory?.content) {
    for (const item of recentHistory.content) {
      const d = item.watchedAt.slice(0, 10);
      logDatesMap[d] = (logDatesMap[d] || 0) + 1;
    }
  }

  for (let i = 111; i >= 0; i--) {
    const d = new Date(today);
    d.setDate(d.getDate() - i);
    const dateStr = d.toISOString().slice(0, 10);
    daysGrid.push({
      dateStr,
      count: logDatesMap[dateStr] || 0,
      date: d,
    });
  }

  return (
    <div className="min-h-screen pt-28 pb-24 px-4 sm:px-6 lg:px-8 max-w-6xl mx-auto space-y-8">
      {/* Page Header */}
      <div className="flex flex-col sm:flex-row sm:items-end justify-between gap-4 pb-6 border-b border-border/20">
        <div>
          <div className="flex items-center gap-2 mb-1">
            <span className="text-xs uppercase font-mono tracking-widest text-amber-400">
              Analytics Profile
            </span>
            {currentUser?.username && (
              <span className="text-xs font-mono text-zinc-500">
                @{currentUser.username}
              </span>
            )}
          </div>
          <h1 className="text-3xl sm:text-4xl font-serif font-bold text-white tracking-tight">
            Watching Statistics
          </h1>
          <p className="text-xs sm:text-sm text-muted-foreground mt-1">
            Comprehensive breakdown of your viewing habits, time invested, and taste profile.
          </p>
        </div>

        <Link href="/watchlog">
          <Button
            variant="outline"
            size="sm"
            className="text-xs border-border/40 hover:bg-white/5 gap-1.5"
          >
            <span>View Watch History</span>
            <ArrowUpRight className="w-3.5 h-3.5 text-rose-400" />
          </Button>
        </Link>
      </div>

      {/* Hero Stats Metric Grid */}
      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
        {/* Total Titles */}
        <motion.div
          initial={{ opacity: 0, y: 10 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.3 }}
          className="p-5 rounded-xl bg-[#131318] border border-border/30 relative overflow-hidden"
        >
          <div className="flex items-center justify-between text-muted-foreground mb-3">
            <span className="text-xs font-medium uppercase tracking-wider">
              Total Logged
            </span>
            <Film className="w-4 h-4 text-rose-400" />
          </div>
          <div className="text-3xl sm:text-4xl font-serif font-bold text-white mb-1">
            {totalWatches}
          </div>
          <div className="text-[11px] text-zinc-400 flex items-center gap-2">
            <span>{stats?.totalMovies ?? 0} movies</span>
            <span>•</span>
            <span>{stats?.totalTvShows ?? 0} TV shows</span>
          </div>
        </motion.div>

        {/* Watch Time */}
        <motion.div
          initial={{ opacity: 0, y: 10 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.3, delay: 0.05 }}
          className="p-5 rounded-xl bg-[#131318] border border-border/30 relative overflow-hidden"
        >
          <div className="flex items-center justify-between text-muted-foreground mb-3">
            <span className="text-xs font-medium uppercase tracking-wider">
              Time Watched
            </span>
            <Clock className="w-4 h-4 text-amber-400" />
          </div>
          <div className="text-2xl sm:text-3xl font-serif font-bold text-amber-400 mb-1">
            {stats?.formattedWatchTime || stats?.totalWatchTimeFormatted || "0m"}
          </div>
          <div className="text-[11px] text-zinc-400">
            {stats?.totalWatchTimeMinutes
              ? `${stats.totalWatchTimeMinutes.toLocaleString()} total minutes`
              : "0 minutes"}
          </div>
        </motion.div>

        {/* Average Rating */}
        <motion.div
          initial={{ opacity: 0, y: 10 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.3, delay: 0.1 }}
          className="p-5 rounded-xl bg-[#131318] border border-border/30 relative overflow-hidden"
        >
          <div className="flex items-center justify-between text-muted-foreground mb-3">
            <span className="text-xs font-medium uppercase tracking-wider">
              Average Score
            </span>
            <Star className="w-4 h-4 text-amber-400 fill-amber-400" />
          </div>
          <div className="text-3xl sm:text-4xl font-serif font-bold text-white mb-1 flex items-baseline gap-1">
            {stats?.averageRating ? stats.averageRating.toFixed(1) : "—"}
            <span className="text-xs font-normal text-muted-foreground font-sans">
              / 10
            </span>
          </div>
          <div className="text-[11px] text-zinc-400">
            {stats?.ratedCount ? `${stats.ratedCount} rated titles` : "No rated titles"}
          </div>
        </motion.div>

        {/* Streaks */}
        <motion.div
          initial={{ opacity: 0, y: 10 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.3, delay: 0.15 }}
          className="p-5 rounded-xl bg-[#131318] border border-border/30 relative overflow-hidden"
        >
          <div className="flex items-center justify-between text-muted-foreground mb-3">
            <span className="text-xs font-medium uppercase tracking-wider">
              Watch Streak
            </span>
            <Flame className="w-4 h-4 text-orange-500 fill-orange-500" />
          </div>
          <div className="text-3xl sm:text-4xl font-serif font-bold text-orange-400 mb-1 flex items-baseline gap-1">
            {stats?.currentStreak ?? stats?.watchStreak ?? 0}
            <span className="text-xs font-normal text-muted-foreground font-sans">
              days
            </span>
          </div>
          <div className="text-[11px] text-zinc-400 flex items-center gap-1.5">
            <Trophy className="w-3 h-3 text-amber-400" />
            <span>Best: {stats?.longestStreak ?? 0} days</span>
          </div>
        </motion.div>
      </div>

      {/* Charts Section: Genres & Locations */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Top Genres Bar Chart (2 columns) */}
        <Card className="lg:col-span-2 bg-[#131318] border-border/30 text-foreground">
          <CardHeader className="pb-2">
            <div className="flex items-center justify-between">
              <div>
                <CardTitle className="text-base sm:text-lg font-serif font-bold text-white flex items-center gap-2">
                  <BarChart3 className="w-4 h-4 text-rose-500" />
                  Top Genres
                </CardTitle>
                <p className="text-xs text-muted-foreground mt-0.5">
                  Most frequently logged genres across your watch history.
                </p>
              </div>
            </div>
          </CardHeader>
          <CardContent className="pt-4">
            {genreData.length > 0 ? (
              <div className="h-64 w-full">
                <ResponsiveContainer width="100%" height="100%">
                  <BarChart
                    data={genreData}
                    layout="vertical"
                    margin={{ top: 5, right: 20, left: 20, bottom: 5 }}
                  >
                    <XAxis type="number" stroke="#52525b" fontSize={11} allowDecimals={false} />
                    <YAxis
                      type="category"
                      dataKey="name"
                      stroke="#a1a1aa"
                      fontSize={11}
                      width={100}
                      tickLine={false}
                    />
                    <Tooltip
                      contentStyle={{
                        backgroundColor: "#18181f",
                        borderColor: "#27272a",
                        borderRadius: "8px",
                        fontSize: "12px",
                        color: "#fff",
                      }}
                      formatter={(val: unknown) => [`${val} titles`, "Logged"]}
                    />
                    <Bar
                      dataKey="count"
                      fill="#e11d48"
                      radius={[0, 4, 4, 0]}
                    />
                  </BarChart>
                </ResponsiveContainer>
              </div>
            ) : (
              <div className="h-64 flex flex-col items-center justify-center text-center text-muted-foreground">
                <p className="text-xs">No genre statistics available yet.</p>
              </div>
            )}
          </CardContent>
        </Card>

        {/* Viewing Location Donut Chart (1 column) */}
        <Card className="bg-[#131318] border-border/30 text-foreground">
          <CardHeader className="pb-2">
            <CardTitle className="text-base sm:text-lg font-serif font-bold text-white flex items-center gap-2">
              <PieChartIcon className="w-4 h-4 text-amber-400" />
              Watch Locations
            </CardTitle>
            <p className="text-xs text-muted-foreground mt-0.5">
              Where you experience your stories.
            </p>
          </CardHeader>
          <CardContent className="pt-2">
            {locationData.length > 0 ? (
              <div className="flex flex-col items-center">
                <div className="h-44 w-full">
                  <ResponsiveContainer width="100%" height="100%">
                    <PieChart>
                      <Pie
                        data={locationData}
                        cx="50%"
                        cy="50%"
                        innerRadius={45}
                        outerRadius={70}
                        paddingAngle={4}
                        dataKey="value"
                      >
                        {locationData.map((entry, index) => (
                          <Cell key={`cell-${index}`} fill={entry.color} />
                        ))}
                      </Pie>
                      <Tooltip
                        contentStyle={{
                          backgroundColor: "#18181f",
                          borderColor: "#27272a",
                          borderRadius: "8px",
                          fontSize: "12px",
                        }}
                      />
                    </PieChart>
                  </ResponsiveContainer>
                </div>

                <div className="w-full grid grid-cols-2 gap-2 mt-2 pt-2 border-t border-border/20">
                  {locationData.map((loc) => (
                    <div key={loc.name} className="flex items-center gap-2 text-xs">
                      <span
                        className="w-2.5 h-2.5 rounded-full shrink-0"
                        style={{ backgroundColor: loc.color }}
                      />
                      <span className="text-zinc-300 truncate">{loc.name}</span>
                      <span className="font-mono text-zinc-500 ml-auto">
                        {loc.value}
                      </span>
                    </div>
                  ))}
                </div>
              </div>
            ) : (
              <div className="h-64 flex flex-col items-center justify-center text-center text-muted-foreground">
                <p className="text-xs">No location logs recorded yet.</p>
              </div>
            )}
          </CardContent>
        </Card>
      </div>

      {/* Secondary Highlights: Heatmap & Periodic Comparisons */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Activity Heatmap Grid (2 cols) */}
        <Card className="lg:col-span-2 bg-[#131318] border-border/30 text-foreground">
          <CardHeader className="pb-3">
            <div className="flex items-center justify-between">
              <div>
                <CardTitle className="text-base sm:text-lg font-serif font-bold text-white flex items-center gap-2">
                  <Calendar className="w-4 h-4 text-emerald-400" />
                  Viewing Frequency (Recent 16 Weeks)
                </CardTitle>
                <p className="text-xs text-muted-foreground mt-0.5">
                  Daily visual activity map of logged watches.
                </p>
              </div>
              <div className="flex items-center gap-1.5 text-[10px] text-zinc-400">
                <span>Less</span>
                <span className="w-2.5 h-2.5 rounded-xs bg-[#1f1f28]" />
                <span className="w-2.5 h-2.5 rounded-xs bg-emerald-900/60" />
                <span className="w-2.5 h-2.5 rounded-xs bg-emerald-600" />
                <span className="w-2.5 h-2.5 rounded-xs bg-emerald-400" />
                <span>More</span>
              </div>
            </div>
          </CardHeader>
          <CardContent>
            <div className="overflow-x-auto pb-2">
              <div className="grid grid-rows-7 grid-flow-col gap-1.5 min-w-[500px]">
                {daysGrid.map((day) => {
                  let bg = "bg-[#1c1c24]";
                  if (day.count === 1) bg = "bg-emerald-800";
                  else if (day.count === 2) bg = "bg-emerald-600";
                  else if (day.count >= 3) bg = "bg-emerald-400";

                  return (
                    <div
                      key={day.dateStr}
                      title={`${day.dateStr}: ${day.count} logged`}
                      className={cn(
                        "w-3 h-3 rounded-xs transition-colors hover:scale-125 cursor-pointer",
                        bg
                      )}
                    />
                  );
                })}
              </div>
            </div>
          </CardContent>
        </Card>

        {/* Periodic Insights & Favorite Decade (1 col) */}
        <Card className="bg-[#131318] border-border/30 text-foreground flex flex-col justify-between">
          <CardHeader className="pb-3">
            <CardTitle className="text-base sm:text-lg font-serif font-bold text-white flex items-center gap-2">
              <Compass className="w-4 h-4 text-rose-400" />
              Pace & Era
            </CardTitle>
            <p className="text-xs text-muted-foreground mt-0.5">
              Period breakdown and favored film era.
            </p>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="flex items-center justify-between p-3 rounded-lg bg-background/40 border border-border/20">
              <span className="text-xs text-zinc-400">This Month</span>
              <span className="text-lg font-serif font-bold text-white">
                {stats?.thisMonthWatches ?? stats?.thisMonth?.movies ?? 0}{" "}
                <span className="text-xs font-sans font-normal text-muted-foreground">
                  titles
                </span>
              </span>
            </div>

            <div className="flex items-center justify-between p-3 rounded-lg bg-background/40 border border-border/20">
              <span className="text-xs text-zinc-400">This Year</span>
              <span className="text-lg font-serif font-bold text-white">
                {stats?.thisYearWatches ?? stats?.thisYear?.movies ?? 0}{" "}
                <span className="text-xs font-sans font-normal text-muted-foreground">
                  titles
                </span>
              </span>
            </div>

            <div className="flex items-center justify-between p-3 rounded-lg bg-background/40 border border-border/20">
              <span className="text-xs text-zinc-400">Favorite Decade</span>
              <span className="text-sm font-mono font-bold text-amber-400">
                {stats?.favoriteDecade || "2020s"}
              </span>
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  );
}
