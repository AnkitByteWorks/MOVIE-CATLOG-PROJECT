import React from "react";

export function PosterCardSkeleton({ className = "" }: { className?: string }) {
  return (
    <div
      className={`relative flex flex-col rounded-xl overflow-hidden bg-[#131318] border border-white/5 shrink-0 ${className}`}
      style={{ aspectRatio: "2/3" }}
    >
      <div className="w-full h-full shimmer" />
      <div className="absolute top-2.5 right-2.5 w-10 h-5 rounded-md shimmer" />
      <div className="absolute bottom-0 left-0 right-0 p-3 flex flex-col gap-2 bg-gradient-to-t from-[#0a0a0f] via-[#0a0a0f]/80 to-transparent">
        <div className="h-4 w-3/4 rounded shimmer" />
        <div className="h-3 w-1/2 rounded shimmer" />
      </div>
    </div>
  );
}

export function MovieRowSkeleton({
  title,
  count = 6,
}: {
  title?: string;
  count?: number;
}) {
  return (
    <div className="w-full py-6 space-y-4">
      <div className="flex items-center justify-between px-4 sm:px-6 lg:px-8">
        {title ? (
          <h2 className="text-xl font-bold text-white tracking-tight">{title}</h2>
        ) : (
          <div className="h-6 w-48 rounded shimmer" />
        )}
        <div className="h-4 w-20 rounded shimmer" />
      </div>

      <div className="flex items-center gap-4 overflow-x-hidden px-4 sm:px-6 lg:px-8">
        {Array.from({ length: count }).map((_, i) => (
          <PosterCardSkeleton
            key={i}
            className="w-[160px] sm:w-[180px] md:w-[200px]"
          />
        ))}
      </div>
    </div>
  );
}

export function TextSkeleton({
  lines = 3,
  className = "",
}: {
  lines?: number;
  className?: string;
}) {
  return (
    <div className={`space-y-2 ${className}`}>
      {Array.from({ length: lines }).map((_, i) => (
        <div
          key={i}
          className="h-3.5 rounded shimmer"
          style={{ width: i === lines - 1 ? "60%" : "100%" }}
        />
      ))}
    </div>
  );
}
