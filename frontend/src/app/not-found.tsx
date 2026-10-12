import React from "react";
import Link from "next/link";
import { Film, Home } from "lucide-react";
import { buttonVariants } from "@/components/ui/button";
import { cn } from "@/lib/utils";

export default function NotFound() {
  return (
    <div className="min-h-[80vh] flex flex-col items-center justify-center text-center px-4">
      <div className="relative mb-6">
        <div className="w-24 h-24 rounded-3xl bg-rose-500/10 border border-rose-500/20 flex items-center justify-center text-rose-500 shadow-2xl shadow-rose-950/40">
          <Film className="w-12 h-12 stroke-[1.5]" />
        </div>
        <span className="absolute -bottom-2 -right-2 px-2.5 py-0.5 rounded-full bg-rose-600 text-white text-xs font-bold font-mono">
          404
        </span>
      </div>

      <h1 className="text-3xl sm:text-5xl font-extrabold text-white tracking-tight mb-3 font-[family-name:var(--font-playfair)]">
        Lost in the Cinema
      </h1>
      <p className="text-zinc-400 text-sm sm:text-base max-w-md mb-8">
        The reel you&apos;re looking for was either moved, deleted, or never existed in our 1.6M+ title library.
      </p>

      <div className="flex items-center gap-3">
        <Link
          href="/"
          className={cn(
            buttonVariants({ variant: "default" }),
            "bg-rose-600 hover:bg-rose-700 text-white gap-2 rounded-lg"
          )}
        >
          <Home className="w-4 h-4" />
          <span>Return to Home</span>
        </Link>
      </div>
    </div>
  );
}
