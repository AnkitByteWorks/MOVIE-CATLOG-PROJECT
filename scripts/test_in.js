const test1 = db.movies.find({ genres: { $in: ["Drama"] } }).limit(1).toArray();
console.log('test1 { genres: { $in: ["Drama"] } }:', test1.length);

const test2 = db.movies.find({ genres: { $in: [["Drama"]] } }).limit(1).toArray();
console.log('test2 { genres: { $in: [["Drama"]] } }:', test2.length);

const test3 = db.movies.find({ genres: "Drama" }).limit(1).toArray();
console.log('test3 { genres: "Drama" }:', test3.length);
