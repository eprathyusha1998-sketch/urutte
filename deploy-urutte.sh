#!/bin/bash

# Urutte Application Deployment Script
set -e

echo '🚀 Starting Urutte Application Deployment...'

# Configuration
DOMAIN='urutte.com'
DB_PASSWORD='urutte123'
OAUTH_CLIENT_ID='1051698753493-qch8map8fnkeidvtiavlekofneh6bo86.apps.googleusercontent.com'
OAUTH_CLIENT_SECRET='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'

echo '📋 Configuration:'
echo "  Domain: $DOMAIN"
echo "  Database Password: $DB_PASSWORD"
echo "  OAuth Client ID: $OAUTH_CLIENT_ID"
echo ''

# Step 1: Install required dependencies
echo '🔧 Step 1: Installing required dependencies...'

# Update package list
sudo apt update

# Install Java 17 if not already installed
if ! command -v java &> /dev/null; then
    echo 'Installing Java 17...'
    sudo apt install -y openjdk-17-jdk
    echo 'export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64' >> ~/.bashrc
    echo 'export PATH=$JAVA_HOME/bin:$PATH' >> ~/.bashrc
    export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
    export PATH=$JAVA_HOME/bin:$PATH
    echo '✅ Java 17 installed successfully'
else
    echo '✅ Java is already installed'
    # Set JAVA_HOME if not set
    if [ -z "$JAVA_HOME" ]; then
        export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
        export PATH=$JAVA_HOME/bin:$PATH
        echo 'export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64' >> ~/.bashrc
        echo 'export PATH=$JAVA_HOME/bin:$PATH' >> ~/.bashrc
    fi
fi

# Install Node.js if not already installed
if ! command -v node &> /dev/null; then
    echo 'Installing Node.js...'
    curl -fsSL https://deb.nodesource.com/setup_18.x | sudo -E bash -
    sudo apt install -y nodejs
    echo '✅ Node.js installed successfully'
else
    echo '✅ Node.js is already installed'
fi

# Install Docker if not already installed
if ! command -v docker &> /dev/null; then
    echo 'Installing Docker...'
    curl -fsSL https://get.docker.com -o get-docker.sh
    sudo sh get-docker.sh
    sudo usermod -aG docker $USER
    echo '✅ Docker installed successfully'
else
    echo '✅ Docker is already installed'
fi

# Install Docker Compose if not already installed
if ! command -v docker-compose &> /dev/null && ! docker compose version &> /dev/null; then
    echo 'Installing Docker Compose...'
    sudo apt install -y docker-compose-plugin
    echo '✅ Docker Compose installed successfully'
else
    echo '✅ Docker Compose is already installed'
fi

echo '✅ Dependencies installation completed'

# Step 2: Clean up
echo '🧹 Step 2: Cleaning up existing deployment...'
docker compose -f docker-compose.prod.yml down --remove-orphans 2>/dev/null || true
docker system prune -f
echo '✅ Cleanup completed'

# Step 3: Build backend
echo '🔨 Step 3: Building backend application...'
cd backend
./gradlew clean build -x test
docker build -t urutte-backend .
cd ..
echo '✅ Backend built successfully'

# Step 4: Build frontend
echo '🔨 Step 4: Building frontend application...'
cd frontend_v2
npm install
npm run build
docker build -t urutte-frontend .
cd ..
echo '✅ Frontend built successfully'

# Step 5: Create docker-compose.prod.yml
echo '📝 Step 5: Creating production configuration...'
cat > docker-compose.prod.yml << 'COMPOSE_EOF'
version: '3.8'

services:
  postgres:
    image: postgres:15
    container_name: urutte-postgres-prod
    environment:
      POSTGRES_DB: urutte_prod
      POSTGRES_USER: urutte_user
      POSTGRES_PASSWORD: urutte123
    volumes:
      - postgres_data:/var/lib/postgresql/data
    ports:
      - "5432:5432"
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U urutte_user -d urutte_prod"]
      interval: 10s
      timeout: 5s
      retries: 5

  backend:
    image: urutte-backend
    container_name: urutte-backend-prod
    environment:
      SPRING_PROFILES_ACTIVE: prod
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/urutte_prod
      SPRING_DATASOURCE_USERNAME: urutte_user
      SPRING_DATASOURCE_PASSWORD: urutte123
      SPRING_JPA_HIBERNATE_DDL_AUTO: update
      UPLOAD_DIR: /app/uploads
      SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_GOOGLE_CLIENT_ID: 1051698753493-qch8map8fnkeidvtiavlekofneh6bo86.apps.googleusercontent.com
      SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_GOOGLE_CLIENT_SECRET: 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
      SPRING_SECURITY_OAUTH2_CLIENT_PROVIDER_GOOGLE_AUTHORIZATION_URI: https://accounts.google.com/oauth/authorize
      SPRING_SECURITY_OAUTH2_CLIENT_PROVIDER_GOOGLE_TOKEN_URI: https://oauth2.googleapis.com/token
      SPRING_SECURITY_OAUTH2_CLIENT_PROVIDER_GOOGLE_USER_INFO_URI: https://www.googleapis.com/oauth2/v2/userinfo
      SPRING_SECURITY_OAUTH2_CLIENT_PROVIDER_GOOGLE_JWK_SET_URI: https://www.googleapis.com/oauth2/v3/certs
      SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_GOOGLE_REDIRECT_URI: https://urutte.com/oauth2/authorization/google
      APP_OAUTH2_REDIRECT_URI: https://urutte.com/login?token={token}
      APP_FRONTEND_URL: https://urutte.com
      APP_CORS_ALLOWED_ORIGINS: https://urutte.com,https://www.urutte.com
    ports:
      - "8080:8080"
    depends_on:
      postgres:
        condition: service_healthy
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8080/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3

  frontend:
    image: urutte-frontend
    container_name: urutte-frontend-prod
    ports:
      - "3000:80"
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:80/health"]
      interval: 30s
      timeout: 10s
      retries: 3

  nginx:
    image: nginx:alpine
    container_name: urutte-nginx-prod
    ports:
      - "443:443"
    volumes:
      - ./nginx.conf:/etc/nginx/nginx.conf:ro
      - ./ssl:/etc/nginx/ssl:ro
    depends_on:
      - frontend
      - backend

volumes:
  postgres_data:
COMPOSE_EOF

# Step 6: Create nginx configuration
echo '🌐 Step 6: Creating nginx configuration...'
cat > nginx.conf << 'NGINX_EOF'
events {
    worker_connections 1024;
}

http {
    upstream frontend {
        server frontend:80;
    }
    
    upstream backend {
        server backend:8080;
    }
    
    server {
        listen 80;
        server_name urutte.com www.urutte.com;
        return 301 https://$server_name$request_uri;
    }
    
    server {
        listen 443 ssl;
        server_name urutte.com www.urutte.com;
        
        ssl_certificate /etc/nginx/ssl/cert.pem;
        ssl_certificate_key /etc/nginx/ssl/key.pem;
        ssl_protocols TLSv1.2 TLSv1.3;
        ssl_ciphers ECDHE-RSA-AES256-GCM-SHA512:DHE-RSA-AES256-GCM-SHA512:ECDHE-RSA-AES256-GCM-SHA384:DHE-RSA-AES256-GCM-SHA384;
        ssl_prefer_server_ciphers off;
        
        location /oauth2/ {
            proxy_pass http://backend/oauth2/;
            proxy_set_header Host $host;
            proxy_set_header X-Real-IP $remote_addr;
            proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
            proxy_set_header X-Forwarded-Proto $scheme;
        }
        
        location /api/ {
            proxy_pass http://backend/api/;
            proxy_set_header Host $host;
            proxy_set_header X-Real-IP $remote_addr;
            proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
            proxy_set_header X-Forwarded-Proto $scheme;
        }
        
        location /uploads/ {
            proxy_pass http://backend/uploads/;
        }
        
        location / {
            proxy_pass http://frontend;
            proxy_set_header Host $host;
            proxy_set_header X-Real-IP $remote_addr;
            proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
            proxy_set_header X-Forwarded-Proto $scheme;
        }
    }
}
NGINX_EOF

# Step 7: Setup SSL certificates
echo '🔒 Step 7: Setting up SSL certificates...'
if [ ! -d "ssl" ]; then
    mkdir -p ssl
    openssl req -x509 -nodes -days 365 -newkey rsa:2048 \
        -keyout ssl/key.pem \
        -out ssl/cert.pem \
        -subj "/C=US/ST=State/L=City/O=Organization/CN=urutte.com"
    echo '✅ SSL certificates generated'
else
    echo '✅ SSL certificates already exist'
fi

# Step 8: Start services
echo '🚀 Step 8: Starting all services...'
docker compose -f docker-compose.prod.yml up -d

# Step 9: Wait and check
echo '⏳ Step 9: Waiting for services to be ready...'
sleep 30

echo '🏥 Step 10: Running health checks...'
if docker exec urutte-postgres-prod pg_isready -U urutte_user -d urutte_prod > /dev/null 2>&1; then
    echo '✅ Database is healthy'
else
    echo '❌ Database health check failed'
fi

if curl -f http://localhost:8080/actuator/health > /dev/null 2>&1; then
    echo '✅ Backend is healthy'
else
    echo '⚠️  Backend health check failed (may still be starting)'
fi

if curl -f http://localhost:3000/health > /dev/null 2>&1; then
    echo '✅ Frontend is healthy'
else
    echo '⚠️  Frontend health check failed (may still be starting)'
fi

# Test OAuth
echo '🔐 Step 11: Testing OAuth endpoint...'
OAUTH_RESPONSE=$(curl -s -I http://localhost:8080/oauth2/authorization/google)
if echo "$OAUTH_RESPONSE" | grep -q "302"; then
    echo '✅ OAuth endpoint is working'
    OAUTH_URL=$(echo "$OAUTH_RESPONSE" | grep -i 'location:' | cut -d' ' -f2-)
    if echo "$OAUTH_URL" | grep -q "https://urutte.com/oauth2/authorization/google"; then
        echo '✅ OAuth redirect URI is correct'
    else
        echo '⚠️  OAuth redirect URI may need verification'
    fi
else
    echo '⚠️  OAuth endpoint test failed'
fi

echo ''
echo '🎉 Deployment completed successfully!'
echo '=========================================='
echo ''
echo '📊 Service Status:'
docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
echo ''
echo '🌐 Access URLs:'
echo '  Frontend (HTTP):  http://18.191.252.18:3000'
echo '  Frontend (HTTPS): https://urutte.com'
echo '  Backend API:      http://18.191.252.18:8080'
echo '  Database:         localhost:5432'
echo ''
echo '🔐 OAuth Configuration:'
echo "  Client ID: $OAUTH_CLIENT_ID"
echo '  Redirect URI: https://urutte.com/oauth2/authorization/google'
echo ''
echo '⚠️  Important Notes:'
echo '  1. Make sure port 3000 is open in EC2 Security Group'
echo '  2. Make sure port 443 is open in EC2 Security Group'
echo '  3. Update Google OAuth Console with the redirect URI'
echo '  4. Test the login at: http://18.191.252.18:3000'
echo ''
echo '✅ Deployment script completed!'
