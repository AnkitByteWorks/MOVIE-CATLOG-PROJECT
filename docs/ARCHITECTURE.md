# 🏛️ Architecture Documentation: Movie Tracker Catalog Service

## 1. Executive Summary

`movie-service` is the high-throughput catalog and discovery backbone of the **Movie Tracker** platform. It provides instant text search, detailed metadata retrieval, trending discovery, genre-based categorization, and content recommendations across **1.67M+ titles** (1,508,074 movies and 164,705 TV shows).

Originally conceived as a proxy to The Movie Database (TMDB) API, the service underwent a **data-first architectural pivot** to achieve zero external runtime dependencies, sub-20ms search response times, and immune resilience against regional ISP throttling or blocking.

```
                              ┌──────────────────────────┐
                              │    Client / Gateway      │
                              └────────────┬─────────────┘
                                           │ HTTP / REST
                                           ▼
                              ┌──────────────────────────┐
                              │      movie-service       │
                              │  (Java 25 / Spring Boot) │
                              └─────┬──────────────┬─────┘
                                    │              │
                   Cache Miss (10-25ms)      Cache Hit (1-3ms)
                                    │              │
                                    ▼              ▼
                           ┌────────────────┐ ┌─────────┐
                           │ MongoDB 7.0    │ │ Redis   │
                           │ 1.67M+ Titles  │ │ 7.4     │
                           │ Text Index     │ │ Cache   │
                           └────────────────┘ └─────────┘
```

---

## 2. The Architectural Pivot: From External API to In-House Catalog

### 2.1 The Problem: Third-Party Fragility & Regional ISP Censorship
The initial system architecture integrated TMDB via Spring Cloud OpenFeign:
1. **Regional ISP Blocking:** In certain jurisdictions (e.g. India), access to `api.themoviedb.org` is blocked at the ISP/DNS level, causing connection timeouts and SSL handshake failures (`Connection refused`, `UnknownHostException`).
2. **Workaround Complexity & Tech Debt:**
   - Attempted multi-tier CORS proxies (`corsproxy.io`, `api.allorigins.win`).
   - Custom `DnsAwareSSLSocketFactory` resolving IPs via Cloudflare/Google DNS-over-HTTPS.
   - CloudFront IP hardcoding and host mapping hacks in Docker Compose.
   - Multi-layer Resilience4j circuit breakers, retries, and rate limiters.
   - Fallback dummy seeders inserting only 3 mock movies (`Inception`, `The Dark Knight`, `Interstellar`).
3. **Flaws in Production:** Proxies introduce latency (300ms–2000ms), external rate limits, security surface vulnerabilities, and downtime risk. A mock dataset of 3 movies is unacceptable for a production-grade portfolio system.

### 2.2 The Solution: The 1.67 Million Title Data Warehouse
Instead of patching an unstable network bridge, we pivoted to an **offline data ingestion model**:
- **Dataset Source:** Comprehensive TMDB daily snapshots and Kaggle datasets covering world cinema, Hollywood, regional Indian film industries (Bollywood, Tollywood, Kollywood), and international television.
- **Bulk Ingestion:** Streamed and batch-inserted **1,508,074 movies** and **164,705 TV shows** directly into MongoDB 7.0 with write batches of 5,000 documents.
- **Clean Separation:** Completely purged OpenFeign, Resilience4j, CORS proxies, DNS socket factories, and mock seeders from the runtime codebase.
- **Result:**
  - **Uptime:** 100% independent of TMDB availability or regional network blocks.
  - **Latency:** Decreased from ~800ms (overseas proxy) to ~15ms (local MongoDB text query) and ~2ms (Redis cache hit).
  - **Zero Cost:** No paid proxy subscriptions or TMDB API keys required at runtime.

---

## 3. Data Store & Search Indexing Strategy

### 3.1 MongoDB 7.0 Document Model
The catalog data resides in the `movietracker_movies` database across four core collections:
- `movies`: 1,508,074 documents containing title, overview, release dates, runtime, popularity, ratings, budget, revenue, and embedded cast/crew.
- `tv_shows`: 164,705 documents containing series name, seasons, episodes, network tags, popularity, and overview.
- `genres`: 35 normalized categories across cinema and television.
- `catalog_metadata`: Ingestion audit log tracking data sources, version, document totals, and timestamp.

### 3.2 Full-Text Search Optimization
To enable search across 1.67M titles without external engines like Elasticsearch:
- **Movies Compound Text Index:**
  ```javascript
  db.movies.createIndex(
    { title: "text", overview: "text" },
    { weights: { title: 10, overview: 2 }, name: "title_text_overview_text" }
  )
  ```
- **TV Shows Compound Text Index:**
  ```javascript
  db.tv_shows.createIndex(
    { name: "text", overview: "text" },
    { weights: { name: 10, overview: 2 }, name: "name_text_overview_text" }
  )
  ```
- **Operational Secondary Indexes:**
  - `{ tmdbId: 1 }` (Unique): O(1) single-title detail lookups.
  - `{ popularity: -1 }`: Instant pagination for Trending and Popular sections.
  - `{ genres: 1 }` (Multikey): Fast genre filtering and recommendation lookups.

---

## 4. Multi-Tier Distributed Caching (Redis 7.4)

### 4.1 Caching Design
Spring Cache abstraction backed by Redis 7.4 prevents redundant text index evaluations for high-frequency queries. Caching is decoupled into `CachedMovieLookupService` to ensure clean Spring AOP proxy interception without self-invocation bypass issues.

### 4.2 TTL & Invalidation Policy

| Cache Name | Cache Key Format | TTL | Rationale |
| :--- | :--- | :--- | :--- |
| `movieSearch` | `movie:search:{query}:{page}` | 6 Hours | Absorbs search spikes for popular titles. |
| `movieDetails`| `movie:details:{tmdbId}` | 24 Hours | Movie metadata is static and rarely changes. |
| `trending` | `movie:trending:{window}:{page}` | 2 Hours | Updates periodically to reflect top popular titles. |
| `recommendations`| `movie:recommendations:{tmdbId}` | 24 Hours | Genre-based relationships remain stable. |
| `tvSearch` | `tv:search:{query}:{page}` | 6 Hours | Television query optimization. |
| `tvDetails` | `tv:details:{tmdbId}` | 24 Hours | TV show metadata caching. |

---

## 5. API Interface Contract

All endpoints follow standard REST semantics under `/api/v1`:

### Movies
- `GET /api/v1/movies/search?q={query}&page={page}`: Text search over 1.5M movies.
- `GET /api/v1/movies/{id}`: Detailed metadata, cast, crew, budget, revenue, and vote averages.
- `GET /api/v1/movies/trending?window={day|week}&page={page}`: Popularity-ranked releases.
- `GET /api/v1/movies/popular?page={page}`: Global popularity leaderboards.
- `GET /api/v1/movies/{id}/recommendations`: Similar titles sharing genres, excluding target title.
- `GET /api/v1/movies/genre/{genre}?page={page}`: Genre filtered catalog.

### Television
- `GET /api/v1/tv/search?q={query}&page={page}`: Text search over 164K TV series.
- `GET /api/v1/tv/{id}`: TV details including seasons, episodes, and networks.
- `GET /api/v1/tv/trending?window={day|week}&page={page}`: Trending TV series.
- `GET /api/v1/tv/popular?page={page}`: Top popular TV shows.

### Catalog Diagnostics & Health
- `GET /api/v1/health`: Basic service liveness.
- `GET /api/v1/health/catalog`: Returns live O(1) document counts from MongoDB metadata and import timestamp.
- `GET /api/v1/genres`: Master list of 35 film and TV genres.

---

## 6. Interview Talking Points

1. **Trade-off Analysis (API vs. Local Data Store):**
   - *Third-party API:* Immediate access to live updates, but subject to vendor rate limits, external outages, censorship, latency jitter, and recurring costs.
   - *In-House MongoDB Catalog:* Eliminates network unpredictability, offers deterministic <20ms performance, enables custom indexing, but requires scheduled or batch updates for new releases.
2. **Why MongoDB Text Index over Elasticsearch for this phase?**
   - MongoDB text search natively handles language stemming, phrase searches, and weighted fields for 1.67M titles in <25ms without the operational overhead of a separate 3-node Elasticsearch/OpenSearch cluster.
3. **Resilience & Fault Tolerance:**
   - Removing fragile HTTP proxies and fallbacks reduced lines of code by ~40%, eliminated intermittent socket exceptions, and removed unnecessary circuit breakers that were masking root networking issues.
