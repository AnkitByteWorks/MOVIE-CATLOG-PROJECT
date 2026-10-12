import type { Metadata } from "next";
import { HomePage } from "@/components/home/HomePage";

export const metadata: Metadata = {
  title: "CinePulse — Discover 1.6M+ Movies & TV Shows",
  description:
    "Search, track, and discover movies and TV shows. Powered by a data-first catalog of 1.67 million titles.",
  openGraph: {
    title: "CinePulse",
    description: "Discover your next favorite film.",
    type: "website",
  },
};

export default function Page() {
  return <HomePage />;
}
