#!/bin/bash

# Urutte Project Status Script
# Shows current project status and available commands

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
NC='\033[0m' # No Color

echo -e "${BLUE}"
echo "🚀 Urutte Project Status"
echo "======================="
echo -e "${NC}"

# Check if scripts exist and are executable
check_script() {
    local script="$1"
    local description="$2"
    
    if [ -f "$script" ] && [ -x "$script" ]; then
        echo -e "${GREEN}✅ $description${NC}"
    else
        echo -e "${RED}❌ $description (missing or not executable)${NC}"
    fi
}

# Check configuration files
check_config() {
    local config="$1"
    local description="$2"
    
    if [ -f "$config" ]; then
        echo -e "${GREEN}✅ $description${NC}"
    else
        echo -e "${RED}❌ $description (missing)${NC}"
    fi
}

echo -e "${PURPLE}📋 Available Scripts:${NC}"
check_script "run-local.sh" "Local Development Runner"
check_script "deploy-production.sh" "Production Deployment Script"
check_script "cleanup-project.sh" "Project Cleanup Utility"

echo -e "\n${PURPLE}⚙️  Configuration Files:${NC}"
check_config "deploy-config.env" "Deployment Configuration"
check_config "config/local.env" "Local Environment Settings"
check_config "config/production.env" "Production Environment Settings"

echo -e "\n${PURPLE}🐳 Docker Compose Files:${NC}"
check_config "docker-compose.local.yml" "Local Docker Compose"
check_config "docker-compose.prod.yml" "Production Docker Compose"

echo -e "\n${PURPLE}📚 Documentation:${NC}"
check_config "DEPLOYMENT_GUIDE.md" "Deployment Guide"

echo -e "\n${PURPLE}🚀 Quick Commands:${NC}"
echo -e "${BLUE}Local Development:${NC}"
echo "  ./run-local.sh                    # Start local development environment"
echo "  docker-compose -f docker-compose.local.yml down  # Stop local environment"

echo -e "\n${BLUE}Production Deployment:${NC}"
echo "  ./deploy-production.sh            # Deploy to EC2 production server"

echo -e "\n${BLUE}Maintenance:${NC}"
echo "  ./cleanup-project.sh              # Clean up project files"
echo "  ./cleanup-project.sh --dry-run    # Preview cleanup (no changes)"

echo -e "\n${PURPLE}📊 Current Project Size:${NC}"
if command -v du &> /dev/null; then
    echo "  Total size: $(du -sh . 2>/dev/null | cut -f1)"
    echo "  Backend: $(du -sh backend 2>/dev/null | cut -f1)"
    echo "  Frontend: $(du -sh frontend_v2 2>/dev/null | cut -f1)"
else
    echo "  Size calculation not available"
fi

echo -e "\n${PURPLE}🗂️  Key Directories:${NC}"
echo "  config/          - Environment configurations"
echo "  scripts/         - Utility scripts"
echo "  backend/         - Spring Boot backend"
echo "  frontend_v2/     - React frontend"
echo "  backups/         - Database backups"
echo "  uploads/         - User uploads"

echo -e "\n${GREEN}✨ Project is ready for development and deployment!${NC}"
echo -e "${YELLOW}💡 Tip: Run './cleanup-project.sh --dry-run' to see what can be cleaned up${NC}"
