"use client";

import React, { useState, useEffect, useRef, useMemo } from "react";
import { useSearchParams, useRouter, usePathname } from "next/navigation";
import { useInfiniteQuery } from "@tanstack/react-query";
import {
  Search,
  SearchX,
  Film,
  Tv,
  Layers,
  Loader2,
  SlidersHorizontal,
  X,
} from "lucide-react";
import { PosterCard } from "@/components/movie/PosterCard";
import { PosterCardSkeleton } from "@/components/common/Skeletons";
import { EmptyState } from "@/components/common/EmptyState";
import { searchMovies, getTrendingMovies, getMoviesByGenre } from "@/lib/api/movies";
import { searchTvShows, getTrendingTvShows } from "@/lib/api/tv";
import { useDebounce } from "@/lib/hooks/useDebounce";
import { MovieSummary, TvShowSummary } from "@/lib/types";

const GENRE_OPTIONS = [
  { id: "all", label: "All Genres" },
  { id: "Drama", label: "Drama 🎭" },
  { id: "Animation", label: "Kids & Family 🎈" },
  { id: "Romance", label: "Romance 💕" },
  { id: "Crime", label: "Crime & Mystery 🕵️‍♂️" },
  { id: "Action", label: "Action 💥" },
  { id: "Comedy", label: "Comedy 😂" },
  { id: "Science Fiction", label: "Sci-Fi 🚀" },
  { id: "Thriller", label: "Thriller ⚡" },
  { id: "Horror", label: "Horror 👻" },
];

export function SearchResults() {
  const searchParams = useSearchParams();
  const router = useRouter();
  const pathname = usePathname();

  const initialQuery = searchParams.get("q") || "";
  const initialType = (searchParams.get("type") || "all") as "all" | "movie" | "tv";
  const initialGenre = searchParams.get("genre") || "all";

  const [inputVal, setInputVal] = useState(initialQuery);
  const [mediaType, setMediaType] = useState<"all" | "movie" | "tv">(initialType);
  const [selectedGenre, setSelectedGenre] = useState<string>(initialGenre);
  const [sortBy, setSortBy] = useState<string>("relevance");

  const debouncedQuery = useDebounce(inputVal, 400);
  const sentinelRef = useRef<HTMLDivElement>(null);

  // Sync debounced input and filters with URL
  useEffect(() => {
    const params = new URLSearchParams(searchParams.toString());
    if (debouncedQuery.trim()) {
      params.set("q", debouncedQuery.trim());
    } else {
      params.delete("q");
    }
    if (mediaType !== "all") {
      params.set("type", mediaType);
    } else {
      params.delete("type");
    }
    if (selectedGenre !== "all") {
      params.set("genre", selectedGenre);
    } else {
      params.delete("genre");
    }
    const queryString = params.toString();
    router.replace(`${pathname}${queryString ? `?${queryString}` : ""}`);
  }, [debouncedQuery, mediaType, selectedGenre, pathname, router, searchParams]);

  // Infinite Query for Movies
  const moviesQuery = useInfiniteQuery({
    queryKey: ["search", "movies", debouncedQuery, selectedGenre, sortBy],
    queryFn: async ({ pageParam = 1 }) => {
      if (debouncedQuery.trim()) {
        return searchMovies({ query: debouncedQuery, page: pageParam });
      }
      if (selectedGenre !== "all") {
        return getMoviesByGenre(selectedGenre, pageParam);
      }
      return getTrendingMovies("week", pageParam);
    },
    initialPageParam: 1,
    getNextPageParam: (lastPage) =>
      lastPage.last || lastPage.page >= lastPage.totalPages
        ? undefined
        : lastPage.page + 1,
    enabled: mediaType === "all" || mediaType === "movie",
  });

  // Infinite Query for TV
  const tvQuery = useInfiniteQuery({
    queryKey: ["search", "tv", debouncedQuery, selectedGenre, sortBy],
    queryFn: async ({ pageParam = 1 }) => {
      if (debouncedQuery.trim()) {
        return searchTvShows({ query: debouncedQuery, page: pageParam });
      }
      return getTrendingTvShows("week", pageParam);
    },
    initialPageParam: 1,
    getNextPageParam: (lastPage) =>
      lastPage.last || lastPage.page >= lastPage.totalPages
        ? undefined
        : lastPage.page + 1,
    enabled: mediaType === "all" || mediaType === "tv",
  });

  // Combine results
  const allItems = useMemo(() => {
    let list: (MovieSummary | TvShowSummary)[] = [];

    if (mediaType === "movie" && moviesQuery.data) {
      list = moviesQuery.data.pages.flatMap((page) => page.content);
    } else if (mediaType === "tv" && tvQuery.data) {
      list = tvQuery.data.pages.flatMap((page) => page.content);
    } else if (mediaType === "all") {
      const mList = moviesQuery.data?.pages.flatMap((page) => page.content) || [];
      const tList = tvQuery.data?.pages.flatMap((page) => page.content) || [];
      const maxLen = Math.max(mList.length, tList.length);
      for (let i = 0; i < maxLen; i++) {
        if (mList[i]) list.push(mList[i]);
        if (tList[i]) list.push(tList[i]);
      }
    }

    // Filter by genre in search results
    if (selectedGenre !== "all" && debouncedQuery.trim()) {
      list = list.filter((item) => {
        if (!item.genres || item.genres.length === 0) return false;
        if (selectedGenre === "Animation") {
          return item.genres.some((g) => g === "Animation" || g === "Family");
        }
        if (selectedGenre === "Crime") {
          return item.genres.some((g) => g === "Crime" || g === "Mystery");
        }
        return item.genres.some((g) =>
          g.toLowerCase().includes(selectedGenre.toLowerCase())
        );
      });
    }

    // Sort if needed
    if (sortBy === "rating") {
      return [...list].sort((a, b) => (b.voteAverage || 0) - (a.voteAverage || 0));
    }
    if (sortBy === "releaseDate") {
      return [...list].sort((a, b) => {
        const dateA = "releaseDate" in a ? a.releaseDate : (a as TvShowSummary).firstAirDate;
        const dateB = "releaseDate" in b ? b.releaseDate : (b as TvShowSummary).firstAirDate;
        return (
          (dateB ? new Date(dateB).getTime() : 0) -
          (dateA ? new Date(dateA).getTime() : 0)
        );
      });
    }

    return list;
  }, [mediaType, moviesQuery.data, tvQuery.data, selectedGenre, debouncedQuery, sortBy]);

  // IntersectionObserver for infinite scroll sentinel
  const activeQuery = mediaType === "tv" ? tvQuery : moviesQuery;
  const { hasNextPage, isFetchingNextPage, fetchNextPage } = activeQuery;

  useEffect(() => {
    if (!sentinelRef.current || !hasNextPage || isFetchingNextPage) return;

    const observer = new IntersectionObserver(
      (entries) => {
        if (entries[0].isIntersecting) {
          fetchNextPage();
        }
      },
      { rootMargin: "300px" }
    );

    observer.observe(sentinelRef.current);
    return () => observer.disconnect();
  }, [hasNextPage, isFetchingNextPage, fetchNextPage]);

  const isLoadingInitial =
    (mediaType === "movie" && moviesQuery.isLoading) ||
    (mediaType === "tv" && tvQuery.isLoading) ||
    (mediaType === "all" && (moviesQuery.isLoading || tvQuery.isLoading));

  return (
    <div className="min-h-screen pt-24 pb-20 px-4 sm:px-6 lg:px-8 max-w-7xl mx-auto w-full">
      {/* Search Header */}
      <div className="flex flex-col gap-3 mb-8">
        <h1 className="text-3xl sm:text-5xl font-bold tracking-tight text-white font-[family-name:var(--font-playfair)]">
          Search Catalog
        </h1>
        {debouncedQuery.trim() ? (
          <p className="text-zinc-400 text-sm sm:text-base">
            <span className="font-semibold text-white">
              {(
                (mediaType === "tv"
                  ? tvQuery.data?.pages?.[0]?.totalElements
                  : moviesQuery.data?.pages?.[0]?.totalElements) || allItems.length
              ).toLocaleString()}{" "}
              results
            </span>{" "}
            for &ldquo;{debouncedQuery}&rdquo;
          </p>
        ) : (
          <p className="text-zinc-400 text-sm sm:text-base max-w-2xl">
            Instantly query over 1,508,074 movies and 164,705 TV shows in the local MongoDB catalog.
          </p>
        )}

        {/* Search Input Box */}
        <div className="relative max-w-2xl mt-2">
          <Search className="absolute left-4 top-1/2 -translate-y-1/2 w-5 h-5 text-zinc-400 pointer-events-none" />
          <input
            type="text"
            value={inputVal}
            onChange={(e) => setInputVal(e.target.value)}
            placeholder="Search titles, actors, directors, keywords..."
            className="w-full h-12 pl-12 pr-12 bg-[#131318] text-base text-[#f5f5f7] placeholder:text-zinc-500 rounded-2xl border border-white/10 focus:outline-none focus:border-rose-500/60 focus:ring-2 focus:ring-rose-500/20 transition-all shadow-xl"
          />
          {inputVal && (
            <button
              onClick={() => setInputVal("")}
              className="absolute right-4 top-1/2 -translate-y-1/2 p-1 rounded-full text-zinc-400 hover:text-white"
            >
              <X className="w-4 h-4" />
            </button>
          )}
        </div>
      </div>

      {/* Filter and Sort Toolbar */}
      <div className="flex flex-col gap-3 py-3.5 px-4 mb-8 bg-[#131318]/60 border border-white/10 rounded-2xl backdrop-blur-md">
        <div className="flex flex-wrap items-center justify-between gap-4">
          {/* Type pills */}
          <div className="flex items-center gap-1.5 sm:gap-2">
            <button
              onClick={() => setMediaType("all")}
              className={`flex items-center gap-1.5 px-3.5 py-1.5 rounded-full text-xs font-semibold transition-all ${
                mediaType === "all"
                  ? "bg-rose-600 text-white shadow-md shadow-rose-950/40"
                  : "bg-white/5 hover:bg-white/10 text-zinc-300 border border-white/10"
              }`}
            >
              <Layers className="w-3.5 h-3.5" />
              <span>All</span>
            </button>
            <button
              onClick={() => setMediaType("movie")}
              className={`flex items-center gap-1.5 px-3.5 py-1.5 rounded-full text-xs font-semibold transition-all ${
                mediaType === "movie"
                  ? "bg-rose-600 text-white shadow-md shadow-rose-950/40"
                  : "bg-white/5 hover:bg-white/10 text-zinc-300 border border-white/10"
              }`}
            >
              <Film className="w-3.5 h-3.5" />
              <span>Movies</span>
            </button>
            <button
              onClick={() => setMediaType("tv")}
              className={`flex items-center gap-1.5 px-3.5 py-1.5 rounded-full text-xs font-semibold transition-all ${
                mediaType === "tv"
                  ? "bg-rose-600 text-white shadow-md shadow-rose-950/40"
                  : "bg-white/5 hover:bg-white/10 text-zinc-300 border border-white/10"
              }`}
            >
              <Tv className="w-3.5 h-3.5" />
              <span>TV Shows</span>
            </button>
          </div>

          {/* Sort Selector */}
          <div className="flex items-center gap-2 text-xs text-zinc-400">
            <SlidersHorizontal className="w-3.5 h-3.5 text-zinc-500" />
            <span className="hidden sm:inline">Sort by:</span>
            <select
              value={sortBy}
              onChange={(e) => setSortBy(e.target.value)}
              className="bg-[#1c1c24] text-zinc-200 text-xs rounded-lg px-3 py-1.5 border border-white/10 focus:outline-none focus:border-rose-500 cursor-pointer"
            >
              <option value="relevance">Relevance</option>
              <option value="rating">Rating</option>
              <option value="releaseDate">Newest</option>
            </select>
          </div>
        </div>

        {/* Genre Filter Pills */}
        <div className="flex items-center gap-1.5 overflow-x-auto pt-2 pb-1 scrollbar-none border-t border-white/5">
          <span className="text-[11px] font-semibold uppercase text-zinc-500 tracking-wider shrink-0 mr-1 hidden sm:inline">
            Genre:
          </span>
          {GENRE_OPTIONS.map((g) => {
            const isSelected = selectedGenre === g.id;
            return (
              <button
                key={g.id}
                onClick={() => setSelectedGenre(g.id)}
                className={`px-3 py-1 rounded-full text-xs font-medium whitespace-nowrap transition-all ${
                  isSelected
                    ? "bg-rose-600 text-white font-semibold shadow-sm shadow-rose-950/40"
                    : "bg-white/5 hover:bg-white/10 text-zinc-300 border border-white/10"
                }`}
              >
                {g.label}
              </button>
            );
          })}
          {selectedGenre !== "all" && (
            <button
              onClick={() => setSelectedGenre("all")}
              className="px-2.5 py-1 rounded-full text-xs text-zinc-400 hover:text-white bg-white/5 hover:bg-white/10 flex items-center gap-1 shrink-0 ml-1 transition-colors"
            >
              <X className="w-3 h-3" />
              <span>Reset</span>
            </button>
          )}
        </div>
      </div>

      {/* Grid of Results */}
      {isLoadingInitial ? (
        <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 2xl:grid-cols-6 gap-4 sm:gap-6">
          {Array.from({ length: 10 }).map((_, i) => (
            <PosterCardSkeleton key={i} />
          ))}
        </div>
      ) : allItems.length > 0 ? (
        <>
          <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 2xl:grid-cols-6 gap-4 sm:gap-6">
            {allItems.map((item, idx) => (
              <PosterCard
                key={`${item.tmdbId}-${idx}`}
                movie={item}
                index={idx}
                priority={idx < 5}
              />
            ))}
          </div>

          {/* Infinite Scroll Sentinel */}
          <div ref={sentinelRef} className="py-12 flex justify-center w-full">
            {isFetchingNextPage ? (
              <div className="flex items-center gap-2 text-sm text-zinc-400">
                <Loader2 className="w-5 h-5 text-rose-500 animate-spin" />
                <span>Loading more titles...</span>
              </div>
            ) : hasNextPage ? (
              <button
                onClick={() => fetchNextPage()}
                className="px-6 py-2 rounded-full bg-white/5 hover:bg-white/10 text-xs font-semibold text-zinc-300 border border-white/10 transition-colors"
              >
                Load More
              </button>
            ) : (
              <p className="text-xs text-zinc-500">
                Reached the end of results
              </p>
            )}
          </div>
        </>
      ) : (
        <EmptyState
          icon={SearchX}
          title={`No matches for "${debouncedQuery}"`}
          description="Try different keywords or browse by genre."
          actionLabel="Browse Popular"
          actionHref="/"
        />
      )}
    </div>
  );
}
