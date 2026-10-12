import React from "react";

export default function MovieLoading() {
  return (
    <div className="min-h-screen pb-20 animate-pulse">
      <div className="w-full h-[400px] sm:h-[500px] bg-[#131318] shimmer" />
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 -mt-48 flex flex-col md:flex-row gap-8">
        <div
          className="w-56 sm:w-64 lg:w-72 rounded-2xl bg-[#1c1c24] shimmer shrink-0"
          style={{ aspectRatio: "2/3" }}
        />
        <div className="flex-1 space-y-4 pt-12">
          <div className="h-10 w-2/3 rounded-xl shimmer" />
          <div className="h-4 w-1/3 rounded-lg shimmer" />
          <div className="h-6 w-1/4 rounded-md shimmer" />
          <div className="h-28 w-full rounded-xl shimmer mt-6" />
        </div>
      </div>
    </div>
  );
}
