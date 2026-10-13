# Design Decisions & Notes

This file tracks architectural decisions, gotchas, and lessons learned
while building Movie Tracker.

---

## Step 3 — Infrastructure
- Chose KRaft mode Kafka (no Zookeeper) for simpler ops
- Redis maxmemory 256MB, allkeys-lru policy (cache workload)
- MySQL 8.4 with default caching_sha2_password (removed deprecated --default-authentication-plugin flag)
- Mapped MySQL host port to 3307 (`3307:3306`) to prevent collisions with host Windows MySQL services
- Switched Kafka to official `apache/kafka:3.9.0` with KRaft metadata mode
- Named volumes so `make down` preserves data; `make nuke` wipes it

---

## Step 4 — JWT Authentication & Security
- Modern JJWT 0.13.0 API with HMAC-SHA256 and strict >=256 bits secret validation on startup
- Refresh token rotation (RTR) stored in Redis under `refresh:token:{uuid}` key format with auto-expiration
- Spring Kafka producer configured with idempotence (`acks=all`, `retries=3`) publishing `user-events`
- `DisabledException` mapped to HTTP 403 Forbidden in `GlobalExceptionHandler`
- `UserPrincipal` extends Spring Security `User` ensuring clean integration while retaining database UUID
- Encapsulated `@EnableJpaAuditing` inside `JpaAuditingConfig` to avoid slice-test context contamination

---

## Step 5 — Movie Catalog Service & The Data-First Pivot
- **Context & Problem:** TMDB API (`api.themoviedb.org`) is blocked at the ISP/DNS level in India. Initial workarounds introduced Feign clients, public CORS proxies (`corsproxy.io`, `allorigins.win`), custom `DnsAwareSSLSocketFactory`, and hardcoded CloudFront IPs.
- **Why Proxies Failed:** External proxies added variable latency (300ms–2000ms), third-party rate limits, fragile URL encoding inside HTTP interceptors, and external points of failure.
- **The Data-First Pivot:** Instead of maintaining a fragile proxy layer to an external API, we imported 1,508,074 movies and 164,705 TV shows directly from Kaggle TMDB datasets into local MongoDB 7.0.
- **Full-Text Search:** Built compound text indexes (`title_text_overview_text` with weights 10:2 for movies, `name_text_overview_text` for TV shows), unique indexes on `tmdbId`, and descending indexes on `popularity`.
- **Simplification & Code Health:**
  - Deleted OpenFeign, Resilience4j, DNS socket factories, and mock seeders.
  - Purged TMDB API keys from `.env`, `.env.example`, `application.yml`, and `docker-compose.yml`.
  - Service now only depends on MongoDB (data store) and Redis (cache).
- **Performance Results:**
  - Search queries execute in ~10-25ms against 1.67M titles via MongoDB text index.
  - Redis cache hits return in ~1-3ms.
  - 100% operational reliability with zero external network latency or vendor outages.
