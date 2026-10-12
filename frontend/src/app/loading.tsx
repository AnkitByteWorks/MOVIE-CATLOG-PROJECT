import React from "react";
import { MovieRowSkeleton } from "@/components/common/Skeletons";

export default function Loading() {
  return (
    <div className="min-h-screen pt-28 pb-16 max-w-7xl mx-auto w-full px-4 sm:px-6 lg:px-8 space-y-12">
      {/* Hero skeleton */}
      <div className="flex flex-col items-center text-center space-y-4 max-w-3xl mx-auto py-12">
        <div className="h-6 w-48 rounded-full shimmer" />
        <div className="h-12 w-3/4 rounded-xl shimmer" />
        <div className="h-5 w-2/3 rounded-lg shimmer" />
        <div className="h-12 w-full max-w-xl rounded-full shimmer mt-4" />
      </div>

      {/* Row skeletons */}
      <MovieRowSkeleton count={6} />
      <MovieRowSkeleton count={6} />
    </div>
  );
}
