import React from "react";
import type { Metadata } from "next";
import { EmptyState } from "@/components/common/EmptyState";
import { Bookmark } from "lucide-react";

export const metadata: Metadata = {
  title: "My Watchlist | CinePulse",
  description: "View and manage your saved movies and TV shows on CinePulse.",
};

export default function WatchlistPage() {
  return (
    <div className="min-h-screen pt-24 pb-16 px-4 sm:px-6 lg:px-8 max-w-7xl mx-auto w-full">
      <div className="flex flex-col gap-2 mb-8">
        <div className="flex items-center gap-2 text-rose-500 text-sm font-semibold tracking-wider uppercase">
          <Bookmark className="w-4 h-4" />
          <span>Personal Collection</span>
        </div>
        <h1 className="text-3xl sm:text-5xl font-bold tracking-tight text-white font-[family-name:var(--font-playfair)]">
          My Watchlist
        </h1>
        <p className="text-zinc-400 text-sm sm:text-base">
          Track movies and series you want to watch or have already completed.
        </p>
      </div>

      <div className="py-16">
        <EmptyState
          title="Your watchlist is empty"
          description="You haven't saved any movies or TV series yet. Explore the 1.6M+ titles in the catalog and save your favorites!"
          actionLabel="Explore Trending"
          actionHref="/"
          icon="film"
        />
      </div>
    </div>
  );
}
