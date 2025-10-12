#!/bin/bash

# Docker Permissions Fix Script for Ubuntu 24
# Run this script if you encounter Docker permission issues

set -e

echo "🔧 Fixing Docker permissions for Ubuntu 24..."

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

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

# Check if Docker is installed
if ! command -v docker &> /dev/null; then
    print_error "Docker is not installed. Please install Docker first."
    echo "Run: curl -fsSL https://get.docker.com -o get-docker.sh && sudo sh get-docker.sh"
    exit 1
fi

print_status "Docker is installed"

# Start Docker service if not running
if ! sudo systemctl is-active --quiet docker; then
    print_status "Starting Docker service..."
    sudo systemctl start docker
    sudo systemctl enable docker
else
    print_status "Docker service is running"
fi

# Add user to docker group if not already added
if ! groups $USER | grep -q docker; then
    print_status "Adding user '$USER' to docker group..."
    sudo usermod -aG docker $USER
    print_warning "User added to docker group."
else
    print_status "User is already in docker group"
fi

# Check if user can access Docker
if docker info &> /dev/null; then
    print_status "Docker permissions are working correctly!"
    echo ""
    echo -e "${BLUE}You can now run the deployment script:${NC}"
    echo "./deploy.sh your-domain.com your-db-password"
else
    print_warning "Docker permissions not yet applied."
    echo ""
    echo -e "${YELLOW}To apply the changes, you need to:${NC}"
    echo "1. Logout and login again, OR"
    echo "2. Run: newgrp docker"
    echo "3. Then run the deployment script again"
    echo ""
    echo -e "${BLUE}Alternative: Run deployment with sudo (not recommended for production):${NC}"
    echo "sudo ./deploy.sh your-domain.com your-db-password"
fi

echo ""
print_status "Docker permissions fix completed!"
