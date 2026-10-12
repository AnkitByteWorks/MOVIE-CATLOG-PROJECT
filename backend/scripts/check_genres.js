const genres = db.movies.distinct('genres');
console.log('Distinct movie genres:');
console.log(JSON.stringify(genres, null, 2));
