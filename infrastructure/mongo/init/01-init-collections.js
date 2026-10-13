
// ====================================================================
// Movie Tracker — MongoDB Initialization Script
// Executed on initial database container bootstrap
// ====================================================================

db = db.getSiblingDB('movietracker_movies');
db.createCollection('movies');
db.createCollection('tv_shows');
db.createCollection('people');
db.createCollection('genres');

// Indexes
db.movies.createIndex({ tmdbId: 1 }, { unique: true, sparse: true });
db.movies.createIndex({ title: "text", overview: "text" });
db.movies.createIndex({ genres: 1 });
db.movies.createIndex({ releaseDate: -1 });
db.movies.createIndex({ popularity: -1 });

db.tv_shows.createIndex({ tmdbId: 1 }, { unique: true, sparse: true });
db.tv_shows.createIndex({ name: "text", overview: "text" });
db.tv_shows.createIndex({ firstAirDate: -1 });

print('✅ movietracker_movies initialized with collections + indexes');
