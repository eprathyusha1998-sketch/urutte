#!/bin/bash

# Urutte.com Deployment Script
# This script automates the deployment process on AWS EC2

set -e  # Exit on any error

echo "🚀 Starting Urutte.com deployment..."

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
DOMAIN=${1:-"localhost"}
DB_PASSWORD=${2:-"secure_password_123"}
ENVIRONMENT=${3:-"production"}

echo -e "${BLUE}📋 Deployment Configuration:${NC}"
echo "Domain: $DOMAIN"
echo "Environment: $ENVIRONMENT"
echo "Database Password: [HIDDEN]"
echo ""

# Function to print status
print_status() {
    echo -e "${GREEN}✅ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

print_error() {
    echo -e "${RED}❌ $1${NC}"
}

# Check if running as root
if [ "$EUID" -eq 0 ]; then
    print_error "Please don't run this script as root. Use a regular user with sudo privileges."
    exit 1
fi

# Update system
print_status "Updating system packages..."
sudo apt update && sudo apt upgrade -y

# Install Docker if not installed
if ! command -v docker &> /dev/null; then
    print_status "Installing Docker..."
    curl -fsSL https://get.docker.com -o get-docker.sh
    sudo sh get-docker.sh
    sudo usermod -aG docker $USER
    rm get-docker.sh
    
    # Start and enable Docker service
    sudo systemctl start docker
    sudo systemctl enable docker
    
    print_warning "Docker installed. Please logout and login again to apply Docker group changes, then run this script again."
    exit 0
fi

# Check if Docker daemon is running
if ! docker info &> /dev/null; then
    print_status "Starting Docker daemon..."
    sudo systemctl start docker
    sudo systemctl enable docker
    
    # Wait a moment for Docker to start
    sleep 3
    
    # Check if user is in docker group
    if ! groups $USER | grep -q docker; then
        print_status "Adding user to docker group..."
        sudo usermod -aG docker $USER
        print_warning "User added to docker group. Please logout and login again, then run this script again."
        exit 0
    fi
    
    # If still can't access Docker, try with sudo
    if ! docker info &> /dev/null; then
        print_warning "Docker daemon is running but user permissions not applied yet."
        print_warning "Please logout and login again, or run: newgrp docker"
        print_warning "Then run this script again."
        exit 0
    fi
fi

# Install Docker Compose if not installed
if ! command -v docker-compose &> /dev/null; then
    print_status "Installing Docker Compose..."
    
    # Try to install via apt first (Ubuntu 24 has it in repos)
    if sudo apt install docker-compose-plugin -y 2>/dev/null; then
        print_status "Docker Compose installed via apt"
    else
        # Fallback to manual installation
        sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
        sudo chmod +x /usr/local/bin/docker-compose
    fi
fi

# Verify Docker Compose installation
if ! command -v docker-compose &> /dev/null && ! docker compose version &> /dev/null; then
    print_error "Docker Compose installation failed"
    exit 1
fi

# Install Nginx if not installed
if ! command -v nginx &> /dev/null; then
    print_status "Installing Nginx..."
    sudo apt install nginx -y
    sudo systemctl start nginx
    sudo systemctl enable nginx
fi

# Create production environment file
print_status "Creating production environment configuration..."
cat > .env.production << EOF
# Database
POSTGRES_DB=urutte_prod
POSTGRES_USER=urutte_user
POSTGRES_PASSWORD=$DB_PASSWORD
POSTGRES_HOST=postgres
POSTGRES_PORT=5432

# Backend
SPRING_PROFILES_ACTIVE=prod
SERVER_PORT=8080
SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/urutte_prod
SPRING_DATASOURCE_USERNAME=urutte_user
SPRING_DATASOURCE_PASSWORD=$DB_PASSWORD

# Frontend
REACT_APP_API_URL=http://$DOMAIN/api
REACT_APP_WS_URL=ws://$DOMAIN/ws

# File Upload
UPLOAD_DIR=/app/uploads
EOF

# Create production docker-compose file
print_status "Creating production Docker Compose configuration..."
cat > docker-compose.prod.yml << EOF
version: '3.8'

services:
  postgres:
    image: postgres:15
    container_name: urutte-postgres-prod
    environment:
      POSTGRES_DB: urutte_prod
      POSTGRES_USER: urutte_user
      POSTGRES_PASSWORD: $DB_PASSWORD
    volumes:
      - postgres_data:/var/lib/postgresql/data
    ports:
      - "5432:5432"
    restart: unless-stopped
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U urutte_user -d urutte_prod"]
      interval: 30s
      timeout: 10s
      retries: 3

  backend:
    build:
      context: ./backend
      dockerfile: Dockerfile
    container_name: urutte-backend-prod
    environment:
      SPRING_PROFILES_ACTIVE: prod
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/urutte_prod
      SPRING_DATASOURCE_USERNAME: urutte_user
      SPRING_DATASOURCE_PASSWORD: $DB_PASSWORD
      UPLOAD_DIR: /app/uploads
    ports:
      - "8080:8080"
    depends_on:
      postgres:
        condition: service_healthy
    restart: unless-stopped
    volumes:
      - ./uploads:/app/uploads
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8080/api/health"]
      interval: 30s
      timeout: 10s
      retries: 3

  frontend:
    build:
      context: ./frontend_v2
      dockerfile: Dockerfile
      args:
        - REACT_APP_API_URL=http://$DOMAIN/api
        - REACT_APP_WS_URL=ws://$DOMAIN/ws
    container_name: urutte-frontend-prod
    ports:
      - "3000:80"
    depends_on:
      - backend
    restart: unless-stopped
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost/health"]
      interval: 30s
      timeout: 10s
      retries: 3

volumes:
  postgres_data:
EOF

# Create uploads directory
print_status "Creating uploads directory..."
mkdir -p uploads
chmod 755 uploads

# Build and start services
print_status "Building and starting services..."

# Use docker compose (new syntax) if available, otherwise docker-compose
if docker compose version &> /dev/null; then
    DOCKER_COMPOSE_CMD="docker compose"
else
    DOCKER_COMPOSE_CMD="docker-compose"
fi

$DOCKER_COMPOSE_CMD -f docker-compose.prod.yml down --remove-orphans
$DOCKER_COMPOSE_CMD -f docker-compose.prod.yml up -d --build

# Wait for services to be healthy
print_status "Waiting for services to be healthy..."
sleep 30

# Check service health
print_status "Checking service health..."
if $DOCKER_COMPOSE_CMD -f docker-compose.prod.yml ps | grep -q "Up (healthy)"; then
    print_status "All services are healthy!"
else
    print_warning "Some services may not be healthy. Check logs with: $DOCKER_COMPOSE_CMD -f docker-compose.prod.yml logs"
fi

# Configure Nginx
print_status "Configuring Nginx reverse proxy..."
sudo tee /etc/nginx/sites-available/urutte.com > /dev/null << EOF
server {
    listen 80;
    server_name $DOMAIN;

    # Frontend (React)
    location / {
        proxy_pass http://localhost:3000;
        proxy_http_version 1.1;
        proxy_set_header Upgrade \$http_upgrade;
        proxy_set_header Connection 'upgrade';
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto \$scheme;
        proxy_cache_bypass \$http_upgrade;
    }

    # Backend API
    location /api/ {
        proxy_pass http://localhost:8080/api/;
        proxy_http_version 1.1;
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto \$scheme;
    }

    # WebSocket support
    location /ws/ {
        proxy_pass http://localhost:8080/ws/;
        proxy_http_version 1.1;
        proxy_set_header Upgrade \$http_upgrade;
        proxy_set_header Connection "upgrade";
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto \$scheme;
    }

    # Static files
    location /uploads/ {
        proxy_pass http://localhost:8080/uploads/;
    }
}
EOF

# Enable site
sudo ln -sf /etc/nginx/sites-available/urutte.com /etc/nginx/sites-enabled/
sudo rm -f /etc/nginx/sites-enabled/default

# Test and restart Nginx
sudo nginx -t && sudo systemctl restart nginx

# Configure firewall
print_status "Configuring firewall..."
sudo ufw allow ssh
sudo ufw allow 'Nginx Full'
sudo ufw --force enable

# Create backup script
print_status "Creating backup script..."
cat > backup.sh << 'EOF'
#!/bin/bash
DATE=$(date +%Y%m%d_%H%M%S)
BACKUP_DIR="/home/$USER/backups"

mkdir -p $BACKUP_DIR

# Backup database
docker exec urutte-postgres-prod pg_dump -U urutte_user urutte_prod > $BACKUP_DIR/db_backup_$DATE.sql

# Backup uploads
tar -czf $BACKUP_DIR/uploads_backup_$DATE.tar.gz uploads/

# Keep only last 7 days of backups
find $BACKUP_DIR -name "*.sql" -mtime +7 -delete
find $BACKUP_DIR -name "*.tar.gz" -mtime +7 -delete

echo "Backup completed: $DATE"
EOF

chmod +x backup.sh

# Create update script
print_status "Creating update script..."
cat > update.sh << 'EOF'
#!/bin/bash
echo "🔄 Updating Urutte.com application..."

# Pull latest changes
git pull

# Rebuild and restart services
# Use docker compose (new syntax) if available, otherwise docker-compose
if docker compose version &> /dev/null; then
    DOCKER_COMPOSE_CMD="docker compose"
else
    DOCKER_COMPOSE_CMD="docker-compose"
fi

$DOCKER_COMPOSE_CMD -f docker-compose.prod.yml down
$DOCKER_COMPOSE_CMD -f docker-compose.prod.yml up -d --build

echo "✅ Update completed!"
EOF

chmod +x update.sh

# Final status check
print_status "Performing final status check..."
echo ""
echo -e "${BLUE}📊 Service Status:${NC}"
$DOCKER_COMPOSE_CMD -f docker-compose.prod.yml ps

echo ""
echo -e "${BLUE}🌐 Application URLs:${NC}"
echo "Frontend: http://$DOMAIN"
echo "Backend API: http://$DOMAIN/api"
echo "Health Check: http://$DOMAIN/api/health"

echo ""
echo -e "${BLUE}📝 Useful Commands:${NC}"
echo "View logs: $DOCKER_COMPOSE_CMD -f docker-compose.prod.yml logs -f"
echo "Restart services: $DOCKER_COMPOSE_CMD -f docker-compose.prod.yml restart"
echo "Update application: ./update.sh"
echo "Backup data: ./backup.sh"

echo ""
print_status "Deployment completed successfully! 🎉"
print_warning "If you're using a domain name, make sure to:"
echo "1. Point your domain to this server's IP address"
echo "2. Install SSL certificate: sudo certbot --nginx -d $DOMAIN"
echo "3. Update REACT_APP_API_URL and REACT_APP_WS_URL to use HTTPS"

echo ""
echo -e "${GREEN}Your Thread-like app is now live at: http://$DOMAIN${NC}"
