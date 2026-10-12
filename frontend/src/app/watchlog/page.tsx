import { Metadata } from "next";
import { WatchHistoryClient } from "@/components/watchlog/WatchHistoryClient";

export const metadata: Metadata = {
  title: "Watch History | Movie Tracker",
  description: "View your personal cinema diary, ratings, reviews, and logged watches.",
};

export default function WatchlogPage() {
  return <WatchHistoryClient />;
}
