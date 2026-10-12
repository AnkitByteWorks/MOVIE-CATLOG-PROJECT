const sample = db.movies.findOne({}, { title: 1, releaseDate: 1, popularity: 1, adult: 1 });
console.log('Sample movie:');
console.log(JSON.stringify(sample, null, 2));
console.log('typeof releaseDate:', typeof sample.releaseDate);
console.log('instanceof Date:', sample.releaseDate instanceof Date);

const sample2025 = db.movies.findOne({
  $expr: {
    $and: [
      { $gte: [{ $year: "$releaseDate" }, 2024] }
    ]
  }
}, { title: 1, releaseDate: 1, popularity: 1 });
console.log('Sample 2024+ movie:');
console.log(JSON.stringify(sample2025, null, 2));
