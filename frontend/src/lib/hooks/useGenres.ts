"use client";

import { useQuery } from "@tanstack/react-query";
import { getGenres } from "@/lib/api/movies";

export function useGenres(type?: "MOVIE" | "TV") {
  return useQuery({
    queryKey: ["genres", type || "all"],
    queryFn: () => getGenres(type),
    staleTime: Infinity,
  });
}
