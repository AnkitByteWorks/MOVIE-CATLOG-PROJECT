import React from "react";
import Link from "next/link";
import { Clapperboard, Database, Layers, Sparkles } from "lucide-react";

export function Footer() {
  return (
    <footer className="bg-[#08080c] border-t border-white/10 text-zinc-400 mt-auto">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12 lg:py-16">
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-8 lg:gap-12">
          {/* Column 1: Brand */}
          <div className="space-y-4">
            <Link href="/" className="flex items-center gap-2 inline-flex">
              <div className="w-8 h-8 rounded-lg bg-gradient-to-br from-rose-500 to-rose-700 flex items-center justify-center text-white shadow-md shadow-rose-900/40">
                <Clapperboard className="w-4 h-4" />
              </div>
              <span className="text-xl font-bold tracking-tight text-white font-[family-name:var(--font-playfair)]">
                CINE<span className="text-rose-500">PULSE</span>
              </span>
            </Link>
            <p className="text-sm text-zinc-400 leading-relaxed">
              Explore over 1.67 million movies and TV shows in a sleek cinematic dark interface. Fast, comprehensive, and built for film lovers.
            </p>
            <div className="flex items-center gap-2 text-xs text-rose-400 font-medium">
              <Sparkles className="w-3.5 h-3.5" />
              <span>Full-text search indexed</span>
            </div>
          </div>

          {/* Column 2: Navigation */}
          <div className="space-y-4">
            <h3 className="text-sm font-semibold uppercase tracking-wider text-white">
              Explore
            </h3>
            <ul className="space-y-2.5 text-sm">
              <li>
                <Link
                  href="/search?type=movie"
                  className="hover:text-white transition-colors"
                >
                  Movies Catalog
                </Link>
              </li>
              <li>
                <Link
                  href="/search?type=tv"
                  className="hover:text-white transition-colors"
                >
                  TV Shows
                </Link>
              </li>
              <li>
                <Link
                  href="/search?sort=popularity"
                  className="hover:text-white transition-colors"
                >
                  Trending Now
                </Link>
              </li>
              <li>
                <Link
                  href="/search?sort=voteAverage"
                  className="hover:text-white transition-colors"
                >
                  Top Rated
                </Link>
              </li>
              <li>
                <Link
                  href="/watchlist"
                  className="hover:text-white transition-colors"
                >
                  My Watchlist
                </Link>
              </li>
              <li>
                <Link
                  href="/watchlog"
                  className="hover:text-white transition-colors text-rose-400/90"
                >
                  Watch Diary
                </Link>
              </li>
              <li>
                <Link
                  href="/stats"
                  className="hover:text-white transition-colors text-amber-400/90"
                >
                  Watch Analytics
                </Link>
              </li>
            </ul>
          </div>

          {/* Column 3: Data */}
          <div className="space-y-4">
            <h3 className="text-sm font-semibold uppercase tracking-wider text-white flex items-center gap-1.5">
              <Database className="w-4 h-4 text-rose-500" />
              Data Source
            </h3>
            <ul className="space-y-2.5 text-sm text-zinc-400">
              <li>1,508,074 Movies in MongoDB</li>
              <li>164,705 TV Series indexed</li>
              <li>Sourced from Kaggle TMDB Open Dataset</li>
              <li>High-res posters from TMDB Public CDN</li>
              <li>Sub-second query latencies via Redis</li>
            </ul>
          </div>

          {/* Column 4: Tech Stack */}
          <div className="space-y-4">
            <h3 className="text-sm font-semibold uppercase tracking-wider text-white flex items-center gap-1.5">
              <Layers className="w-4 h-4 text-rose-500" />
              Architecture
            </h3>
            <ul className="space-y-2.5 text-sm text-zinc-400">
              <li>Next.js 16 (React 19 & App Router)</li>
              <li>Tailwind CSS v4 & shadcn/ui</li>
              <li>TanStack React Query v5</li>
              <li>Spring Boot 3.3 Microservices</li>
              <li>MongoDB & Redis Caching</li>
            </ul>
          </div>
        </div>

        {/* Bottom bar */}
        <div className="mt-12 pt-8 border-t border-white/5 flex flex-col sm:flex-row items-center justify-between gap-4 text-xs text-zinc-500">
          <p>© {new Date().getFullYear()} CinePulse. All rights reserved.</p>
          <p className="flex items-center gap-1 text-zinc-400">
            Built with Next.js & Spring Boot microservices
          </p>
        </div>
      </div>
    </footer>
  );
}
