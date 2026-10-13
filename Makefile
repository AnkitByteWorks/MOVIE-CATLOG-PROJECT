.PHONY: help up down restart logs ps clean nuke mysql-shell redis-cli kafka-topics mongo-shell movie-build verify-movie

# Default target
.DEFAULT_GOAL := help

help: ## Show this help message with available commands
	@echo "======================================================================"
	@echo "🎬 Movie Tracker Infrastructure Management"
	@echo "======================================================================"
	@echo "Available make commands:"
	@echo "  make up           - Start all infrastructure containers in background"
	@echo "  make down         - Stop all infrastructure containers"
	@echo "  make restart      - Restart all infrastructure containers"
	@echo "  make logs         - Stream logs from all running containers"
	@echo "  make ps           - List status and health of all containers"
	@echo "  make clean        - Stop containers and remove named volumes"
	@echo "  make nuke         - Deep clean: stop containers, wipe volumes and networks"
	@echo "  make mysql-shell  - Open an interactive MySQL shell as root"
	@echo "  make redis-cli    - Open an interactive Redis CLI"
	@echo "  make mongo-shell  - Open an interactive MongoDB shell"
	@echo "  make kafka-topics - List all active Kafka topics"
	@echo "======================================================================"

up: ## Start all infrastructure containers in background
	@echo "🚀 Starting Movie Tracker infrastructure..."
	docker compose up -d

down: ## Stop all infrastructure containers
	@echo "🛑 Stopping Movie Tracker infrastructure..."
	docker compose down

restart: down up ## Restart all infrastructure containers

logs: ## Stream logs from all running containers
	@echo "📜 Streaming infrastructure logs (Ctrl+C to exit)..."
	docker compose logs -f

ps: ## List status and health of all containers
	docker compose ps

clean: ## Stop containers and remove named volumes
	@echo "⚠️  Stopping containers and removing persistent volumes..."
	docker compose down -v

nuke: ## Deep clean: stop containers, wipe volumes, remove orphaned networks
	@echo "💥 Nuking all containers, volumes, and networks..."
	docker compose down -v --remove-orphans

mysql-shell: ## Open an interactive MySQL shell as root
	@echo "🐬 Connecting to MySQL shell..."
	docker exec -it movietracker-mysql mysql -uroot -proot movietracker_user

redis-cli: ## Open an interactive Redis CLI
	@echo "⚡ Connecting to Redis CLI..."
	docker exec -it movietracker-redis redis-cli

mongo-shell: ## Open an interactive MongoDB shell
	@echo "🍃 Connecting to MongoDB shell..."
	docker exec -it movietracker-mongo mongosh -u movietracker -p movietracker --authenticationDatabase admin movietracker_movies

kafka-topics: ## List all active Kafka topics
	@echo "📨 Listing Kafka topics..."
	docker exec -it movietracker-kafka /opt/kafka/bin/kafka-topics.sh --bootstrap-server localhost:29092 --list

movie-build: ## Build movie-service docker image
	@echo "🔨 Building movie-service container..."
	docker compose build movie-service

verify-movie: ## Check movie-service endpoints
	@echo "🩺 Checking movie-service health..."
	@curl -s http://localhost:8082/api/v1/health
	@echo "\n🛡️ Checking TMDB circuit breaker..."
	@curl -s http://localhost:8082/api/v1/health/tmdb
	@echo "\n🔍 Checking movie search (Inception)..."
	@curl -s "http://localhost:8082/api/v1/movies/search?q=inception&page=1"
	@echo "\n🎭 Checking genres..."
	@curl -s http://localhost:8082/api/v1/genres | cut -c 1-120
	@echo "..."
