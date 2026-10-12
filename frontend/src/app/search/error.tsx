"use client";

import React, { useEffect } from "react";
import { AlertTriangle, RefreshCw } from "lucide-react";
import { Button } from "@/components/ui/button";

export default function SearchError({
  error,
  reset,
}: {
  error: Error & { digest?: string };
  reset: () => void;
}) {
  useEffect(() => {
    console.error("Search page error:", error);
  }, [error]);

  return (
    <div className="min-h-[60vh] flex flex-col items-center justify-center text-center px-4">
      <div className="w-14 h-14 rounded-2xl bg-rose-500/10 border border-rose-500/20 flex items-center justify-center text-rose-500 mb-4 shadow-xl">
        <AlertTriangle className="w-7 h-7" />
      </div>
      <h2 className="text-xl font-bold text-white mb-2 font-[family-name:var(--font-playfair)]">
        Search Query Failed
      </h2>
      <p className="text-xs sm:text-sm text-zinc-400 mb-6 max-w-md">
        {error.message || "Could not retrieve matching results from MongoDB."}
      </p>
      <Button
        onClick={() => reset()}
        className="bg-rose-600 hover:bg-rose-700 text-white gap-2 rounded-lg"
      >
        <RefreshCw className="w-4 h-4" />
        <span>Retry Search</span>
      </Button>
    </div>
  );
}
