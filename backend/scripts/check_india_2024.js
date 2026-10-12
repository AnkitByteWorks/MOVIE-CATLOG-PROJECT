const topIndia2024 = db.movies.find(
  {
    originalLanguage: { $in: ['hi', 'te', 'ta', 'ml', 'kn', 'pa', 'bn'] },
    releaseDate: { $gte: new Date('2024-01-01') },
    adult: false
  },
  { title: 1, releaseDate: 1, popularity: 1, originalLanguage: 1, tmdbId: 1, posterPath: 1 }
).sort({ popularity: -1 }).limit(15).toArray();

console.log('Top 2024+ Indian movies:');
console.log(JSON.stringify(topIndia2024, null, 2));
