"use client";

import React from "react";
import Link from "next/link";
import { AlertTriangle, Home, RefreshCw } from "lucide-react";
import { Button, buttonVariants } from "@/components/ui/button";
import { cn } from "@/lib/utils";

export default function MovieError({
  reset,
}: {
  error: Error & { digest?: string };
  reset: () => void;
}) {
  return (
    <div className="min-h-[70vh] flex flex-col items-center justify-center text-center px-4">
      <div className="w-14 h-14 rounded-2xl bg-rose-500/10 border border-rose-500/20 flex items-center justify-center text-rose-500 mb-4">
        <AlertTriangle className="w-7 h-7" />
      </div>
      <h2 className="text-xl font-bold text-white mb-2 font-[family-name:var(--font-playfair)]">
        Could Not Load Movie Profile
      </h2>
      <p className="text-sm text-zinc-400 mb-6 max-w-sm">
        An error occurred while fetching details for this title from the local catalog.
      </p>
      <div className="flex items-center gap-3">
        <Button
          onClick={() => reset()}
          className="bg-rose-600 hover:bg-rose-700 text-white gap-1.5"
        >
          <RefreshCw className="w-4 h-4" />
          <span>Try Again</span>
        </Button>
        <Link
          href="/"
          className={cn(
            buttonVariants({ variant: "outline" }),
            "border-white/10 hover:bg-white/5 text-white gap-1.5"
          )}
        >
          <Home className="w-4 h-4" />
          <span>Home</span>
        </Link>
      </div>
    </div>
  );
}
