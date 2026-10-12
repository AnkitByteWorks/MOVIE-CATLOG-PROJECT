"use client";

import React, { useState, useEffect, useRef } from "react";
import { useRouter } from "next/navigation";
import Image from "next/image";
import { Search, Loader2, Star, Film, X } from "lucide-react";
import { motion, AnimatePresence } from "framer-motion";
import { useDebounce } from "@/lib/hooks/useDebounce";
import { useMovieSearch } from "@/lib/hooks/useMovies";
import { buildPosterUrl, POSTER_FALLBACK } from "@/lib/utils";
import { MovieSummary } from "@/lib/types";

interface SearchBarProps {
  className?: string;
  placeholder?: string;
  onSearchSubmit?: () => void;
}

export function SearchBar({
  className = "",
  placeholder = "Search 1.6M+ movies, TV shows, actors...",
  onSearchSubmit,
}: SearchBarProps) {
  const router = useRouter();
  const [query, setQuery] = useState("");
  const [isOpen, setIsOpen] = useState(false);
  const [selectedIndex, setSelectedIndex] = useState(-1);
  const [brokenImgs, setBrokenImgs] = useState<Record<number, boolean>>({});
  const inputRef = useRef<HTMLInputElement>(null);
  const dropdownRef = useRef<HTMLDivElement>(null);

  const debouncedQuery = useDebounce(query, 300);
  const { data, isLoading } = useMovieSearch(debouncedQuery, 1);

  const results: MovieSummary[] = data?.content?.slice(0, 8) || [];

  // Global ⌘K / Ctrl+K listener
  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      if ((e.metaKey || e.ctrlKey) && e.key.toLowerCase() === "k") {
        e.preventDefault();
        inputRef.current?.focus();
        setIsOpen(true);
      }
    };

    window.addEventListener("keydown", handleKeyDown);
    return () => window.removeEventListener("keydown", handleKeyDown);
  }, []);

  // Click outside to close dropdown
  useEffect(() => {
    const handleClickOutside = (e: MouseEvent) => {
      if (
        dropdownRef.current &&
        !dropdownRef.current.contains(e.target as Node) &&
        inputRef.current &&
        !inputRef.current.contains(e.target as Node)
      ) {
        setIsOpen(false);
      }
    };

    document.addEventListener("mousedown", handleClickOutside);
    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, []);

  const handleSelectMovie = (movie: MovieSummary) => {
    setIsOpen(false);
    setQuery("");
    const isTv = movie.mediaType === "TV";
    const href = isTv ? `/tv/${movie.tmdbId}` : `/movie/${movie.tmdbId}`;
    router.push(href);
    if (onSearchSubmit) onSearchSubmit();
  };

  const handleSubmit = (e?: React.FormEvent) => {
    if (e) e.preventDefault();
    if (selectedIndex >= 0 && selectedIndex < results.length) {
      handleSelectMovie(results[selectedIndex]);
      return;
    }
    const trimmed = query.trim();
    if (trimmed) {
      setIsOpen(false);
      router.push(`/search?q=${encodeURIComponent(trimmed)}`);
      if (onSearchSubmit) onSearchSubmit();
    }
  };

  const handleKeyDown = (e: React.KeyboardEvent<HTMLInputElement>) => {
    if (!isOpen && e.key !== "Escape") {
      setIsOpen(true);
    }

    if (e.key === "ArrowDown") {
      e.preventDefault();
      setSelectedIndex((prev) => (prev < results.length - 1 ? prev + 1 : 0));
    } else if (e.key === "ArrowUp") {
      e.preventDefault();
      setSelectedIndex((prev) => (prev > 0 ? prev - 1 : results.length - 1));
    } else if (e.key === "Enter") {
      e.preventDefault();
      handleSubmit();
    } else if (e.key === "Escape") {
      e.preventDefault();
      setIsOpen(false);
      inputRef.current?.blur();
    }
  };

  return (
    <div className={`relative flex items-center w-full max-w-md ${className}`}>
      <form onSubmit={handleSubmit} className="relative w-full">
        <Search className="absolute left-3.5 top-1/2 -translate-y-1/2 w-4 h-4 text-zinc-400 pointer-events-none" />
        <input
          ref={inputRef}
          type="text"
          value={query}
          onFocus={() => setIsOpen(true)}
          onChange={(e) => {
            setQuery(e.target.value);
            setSelectedIndex(-1);
            if (!isOpen) setIsOpen(true);
          }}
          onKeyDown={handleKeyDown}
          placeholder={placeholder}
          className="w-full h-10 pl-10 pr-20 bg-[#131318]/90 text-sm text-[#f5f5f7] placeholder:text-zinc-500 rounded-full border border-white/10 focus:outline-none focus:border-rose-500/60 focus:ring-2 focus:ring-rose-500/20 transition-all shadow-inner"
        />

        <div className="absolute right-3 top-1/2 -translate-y-1/2 flex items-center gap-1.5">
          {isLoading && query.trim().length >= 2 ? (
            <Loader2 className="w-4 h-4 text-rose-500 animate-spin" />
          ) : query ? (
            <button
              type="button"
              onClick={() => {
                setQuery("");
                setSelectedIndex(-1);
                inputRef.current?.focus();
              }}
              className="p-1 rounded-full text-zinc-400 hover:text-white"
            >
              <X className="w-3.5 h-3.5" />
            </button>
          ) : (
            <kbd className="hidden sm:inline-flex items-center justify-center px-1.5 py-0.5 text-[10px] font-mono text-zinc-400 bg-white/5 border border-white/10 rounded pointer-events-none">
              ⌘K
            </kbd>
          )}
        </div>
      </form>

      {/* Dropdown Live Results Panel */}
      <AnimatePresence>
        {isOpen && query.trim().length >= 2 && (
          <motion.div
            ref={dropdownRef}
            initial={{ opacity: 0, y: -8, scale: 0.98 }}
            animate={{ opacity: 1, y: 0, scale: 1 }}
            exit={{ opacity: 0, y: -8, scale: 0.98 }}
            transition={{ duration: 0.15, ease: "easeOut" }}
            className="absolute top-12 left-0 right-0 z-50 bg-[#131318] border border-white/10 rounded-2xl shadow-2xl overflow-hidden backdrop-blur-xl divide-y divide-white/5"
          >
            {isLoading ? (
              <div className="flex items-center justify-center py-8 gap-2 text-xs text-zinc-400">
                <Loader2 className="w-4 h-4 text-rose-500 animate-spin" />
                <span>Searching 1.6M+ catalog...</span>
              </div>
            ) : results.length > 0 ? (
              <div>
                <div className="px-3.5 py-2 text-[11px] font-semibold uppercase tracking-wider text-zinc-500 flex items-center justify-between">
                  <span>Results</span>
                  <span>Press ↑↓ to navigate</span>
                </div>
                <ul className="max-h-80 overflow-y-auto divide-y divide-white/5">
                  {results.map((item, index) => {
                    const poster = item.posterUrl || item.posterPath;
                    const year = item.releaseDate
                      ? new Date(item.releaseDate).getFullYear()
                      : "N/A";
                    const isSelected = index === selectedIndex;
                    const rating = item.voteAverage
                      ? Number(item.voteAverage.toFixed(1))
                      : null;

                    return (
                      <li
                        key={item.tmdbId}
                        onMouseEnter={() => setSelectedIndex(index)}
                        onClick={() => handleSelectMovie(item)}
                        className={`flex items-center gap-3 px-3.5 py-2.5 cursor-pointer transition-colors ${
                          isSelected ? "bg-rose-600/20 text-white" : "hover:bg-white/5 text-zinc-200"
                        }`}
                      >
                        {/* Thumbnail */}
                        <div className="relative w-8 h-12 rounded bg-[#1c1c24] overflow-hidden shrink-0 border border-white/10">
                          <Image
                            src={
                              brokenImgs[item.tmdbId]
                                ? POSTER_FALLBACK
                                : buildPosterUrl(poster, "w342")
                            }
                            alt={item.title || "Poster"}
                            fill
                            sizes="32px"
                            className="object-cover"
                            onError={() =>
                              setBrokenImgs((prev) => ({
                                ...prev,
                                [item.tmdbId]: true,
                              }))
                            }
                          />
                        </div>

                        {/* Title and details */}
                        <div className="flex-1 min-w-0">
                          <p className="text-sm font-medium truncate">
                            {item.title}
                          </p>
                          <div className="flex items-center gap-2 text-xs text-zinc-400 mt-0.5">
                            <span>{year}</span>
                            <span>•</span>
                            <span className="uppercase text-[10px] font-bold px-1 py-0.2 rounded bg-white/10 text-zinc-300">
                              {item.mediaType || "MOVIE"}
                            </span>
                            {rating && (
                              <span className="flex items-center gap-0.5 text-amber-400 font-semibold">
                                <Star className="w-3 h-3 fill-current" />
                                {rating}
                              </span>
                            )}
                          </div>
                        </div>
                      </li>
                    );
                  })}
                </ul>
                <div
                  onClick={() => handleSubmit()}
                  className="px-4 py-2.5 text-xs text-center text-rose-400 hover:text-rose-300 hover:bg-rose-950/20 cursor-pointer font-medium border-t border-white/5 transition-colors"
                >
                  View all results for &ldquo;{query}&rdquo; →
                </div>
              </div>
            ) : (
              <div className="p-6 text-center text-sm text-zinc-400">
                <Film className="w-8 h-8 text-zinc-600 mx-auto mb-2" />
                <p>No matches for &ldquo;{query}&rdquo;</p>
                <p className="text-xs text-zinc-500 mt-1">
                  Try checking spelling or search by director / actor
                </p>
              </div>
            )}
          </motion.div>
        )}
      </AnimatePresence>
    </div>
  );
}
