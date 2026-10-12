import React from "react";
import { PosterCardSkeleton } from "@/components/common/Skeletons";

export default function SearchLoading() {
  return (
    <div className="min-h-screen pt-24 pb-20 px-4 sm:px-6 lg:px-8 max-w-7xl mx-auto w-full">
      <div className="space-y-4 mb-8">
        <div className="h-10 w-64 rounded-xl shimmer" />
        <div className="h-4 w-96 rounded shimmer" />
        <div className="h-12 max-w-2xl rounded-2xl shimmer mt-4" />
      </div>

      <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 gap-4 sm:gap-6">
        {Array.from({ length: 15 }).map((_, i) => (
          <PosterCardSkeleton key={i} />
        ))}
      </div>
    </div>
  );
}
