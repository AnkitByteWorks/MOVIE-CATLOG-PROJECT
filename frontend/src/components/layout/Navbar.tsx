"use client";

import React, { useState, useEffect } from "react";
import Link from "next/link";
import { usePathname } from "next/navigation";
import { Film, Menu, Bookmark, Tv, Clapperboard, Calendar, BarChart3, Sparkles } from "lucide-react";
import { SearchBar } from "./SearchBar";
import { UserMenu } from "./UserMenu";
import { Avatar, AvatarFallback } from "@/components/ui/avatar";
import {
  Sheet,
  SheetContent,
  SheetHeader,
  SheetTitle,
  SheetTrigger,
} from "@/components/ui/sheet";
import { useAuth } from "@/lib/hooks/useAuth";

export function Navbar() {
  const [isScrolled, setIsScrolled] = useState(false);
  const [mobileOpen, setMobileOpen] = useState(false);
  const { user: authUser, logout } = useAuth();
  const pathname = usePathname();

  useEffect(() => {
    const handleScroll = () => {
      setIsScrolled(window.scrollY > 20);
    };

    window.addEventListener("scroll", handleScroll);
    return () => window.removeEventListener("scroll", handleScroll);
  }, []);

  const navLinks = [
    { label: "Movies", href: "/search?type=movie", icon: Film },
    { label: "TV Shows", href: "/search?type=tv", icon: Tv },
    { label: "Genres", href: "/genre/Drama", icon: Sparkles },
    { label: "Watchlist", href: "/watchlist", icon: Bookmark },
    { label: "Diary", href: "/watchlog", icon: Calendar },
    { label: "Stats", href: "/stats", icon: BarChart3 },
  ];

  return (
    <header
      className={`fixed top-0 left-0 right-0 z-50 transition-all duration-300 ${
        isScrolled
          ? "bg-[#0a0a0f]/85 backdrop-blur-xl border-b border-white/10 shadow-lg shadow-black/20 py-3"
          : "bg-gradient-to-b from-[#0a0a0f]/95 via-[#0a0a0f]/60 to-transparent py-4"
      }`}
    >
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex items-center justify-between gap-4">
          {/* Left: Brand Logo */}
          <Link
            href="/"
            className="flex items-center gap-2 group shrink-0 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-rose-500 rounded-lg p-1"
          >
            <div className="w-9 h-9 rounded-lg bg-gradient-to-br from-rose-500 to-rose-700 flex items-center justify-center shadow-lg shadow-rose-900/30 group-hover:scale-105 transition-transform duration-300">
              <Clapperboard className="w-5 h-5 text-white" />
            </div>
            <div className="flex flex-col">
              <span className="text-xl font-bold tracking-tight text-white font-[family-name:var(--font-playfair)]">
                CINE<span className="text-rose-500">PULSE</span>
              </span>
              <span className="text-[9px] tracking-widest text-zinc-400 font-medium uppercase -mt-1 hidden sm:block">
                1.6M+ Catalog
              </span>
            </div>
          </Link>

          {/* Center: Desktop Navigation Links */}
          <nav className="hidden md:flex items-center gap-1 lg:gap-2">
            {navLinks.map((link) => {
              const isActive = pathname === link.href.split("?")[0];
              const Icon = link.icon;
              return (
                <Link
                  key={link.href}
                  href={link.href}
                  className={`flex items-center gap-1.5 px-3 py-1.5 rounded-full text-sm font-medium transition-all ${
                    isActive
                      ? "text-white bg-white/10"
                      : "text-zinc-300 hover:text-white hover:bg-white/5"
                  }`}
                >
                  <Icon className="w-4 h-4 text-zinc-400 group-hover:text-rose-400" />
                  {link.label}
                </Link>
              );
            })}
          </nav>

          {/* Right: SearchBar + Watchlist + UserMenu */}
          <div className="hidden lg:flex items-center gap-3">
            <SearchBar className="w-64 xl:w-80" />
            <Link
              href="/watchlist"
              className="relative p-2 rounded-full hover:bg-white/5 text-zinc-400 hover:text-white transition-colors"
              title="Watchlist"
            >
              <Bookmark className="w-5 h-5" />
            </Link>
            <UserMenu />
          </div>

          {/* Mobile Right Controls: Search icon / Sheet trigger */}
          <div className="flex md:hidden items-center gap-2">
            <Sheet open={mobileOpen} onOpenChange={setMobileOpen}>
              <SheetTrigger
                render={
                  <button
                    aria-label="Open mobile menu"
                    className="p-2 rounded-lg text-zinc-300 hover:text-white hover:bg-white/5"
                  />
                }
              >
                <Menu className="w-6 h-6" />
              </SheetTrigger>
              <SheetContent
                side="right"
                className="bg-[#131318] border-white/10 text-white w-72 p-6 flex flex-col justify-between"
              >
                <div>
                  <SheetHeader className="mb-6 text-left">
                    <SheetTitle className="text-xl font-bold font-[family-name:var(--font-playfair)] flex items-center gap-2">
                      <Clapperboard className="w-5 h-5 text-rose-500" />
                      CINE<span className="text-rose-500">PULSE</span>
                    </SheetTitle>
                  </SheetHeader>

                  <div className="mb-6">
                    <SearchBar
                      onSearchSubmit={() => setMobileOpen(false)}
                      placeholder="Search catalog..."
                    />
                  </div>

                  <nav className="flex flex-col gap-2">
                    {navLinks.map((link) => {
                      const Icon = link.icon;
                      return (
                        <Link
                          key={link.href}
                          href={link.href}
                          onClick={() => setMobileOpen(false)}
                          className="flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium text-zinc-300 hover:text-white hover:bg-white/5 transition-colors"
                        >
                          <Icon className="w-5 h-5 text-rose-500" />
                          {link.label}
                        </Link>
                      );
                    })}
                  </nav>
                </div>

                <div className="pt-6 border-t border-white/10 space-y-3">
                  {authUser ? (
                    <div className="space-y-3">
                      <Link
                        href="/profile"
                        onClick={() => setMobileOpen(false)}
                        className="flex items-center gap-3 hover:opacity-85 transition-opacity"
                      >
                        <Avatar className="w-10 h-10 border border-white/15">
                          <AvatarFallback className="bg-gradient-to-tr from-rose-600 to-amber-600 text-white text-xs font-bold">
                            {(authUser.displayName || authUser.username || "U").slice(0, 2).toUpperCase()}
                          </AvatarFallback>
                        </Avatar>
                        <div className="flex flex-col truncate">
                          <span className="text-sm font-semibold text-white truncate">
                            {authUser.displayName || authUser.username}
                          </span>
                          <span className="text-xs text-zinc-400 font-mono">@{authUser.username}</span>
                        </div>
                      </Link>
                      <div className="grid grid-cols-2 gap-2 pt-1">
                        <Link
                          href="/profile"
                          onClick={() => setMobileOpen(false)}
                          className="text-center py-2 px-3 rounded-lg bg-white/5 hover:bg-white/10 text-xs font-medium text-zinc-200 transition-colors"
                        >
                          Profile
                        </Link>
                        <button
                          type="button"
                          onClick={() => {
                            setMobileOpen(false);
                            logout();
                          }}
                          className="text-center py-2 px-3 rounded-lg bg-rose-950/30 hover:bg-rose-900/40 text-xs font-medium text-rose-300 transition-colors"
                        >
                          Sign Out
                        </button>
                      </div>
                    </div>
                  ) : (
                    <div className="flex flex-col gap-2">
                      <Link
                        href="/login"
                        onClick={() => setMobileOpen(false)}
                        className="w-full py-2.5 px-4 rounded-lg bg-white/5 hover:bg-white/10 text-center text-sm font-medium text-white transition-colors"
                      >
                        Sign In
                      </Link>
                      <Link
                        href="/register"
                        onClick={() => setMobileOpen(false)}
                        className="w-full py-2.5 px-4 rounded-lg bg-rose-600 hover:bg-rose-500 text-center text-sm font-medium text-white shadow-lg shadow-rose-950 transition-colors"
                      >
                        Create Account
                      </Link>
                    </div>
                  )}
                </div>
              </SheetContent>
            </Sheet>
          </div>
        </div>
      </div>
    </header>
  );
}
