export interface MovieSummary {
  id?: string;
  tmdbId: number;
  title: string;
  adult?: boolean;
  posterPath?: string | null;
  posterUrl?: string | null;
  backdropPath?: string | null;
  backdropUrl?: string | null;
  releaseDate?: string | null;
  voteAverage: number;
  voteCount: number;
  popularity?: number;
  overview?: string;
  genres: string[];
  mediaType?: string;
}

export interface TvShowSummary {
  id?: string;
  tmdbId: number;
  name: string;
  adult?: boolean;
  posterPath?: string | null;
  posterUrl?: string | null;
  backdropPath?: string | null;
  backdropUrl?: string | null;
  firstAirDate?: string | null;
  voteAverage: number;
  voteCount: number;
  popularity?: number;
  overview?: string;
  genres: string[];
  mediaType?: string;
}

export interface CastMember {
  id: number;
  name: string;
  character: string;
  profilePath: string | null;
}

export interface CrewMember {
  id: number;
  name: string;
  job: string;
  department: string;
}

export interface MovieDetails {
  id?: string;
  tmdbId: number;
  title: string;
  originalTitle?: string;
  adult?: boolean;
  posterPath?: string | null;
  posterUrl?: string | null;
  backdropPath?: string | null;
  backdropUrl?: string | null;
  releaseDate?: string | null;
  voteAverage: number;
  voteCount: number;
  popularity?: number;
  overview?: string;
  genres: string[];
  tagline?: string | null;
  runtime?: number | null;
  budget?: number;
  revenue?: number;
  status?: string;
  originalLanguage?: string;
  imdbId?: string | null;
  homepage?: string | null;
  cast?: CastMember[];
  crew?: CrewMember[];
}

export interface TvShowDetails {
  id?: string;
  tmdbId: number;
  name: string;
  originalName?: string;
  adult?: boolean;
  posterPath?: string | null;
  posterUrl?: string | null;
  backdropPath?: string | null;
  backdropUrl?: string | null;
  firstAirDate?: string | null;
  lastAirDate?: string | null;
  numberOfSeasons?: number;
  numberOfEpisodes?: number;
  episodeRunTime?: number[];
  inProduction?: boolean;
  voteAverage: number;
  voteCount: number;
  popularity?: number;
  overview?: string;
  genres: string[];
  tagline?: string | null;
  status?: string;
  networks?: string[];
  createdBy?: string[];
  cast?: CastMember[];
  crew?: CrewMember[];
}

export interface PagedResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  last: boolean;
}

export interface Genre {
  id?: number | string;
  name: string;
  type?: string;
}

export type WatchLocation = 'THEATER' | 'HOME' | 'STREAMING' | 'PLANE' | 'OTHER';

export interface WatchLog {
  id: string;
  tmdbId: number;
  mediaType: 'MOVIE' | 'TV';
  title: string;
  posterUrl: string | null;
  backdropUrl: string | null;
  watchedAt: string;
  rating: number | null; // 1-10
  review: string | null;
  watchLocation: WatchLocation | null;
  rewatchCount: number;
  createdAt: string;
  updatedAt: string;
}

export interface CreateWatchLogRequest {
  tmdbId: number;
  mediaType: 'MOVIE' | 'TV';
  watchedAt: string;
  rating?: number | null;
  review?: string | null;
  watchLocation?: WatchLocation | null;
}

export interface UpdateWatchLogRequest {
  rating?: number | null;
  review?: string | null;
  watchLocation?: WatchLocation | null;
}

export interface TopGenreStat {
  genre: string;
  count: number;
}

export interface TopActorStat {
  name: string;
  count: number;
}

export interface WatchStats {
  userId?: string;
  username?: string;
  totalWatches?: number;
  totalMovies: number;
  totalTvShows: number;
  totalEpisodes?: number;
  totalWatchTimeMinutes: number;
  formattedWatchTime?: string;
  totalWatchTimeFormatted?: string;
  averageRating: number | null;
  ratedCount?: number;
  currentStreak?: number;
  watchStreak?: number;
  longestStreak: number;
  thisMonthWatches?: number;
  thisYearWatches?: number;
  thisMonth?: { movies: number; tvEpisodes: number };
  thisYear?: { movies: number; tvEpisodes: number };
  topGenres: TopGenreStat[];
  topActors: TopActorStat[];
  byLocation: Partial<Record<WatchLocation, number>>;
  favoriteDecade?: string | null;
  firstWatch?: string | null;
  lastWatch?: string | null;
}

export interface AuthUser {
  id: string;
  username: string;
  email: string;
  displayName: string | null;
  role: 'USER' | 'ADMIN' | 'MODERATOR';
  enabled: boolean;
  emailVerified: boolean;
  createdAt: string;
  lastLoginAt: string | null;
  bio?: string | null;
  avatarUrl?: string | null;
  location?: string | null;
  website?: string | null;
}

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: 'Bearer';
  expiresIn: number;
  user: AuthUser;
}

export interface RegisterRequest {
  username: string;
  email: string;
  password: string;
  displayName?: string;
}

export interface LoginRequest {
  usernameOrEmail: string;
  password: string;
}

export interface UpdateProfileRequest {
  displayName?: string;
  bio?: string;
  avatarUrl?: string;
  location?: string;
  website?: string;
}

export interface ChangePasswordRequest {
  currentPassword: string;
  newPassword: string;
}
