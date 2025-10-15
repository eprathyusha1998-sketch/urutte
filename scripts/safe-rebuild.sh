#!/bin/bash

# Urutte Safe Rebuild Script
# This script safely rebuilds your application while preserving data

set -e

# Configuration
ENVIRONMENT=${1:-"local"}
BACKUP_BEFORE_REBUILD=${2:-"true"}

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${GREEN}🔄 Urutte Safe Rebuild Script${NC}"
echo -e "${YELLOW}Environment: $ENVIRONMENT${NC}"
echo -e "${YELLOW}Backup before rebuild: $BACKUP_BEFORE_REBUILD${NC}"
echo ""

# Function to show usage
show_usage() {
    echo -e "${YELLOW}Usage: $0 [environment] [backup]${NC}"
    echo -e "${YELLOW}Environments: local, prod, default${NC}"
    echo -e "${YELLOW}Backup: true, false (default: true)${NC}"
    echo ""
    echo -e "${BLUE}Examples:${NC}"
    echo -e "  $0 local true    # Rebuild local with backup"
    echo -e "  $0 prod false    # Rebuild production without backup"
    echo -e "  $0               # Rebuild default with backup"
}

# Function to backup data
backup_data() {
    local env=$1
    echo -e "${BLUE}📦 Creating backup before rebuild...${NC}"
    
    if ./scripts/backup-data.sh "$env"; then
        echo -e "${GREEN}✅ Backup completed successfully${NC}"
    else
        echo -e "${RED}❌ Backup failed! Aborting rebuild.${NC}"
        exit 1
    fi
}

# Function to stop containers
stop_containers() {
    local env=$1
    echo -e "${BLUE}🛑 Stopping containers...${NC}"
    
    case $env in
        "local")
            docker-compose down
            ;;
        "prod")
            docker-compose -f docker-compose.prod.yml down
            ;;
        *)
            docker-compose down
            ;;
    esac
    
    echo -e "${GREEN}✅ Containers stopped${NC}"
}

# Function to rebuild containers
rebuild_containers() {
    local env=$1
    echo -e "${BLUE}🔨 Rebuilding containers...${NC}"
    
    case $env in
        "local")
            docker-compose up -d --build
            ;;
        "prod")
            docker-compose -f docker-compose.prod.yml up -d --build
            ;;
        *)
            docker-compose up -d --build
            ;;
    esac
    
    echo -e "${GREEN}✅ Containers rebuilt and started${NC}"
}

# Function to verify services
verify_services() {
    local env=$1
    echo -e "${BLUE}🔍 Verifying services...${NC}"
    
    # Wait for services to be ready
    echo -e "${YELLOW}⏳ Waiting for services to start...${NC}"
    sleep 10
    
    # Check container status
    case $env in
        "local")
            docker-compose ps
            ;;
        "prod")
            docker-compose -f docker-compose.prod.yml ps
            ;;
        *)
            docker-compose ps
            ;;
    esac
    
    echo -e "${GREEN}✅ Services verification completed${NC}"
}

# Function to show data status
show_data_status() {
    local env=$1
    echo -e "${BLUE}📊 Checking data status...${NC}"
    
    # Check if data is still there
    case $env in
        "local")
            container_name="urutte-postgres"
            ;;
        "prod")
            container_name="urutte-postgres-prod"
            ;;
        *)
            container_name="urutte-postgres"
            ;;
    esac
    
    if docker ps | grep -q "$container_name"; then
        echo -e "${GREEN}✅ Database container is running${NC}"
        
        # Try to connect to database
        if docker exec "$container_name" pg_isready -U urutte_user >/dev/null 2>&1; then
            echo -e "${GREEN}✅ Database is accessible${NC}"
        else
            echo -e "${YELLOW}⚠️  Database is not ready yet${NC}"
        fi
    else
        echo -e "${RED}❌ Database container is not running${NC}"
    fi
}

# Main execution
echo -e "${GREEN}🚀 Starting safe rebuild process...${NC}"

# Step 1: Backup data (if requested)
if [ "$BACKUP_BEFORE_REBUILD" = "true" ]; then
    backup_data "$ENVIRONMENT"
else
    echo -e "${YELLOW}⚠️  Skipping backup (as requested)${NC}"
fi

# Step 2: Stop containers
stop_containers "$ENVIRONMENT"

# Step 3: Rebuild containers
rebuild_containers "$ENVIRONMENT"

# Step 4: Verify services
verify_services "$ENVIRONMENT"

# Step 5: Check data status
show_data_status "$ENVIRONMENT"

echo ""
echo -e "${GREEN}🎉 Safe rebuild completed successfully!${NC}"
echo ""
echo -e "${YELLOW}📋 Next steps:${NC}"
echo -e "  1. Check your application: http://localhost (or your configured URL)"
echo -e "  2. Verify your data is still there"
echo -e "  3. Check logs if needed: docker-compose logs"
echo ""
echo -e "${YELLOW}💡 Useful commands:${NC}"
echo -e "  - Check status: ./scripts/manage-data.sh status $ENVIRONMENT"
echo -e "  - View logs: docker-compose logs -f"
echo -e "  - List backups: ./scripts/manage-data.sh list"
