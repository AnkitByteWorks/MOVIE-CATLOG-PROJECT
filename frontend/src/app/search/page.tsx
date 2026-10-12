import React, { Suspense } from "react";
import type { Metadata } from "next";
import { SearchResults } from "@/components/search/SearchResults";
import { PosterCardSkeleton } from "@/components/common/Skeletons";

export async function generateMetadata({
  searchParams,
}: {
  searchParams: Promise<{ q?: string }>;
}): Promise<Metadata> {
  const { q } = await searchParams;
  if (q && q.trim()) {
    return {
      title: `Search: "${q.trim()}" | CinePulse`,
      description: `Results for "${q.trim()}" across 1.67M titles on CinePulse.`,
    };
  }
  return {
    title: "Search 1.6M+ Movies & TV Shows | CinePulse",
    description: "Search and explore 1.6M+ titles in the CinePulse catalog.",
  };
}

export default function SearchPage() {
  return (
    <Suspense
      fallback={
        <div className="min-h-screen pt-24 px-4 sm:px-6 lg:px-8 max-w-7xl mx-auto w-full">
          <div className="h-8 w-64 rounded shimmer mb-4" />
          <div className="h-4 w-96 rounded shimmer mb-8" />
          <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 gap-4 sm:gap-6">
            {Array.from({ length: 10 }).map((_, i) => (
              <PosterCardSkeleton key={i} />
            ))}
          </div>
        </div>
      }
    >
      <SearchResults />
    </Suspense>
  );
}
