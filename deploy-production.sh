#!/bin/bash

# Urutte Production Deployment Script
# This script deploys the application to EC2 production server
# Usage: ./deploy-production.sh

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
PROD_ENV_FILE="$CONFIG_DIR/production.env"
DEPLOY_CONFIG_FILE="deploy-config.env"
COMPOSE_FILE="docker-compose.prod.yml"

# Load deployment configuration
if [ ! -f "$DEPLOY_CONFIG_FILE" ]; then
    error "Deployment configuration file not found: $DEPLOY_CONFIG_FILE"
fi

source "$DEPLOY_CONFIG_FILE"

# Validate required configuration
if [ -z "$SERVER_IP" ] || [ -z "$SERVER_USER" ] || [ -z "$KEY_PATH" ] || [ -z "$DOMAIN" ]; then
    error "Missing required configuration in $DEPLOY_CONFIG_FILE"
fi

# Check prerequisites
check_prerequisites() {
    log "Checking prerequisites..."
    
    if ! command -v docker &> /dev/null; then
        error "Docker is not installed or not in PATH"
    fi
    
    if ! command -v docker-compose &> /dev/null; then
        error "Docker Compose is not installed or not in PATH"
    fi
    
    if [ ! -f "$KEY_PATH" ]; then
        error "SSH key file not found: $KEY_PATH"
    fi
    
    if [ ! -f "$PROD_ENV_FILE" ]; then
        error "Production environment file not found: $PROD_ENV_FILE"
    fi
    
    # Test SSH connection
    if ! ssh -i "$KEY_PATH" -o ConnectTimeout=10 -o BatchMode=yes "$SERVER_USER@$SERVER_IP" exit 2>/dev/null; then
        error "Cannot connect to server $SERVER_IP with user $SERVER_USER"
    fi
    
    success "Prerequisites check passed"
}

# Clean up old local files
cleanup_local_files() {
    log "Cleaning up old local files..."
    
    # Remove old tar files
    rm -f *.tar.gz 2>/dev/null || true
    
    # Remove old test files
    rm -f test-*.html 2>/dev/null || true
    
    # Remove old SQL files (except important ones)
    rm -f fix-database-*.sql 2>/dev/null || true
    
    # Clean up build directories
    rm -rf backend/build 2>/dev/null || true
    rm -rf frontend_v2/build 2>/dev/null || true
    
    success "Local cleanup completed"
}

# Build backend
build_backend() {
    log "Building backend application..."
    
    cd backend
    
    # Clean and build
    ./gradlew clean build -x test
    
    # Build Docker image for production
    docker build --platform linux/amd64 -t urutte-backend-prod .
    
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
    
    # Copy production environment file for build
    cp ../$PROD_ENV_FILE .env.production
    
    # Build the application
    npm run build
    
    # Build Docker image for production
    docker build --platform linux/amd64 -t urutte-frontend-prod .
    
    cd ..
    
    success "Frontend build completed"
}

# Create deployment packages
create_deployment_packages() {
    log "Creating deployment packages..."
    
    # Create backend package
    docker save urutte-backend-prod | gzip > backend-prod.tar.gz
    
    # Create frontend package
    docker save urutte-frontend-prod | gzip > frontend-prod.tar.gz
    
    success "Deployment packages created"
}

# Upload packages to server
upload_packages() {
    log "Uploading packages to server..."
    
    # Upload backend
    scp -i "$KEY_PATH" backend-prod.tar.gz "$SERVER_USER@$SERVER_IP:/home/$SERVER_USER/"
    
    # Upload frontend
    scp -i "$KEY_PATH" frontend-prod.tar.gz "$SERVER_USER@$SERVER_IP:/home/$SERVER_USER/"
    
    # Upload production environment file
    scp -i "$KEY_PATH" "$PROD_ENV_FILE" "$SERVER_USER@$SERVER_IP:/home/$SERVER_USER/"
    
    success "Packages uploaded to server"
}

# Deploy on server
deploy_on_server() {
    log "Deploying on server..."
    
    ssh -i "$KEY_PATH" "$SERVER_USER@$SERVER_IP" << 'EOF'
        set -e
        
        # Load new images
        docker load < backend-prod.tar.gz
        docker load < frontend-prod.tar.gz
        
        # Stop current services (preserve data)
        docker compose -f docker-compose.prod.yml stop backend frontend
        
        # Start services with new images
        docker compose -f docker-compose.prod.yml up -d backend frontend
        
        # Clean up old images (keep latest)
        docker images | grep 'urutte-backend-prod' | grep -v 'latest' | awk '{print $3}' | xargs -r docker rmi 2>/dev/null || true
        docker images | grep 'urutte-frontend-prod' | grep -v 'latest' | awk '{print $3}' | xargs -r docker rmi 2>/dev/null || true
        
        # Clean up old tar files (keep latest)
        rm -f backend-prod-old.tar.gz frontend-prod-old.tar.gz 2>/dev/null || true
        mv backend-prod.tar.gz backend-prod-latest.tar.gz 2>/dev/null || true
        mv frontend-prod.tar.gz frontend-prod-latest.tar.gz 2>/dev/null || true
        
        # Clean up dangling images
        docker image prune -f 2>/dev/null || true
        
        echo "Deployment completed on server"
EOF
    
    success "Deployment on server completed"
}

# Wait for services to be ready
wait_for_services() {
    log "Waiting for services to be ready..."
    
    # Wait for backend
    log "Waiting for backend to be healthy..."
    timeout=60
    while [ $timeout -gt 0 ]; do
        if curl -s "https://$DOMAIN/api/actuator/health" > /dev/null 2>&1; then
            success "Backend is ready"
            break
        fi
        sleep 5
        timeout=$((timeout - 5))
    done
    
    if [ $timeout -le 0 ]; then
        warning "Backend health check timeout"
    fi
    
    # Wait for frontend
    log "Waiting for frontend to be ready..."
    timeout=30
    while [ $timeout -gt 0 ]; do
        if curl -s "https://$DOMAIN" > /dev/null 2>&1; then
            success "Frontend is ready"
            break
        fi
        sleep 5
        timeout=$((timeout - 5))
    done
    
    if [ $timeout -le 0 ]; then
        warning "Frontend health check timeout"
    fi
}

# Show deployment status
show_status() {
    log "Checking deployment status..."
    
    ssh -i "$KEY_PATH" "$SERVER_USER@$SERVER_IP" "docker compose -f docker-compose.prod.yml ps"
    
    echo -e "\n${GREEN}🎉 Production deployment completed!"
    echo -e "${NC}======================================"
    echo "Your application is now running at:"
    echo "  🌐 Frontend: https://$DOMAIN"
    echo "  🔧 Backend API: https://$DOMAIN/api"
    echo "  📊 Health Check: https://$DOMAIN/api/actuator/health"
    echo ""
    echo "To check logs:"
    echo "  ssh -i $KEY_PATH $SERVER_USER@$SERVER_IP 'docker compose -f docker-compose.prod.yml logs -f'"
    echo ""
    echo "To restart services:"
    echo "  ssh -i $KEY_PATH $SERVER_USER@$SERVER_IP 'docker compose -f docker-compose.prod.yml restart'"
    echo -e "${NC}"
}

# Clean up local files after deployment
cleanup_after_deployment() {
    log "Cleaning up local deployment files..."
    
    # Remove deployment packages
    rm -f backend-prod.tar.gz frontend-prod.tar.gz 2>/dev/null || true
    
    # Remove build directories
    rm -rf backend/build 2>/dev/null || true
    rm -rf frontend_v2/build 2>/dev/null || true
    
    success "Local cleanup completed"
}

# Main deployment function
main() {
    echo -e "${BLUE}"
    echo "🚀 Urutte Production Deployment"
    echo "==============================="
    echo -e "${NC}"
    
    log "Starting production deployment to $SERVER_IP..."
    
    check_prerequisites
    cleanup_local_files
    build_backend
    build_frontend
    create_deployment_packages
    upload_packages
    deploy_on_server
    wait_for_services
    show_status
    cleanup_after_deployment
    
    log "Production deployment completed successfully!"
}

# Run main function
main "$@"