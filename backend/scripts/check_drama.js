const opp = db.movies.findOne({ title: "Oppenheimer" }, { title: 1, genres: 1, genreIds: 1 });
console.log('Oppenheimer genres:');
console.log(JSON.stringify(opp, null, 2));

const genreTest = db.movies.find({ genres: { $in: ["Drama"] }, adult: { $ne: true } }).limit(2).toArray();
console.log('Drama query test:', genreTest.length);
if (genreTest.length > 0) {
  console.log(genreTest[0].title, genreTest[0].genres);
}
