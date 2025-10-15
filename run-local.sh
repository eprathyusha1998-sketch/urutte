#!/bin/bash

# Urutte Local Development Runner
# This script runs the application locally for development and testing
# Usage: ./run-local.sh

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Logging function
log() {
    echo -e "${BLUE}[$(date +'%Y-%m-%d %H:%M:%S')]${NC} $1"
}

success() {
    echo -e "${GREEN}✅ $1${NC}"
}

warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

error() {
    echo -e "${RED}❌ $1${NC}"
    exit 1
}

# Configuration
CONFIG_DIR="config"
LOCAL_ENV_FILE="$CONFIG_DIR/local.env"
COMPOSE_FILE="docker-compose.local.yml"

# Check prerequisites
check_prerequisites() {
    log "Checking prerequisites..."
    
    if ! command -v docker &> /dev/null; then
        error "Docker is not installed or not in PATH"
    fi
    
    if ! command -v docker-compose &> /dev/null; then
        error "Docker Compose is not installed or not in PATH"
    fi
    
    if [ ! -f "$LOCAL_ENV_FILE" ]; then
        error "Local environment file not found: $LOCAL_ENV_FILE"
    fi
    
    success "Prerequisites check passed"
}

# Clean up old containers and images
cleanup_old_deployment() {
    log "Cleaning up old deployment..."
    
    # Stop and remove containers
    docker-compose -f $COMPOSE_FILE down 2>/dev/null || true
    
    # Remove old images (keep latest)
    docker images | grep 'urutte-backend-local' | grep -v 'latest' | awk '{print $3}' | xargs -r docker rmi 2>/dev/null || true
    docker images | grep 'urutte-frontend-local' | grep -v 'latest' | awk '{print $3}' | xargs -r docker rmi 2>/dev/null || true
    
    # Clean up dangling images
    docker image prune -f 2>/dev/null || true
    
    success "Cleanup completed"
}

# Build backend
build_backend() {
    log "Building backend application..."
    
    cd backend
    
    # Clean and build
    ./gradlew clean build -x test
    
    # Build Docker image with local configuration
    docker build --platform linux/arm64 -t urutte-backend-local .
    
    cd ..
    
    success "Backend build completed"
}

# Build frontend
build_frontend() {
    log "Building frontend application..."
    
    cd frontend_v2
    
    # Install dependencies if node_modules doesn't exist
    if [ ! -d "node_modules" ]; then
        log "Installing frontend dependencies..."
        npm install
    fi
    
    # Copy local environment file for build
    cp ../$LOCAL_ENV_FILE .env.local
    
    # Build the application
    npm run build
    
    # Build Docker image with local URLs
    docker build --platform linux/arm64 -f Dockerfile.local -t urutte-frontend-local .
    
    cd ..
    
    success "Frontend build completed"
}

# Create local docker-compose file
create_local_compose() {
    log "Creating local docker-compose configuration..."
    
    cat > $COMPOSE_FILE << 'EOF'
services:
  postgres:
    image: postgres:15
    container_name: urutte-postgres-local
    restart: unless-stopped
    environment:
      POSTGRES_DB: urutte_local
      POSTGRES_USER: urutte_user
      POSTGRES_PASSWORD: urutte_pass
    ports:
      - "5432:5432"
    volumes:
      - postgres_data_local:/var/lib/postgresql/data
    networks:
      - urutte-network
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U urutte_user -d urutte_local"]
      interval: 10s
      timeout: 5s
      retries: 5

  backend:
    image: urutte-backend-local
    container_name: urutte-backend-local
    restart: unless-stopped
    env_file:
      - config/local.env
    ports:
      - "8080:8080"
    volumes:
      - backend_uploads_local:/app/uploads
    depends_on:
      postgres:
        condition: service_healthy
    networks:
      - urutte-network
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8080/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3

  frontend:
    image: urutte-frontend-local
    container_name: urutte-frontend-local
    restart: unless-stopped
    ports:
      - "3000:80"
    healthcheck:
      test: ["CMD", "wget", "--quiet", "--tries=1", "--spider", "http://localhost/health"]
      interval: 30s
      timeout: 10s
      retries: 3
    networks:
      - urutte-network

volumes:
  postgres_data_local:
  backend_uploads_local:

networks:
  urutte-network:
    driver: bridge
EOF
    
    success "Local docker-compose configuration created"
}

# Start services
start_services() {
    log "Starting services..."
    
    docker-compose -f $COMPOSE_FILE up -d
    
    success "Services started"
}

# Wait for services to be ready
wait_for_services() {
    log "Waiting for services to be ready..."
    
    # Wait for backend
    log "Waiting for backend to be healthy..."
    timeout=60
    while [ $timeout -gt 0 ]; do
        if curl -s http://localhost:8080/actuator/health > /dev/null 2>&1; then
            success "Backend is ready"
            break
        fi
        sleep 2
        timeout=$((timeout - 2))
    done
    
    if [ $timeout -le 0 ]; then
        warning "Backend health check timeout"
    fi
    
    # Wait for frontend
    log "Waiting for frontend to be ready..."
    timeout=30
    while [ $timeout -gt 0 ]; do
        if curl -s http://localhost:3000 > /dev/null 2>&1; then
            success "Frontend is ready"
            break
        fi
        sleep 2
        timeout=$((timeout - 2))
    done
    
    if [ $timeout -le 0 ]; then
        warning "Frontend health check timeout"
    fi
}

# Show status
show_status() {
    log "Service status:"
    docker-compose -f $COMPOSE_FILE ps
    
    echo -e "\n${GREEN}🎉 Local development environment is ready!"
    echo -e "${NC}======================================"
    echo "Your application is now running locally:"
    echo "  🌐 Frontend: http://localhost:3000"
    echo "  🔧 Backend API: http://localhost:8080/api"
    echo "  📊 Health Check: http://localhost:8080/actuator/health"
    echo "  🗄️  Database: localhost:5432"
    echo ""
    echo "To stop the services:"
    echo "  docker-compose -f $COMPOSE_FILE down"
    echo ""
    echo "To view logs:"
    echo "  docker-compose -f $COMPOSE_FILE logs -f"
    echo ""
    echo "To restart:"
    echo "  ./run-local.sh"
    echo -e "${NC}"
}

# Main deployment function
main() {
    echo -e "${BLUE}"
    echo "🚀 Urutte Local Development Environment"
    echo "======================================"
    echo -e "${NC}"
    
    log "Starting local development setup..."
    
    check_prerequisites
    cleanup_old_deployment
    build_backend
    build_frontend
    create_local_compose
    start_services
    wait_for_services
    show_status
    
    log "Local development environment setup completed!"
}

# Run main function
main "$@"
