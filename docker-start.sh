#!/bin/bash

# Urutte Docker Start Script
# This script helps you start the application with Docker

set -e

echo "╔════════════════════════════════════════╗"
echo "║   Urutte - Social Platform Setup      ║"
echo "╚════════════════════════════════════════╝"
echo ""

# Check if Docker is installed
if ! command -v docker &> /dev/null; then
    echo "❌ Error: Docker is not installed"
    echo "Please install Docker from: https://www.docker.com/products/docker-desktop"
    exit 1
fi

# Check if Docker Compose is installed
if ! command -v docker-compose &> /dev/null; then
    echo "❌ Error: Docker Compose is not installed"
    echo "Please install Docker Compose"
    exit 1
fi

echo "✅ Docker and Docker Compose are installed"
echo ""

# Check if .env file exists
if [ ! -f .env ]; then
    echo "⚠️  Warning: .env file not found"
    echo ""
    echo "Creating .env file from template..."
    
    if [ -f env.docker.example ]; then
        cp env.docker.example .env
        echo "✅ Created .env file"
        echo ""
        echo "⚠️  IMPORTANT: Please edit .env and add your Google OAuth credentials:"
        echo "   - GOOGLE_CLIENT_ID"
        echo "   - GOOGLE_CLIENT_SECRET"
        echo ""
        echo "Get credentials from: https://console.cloud.google.com/apis/credentials"
        echo ""
        read -p "Press Enter after you've updated the .env file..."
    else
        echo "❌ Error: env.docker.example not found"
        exit 1
    fi
fi

echo ""
echo "📦 Starting Docker containers..."
echo ""
echo "This will:"
echo "  1. Build the frontend (React + TypeScript)"
echo "  2. Build the backend (Spring Boot + Java)"
echo "  3. Start PostgreSQL database"
echo "  4. Configure networking between services"
echo ""
echo "This may take a few minutes on first run..."
echo ""

# Stop existing containers if any
echo "🛑 Stopping any existing containers..."
docker-compose down 2>/dev/null || true

# Build and start services
echo ""
echo "🏗️  Building and starting services..."
docker-compose up --build -d

# Wait for services to be ready
echo ""
echo "⏳ Waiting for services to be ready..."
echo ""

# Wait for PostgreSQL
echo "   Waiting for PostgreSQL..."
until docker exec urutte-postgres pg_isready -U urutte_user &>/dev/null; do
    sleep 1
done
echo "   ✅ PostgreSQL is ready"

# Wait for Backend
echo "   Waiting for Backend..."
for i in {1..60}; do
    if curl -s http://localhost:8080/actuator/health &>/dev/null; then
        echo "   ✅ Backend is ready"
        break
    fi
    if [ $i -eq 60 ]; then
        echo "   ⚠️  Backend took longer than expected to start"
        echo "   Check logs with: docker-compose logs backend"
    fi
    sleep 2
done

# Wait for Frontend
echo "   Waiting for Frontend..."
for i in {1..30}; do
    if curl -s http://localhost/health &>/dev/null; then
        echo "   ✅ Frontend is ready"
        break
    fi
    if [ $i -eq 30 ]; then
        echo "   ⚠️  Frontend took longer than expected to start"
        echo "   Check logs with: docker-compose logs frontend"
    fi
    sleep 1
done

echo ""
echo "╔════════════════════════════════════════╗"
echo "║          🎉 SUCCESS! 🎉                ║"
echo "╚════════════════════════════════════════╝"
echo ""
echo "Your application is running!"
echo ""
echo "📍 Access URLs:"
echo "   Frontend:  http://localhost"
echo "   Backend:   http://localhost:8080"
echo "   API:       http://localhost:8080/api"
echo ""
echo "📊 View logs:"
echo "   All:       docker-compose logs -f"
echo "   Frontend:  docker-compose logs -f frontend"
echo "   Backend:   docker-compose logs -f backend"
echo "   Database:  docker-compose logs -f postgres"
echo ""
echo "🛑 Stop application:"
echo "   docker-compose stop"
echo ""
echo "🔄 Restart application:"
echo "   docker-compose restart"
echo ""
echo "🗑️  Remove containers:"
echo "   docker-compose down"
echo ""
echo "Opening browser to http://localhost..."
echo ""

# Try to open browser (works on macOS, Linux, and WSL)
if command -v open &> /dev/null; then
    open http://localhost
elif command -v xdg-open &> /dev/null; then
    xdg-open http://localhost
elif command -v wslview &> /dev/null; then
    wslview http://localhost
else
    echo "Please open http://localhost in your browser"
fi

echo ""
echo "Enjoy! 🚀"

