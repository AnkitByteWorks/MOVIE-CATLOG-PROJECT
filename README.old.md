# CinePulse — Movie Catalog & Watchlog Platform

A modern, full-stack movie and TV tracker platform built with Spring Boot microservices on the backend and Next.js 16 (React 19) on the frontend.

---

## 📁 Repository Structure

```text
movie-catlog-project/
├── .env.example              # Environment variables template
├── docker-compose.yml        # Full local container orchestration
├── Makefile                  # Helper commands (up, down, logs, ps, clean, etc.)
├── NOTES.md                  # Project notes and documentation
├── README.md                 # Project root documentation
├── backend/                  # Spring Boot Microservices
│   ├── user-service/         # User Service (Auth, Profiles, JWT) - Port 8081
│   ├── movie-service/        # Movie & Catalog Service (TMDB, Caching) - Port 8082
│   └── watchlog-service/     # Watchlog & Diary Service (Stats, Logs) - Port 8083
├── frontend/                 # Next.js 16 + React 19 Frontend
│   ├── src/
│   │   ├── app/              # Next.js App Router (pages: login, register, profile, watchlog, stats, etc.)
│   │   ├── components/       # UI Components (shadcn/ui, Cinematic Dark theme)
│   │   └── lib/              # Zustand stores, TanStack React Query hooks, API clients
│   ├── package.json
│   └── tailwind.config.ts
├── infrastructure/           # Docker, Kafka, Prometheus & Grafana configs
├── docs/                     # Architecture & system documentation
└── scripts/                  # Utility and database setup scripts
```

---

## 🚀 Quick Start

### 1. Infrastructure Setup
Make sure Docker Desktop is running.
```bash
# Start all infrastructure containers (MySQL, Redis, Kafka, MongoDB, etc.)
docker compose up -d
# Or use the Makefile:
make up
```
Microservices:
- **user-service**: `http://localhost:8081`
- **movie-service**: `http://localhost:8082`
- **watchlog-service**: `http://localhost:8083`

### 2. Frontend Setup
```bash
cd frontend
npm install
npm run dev
```
Open [http://localhost:3000](http://localhost:3000) in your browser.
