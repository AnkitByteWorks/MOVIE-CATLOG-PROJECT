# CinePulse — Movie Catalog & Watchlog Platform

A modern, full-stack movie and TV tracker platform built with Spring Boot microservices on the backend and Next.js 16 (React 19) on the frontend.

---

## 📁 Repository Structure

```text
movie-catlog-project/
├── backend/                  # Spring Boot Microservices
│   ├── src/                  # User Service (Auth, Profiles, JWT) - Port 8081
│   ├── movie-service/        # Movie & Catalog Service (TMDB, Caching) - Port 8082
│   ├── watchlog-service/     # Watchlog & Diary Service (Stats, Logs) - Port 8083
│   ├── infrastructure/       # Docker, Kafka, Prometheus & Grafana configs
│   └── docker-compose.yml    # Full local container orchestration
│
└── frontend/                 # Next.js 16 + React 19 Frontend
    ├── src/
    │   ├── app/              # Next.js App Router (pages: login, register, profile, watchlog, stats, etc.)
    │   ├── components/       # UI Components (shadcn/ui, Cinematic Dark theme)
    │   └── lib/              # Zustand stores, TanStack React Query hooks, API clients
    ├── package.json
    └── tailwind.config.ts
```

---

## 🚀 Quick Start

### 1. Backend Setup
Make sure Docker Desktop is running.
```bash
cd backend
docker-compose up -d
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
