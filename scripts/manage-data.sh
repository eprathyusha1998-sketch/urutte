#!/bin/bash

# Urutte Data Management Script
# This script provides various data management operations

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to show usage
show_usage() {
    echo -e "${GREEN}Urutte Data Management Script${NC}"
    echo -e "${YELLOW}Usage: $0 <command> [environment]${NC}"
    echo ""
    echo -e "${BLUE}Commands:${NC}"
    echo -e "  backup     - Backup database and uploads"
    echo -e "  restore    - Restore from backup (interactive)"
    echo -e "  list       - List available backups"
    echo -e "  clean      - Clean old backups (keep last 5)"
    echo -e "  volumes    - Show volume information"
    echo -e "  reset      - Reset all data (DANGEROUS!)"
    echo -e "  status     - Show data status"
    echo ""
    echo -e "${BLUE}Environments:${NC}"
    echo -e "  local      - Local development"
    echo -e "  prod       - Production"
    echo -e "  default    - Default (if not specified)"
    echo ""
    echo -e "${BLUE}Examples:${NC}"
    echo -e "  $0 backup local"
    echo -e "  $0 restore local"
    echo -e "  $0 list"
    echo -e "  $0 volumes"
    echo -e "  $0 status local"
}

# Function to show volume information
show_volumes() {
    echo -e "${GREEN}📊 Docker Volume Information${NC}"
    echo ""
    
    echo -e "${YELLOW}Database Volumes:${NC}"
    docker volume ls | grep postgres || echo "No database volumes found"
    echo ""
    
    echo -e "${YELLOW}Upload Volumes:${NC}"
    docker volume ls | grep uploads || echo "No upload volumes found"
    echo ""
    
    echo -e "${YELLOW}Volume Details:${NC}"
    for volume in $(docker volume ls -q | grep -E "(postgres|uploads)"); do
        echo -e "${BLUE}Volume: $volume${NC}"
        docker volume inspect "$volume" | grep -E "(Mountpoint|Size)" || true
        echo ""
    done
}

# Function to show data status
show_status() {
    local env=${1:-"default"}
    
    echo -e "${GREEN}📊 Data Status for Environment: $env${NC}"
    echo ""
    
    # Check containers
    echo -e "${YELLOW}Container Status:${NC}"
    case $env in
        "local")
            docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}" | grep -E "(postgres-local|backend-local|frontend-local)" || echo "No local containers running"
            ;;
        "prod")
            docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}" | grep -E "(postgres-prod|backend-prod|frontend-prod)" || echo "No production containers running"
            ;;
        *)
            docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}" | grep -E "(postgres|backend|frontend)" | grep -v local | grep -v prod || echo "No default containers running"
            ;;
    esac
    echo ""
    
    # Check volumes
    echo -e "${YELLOW}Volume Status:${NC}"
    show_volumes
    
    # Check backups
    echo -e "${YELLOW}Backup Status:${NC}"
    if [ -d "./backups" ]; then
        echo -e "${GREEN}Backup directory exists${NC}"
        echo -e "${BLUE}Recent backups:${NC}"
        ls -la ./backups/ | tail -10
    else
        echo -e "${RED}No backup directory found${NC}"
    fi
}

# Function to list backups
list_backups() {
    echo -e "${GREEN}📋 Available Backups${NC}"
    echo ""
    
    if [ ! -d "./backups" ]; then
        echo -e "${RED}No backup directory found${NC}"
        return 1
    fi
    
    echo -e "${YELLOW}Database Backups:${NC}"
    ls -la ./backups/database_*.sql 2>/dev/null || echo "No database backups found"
    echo ""
    
    echo -e "${YELLOW}Upload Backups:${NC}"
    ls -la ./backups/uploads_*.tar.gz 2>/dev/null || echo "No upload backups found"
    echo ""
    
    echo -e "${YELLOW}Backup Info Files:${NC}"
    ls -la ./backups/backup_info_*.txt 2>/dev/null || echo "No backup info files found"
}

# Function to clean old backups
clean_backups() {
    echo -e "${YELLOW}🧹 Cleaning old backups (keeping last 5)...${NC}"
    
    if [ ! -d "./backups" ]; then
        echo -e "${RED}No backup directory found${NC}"
        return 1
    fi
    
    # Keep last 5 database backups
    echo -e "${BLUE}Cleaning database backups...${NC}"
    ls -t ./backups/database_*.sql 2>/dev/null | tail -n +6 | xargs -r rm -f
    
    # Keep last 5 upload backups
    echo -e "${BLUE}Cleaning upload backups...${NC}"
    ls -t ./backups/uploads_*.tar.gz 2>/dev/null | tail -n +6 | xargs -r rm -f
    
    # Keep last 5 info files
    echo -e "${BLUE}Cleaning backup info files...${NC}"
    ls -t ./backups/backup_info_*.txt 2>/dev/null | tail -n +6 | xargs -r rm -f
    
    echo -e "${GREEN}✅ Cleanup completed${NC}"
}

# Function to reset all data (DANGEROUS!)
reset_data() {
    local env=${1:-"default"}
    
    echo -e "${RED}⚠️  DANGER: This will permanently delete ALL data!${NC}"
    echo -e "${RED}This includes:${NC}"
    echo -e "${RED}  - All database data${NC}"
    echo -e "${RED}  - All uploaded files${NC}"
    echo -e "${RED}  - All user accounts and posts${NC}"
    echo ""
    
    read -p "Type 'DELETE ALL DATA' to confirm: " confirm
    if [ "$confirm" != "DELETE ALL DATA" ]; then
        echo -e "${YELLOW}❌ Reset cancelled${NC}"
        return 1
    fi
    
    echo -e "${YELLOW}🔄 Resetting data for environment: $env${NC}"
    
    # Stop containers
    case $env in
        "local")
            docker-compose -f docker-compose.local.yml down
            ;;
        "prod")
            docker-compose -f docker-compose.prod.yml down
            ;;
        *)
            docker-compose down
            ;;
    esac
    
    # Remove volumes
    case $env in
        "local")
            docker volume rm postgres_local_data backend_uploads_local 2>/dev/null || true
            ;;
        "prod")
            docker volume rm postgres_data_prod backend_uploads_prod 2>/dev/null || true
            ;;
        *)
            docker volume rm postgres_data backend_uploads 2>/dev/null || true
            ;;
    esac
    
    echo -e "${GREEN}✅ Data reset completed${NC}"
    echo -e "${YELLOW}💡 You can now start fresh with: docker-compose up -d${NC}"
}

# Main script logic
COMMAND=${1:-""}
ENVIRONMENT=${2:-"default"}

case $COMMAND in
    "backup")
        echo -e "${GREEN}🔄 Starting backup for environment: $ENVIRONMENT${NC}"
        ./scripts/backup-data.sh "$ENVIRONMENT"
        ;;
    "restore")
        echo -e "${GREEN}🔄 Starting restore for environment: $ENVIRONMENT${NC}"
        ./scripts/restore-data.sh "$ENVIRONMENT"
        ;;
    "list")
        list_backups
        ;;
    "clean")
        clean_backups
        ;;
    "volumes")
        show_volumes
        ;;
    "reset")
        reset_data "$ENVIRONMENT"
        ;;
    "status")
        show_status "$ENVIRONMENT"
        ;;
    *)
        show_usage
        ;;
esac
