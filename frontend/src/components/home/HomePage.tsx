"use client";

import React, { useEffect, useState } from "react";
import Link from "next/link";
import Image from "next/image";
import { useRouter } from "next/navigation";
import {
  Sparkles,
  Film,
  Tv,
  Zap,
  Flame,
  Shuffle,
  ChevronRight,
} from "lucide-react";
import { motion, useScroll, useTransform } from "framer-motion";
import { Button } from "@/components/ui/button";
import { SearchBar } from "@/components/layout/SearchBar";
import { MovieRow } from "@/components/movie/MovieRow";
import {
  useTrendingIndia,
  useTrendingGlobal,
  useMoviesByGenre,
} from "@/lib/hooks/useMovies";
import { buildBackdropUrl } from "@/lib/utils";

// Animated counter hook
function useCountUp(target: number, durationMs = 1500) {
  const [count, setCount] = useState(0);

  useEffect(() => {
    if (!target) return;
    let start = 0;
    const step = Math.max(1, Math.floor(target / (durationMs / 30)));
    const timer = setInterval(() => {
      start += step;
      if (start >= target) {
        setCount(target);
        clearInterval(timer);
      } else {
        setCount(start);
      }
    }, 30);

    return () => clearInterval(timer);
  }, [target, durationMs]);

  return count;
}

function pickRandomMovie(list: { tmdbId: number }[]) {
  if (!list || list.length === 0) return 27205; // Fallback to Inception
  const randomIndex = Math.floor(Math.random() * list.length);
  return list[randomIndex].tmdbId;
}

export function HomePage() {
  const router = useRouter();

  // 1. Data queries — Indian Trending, Global Trending, Action, Drama, Romance, Kids/Animation, Crime
  const trendingIndiaQuery = useTrendingIndia(1);
  const trendingGlobalQuery = useTrendingGlobal(1);
  const actionQuery = useMoviesByGenre("Action", 1);
  const dramaQuery = useMoviesByGenre("Drama", 1);
  const romanceQuery = useMoviesByGenre("Romance", 1);
  const kidsQuery = useMoviesByGenre("Animation", 1);
  const crimeQuery = useMoviesByGenre("Crime", 1);

  // Client-side dedup across rows for distinct discovery
  const seenIds = new Set<number>();
  const trendingIndiaMovies = (trendingIndiaQuery.data?.content || []).filter((m) => {
    seenIds.add(m.tmdbId);
    return true;
  });
  const trendingGlobalMovies = (trendingGlobalQuery.data?.content || []).filter((m) => {
    if (seenIds.has(m.tmdbId)) return false;
    seenIds.add(m.tmdbId);
    return true;
  });
  const actionMovies = (actionQuery.data?.content || []).filter((m) => {
    if (seenIds.has(m.tmdbId)) return false;
    seenIds.add(m.tmdbId);
    return true;
  });
  const dramaMovies = (dramaQuery.data?.content || []).filter((m) => {
    if (seenIds.has(m.tmdbId)) return false;
    seenIds.add(m.tmdbId);
    return true;
  });
  const kidsMovies = (kidsQuery.data?.content || []).filter((m) => {
    if (seenIds.has(m.tmdbId)) return false;
    seenIds.add(m.tmdbId);
    return true;
  });
  const romanceMovies = (romanceQuery.data?.content || []).filter((m) => {
    if (seenIds.has(m.tmdbId)) return false;
    seenIds.add(m.tmdbId);
    return true;
  });
  const crimeMovies = (crimeQuery.data?.content || []).filter((m) => {
    if (seenIds.has(m.tmdbId)) return false;
    seenIds.add(m.tmdbId);
    return true;
  });

  const animatedMovies = useCountUp(1672779);
  const animatedTv = useCountUp(164705);

  // POLISH 2 (item 6): Hero section subtle parallax on scroll
  const { scrollY } = useScroll();
  const heroY = useTransform(scrollY, [0, 500], [0, 150]);

  // Backdrop for hero from trending items
  const heroMovie = trendingGlobalMovies[0] || trendingIndiaMovies[0];
  const heroBackdrop = heroMovie
    ? heroMovie.backdropUrl || heroMovie.backdropPath
    : null;

  const handleSurpriseMe = () => {
    const list = [...trendingIndiaMovies, ...trendingGlobalMovies];
    const targetId = pickRandomMovie(list);
    router.push(`/movie/${targetId}`);
  };

  const scrollToTrending = () => {
    const el = document.getElementById("trending-section");
    el?.scrollIntoView({ behavior: "smooth" });
  };

  const popularGenreList = [
    { name: "Action", color: "from-red-600 to-rose-900" },
    { name: "Drama", color: "from-blue-600 to-indigo-900" },
    { name: "Comedy", color: "from-amber-500 to-orange-800" },
    { name: "Sci-Fi", color: "from-purple-600 to-violet-950" },
    { name: "Thriller", color: "from-emerald-600 to-teal-950" },
    { name: "Animation", color: "from-pink-500 to-rose-800" },
    { name: "Horror", color: "from-zinc-700 to-black" },
    { name: "Romance", color: "from-rose-500 to-pink-900" },
    { name: "Crime", color: "from-yellow-600 to-amber-950" },
    { name: "Adventure", color: "from-cyan-600 to-blue-950" },
    { name: "Mystery", color: "from-indigo-600 to-purple-950" },
    { name: "Fantasy", color: "from-fuchsia-600 to-pink-950" },
  ];

  return (
    <div className="flex flex-col min-h-screen">
      {/* Hero Section */}
      <section className="relative w-full pt-28 pb-16 md:pt-40 md:pb-28 overflow-hidden border-b border-white/5">
        {/* Dynamic Backdrop or Ambient Glow with Parallax */}
        {heroBackdrop && (
          <motion.div
            style={{ y: heroY }}
            className="absolute inset-0 -z-10 opacity-20 filter blur-sm"
          >
            <Image
              src={buildBackdropUrl(heroBackdrop, "w1280")}
              alt="Hero Backdrop"
              fill
              priority
              className="object-cover object-top"
            />
          </motion.div>
        )}
        <div className="absolute inset-0 bg-gradient-to-b from-[#0a0a0f]/80 via-[#0a0a0f]/90 to-[#0a0a0f] pointer-events-none" />
        <div className="absolute top-0 left-1/2 -translate-x-1/2 w-[1100px] h-[550px] bg-gradient-to-b from-rose-600/15 via-rose-950/10 to-transparent blur-3xl pointer-events-none rounded-full" />

        <div className="relative max-w-5xl mx-auto px-4 sm:px-6 lg:px-8 text-center flex flex-col items-center">
          {/* Badge */}
          <motion.div
            initial={{ opacity: 0, y: -10 }}
            animate={{ opacity: 1, y: 0 }}
            className="inline-flex items-center gap-2 px-3.5 py-1.5 rounded-full bg-white/5 border border-white/10 text-xs font-semibold text-rose-400 mb-6 shadow-sm backdrop-blur-md"
          >
            <Sparkles className="w-3.5 h-3.5 text-rose-500" />
            <span>1.67 Million Titles Indexed in Local MongoDB</span>
          </motion.div>

          {/* Headline */}
          <motion.h1
            initial={{ opacity: 0, y: 15 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: 0.1 }}
            className="text-4xl sm:text-6xl lg:text-7xl font-extrabold text-white tracking-tight leading-[1.1] mb-6 font-[family-name:var(--font-playfair)]"
          >
            Discover Your Next{" "}
            <span className="text-transparent bg-clip-text bg-gradient-to-r from-rose-400 via-rose-500 to-amber-400">
              Favorite Film
            </span>
          </motion.h1>

          {/* Subtitle */}
          <motion.p
            initial={{ opacity: 0, y: 15 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: 0.2 }}
            className="text-base sm:text-xl text-zinc-400 max-w-2xl mb-8 leading-relaxed"
          >
            Browse 1.6 million movies and TV shows — completely local,
            lightning fast, with zero TMDB ISP rate limits.
          </motion.p>

          {/* SearchBar */}
          <motion.div
            initial={{ opacity: 0, scale: 0.96 }}
            animate={{ opacity: 1, scale: 1 }}
            transition={{ delay: 0.25 }}
            className="w-full max-w-2xl mb-8"
          >
            <SearchBar
              placeholder="Search 1.6M+ movies, TV shows, actors, directors..."
              className="max-w-none shadow-2xl scale-100 sm:scale-105"
            />
          </motion.div>

          {/* Action Buttons */}
          <motion.div
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            transition={{ delay: 0.3 }}
            className="flex flex-wrap items-center justify-center gap-3 mb-12"
          >
            <Button
              onClick={scrollToTrending}
              className="bg-rose-600 hover:bg-rose-700 text-white gap-2 rounded-full px-6 h-10 shadow-lg shadow-rose-950/40"
            >
              <Flame className="w-4 h-4" />
              <span>Browse Trending</span>
            </Button>
            <Button
              onClick={handleSurpriseMe}
              variant="outline"
              className="border-white/15 bg-white/5 hover:bg-white/10 text-white gap-2 rounded-full px-6 h-10"
            >
              <Shuffle className="w-4 h-4 text-amber-400" />
              <span>Surprise Me</span>
            </Button>
          </motion.div>

          {/* Live Catalog Stats */}
          <div className="grid grid-cols-3 gap-4 sm:gap-8 pt-8 border-t border-white/10 w-full max-w-2xl">
            <div className="flex flex-col items-center">
              <div className="flex items-center gap-1.5 text-xl sm:text-2xl font-bold text-white font-mono">
                <Film className="w-4 h-4 text-rose-500 hidden sm:block" />
                <span>{animatedMovies.toLocaleString()}</span>
              </div>
              <span className="text-xs text-zinc-400 mt-1">Movies in MongoDB</span>
            </div>

            <div className="flex flex-col items-center">
              <div className="flex items-center gap-1.5 text-xl sm:text-2xl font-bold text-white font-mono">
                <Tv className="w-4 h-4 text-amber-500 hidden sm:block" />
                <span>{animatedTv.toLocaleString()}</span>
              </div>
              <span className="text-xs text-zinc-400 mt-1">TV Shows Indexed</span>
            </div>

            <div className="flex flex-col items-center">
              <div className="flex items-center gap-1.5 text-xl sm:text-2xl font-bold text-emerald-400 font-mono">
                <Zap className="w-4 h-4 text-emerald-400 hidden sm:block" />
                <span>0 ms</span>
              </div>
              <span className="text-xs text-zinc-400 mt-1">TMDB Dependency</span>
            </div>
          </div>
        </div>
      </section>

      {/* Main Content Movie Rows */}
      <main className="max-w-7xl mx-auto w-full py-8 space-y-8">
        {/* Quick Category Filter Chips */}
        <div className="px-4 sm:px-6 lg:px-8">
          <div className="flex items-center gap-2 overflow-x-auto pb-2 scrollbar-none">
            <span className="text-xs font-semibold uppercase text-zinc-400 tracking-wider shrink-0 mr-1">
              Quick Filter:
            </span>
            <Link
              href="/search?sort=popularity"
              className="px-3.5 py-1.5 rounded-full text-xs font-semibold bg-rose-600 text-white shrink-0 shadow-md shadow-rose-950/40 hover:bg-rose-500 transition-colors"
            >
              Trending Now 🔥
            </Link>
            <Link
              href="/genre/Drama"
              className="px-3.5 py-1.5 rounded-full text-xs font-semibold bg-white/5 hover:bg-white/10 text-zinc-300 border border-white/10 shrink-0 transition-colors"
            >
              Drama 🎭
            </Link>
            <Link
              href="/genre/Animation"
              className="px-3.5 py-1.5 rounded-full text-xs font-semibold bg-white/5 hover:bg-white/10 text-zinc-300 border border-white/10 shrink-0 transition-colors"
            >
              Kids & Animation 🎈
            </Link>
            <Link
              href="/genre/Romance"
              className="px-3.5 py-1.5 rounded-full text-xs font-semibold bg-white/5 hover:bg-white/10 text-zinc-300 border border-white/10 shrink-0 transition-colors"
            >
              Romance 💕
            </Link>
            <Link
              href="/genre/Crime"
              className="px-3.5 py-1.5 rounded-full text-xs font-semibold bg-white/5 hover:bg-white/10 text-zinc-300 border border-white/10 shrink-0 transition-colors"
            >
              Crime & Mystery 🕵️‍♂️
            </Link>
            <Link
              href="/genre/Action"
              className="px-3.5 py-1.5 rounded-full text-xs font-semibold bg-white/5 hover:bg-white/10 text-zinc-300 border border-white/10 shrink-0 transition-colors"
            >
              Action 💥
            </Link>
            <Link
              href="/genre/Comedy"
              className="px-3.5 py-1.5 rounded-full text-xs font-semibold bg-white/5 hover:bg-white/10 text-zinc-300 border border-white/10 shrink-0 transition-colors"
            >
              Comedy 😂
            </Link>
            <Link
              href="/genre/Science%20Fiction"
              className="px-3.5 py-1.5 rounded-full text-xs font-semibold bg-white/5 hover:bg-white/10 text-zinc-300 border border-white/10 shrink-0 transition-colors"
            >
              Sci-Fi 🚀
            </Link>
            <Link
              href="/genre/Thriller"
              className="px-3.5 py-1.5 rounded-full text-xs font-semibold bg-white/5 hover:bg-white/10 text-zinc-300 border border-white/10 shrink-0 transition-colors"
            >
              Thriller ⚡
            </Link>
          </div>
        </div>

        {/* Row 1: Trending in India */}
        <div id="trending-section">
          <MovieRow
            title="Trending in India 🇮🇳"
            seeAllHref="/search?q=india"
            movies={trendingIndiaMovies}
            loading={trendingIndiaQuery.isLoading}
            error={trendingIndiaQuery.error}
            onRetry={() => trendingIndiaQuery.refetch()}
          />
        </div>

        {/* Row 2: Global Trending */}
        <MovieRow
          title="Global Trending 🌍"
          seeAllHref="/search?sort=popularity"
          movies={trendingGlobalMovies}
          loading={trendingGlobalQuery.isLoading}
          error={trendingGlobalQuery.error}
          onRetry={() => trendingGlobalQuery.refetch()}
        />

        {/* Row 3: Kids & Family Animation */}
        <MovieRow
          title="Kids & Animation 🎈"
          seeAllHref="/genre/Animation"
          movies={kidsMovies}
          loading={kidsQuery.isLoading}
          error={kidsQuery.error}
          onRetry={() => kidsQuery.refetch()}
        />

        {/* Row 4: Romance & Love Stories */}
        <MovieRow
          title="Romance & Love Stories 💕"
          seeAllHref="/genre/Romance"
          movies={romanceMovies}
          loading={romanceQuery.isLoading}
          error={romanceQuery.error}
          onRetry={() => romanceQuery.refetch()}
        />

        {/* Row 5: Crime & Mystery Thrillers */}
        <MovieRow
          title="Crime & Mystery 🕵️‍♂️"
          seeAllHref="/genre/Crime"
          movies={crimeMovies}
          loading={crimeQuery.isLoading}
          error={crimeQuery.error}
          onRetry={() => crimeQuery.refetch()}
        />

        {/* Row 6: Highly Rated Dramas */}
        <MovieRow
          title="Highly Rated Dramas 🎭"
          seeAllHref="/genre/Drama"
          movies={dramaMovies}
          loading={dramaQuery.isLoading}
          error={dramaQuery.error}
          onRetry={() => dramaQuery.refetch()}
        />

        {/* Row 7: Popular in Action */}
        <MovieRow
          title="Popular in Action 💥"
          seeAllHref="/genre/Action"
          movies={actionMovies}
          loading={actionQuery.isLoading}
          error={actionQuery.error}
          onRetry={() => actionQuery.refetch()}
        />

        {/* Visual Genre Grid */}
        <section className="px-4 sm:px-6 lg:px-8 py-10 border-t border-white/5">
          <div className="flex items-center justify-between mb-6">
            <div>
              <h2 className="text-2xl font-bold tracking-tight text-white font-[family-name:var(--font-playfair)]">
                Explore by Genre
              </h2>
              <p className="text-sm text-zinc-400 mt-1">
                Dive deep into specific categories across the entire catalog
              </p>
            </div>
          </div>

          <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-6 gap-3 sm:gap-4">
            {popularGenreList.map((g) => (
              <Link
                key={g.name}
                href={`/genre/${encodeURIComponent(g.name)}`}
                className={`relative group flex flex-col justify-between p-4 rounded-xl bg-gradient-to-br ${g.color} border border-white/10 hover:border-white/30 shadow-lg hover:scale-105 transition-all duration-300 min-h-[90px]`}
              >
                <div className="flex items-center justify-between">
                  <span className="text-xs uppercase font-bold tracking-wider text-white/80">
                    Genre
                  </span>
                  <ChevronRight className="w-4 h-4 text-white/60 group-hover:text-white group-hover:translate-x-1 transition-transform" />
                </div>
                <span className="text-base font-bold text-white tracking-wide">
                  {g.name}
                </span>
              </Link>
            ))}
          </div>
        </section>
      </main>
    </div>
  );
}
