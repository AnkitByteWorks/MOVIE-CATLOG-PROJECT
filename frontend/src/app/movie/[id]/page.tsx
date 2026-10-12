import type { Metadata } from "next";
import { MovieDetailsClient } from "@/components/movie/MovieDetailsClient";
import { getMovieDetails } from "@/lib/api/movies";

export async function generateMetadata({
  params,
}: {
  params: Promise<{ id: string }>;
}): Promise<Metadata> {
  const { id } = await params;
  try {
    const movie = await getMovieDetails(Number(id));
    const year = movie.releaseDate
      ? ` (${new Date(movie.releaseDate).getFullYear()})`
      : "";
    return {
      title: `${movie.title}${year} | CinePulse`,
      description:
        movie.overview ||
        `Explore cast, ratings, and details for ${movie.title} on CinePulse.`,
      openGraph: {
        title: `${movie.title}${year}`,
        description: movie.overview || undefined,
        images: movie.posterUrl ? [movie.posterUrl] : [],
      },
    };
  } catch {
    return {
      title: "Movie Details | CinePulse",
      description: "Explore 1.6M+ movies in the CinePulse catalog.",
    };
  }
}

export default async function MoviePage({
  params,
}: {
  params: Promise<{ id: string }>;
}) {
  const { id } = await params;
  return <MovieDetailsClient id={id} />;
}
