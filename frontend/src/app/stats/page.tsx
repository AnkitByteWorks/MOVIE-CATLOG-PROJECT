import { Metadata } from "next";
import { StatsClient } from "@/components/watchlog/StatsClient";

export const metadata: Metadata = {
  title: "Watch Stats & Analytics | Movie Tracker",
  description: "Detailed analytics of your watch time, genre distribution, streaks, and habits.",
};

export default function StatsPage() {
  return <StatsClient />;
}
