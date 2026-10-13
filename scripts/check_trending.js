const indiaMovies = db.movies.find(
  {
    originalLanguage: { $in: ['hi', 'te', 'ta', 'ml', 'kn', 'pa', 'bn'] },
    adult: false,
    releaseDate: { $gte: '2025-01-01', $lte: '2026-12-31' },
    posterPath: { $ne: null }
  },
  { title: 1, releaseDate: 1, popularity: 1, originalLanguage: 1, posterPath: 1 }
).sort({ popularity: -1 }).limit(10).toArray();

console.log('--- TOP 2025-2026 INDIA ---');
console.log(JSON.stringify(indiaMovies, null, 2));

const globalMovies = db.movies.find(
  {
    adult: false,
    releaseDate: { $gte: '2025-01-01', $lte: '2026-12-31' },
    voteCount: { $gte: 1 },
    posterPath: { $ne: null }
  },
  { title: 1, releaseDate: 1, popularity: 1, voteCount: 1, posterPath: 1 }
).sort({ popularity: -1 }).limit(10).toArray();

console.log('--- TOP 2025-2026 GLOBAL ---');
console.log(JSON.stringify(globalMovies, null, 2));
