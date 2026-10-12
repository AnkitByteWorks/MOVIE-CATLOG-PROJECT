"use client";

import React, { useRef, useState, useEffect } from "react";
import Link from "next/link";
import { ChevronLeft, ChevronRight, ArrowRight } from "lucide-react";
import { MovieSummary, TvShowSummary } from "@/lib/types";
import { PosterCard } from "./PosterCard";
import { PosterCardSkeleton } from "@/components/common/Skeletons";
import { ErrorFallback } from "@/components/common/ErrorFallback";

interface MovieRowProps {
  title: string;
  seeAllHref?: string;
  movies?: (MovieSummary | TvShowSummary)[];
  loading?: boolean;
  error?: Error | null;
  onRetry?: () => void;
}

export function MovieRow({
  title,
  seeAllHref,
  movies = [],
  loading = false,
  error = null,
  onRetry,
}: MovieRowProps) {
  const rowRef = useRef<HTMLDivElement>(null);
  const [canScrollLeft, setCanScrollLeft] = useState(false);
  const [canScrollRight, setCanScrollRight] = useState(true);

  const checkScroll = () => {
    if (rowRef.current) {
      const { scrollLeft, scrollWidth, clientWidth } = rowRef.current;
      setCanScrollLeft(scrollLeft > 10);
      setCanScrollRight(scrollLeft < scrollWidth - clientWidth - 10);
    }
  };

  useEffect(() => {
    checkScroll();
    window.addEventListener("resize", checkScroll);
    return () => window.removeEventListener("resize", checkScroll);
  }, [movies, loading]);

  const scroll = (direction: "left" | "right") => {
    if (rowRef.current) {
      const { clientWidth } = rowRef.current;
      const scrollAmount = direction === "left" ? -clientWidth * 0.75 : clientWidth * 0.75;
      rowRef.current.scrollBy({ left: scrollAmount, behavior: "smooth" });
    }
  };

  // POLISH 6: If API returns 0 movies and not loading, hide the row entirely
  if (!loading && !error && (!movies || movies.length === 0)) {
    return null;
  }

  return (
    <section className="relative w-full py-5 group/row">
      {/* Header */}
      <div className="flex items-center justify-between px-4 sm:px-6 lg:px-8 mb-3">
        <h2 className="text-xl sm:text-2xl font-bold tracking-tight text-white font-[family-name:var(--font-playfair)]">
          {title}
        </h2>
        {seeAllHref && (
          <Link
            href={seeAllHref}
            className="flex items-center gap-1 text-xs sm:text-sm font-medium text-rose-500 hover:text-rose-400 transition-colors"
          >
            <span>See all</span>
            <ArrowRight className="w-4 h-4" />
          </Link>
        )}
      </div>

      {error ? (
        <div className="px-4 sm:px-6 lg:px-8">
          <ErrorFallback
            title={`Could not load ${title}`}
            message={error.message || "Unable to reach movie catalog service"}
            onRetry={onRetry}
          />
        </div>
      ) : (
        /* Row Wrapper with hover arrows */
        <div className="relative">
          {/* Left Arrow */}
          {canScrollLeft && (
            <button
              onClick={() => scroll("left")}
              aria-label="Scroll left"
              className="hidden md:flex absolute left-2 top-1/2 -translate-y-1/2 z-20 w-10 h-10 rounded-full bg-black/70 hover:bg-rose-600 text-white items-center justify-center backdrop-blur-md border border-white/10 transition-all opacity-0 group-hover/row:opacity-100 hover:scale-110 shadow-xl"
            >
              <ChevronLeft className="w-6 h-6" />
            </button>
          )}

          {/* Scroll Container */}
          <div
            ref={rowRef}
            onScroll={checkScroll}
            className="flex items-center gap-4 overflow-x-auto scrollbar-none px-4 sm:px-6 lg:px-8 py-2 snap-x snap-mandatory scroll-smooth"
            style={{
              scrollbarWidth: "none",
              msOverflowStyle: "none",
            }}
          >
            {loading
              ? Array.from({ length: 6 }).map((_, i) => (
                  <div key={i} className="snap-start shrink-0">
                    <PosterCardSkeleton className="w-[150px] sm:w-[170px] md:w-[190px]" />
                  </div>
                ))
              : movies.map((movie, index) => (
                  <div key={movie.id || movie.tmdbId} className="snap-start shrink-0">
                    <PosterCard
                      movie={movie}
                      index={index}
                      priority={index < 4}
                      className="w-[150px] sm:w-[170px] md:w-[190px]"
                    />
                  </div>
                ))}
          </div>

          {/* Right Arrow */}
          {canScrollRight && (
            <button
              onClick={() => scroll("right")}
              aria-label="Scroll right"
              className="hidden md:flex absolute right-2 top-1/2 -translate-y-1/2 z-20 w-10 h-10 rounded-full bg-black/70 hover:bg-rose-600 text-white items-center justify-center backdrop-blur-md border border-white/10 transition-all opacity-0 group-hover/row:opacity-100 hover:scale-110 shadow-xl"
            >
              <ChevronRight className="w-6 h-6" />
            </button>
          )}
        </div>
      )}
    </section>
  );
}
