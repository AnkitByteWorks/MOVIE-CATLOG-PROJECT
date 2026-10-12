const fs = require('fs');
const path = require('path');
const { spawn } = require('child_process');
const { Transform, pipeline } = require('stream');

const ARCHIVE_DIR = 'C:\\Users\\ankit\\Downloads\\archive';
const MOVIE_CSV = path.join(ARCHIVE_DIR, 'TMDB_movie_dataset_v11.csv');
const TV_CSV = path.join(ARCHIVE_DIR, 'TMDB_tv_dataset_v3.csv');

function parseCSVLine(text) {
    const result = [];
    let cur = '';
    let inQuotes = false;
    for (let i = 0; i < text.length; i++) {
        const c = text[i];
        if (c === '"') {
            if (inQuotes && text[i + 1] === '"') {
                cur += '"';
                i++;
            } else {
                inQuotes = !inQuotes;
            }
        } else if (c === ',' && !inQuotes) {
            result.push(cur);
            cur = '';
        } else {
            cur += c;
        }
    }
    result.push(cur);
    return result;
}

class CsvToMongoJsonTransform extends Transform {
    constructor(type) {
        super();
        this.type = type;
        this.buffer = '';
        this.inQuotes = false;
        this.isFirstLine = true;
        this.count = 0;
        this.nowIso = new Date().toISOString();
    }

    _transform(chunk, encoding, callback) {
        const str = chunk.toString('utf8');
        for (let i = 0; i < str.length; i++) {
            const c = str[i];
            if (c === '"') {
                if (this.inQuotes && str[i + 1] === '"') {
                    this.buffer += '""';
                    i++;
                } else {
                    this.inQuotes = !this.inQuotes;
                    this.buffer += '"';
                }
            } else if ((c === '\n' || c === '\r') && !this.inQuotes) {
                if (c === '\r' && str[i + 1] === '\n') {
                    i++;
                }
                if (this.buffer.length > 0) {
                    const jsonLine = this.convertRecord(this.buffer);
                    if (jsonLine) {
                        this.push(jsonLine + '\n');
                        this.count++;
                        if (this.count % 50000 === 0) {
                            console.log(`Streamed ${this.count} ${this.type} records to mongoimport...`);
                        }
                    }
                    this.buffer = '';
                }
            } else {
                this.buffer += c;
            }
        }
        callback();
    }

    _flush(callback) {
        if (this.buffer.length > 0) {
            const jsonLine = this.convertRecord(this.buffer);
            if (jsonLine) {
                this.push(jsonLine + '\n');
            }
        }
        callback();
    }

    convertRecord(rawLine) {
        if (this.isFirstLine) {
            this.isFirstLine = false;
            return null;
        }
        if (!rawLine || rawLine.trim().length === 0) {
            return null;
        }

        const r = parseCSVLine(rawLine);
        if (!r || r.length < 2 || !r[0] || isNaN(Number(r[0]))) {
            return null;
        }

        if (this.type === 'movie') {
            const doc = {
                tmdbId: { "$numberLong": r[0].trim() },
                title: r[1] || "",
                voteAverage: parseFloat(r[2]) || 0.0,
                voteCount: parseInt(r[3], 10) || 0,
                status: r[4] || null,
                releaseDate: /^\d{4}-\d{2}-\d{2}$/.test(r[5]?.trim()) ? { "$date": r[5].trim() + "T00:00:00Z" } : null,
                revenue: { "$numberLong": String(Math.floor(Number(r[6]) || 0)) },
                runtime: parseInt(r[7], 10) || null,
                adult: r[8]?.toLowerCase() === 'true',
                backdropPath: r[9] || null,
                budget: { "$numberLong": String(Math.floor(Number(r[10]) || 0)) },
                homepage: r[11] || null,
                imdbId: r[12] || null,
                originalLanguage: r[13] || null,
                originalTitle: r[14] || null,
                overview: r[15] || "",
                popularity: parseFloat(r[16]) || 0.0,
                posterPath: r[17] || null,
                tagline: r[18] || null,
                genres: r[19] ? r[19].split(',').map(s => s.trim()).filter(Boolean) : [],
                genreIds: [],
                cast: [],
                crew: [],
                createdAt: { "$date": this.nowIso },
                updatedAt: { "$date": this.nowIso },
                lastSyncedFromTmdb: { "$date": this.nowIso },
                _class: 'com.movietracker.movieservice.entity.Movie'
            };
            return JSON.stringify(doc);
        } else {
            const doc = {
                tmdbId: { "$numberLong": r[0].trim() },
                name: r[1] || "",
                numberOfSeasons: parseInt(r[2], 10) || null,
                numberOfEpisodes: parseInt(r[3], 10) || null,
                originalLanguage: r[4] || null,
                voteCount: parseInt(r[5], 10) || 0,
                voteAverage: parseFloat(r[6]) || 0.0,
                overview: r[7] || "",
                backdropPath: r[9] || null,
                firstAirDate: /^\d{4}-\d{2}-\d{2}$/.test(r[10]?.trim()) ? { "$date": r[10].trim() + "T00:00:00Z" } : null,
                lastAirDate: /^\d{4}-\d{2}-\d{2}$/.test(r[11]?.trim()) ? { "$date": r[11].trim() + "T00:00:00Z" } : null,
                inProduction: r[13]?.toLowerCase() === 'true',
                originalName: r[14] || null,
                popularity: parseFloat(r[15]) || 0.0,
                posterPath: r[16] || null,
                status: r[18] || null,
                tagline: r[19] || null,
                genres: r[20] ? r[20].split(',').map(s => s.trim()).filter(Boolean) : [],
                createdBy: r[21] ? r[21].split(',').map(s => s.trim()).filter(Boolean) : [],
                networks: r[23] ? r[23].split(',').map(s => s.trim()).filter(Boolean) : [],
                episodeRunTime: r[28] && Number(r[28]) ? [parseInt(r[28], 10)] : [],
                genreIds: [],
                cast: [],
                crew: [],
                createdAt: { "$date": this.nowIso },
                updatedAt: { "$date": this.nowIso },
                lastSyncedFromTmdb: { "$date": this.nowIso },
                _class: 'com.movietracker.movieservice.entity.TvShow'
            };
            return JSON.stringify(doc);
        }
    }
}

function importFile(csvPath, type) {
    return new Promise((resolve, reject) => {
        if (!fs.existsSync(csvPath)) {
            console.error(`File not found: ${csvPath}`);
            return reject(new Error(`File not found: ${csvPath}`));
        }

        const collection = type === 'movie' ? 'movies' : 'tv_shows';
        console.log(`\n======================================================`);
        console.log(`Starting bulk import for ${type.toUpperCase()} from: ${csvPath}`);
        console.log(`Target MongoDB collection: ${collection}`);
        console.log(`======================================================\n`);

        const mongoimport = spawn('docker', [
            'exec', '-i', 'movietracker-mongo',
            'mongoimport',
            '-u', 'movietracker',
            '-p', 'movietracker',
            '--authenticationDatabase', 'admin',
            '--db', 'movietracker_movies',
            '--collection', collection,
            '--type', 'json',
            '--mode', 'upsert',
            '--upsertFields', 'tmdbId',
            '--numInsertionWorkers', '4'
        ], { stdio: ['pipe', 'inherit', 'inherit'] });

        const fileStream = fs.createReadStream(csvPath, { encoding: 'utf8', highWaterMark: 256 * 1024 });
        const transformStream = new CsvToMongoJsonTransform(type);

        pipeline(fileStream, transformStream, mongoimport.stdin, (err) => {
            if (err) {
                console.error('Pipeline error:', err);
                reject(err);
            }
        });

        mongoimport.on('close', (code) => {
            if (code === 0) {
                console.log(`\n[SUCCESS] Completed import for ${collection}! (${transformStream.count} records processed)`);
                resolve();
            } else {
                console.error(`mongoimport exited with code ${code}`);
                reject(new Error(`mongoimport failed with code ${code}`));
            }
        });

        mongoimport.on('error', (err) => {
            console.error('mongoimport spawn error:', err);
            reject(err);
        });
    });
}

async function createIndexes() {
    console.log('\nCreating text and search indexes in MongoDB...');
    const commands = [
        'db.movies.createIndex({ title: "text", overview: "text" }, { name: "title_text_overview_text", background: true })',
        'db.movies.createIndex({ tmdbId: 1 }, { unique: true, sparse: true })',
        'db.tv_shows.createIndex({ name: "text", overview: "text" }, { name: "name_text_overview_text", background: true })',
        'db.tv_shows.createIndex({ tmdbId: 1 }, { unique: true, sparse: true })'
    ];
    for (const cmd of commands) {
        await new Promise((resolve) => {
            const child = spawn('docker', [
                'exec', '-i', 'movietracker-mongo',
                'mongosh',
                '-u', 'movietracker',
                '-p', 'movietracker',
                '--authenticationDatabase', 'admin',
                'movietracker_movies',
                '--eval', cmd
            ], { stdio: 'inherit' });
            child.on('close', resolve);
        });
    }
}

async function run() {
    try {
        console.log('Starting full catalog import into MongoDB movietracker_movies...');
        if (fs.existsSync(MOVIE_CSV)) {
            await importFile(MOVIE_CSV, 'movie');
        } else {
            console.warn(`Movie CSV not found at: ${MOVIE_CSV}`);
        }

        if (fs.existsSync(TV_CSV)) {
            await importFile(TV_CSV, 'tv');
        } else {
            console.warn(`TV CSV not found at: ${TV_CSV}`);
        }

        await createIndexes();
        console.log('\n======================================================');
        console.log('ALL IMPORTS AND INDEXES SUCCESSFULLY COMPLETED!');
        console.log('======================================================\n');
    } catch (err) {
        console.error('Import failed with error:', err);
        process.exit(1);
    }
}

run();
