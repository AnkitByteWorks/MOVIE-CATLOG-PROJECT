const count2024 = db.movies.countDocuments({
  releaseDate: { $gte: new Date('2024-01-01'), $lte: new Date('2024-12-31') },
  adult: false
});
console.log('Total 2024 movies:', count2024);

const count2025 = db.movies.countDocuments({
  releaseDate: { $gte: new Date('2025-01-01'), $lte: new Date('2025-12-31') },
  adult: false
});
console.log('Total 2025 movies:', count2025);

const top2024 = db.movies.find(
  {
    releaseDate: { $gte: new Date('2024-01-01') },
    adult: false,
    popularity: { $gt: 5 }
  },
  { title: 1, releaseDate: 1, popularity: 1, voteCount: 1, originalLanguage: 1 }
).sort({ popularity: -1 }).limit(10).toArray();

console.log('Top 2024+ by popularity:');
console.log(JSON.stringify(top2024, null, 2));
