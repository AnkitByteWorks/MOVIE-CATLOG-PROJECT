import { clsx, type ClassValue } from "clsx";
import { twMerge } from "tailwind-merge";

export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs));
}

export function formatDate(isoDate: string | null | undefined): string {
  if (!isoDate) return "N/A";
  const date = new Date(isoDate);
  if (isNaN(date.getTime())) return "N/A";

  return new Intl.DateTimeFormat("en-US", {
    month: "short",
    day: "numeric",
    year: "numeric",
  }).format(date);
}

export function formatRuntime(minutes: number | null | undefined): string {
  if (!minutes || minutes <= 0) return "N/A";
  const hours = Math.floor(minutes / 60);
  const remainingMinutes = minutes % 60;

  if (hours === 0) {
    return `${remainingMinutes}m`;
  }
  if (remainingMinutes === 0) {
    return `${hours}h`;
  }
  return `${hours}h ${remainingMinutes}m`;
}

export function formatVoteCount(count: number | null | undefined): string {
  if (count === null || count === undefined) return "0";
  if (count < 1000) return count.toString();
  if (count < 1000000) {
    return `${(count / 1000).toFixed(1).replace(/\.0$/, "")}k`;
  }
  return `${(count / 1000000).toFixed(1).replace(/\.0$/, "")}M`;
}

const TMDB_IMAGE_BASE =
  process.env.NEXT_PUBLIC_IMAGE_BASE_URL || "https://image.tmdb.org/t/p";

export const POSTER_FALLBACK =
  "data:image/svg+xml;charset=UTF-8,%3Csvg%20xmlns%3D%22http%3A%2F%2Fwww.w3.org%2F2000%2Fsvg%22%20width%3D%22500%22%20height%3D%22750%22%20viewBox%3D%220%200%20500%20750%22%3E%3Crect%20fill%3D%22%23131318%22%20width%3D%22500%22%20height%3D%22750%22%2F%3E%3Ctext%20fill%3D%22%2371717a%22%20font-family%3D%22sans-serif%22%20font-size%3D%2224%22%20x%3D%2250%25%22%20y%3D%2250%25%22%20text-anchor%3D%22middle%22%20dominant-baseline%3D%22middle%22%3ENo%20Poster%20Available%3C%2Ftext%3E%3C%2Fsvg%3E";

export const BACKDROP_FALLBACK =
  "data:image/svg+xml;charset=UTF-8,%3Csvg%20xmlns%3D%22http%3A%2F%2Fwww.w3.org%2F2000%2Fsvg%22%20width%3D%221280%22%20height%3D%22720%22%20viewBox%3D%220%200%201280%20720%22%3E%3Crect%20fill%3D%22%23131318%22%20width%3D%221280%22%20height%3D%22720%22%2F%3E%3Ctext%20fill%3D%22%2371717a%22%20font-family%3D%22sans-serif%22%20font-size%3D%2232%22%20x%3D%2250%25%22%20y%3D%2250%25%22%20text-anchor%3D%22middle%22%20dominant-baseline%3D%22middle%22%3ENo%20Backdrop%20Available%3C%2Ftext%3E%3C%2Fsvg%3E";

export function buildPosterUrl(
  path: string | null | undefined,
  size: "w342" | "w500" | "original" = "w500"
): string {
  if (!path) return POSTER_FALLBACK;
  if (path.startsWith("http://") || path.startsWith("https://")) return path;
  const cleanPath = path.startsWith("/") ? path : `/${path}`;
  return `${TMDB_IMAGE_BASE}/${size}${cleanPath}`;
}

export function buildBackdropUrl(
  path: string | null | undefined,
  size: "w780" | "w1280" | "original" = "w1280"
): string {
  if (!path) return BACKDROP_FALLBACK;
  if (path.startsWith("http://") || path.startsWith("https://")) return path;
  const cleanPath = path.startsWith("/") ? path : `/${path}`;
  return `${TMDB_IMAGE_BASE}/${size}${cleanPath}`;
}
