"use client";

import React, { useState } from "react";
import Image from "next/image";
import Link from "next/link";
import { useRouter } from "next/navigation";
import {
  Star,
  Calendar,
  Layers,
  Play,
  BookmarkPlus,
  BookmarkCheck,
  ArrowLeft,
  Share2,
  User,
} from "lucide-react";
import { motion } from "framer-motion";
import { toast } from "sonner";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { EmptyState } from "@/components/common/EmptyState";
import { useTvShowDetails } from "@/lib/hooks/useTv";
import { useWatchlist } from "@/lib/hooks/useWatchlist";
import { LogWatchButton } from "@/components/watchlog/LogWatchButton";
import {
  buildPosterUrl,
  buildBackdropUrl,
  POSTER_FALLBACK,
} from "@/lib/utils";

export function TvDetailsClient({ id }: { id: string }) {
  const router = useRouter();
  const tmdbId = Number(id);
  const [posterFailed, setPosterFailed] = useState(false);
  const { inWatchlist, toggleWatchlist } = useWatchlist(tmdbId);

  const { data: tvShow, isLoading, error } = useTvShowDetails(tmdbId);

  if (isLoading) {
    return (
      <div className="min-h-screen pb-20 animate-pulse">
        <div className="w-full h-[400px] sm:h-[500px] bg-[#131318] shimmer" />
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 -mt-48 flex flex-col md:flex-row gap-8">
          <div className="w-56 sm:w-64 lg:w-72 h-96 rounded-2xl bg-[#1c1c24] shimmer shrink-0 mx-auto md:mx-0" />
          <div className="flex-1 space-y-4 pt-12">
            <div className="h-10 w-2/3 rounded shimmer" />
            <div className="h-4 w-1/3 rounded shimmer" />
            <div className="h-24 w-full rounded shimmer" />
          </div>
        </div>
      </div>
    );
  }

  if (error || !tvShow) {
    return (
      <div className="min-h-screen pt-32 px-4 max-w-lg mx-auto">
        <EmptyState
          title="TV Show Not Found"
          description="We couldn't locate this television series in the catalog."
          actionLabel="Return to Catalog"
          actionHref="/"
          icon="film"
        />
      </div>
    );
  }

  const poster = tvShow.posterUrl || tvShow.posterPath;
  const backdrop = tvShow.backdropUrl || tvShow.backdropPath;
  const firstYear = tvShow.firstAirDate
    ? new Date(tvShow.firstAirDate).getFullYear()
    : "N/A";
  const rating = tvShow.voteAverage ? tvShow.voteAverage.toFixed(1) : "NR";

  const handleShare = () => {
    if (typeof window !== "undefined") {
      navigator.clipboard.writeText(window.location.href);
      toast.success("Link copied to clipboard");
    }
  };

  const handleWatchlist = () => {
    toggleWatchlist(tvShow.name);
  };

  const handlePlayTrailer = () => {
    toast.info("Trailer coming soon 🎬");
  };

  return (
    <div className="relative min-h-screen pb-24">
      {/* Full-bleed Backdrop Hero with Vignette Overlay */}
      <div className="relative w-full h-[420px] sm:h-[500px] lg:h-[600px] bg-[#0a0a0f] overflow-hidden">
        {backdrop && (
          <Image
            src={buildBackdropUrl(backdrop, "original")}
            alt={tvShow.name}
            fill
            priority
            className="object-cover object-top opacity-35 sm:opacity-45"
          />
        )}
        {/* Dark bottom fade to #0a0a0f */}
        <div className="absolute inset-0 bg-gradient-to-t from-[#0a0a0f] via-[#0a0a0f]/60 to-transparent" />
        {/* Subtle radial vignette effect */}
        <div className="absolute inset-0 bg-[radial-gradient(ellipse_at_center,_var(--tw-gradient-stops))] from-transparent via-[#0a0a0f]/50 to-[#0a0a0f]" />

        {/* Floating Glass Back Button (top-left) */}
        <div className="absolute top-24 left-4 sm:left-8 z-30">
          <button
            onClick={() => router.back()}
            aria-label="Go back"
            className="w-10 h-10 rounded-full bg-black/60 hover:bg-black/90 backdrop-blur-md border border-white/15 text-white flex items-center justify-center shadow-xl transition-all hover:scale-105 group"
          >
            <ArrowLeft className="w-5 h-5 text-zinc-300 group-hover:text-white transition-colors" />
          </button>
        </div>
      </div>

      {/* Main Details Body */}
      <div className="relative max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 -mt-52 sm:-mt-64 z-20">
        <div className="flex flex-col md:flex-row gap-8 lg:gap-12">
          {/* Left Column: Sticky Poster */}
          <div className="w-56 sm:w-64 lg:w-72 shrink-0 mx-auto md:mx-0 md:sticky md:top-24 self-start">
            <motion.div
              initial={{ opacity: 0, scale: 0.96 }}
              animate={{ opacity: 1, scale: 1 }}
              transition={{ duration: 0.4, ease: "easeOut" }}
              className="relative w-full rounded-2xl overflow-hidden bg-[#131318] border border-white/15 shadow-2xl shadow-black/90 poster-glow"
              style={{ aspectRatio: "2/3" }}
            >
              <Image
                src={
                  posterFailed
                    ? POSTER_FALLBACK
                    : buildPosterUrl(poster, "w500")
                }
                alt={tvShow.name}
                fill
                sizes="(max-width: 768px) 256px, 288px"
                priority
                className="object-cover"
                onError={() => setPosterFailed(true)}
              />
            </motion.div>
          </div>

          {/* Right Column: Details, Metadata, Actions, Synopsis, Cast */}
          <div className="flex-1 flex flex-col pt-2 sm:pt-6">
            <motion.h1
              initial={{ opacity: 0, y: 15 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.4, ease: "easeOut" }}
              className="text-3xl sm:text-5xl lg:text-6xl font-extrabold text-white tracking-tight mb-2 font-[family-name:var(--font-playfair)]"
            >
              {tvShow.name}
            </motion.h1>

            {tvShow.tagline && (
              <motion.p
                initial={{ opacity: 0, y: 12 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ duration: 0.4, delay: 0.1, ease: "easeOut" }}
                className="text-sm sm:text-base italic text-amber-400 mb-4"
              >
                &ldquo;{tvShow.tagline}&rdquo;
              </motion.p>
            )}

            {/* Metadata Pills */}
            <motion.div
              initial={{ opacity: 0, y: 12 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.4, delay: 0.15, ease: "easeOut" }}
              className="flex flex-wrap items-center gap-3 sm:gap-4 text-xs sm:text-sm text-zinc-300 mb-6"
            >
              <div className="flex items-center gap-1.5 px-2.5 py-1 rounded-md bg-amber-500/20 text-amber-400 font-bold border border-amber-500/30">
                <Star className="w-4 h-4 fill-current" />
                <span>{rating}</span>
                <span className="text-[10px] text-zinc-400 font-normal">
                  ({tvShow.voteCount} votes)
                </span>
              </div>
              <div className="flex items-center gap-1.5 text-zinc-400">
                <Calendar className="w-4 h-4" />
                <span>First Aired: {firstYear}</span>
              </div>
              {tvShow.numberOfSeasons ? (
                <div className="flex items-center gap-1.5 text-zinc-400">
                  <Layers className="w-4 h-4" />
                  <span>{tvShow.numberOfSeasons} Seasons</span>
                </div>
              ) : null}
              {tvShow.status && (
                <Badge variant="outline" className="border-white/10 text-zinc-300">
                  {tvShow.status}
                </Badge>
              )}
            </motion.div>

            {/* Genre Pills */}
            {tvShow.genres && tvShow.genres.length > 0 && (
              <div className="flex flex-wrap gap-2 mb-6">
                {tvShow.genres.map((g) => (
                  <Link
                    key={g}
                    href={`/genre/${encodeURIComponent(g)}`}
                    className="px-3 py-1 rounded-full text-xs font-medium bg-[#1c1c24] hover:bg-rose-600/20 text-zinc-300 hover:text-rose-300 border border-white/10 hover:border-rose-500/30 transition-all"
                  >
                    {g}
                  </Link>
                ))}
              </div>
            )}

            {/* Action Buttons */}
            <div className="flex flex-wrap items-center gap-3 mb-8">
              <Button
                onClick={handlePlayTrailer}
                className="bg-rose-600 hover:bg-rose-700 text-white gap-2 rounded-lg px-6 h-11 font-semibold shadow-lg shadow-rose-950/40"
              >
                <Play className="w-4 h-4 fill-current" />
                <span>Watch Trailer</span>
              </Button>
              <Button
                onClick={handleWatchlist}
                variant={inWatchlist ? "secondary" : "outline"}
                className={`gap-2 rounded-lg h-11 transition-all ${
                  inWatchlist
                    ? "bg-rose-950/40 text-rose-300 border-rose-500/40 hover:bg-rose-950/60"
                    : "border-white/10 hover:bg-white/5 text-white"
                }`}
              >
                {inWatchlist ? (
                  <>
                    <BookmarkCheck className="w-4 h-4 text-rose-400" />
                    <span>In Watchlist ✓</span>
                  </>
                ) : (
                  <>
                    <BookmarkPlus className="w-4 h-4" />
                    <span>Add to Watchlist</span>
                  </>
                )}
              </Button>
              <LogWatchButton
                tmdbId={tvShow.tmdbId}
                mediaType="TV"
                title={tvShow.name}
                posterUrl={tvShow.posterUrl || (poster ? buildPosterUrl(poster) : null)}
                backdropUrl={tvShow.backdropUrl || (backdrop ? buildBackdropUrl(backdrop) : null)}
                className="h-11"
              />
              <Button
                onClick={handleShare}
                variant="outline"
                size="icon"
                className="rounded-lg border-white/10 hover:bg-white/5 text-zinc-400 hover:text-white h-11 w-11"
                aria-label="Share show"
              >
                <Share2 className="w-4 h-4" />
              </Button>
            </div>

            {/* Synopsis */}
            <div className="space-y-2 mb-8">
              <h2 className="text-lg font-semibold text-white tracking-wide">
                Series Overview
              </h2>
              <p className="text-sm sm:text-base text-zinc-300 leading-relaxed max-w-3xl">
                {tvShow.overview || "No overview available for this series."}
              </p>
            </div>

            {/* Creators */}
            {tvShow.createdBy && tvShow.createdBy.length > 0 && (
              <div className="mb-8">
                <span className="text-xs uppercase font-bold text-zinc-400">
                  Created by:
                </span>
                <p className="text-sm font-medium text-white mt-1">
                  {tvShow.createdBy.join(", ")}
                </p>
              </div>
            )}

            {/* Cast Section with Horizontal Carousel & Stagger-Fade */}
            {tvShow.cast && tvShow.cast.length > 0 && (
              <div className="space-y-3 mb-10">
                <h2 className="text-lg font-semibold text-white tracking-wide">
                  Series Cast
                </h2>
                <div className="flex items-center gap-4 overflow-x-auto pb-4 scrollbar-none">
                  {tvShow.cast.slice(0, 15).map((actor, idx) => (
                    <motion.div
                      key={actor.id || idx}
                      initial={{ opacity: 0, y: 10 }}
                      whileInView={{ opacity: 1, y: 0 }}
                      viewport={{ once: true, margin: "20px" }}
                      transition={{
                        duration: 0.3,
                        delay: Math.min(idx * 0.04, 0.3),
                        ease: "easeOut",
                      }}
                      className="flex flex-col items-center text-center gap-2 w-24 shrink-0"
                    >
                      {/* 80px Profile Pic / Fallback */}
                      <div className="relative w-20 h-20 rounded-full overflow-hidden bg-[#1c1c24] border border-white/10 shadow-md flex items-center justify-center">
                        {actor.profilePath ? (
                          <Image
                            src={buildPosterUrl(actor.profilePath, "w342")}
                            alt={actor.name}
                            fill
                            sizes="80px"
                            className="object-cover"
                          />
                        ) : (
                          <div className="w-full h-full bg-zinc-800 flex items-center justify-center text-zinc-500">
                            <User className="w-8 h-8" />
                          </div>
                        )}
                      </div>
                      <span className="text-xs font-semibold text-white truncate w-full">
                        {actor.name}
                      </span>
                      <span className="text-[10px] text-zinc-400 truncate w-full">
                        {actor.character}
                      </span>
                    </motion.div>
                  ))}
                </div>
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
