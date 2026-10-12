import type { Metadata } from "next";
import { TvDetailsClient } from "@/components/tv/TvDetailsClient";
import { getTvShowDetails } from "@/lib/api/tv";

export async function generateMetadata({
  params,
}: {
  params: Promise<{ id: string }>;
}): Promise<Metadata> {
  const { id } = await params;
  try {
    const show = await getTvShowDetails(Number(id));
    const year = show.firstAirDate
      ? ` (${new Date(show.firstAirDate).getFullYear()})`
      : "";
    return {
      title: `${show.name}${year} | CinePulse`,
      description:
        show.overview ||
        `Explore seasons, cast, and details for ${show.name} on CinePulse.`,
      openGraph: {
        title: `${show.name}${year}`,
        description: show.overview || undefined,
        images: show.posterUrl ? [show.posterUrl] : [],
      },
    };
  } catch {
    return {
      title: "TV Show Details | CinePulse",
      description: "Explore 164K+ TV shows in the CinePulse catalog.",
    };
  }
}

export default async function TvPage({
  params,
}: {
  params: Promise<{ id: string }>;
}) {
  const { id } = await params;
  return <TvDetailsClient id={id} />;
}
