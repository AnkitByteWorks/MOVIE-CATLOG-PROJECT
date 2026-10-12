"use client";

import React, { useState } from "react";
import Image from "next/image";
import Link from "next/link";
import { Star } from "lucide-react";
import { motion } from "framer-motion";
import { useQueryClient } from "@tanstack/react-query";
import { MovieSummary, TvShowSummary } from "@/lib/types";
import { buildPosterUrl, POSTER_FALLBACK } from "@/lib/utils";
import { getMovieDetails } from "@/lib/api/movies";
import { getTvShowDetails } from "@/lib/api/tv";

interface PosterCardProps {
  movie: MovieSummary | TvShowSummary;
  priority?: boolean;
  aspectRatio?: string;
  className?: string;
  index?: number;
}

export function PosterCard({
  movie,
  priority = false,
  aspectRatio = "2/3",
  className = "",
  index = 0,
}: PosterCardProps) {
  const queryClient = useQueryClient();
  const isTv = "name" in movie || movie.mediaType === "TV";
  const title =
    "name" in movie
      ? movie.name
      : "title" in movie
      ? movie.title
      : "Untitled";
  const releaseDate =
    isTv && "firstAirDate" in movie
      ? movie.firstAirDate
      : "releaseDate" in movie
      ? movie.releaseDate
      : null;
  const targetHref = isTv ? `/tv/${movie.tmdbId}` : `/movie/${movie.tmdbId}`;

  const year = releaseDate
    ? new Date(releaseDate).getFullYear() || "N/A"
    : "N/A";
  const vote = movie.voteAverage ? Number(movie.voteAverage.toFixed(1)) : 0;
  const isHighRating = vote >= 7.5;

  const rawPoster = movie.posterUrl || movie.posterPath;
  const [imgSrc, setImgSrc] = useState(() =>
    buildPosterUrl(rawPoster, "w500")
  );

  const handleMouseEnter = () => {
    if (movie.tmdbId) {
      if (isTv) {
        queryClient.prefetchQuery({
          queryKey: ["tv", "details", movie.tmdbId],
          queryFn: () => getTvShowDetails(movie.tmdbId),
          staleTime: 1000 * 60 * 30,
        });
      } else {
        queryClient.prefetchQuery({
          queryKey: ["movies", "details", movie.tmdbId],
          queryFn: () => getMovieDetails(movie.tmdbId),
          staleTime: 1000 * 60 * 30,
        });
      }
    }
  };

  return (
    <motion.div
      initial={{ opacity: 0, scale: 0.95 }}
      whileInView={{ opacity: 1, scale: 1 }}
      viewport={{ once: true, margin: "40px" }}
      transition={{
        duration: 0.35,
        ease: "easeOut",
        delay: Math.min(index * 0.03, 0.25),
      }}
      className={`shrink-0 ${className}`}
      style={{ aspectRatio }}
    >
      <Link
        href={targetHref}
        onMouseEnter={handleMouseEnter}
        aria-label={`View details for ${title}`}
        className="group relative flex flex-col w-full h-full rounded-xl overflow-hidden bg-[#131318] border border-white/10 poster-glow focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-rose-500 select-none"
      >
        {/* Poster Image */}
        <Image
          src={imgSrc}
          alt={title || "Poster"}
          fill
          sizes="(max-width: 640px) 160px, (max-width: 768px) 180px, (max-width: 1024px) 200px, 220px"
          priority={priority}
          className="object-cover transition-transform duration-500 group-hover:scale-105"
          onError={() => setImgSrc(POSTER_FALLBACK)}
        />

        {/* Subtle vignette gradient */}
        <div className="absolute inset-0 bg-gradient-to-t from-[#0a0a0f] via-transparent to-black/30 pointer-events-none" />

        {/* Top badges: Year */}
        <div className="absolute top-2.5 right-2.5 z-10">
          <span className="px-2 py-0.5 text-[11px] font-semibold tracking-wider rounded-md bg-black/60 backdrop-blur-md text-zinc-300 border border-white/10 shadow-sm">
            {year}
          </span>
        </div>

        {/* Bottom overlay: Rating, Title & Genres */}
        <div className="absolute bottom-0 inset-x-0 p-3 flex flex-col justify-end bg-gradient-to-t from-[#0a0a0f] via-[#0a0a0f]/90 to-transparent pt-10 z-10 transition-all duration-300">
          <div className="flex items-center justify-between gap-1 mb-1">
            {/* Rating */}
            <div
              className={`flex items-center gap-1 text-xs font-bold px-1.5 py-0.5 rounded-md ${
                isHighRating
                  ? "bg-amber-500/20 text-amber-400 border border-amber-500/30"
                  : "bg-zinc-800/80 text-zinc-300 border border-zinc-700/50"
              }`}
            >
              <Star className="w-3 h-3 fill-current" />
              <span>{vote > 0 ? vote.toFixed(1) : "NR"}</span>
            </div>

            {/* Type Pill */}
            <span className="text-[10px] uppercase font-bold text-zinc-400 tracking-wider">
              {isTv ? "TV" : "Movie"}
            </span>
          </div>

          {/* Title */}
          <h3 className="text-sm font-semibold text-[#f5f5f7] line-clamp-1 group-hover:text-rose-400 transition-colors">
            {title}
          </h3>

          {/* Genres on hover */}
          {movie.genres && movie.genres.length > 0 && (
            <p className="text-[11px] text-zinc-400 line-clamp-1 mt-0.5 opacity-80 group-hover:opacity-100 transition-opacity">
              {movie.genres.slice(0, 2).join(" • ")}
            </p>
          )}
        </div>
      </Link>
    </motion.div>
  );
}
