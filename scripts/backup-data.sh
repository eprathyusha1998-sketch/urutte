#!/bin/bash

# Urutte Data Backup Script
# This script backs up database and uploaded files

set -e

# Configuration
BACKUP_DIR="./backups"
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
ENVIRONMENT=${1:-"local"}  # local, prod, or default

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${GREEN}🔄 Starting Urutte Data Backup...${NC}"

# Create backup directory
mkdir -p "$BACKUP_DIR"

# Function to backup database
backup_database() {
    local env=$1
    local container_name=""
    local db_name=""
    local db_user=""
    local db_password=""
    
    case $env in
        "local")
            container_name="urutte-postgres"
            db_name="urutte"
            db_user="urutte_user"
            db_password="urutte_pass"
            ;;
        "prod")
            container_name="urutte-postgres-prod"
            db_name="urutte_prod"
            db_user="urutte_user"
            db_password="urutte_pass"
            ;;
        *)
            container_name="urutte-postgres"
            db_name="urutte"
            db_user="urutte_user"
            db_password="urutte_pass"
            ;;
    esac
    
    echo -e "${YELLOW}📊 Backing up database ($db_name)...${NC}"
    
    # Check if container is running
    if ! docker ps | grep -q "$container_name"; then
        echo -e "${RED}❌ Database container $container_name is not running!${NC}"
        return 1
    fi
    
    # Create database backup
    docker exec "$container_name" pg_dump -U "$db_user" -d "$db_name" > "$BACKUP_DIR/database_${env}_${TIMESTAMP}.sql"
    
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}✅ Database backup completed: database_${env}_${TIMESTAMP}.sql${NC}"
    else
        echo -e "${RED}❌ Database backup failed!${NC}"
        return 1
    fi
}

# Function to backup uploads
backup_uploads() {
    local env=$1
    local volume_name=""
    local container_name=""
    
    case $env in
        "local")
            volume_name="backend_uploads_local"
            container_name="urutte-backend-local"
            ;;
        "prod")
            volume_name="backend_uploads_prod"
            container_name="urutte-backend-prod"
            ;;
        *)
            volume_name="backend_uploads"
            container_name="urutte-backend"
            ;;
    esac
    
    echo -e "${YELLOW}📁 Backing up uploaded files...${NC}"
    
    # Create a temporary container to access the volume
    docker run --rm -v "$volume_name":/data -v "$(pwd)/$BACKUP_DIR":/backup alpine tar czf "/backup/uploads_${env}_${TIMESTAMP}.tar.gz" -C /data .
    
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}✅ Uploads backup completed: uploads_${env}_${TIMESTAMP}.tar.gz${NC}"
    else
        echo -e "${RED}❌ Uploads backup failed!${NC}"
        return 1
    fi
}

# Function to create backup info file
create_backup_info() {
    local env=$1
    cat > "$BACKUP_DIR/backup_info_${env}_${TIMESTAMP}.txt" << EOF
Urutte Backup Information
========================
Environment: $env
Timestamp: $TIMESTAMP
Date: $(date)
Docker Compose Version: $(docker-compose --version)
Docker Version: $(docker --version)

Backed up files:
- database_${env}_${TIMESTAMP}.sql
- uploads_${env}_${TIMESTAMP}.tar.gz

To restore this backup:
1. Run: ./scripts/restore-data.sh $env database_${env}_${TIMESTAMP}.sql uploads_${env}_${TIMESTAMP}.tar.gz
EOF
    
    echo -e "${GREEN}✅ Backup info created: backup_info_${env}_${TIMESTAMP}.txt${NC}"
}

# Main execution
echo -e "${YELLOW}Environment: $ENVIRONMENT${NC}"

# Backup database
backup_database "$ENVIRONMENT"

# Backup uploads
backup_uploads "$ENVIRONMENT"

# Create backup info
create_backup_info "$ENVIRONMENT"

echo -e "${GREEN}🎉 Backup completed successfully!${NC}"
echo -e "${YELLOW}Backup location: $BACKUP_DIR${NC}"
echo -e "${YELLOW}Files created:${NC}"
ls -la "$BACKUP_DIR"/*"$TIMESTAMP"*
