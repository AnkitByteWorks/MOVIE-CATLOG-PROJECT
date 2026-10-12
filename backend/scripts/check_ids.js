const ids = [857598, 801688, 784651, 622792, 869760, 936622, 1146210, 811941, 1184889, 693134, 533535, 1022789, 976573, 939243, 912649, 786892, 823464];
const found = db.movies.find({ tmdbId: { $in: ids } }, { tmdbId: 1, title: 1, releaseDate: 1, posterPath: 1 }).toArray();
console.log('Found ' + found.length + ' of ' + ids.length + ' movies:');
found.forEach(m => console.log(m.tmdbId + ' -> ' + m.title + ' (' + m.releaseDate + ') poster: ' + m.posterPath));
