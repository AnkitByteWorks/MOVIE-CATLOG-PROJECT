# 🎬 Movie Tracker

![Java](https://img.shields.io/badge/Java-25%20LTS-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.1-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![MongoDB](https://img.shields.io/badge/MongoDB-7.0-47A248?style=for-the-badge&logo=mongodb&logoColor=white)
![Redis](https://img.shields.io/badge/Redis-7.4-DC382D?style=for-the-badge&logo=redis&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?style=for-the-badge&logo=docker&logoColor=white)

Movie Tracker is an event-driven microservices platform engineered to track, rate, and discover films and television series, powered by an in-house catalog of **1.67M+ titles** (1,508,074 movies and 164,705 TV shows), sub-20ms MongoDB full-text search, distributed Redis caching, and asynchronous notifications.

---

## 🏛️ System Architecture

Detailed architectural documentation, trade-off analysis, and system evolution logs are available in [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md).

```text
                           ┌─────────────────────────┐
                           │   Client / Frontend     │
                           │      (React 19)         │
                           └────────────┬────────────┘
                                        │
                                        ▼
                           ┌─────────────────────────┐
                           │      API Gateway        │
                           │     (Port: 8080)        │
                           └────────────┬────────────┘
                                        │
             ┌──────────────────────────┼──────────────────────────┐
             ▼                          ▼                          ▼
  ┌─────────────────────┐    ┌─────────────────────┐    ┌─────────────────────┐
  │    user-service     │    │    movie-service    │    │   watchlog-service  │
  │     (Port: 8081)    │    │     (Port: 8082)    │    │     (Port: 8083)    │
  └──────────┬──────────┘    └──────────┬──────────┘    └──────────┬──────────┘
             │                          │                          │
             ├─────────────┐            │                          │
             ▼             ▼            ▼                          ▼
        ┌─────────┐   ┌─────────┐  ┌─────────┐                ┌─────────┐
        │  MySQL  │   │  Redis  │  │ MongoDB │                │  MySQL  │
        │ (3306)  │   │ (6379)  │  │ (27017) │                │ (3306)  │
        └─────────┘   └─────────┘  └─────────┘                └─────────┘
             │                          │                          │
             └──────────────────────────┼──────────────────────────┘
                                        │
                                        ▼
                           ┌─────────────────────────┐
                           │   Apache Kafka (KRaft)  │
                           │   (Port: 9092 / 29092)  │
                           └────────────┬────────────┘
                                        │
                                        ▼
                           ┌─────────────────────────┐
                           │   notification-service  │
                           │       (Consumer)        │
                           └─────────────────────────┘
```

---

## 🛠️ Technology Stack

| Component | Technology | Version | Purpose |
| :--- | :--- | :--- | :--- |
| **Language** | Java (OpenJDK) | 25 LTS | Core backend runtime with virtual threads |
| **Framework** | Spring Boot | 4.1.1 | Microservice framework & dependency injection |
| **Relational DB** | MySQL Server | 8.4 LTS | Relational storage for users, auth, & watchlogs |
| **Document DB** | MongoDB | 7.0 | Document store for 1.67M+ movies, TV series, & text index |
| **Caching** | Redis | 7.4 Alpine | Distributed caching, session store, token blacklist |
| **Event Broker** | Apache Kafka | 3.9 (KRaft) | Asynchronous event streaming & pub/sub |
| **Metrics** | Prometheus | Latest | Time-series metrics collection |
| **Visualization**| Grafana | Latest | Telemetry & performance dashboards |
| **Container Engine**| Docker Compose | Modern Compose | Local infrastructure orchestration |

---

## 📋 Prerequisites

- **Docker Desktop** (v24+ with Compose v2.20+)
- **Java JDK 25** (for local service compilation)
- **Make** (optional, recommended for CLI task execution)

---

## 🚀 Quick Start

### 1. Configure Environment
Copy the environment template and customize credentials if needed:
```bash
cp .env.example .env
```

### 2. Boot Infrastructure & Microservices
Start all databases, brokers, and active microservices:
```bash
docker compose up -d
```

### 3. Check Container Health
Verify that all services report healthy status:
```bash
docker compose ps
```

### 4. Query Catalog Endpoints (movie-service on :8082)
```bash
# Health & live catalog counts
curl -s http://localhost:8082/api/v1/health/catalog

# Full-text movie search (sub-20ms)
curl -s "http://localhost:8082/api/v1/movies/search?q=inception"

# Bollywood / Regional Indian cinema
curl -s "http://localhost:8082/api/v1/movies/search?q=dangal"

# TV show search
curl -s "http://localhost:8082/api/v1/tv/search?q=panchayat"

# Trending movies
curl -s "http://localhost:8082/api/v1/movies/trending?window=day"
```

---

## 🌐 Services & Port Allocation

| Service | Container Name | Host Port | Internal Port | Description / URL |
| :--- | :--- | :--- | :--- | :--- |
| **movie-service** | `movietracker-movie-service` | `8082` | `8082` | Movie & TV catalog, text search, Redis cache |
| **MySQL** | `movietracker-mysql` | `3307` | `3306` | Relational database (`user`, `watchlog`) |
| **Redis** | `movietracker-redis` | `6379` | `6379` | Distributed cache server |
| **Kafka (KRaft)** | `movietracker-kafka` | `9092` | `29092` | Event bus & stream broker |
| **Kafka UI** | `movietracker-kafka-ui` | `8090` | `8080` | Web UI at [http://localhost:8090](http://localhost:8090) |
| **MongoDB** | `movietracker-mongo` | `27017` | `27017` | Document database (1.67M+ titles) |
| **Mongo Express** | `movietracker-mongo-express`| `8081` | `8081` | Web GUI at [http://localhost:8081](http://localhost:8081) |
| **RedisInsight** | `movietracker-redis-insight` | `5540` | `5540` | Redis GUI at [http://localhost:5540](http://localhost:5540) |
| **Prometheus** | `movietracker-prometheus` | `9090` | `9090` | Metrics dashboard at [http://localhost:9090](http://localhost:9090) |
| **Grafana** | `movietracker-grafana` | `3001` | `3000` | Dashboards at [http://localhost:3001](http://localhost:3001) (`admin`/`admin`) |

---

## 📁 Repository Structure

```text
movie-tracker/
├── docs/
│   └── ARCHITECTURE.md         # In-depth architectural design & pivot analysis
├── infrastructure/
│   ├── mysql/init/             # Initial database DDL & permissions
│   ├── redis/                  # Tuned redis.conf cache configuration
│   ├── kafka/                  # Kafka broker volumes and scripts
│   ├── mongo/init/             # Initial collections and compound text indexes
│   ├── prometheus/             # Prometheus metric scrape configurations
│   └── grafana/provisioning/   # Automatic datasource and dashboard provisioning
├── movie-service/              # Movie & TV catalog service (MongoDB + Redis)
├── src/                        # user-service microservice source code
├── docker-compose.yml          # Local developer infrastructure orchestration
├── Makefile                    # Developer ergonomics CLI tasks
├── .env                        # Local developer environment variables
├── .env.example                # Safe environment variable template
└── NOTES.md                    # Architectural decision log & lessons learned
```

---

## 🗺️ Roadmap & Milestones

- [x] **Step 1:** Microservice base setup & POM dependencies
- [x] **Step 2:** Enterprise scaffolding, security filter chains & exception handlers
- [x] **Step 3:** Full local Docker Compose infrastructure layer (MySQL, Redis, Kafka, MongoDB, Observability)
- [x] **Step 4:** User authentication & JWT token issuance business logic
- [x] **Step 5:** Movie catalog service & 1.67M title MongoDB search engine
- [ ] **Step 6:** Watchlog service & Kafka event publishing
- [ ] **Step 7:** Notification consumer service
- [ ] **Step 8:** React 19 web application frontend
