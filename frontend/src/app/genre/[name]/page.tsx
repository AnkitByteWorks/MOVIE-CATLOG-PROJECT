"use client";

import React, { use } from "react";
import Link from "next/link";
import { ArrowLeft, Sparkles } from "lucide-react";
import { PosterCard } from "@/components/movie/PosterCard";
import { PosterCardSkeleton } from "@/components/common/Skeletons";
import { EmptyState } from "@/components/common/EmptyState";
import { useMoviesByGenre } from "@/lib/hooks/useMovies";

const GENRE_LIST = [
  { name: "Drama", label: "Drama 🎭" },
  { name: "Animation", label: "Kids & Family 🎈" },
  { name: "Romance", label: "Romance 💕" },
  { name: "Crime", label: "Crime & Mystery 🕵️‍♂️" },
  { name: "Action", label: "Action 💥" },
  { name: "Comedy", label: "Comedy 😂" },
  { name: "Science Fiction", label: "Sci-Fi 🚀" },
  { name: "Thriller", label: "Thriller ⚡" },
  { name: "Horror", label: "Horror 👻" },
];

export default function GenrePage({
  params,
}: {
  params: Promise<{ name: string }>;
}) {
  const { name } = use(params);
  const genreName = decodeURIComponent(name);

  const { data, isLoading, error, refetch } = useMoviesByGenre(genreName, 1);
  const movies = data?.content || [];

  return (
    <div className="min-h-screen pt-24 pb-20 px-4 sm:px-6 lg:px-8 max-w-7xl mx-auto w-full">
      {/* Header */}
      <div className="flex flex-col gap-3 mb-8">
        <div className="flex items-center gap-2">
          <Link
            href="/"
            className="flex items-center gap-1.5 text-xs text-zinc-400 hover:text-white transition-colors"
          >
            <ArrowLeft className="w-4 h-4" />
            <span>Back to Catalog</span>
          </Link>
        </div>

        <div className="flex items-center gap-2 text-rose-500 text-xs font-semibold uppercase tracking-wider">
          <Sparkles className="w-3.5 h-3.5" />
          <span>Genre Explorer</span>
        </div>

        <h1 className="text-3xl sm:text-5xl font-extrabold text-white tracking-tight font-[family-name:var(--font-playfair)]">
          {genreName} Movies
        </h1>
        <p className="text-zinc-400 text-sm sm:text-base max-w-xl">
          Discover all titles tagged with {genreName} across the 1.67M title local catalog.
        </p>

        {/* Quick Genre Switcher */}
        <div className="flex items-center gap-1.5 overflow-x-auto pb-2 pt-2 scrollbar-none border-t border-white/5">
          {GENRE_LIST.map((g) => (
            <Link
              key={g.name}
              href={`/genre/${encodeURIComponent(g.name)}`}
              className={`px-3 py-1 rounded-full text-xs font-semibold whitespace-nowrap transition-all ${
                genreName.toLowerCase() === g.name.toLowerCase()
                  ? "bg-rose-600 text-white shadow-sm shadow-rose-950/40"
                  : "bg-white/5 hover:bg-white/10 text-zinc-300 border border-white/10"
              }`}
            >
              {g.label}
            </Link>
          ))}
        </div>
      </div>

      {/* Grid */}
      {isLoading ? (
        <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 2xl:grid-cols-6 gap-4 sm:gap-6">
          {Array.from({ length: 10 }).map((_, i) => (
            <PosterCardSkeleton key={i} />
          ))}
        </div>
      ) : error ? (
        <EmptyState
          title={`Error loading ${genreName} movies`}
          description="Could not connect to the movie catalog service."
          actionLabel="Try Again"
          onAction={() => refetch()}
        />
      ) : movies.length > 0 ? (
        <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 2xl:grid-cols-6 gap-4 sm:gap-6">
          {movies.map((m, idx) => (
            <PosterCard key={m.tmdbId} movie={m} index={idx} priority={idx < 5} />
          ))}
        </div>
      ) : (
        <EmptyState
          title={`No movies in this genre yet`}
          description="Try exploring other genres from the home page."
          actionLabel="Browse Popular"
          actionHref="/"
        />
      )}
    </div>
  );
}
