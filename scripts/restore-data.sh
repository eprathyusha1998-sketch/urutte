#!/bin/bash

# Urutte Data Restore Script
# This script restores database and uploaded files from backup

set -e

# Configuration
BACKUP_DIR="./backups"
ENVIRONMENT=${1:-"local"}
DATABASE_BACKUP=${2:-""}
UPLOADS_BACKUP=${3:-""}

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${GREEN}🔄 Starting Urutte Data Restore...${NC}"

# Function to show usage
show_usage() {
    echo -e "${YELLOW}Usage: $0 <environment> [database_backup] [uploads_backup]${NC}"
    echo -e "${YELLOW}Example: $0 local database_local_20231201_120000.sql uploads_local_20231201_120000.tar.gz${NC}"
    echo -e "${YELLOW}Available backups:${NC}"
    ls -la "$BACKUP_DIR" 2>/dev/null || echo "No backups found in $BACKUP_DIR"
    exit 1
}

# Check if backup directory exists
if [ ! -d "$BACKUP_DIR" ]; then
    echo -e "${RED}❌ Backup directory $BACKUP_DIR not found!${NC}"
    exit 1
fi

# If no specific backups provided, show available backups
if [ -z "$DATABASE_BACKUP" ] || [ -z "$UPLOADS_BACKUP" ]; then
    echo -e "${YELLOW}Available backups for environment '$ENVIRONMENT':${NC}"
    ls -la "$BACKUP_DIR"/*"$ENVIRONMENT"* 2>/dev/null || echo "No backups found for environment '$ENVIRONMENT'"
    show_usage
fi

# Check if backup files exist
if [ ! -f "$BACKUP_DIR/$DATABASE_BACKUP" ]; then
    echo -e "${RED}❌ Database backup file not found: $BACKUP_DIR/$DATABASE_BACKUP${NC}"
    exit 1
fi

if [ ! -f "$BACKUP_DIR/$UPLOADS_BACKUP" ]; then
    echo -e "${RED}❌ Uploads backup file not found: $BACKUP_DIR/$UPLOADS_BACKUP${NC}"
    exit 1
fi

# Function to restore database
restore_database() {
    local env=$1
    local backup_file=$2
    local container_name=""
    local db_name=""
    local db_user=""
    
    case $env in
        "local")
            container_name="urutte-postgres"
            db_name="urutte"
            db_user="urutte_user"
            ;;
        "prod")
            container_name="urutte-postgres-prod"
            db_name="urutte_prod"
            db_user="urutte_user"
            ;;
        *)
            container_name="urutte-postgres"
            db_name="urutte"
            db_user="urutte_user"
            ;;
    esac
    
    echo -e "${YELLOW}📊 Restoring database ($db_name)...${NC}"
    
    # Check if container is running
    if ! docker ps | grep -q "$container_name"; then
        echo -e "${RED}❌ Database container $container_name is not running!${NC}"
        echo -e "${YELLOW}💡 Start the database container first: docker-compose up -d postgres${NC}"
        return 1
    fi
    
    # Confirm before proceeding
    echo -e "${RED}⚠️  WARNING: This will replace all existing data in the database!${NC}"
    read -p "Are you sure you want to continue? (yes/no): " confirm
    if [ "$confirm" != "yes" ]; then
        echo -e "${YELLOW}❌ Database restore cancelled.${NC}"
        return 1
    fi
    
    # Drop and recreate database
    echo -e "${BLUE}🔄 Dropping and recreating database...${NC}"
    docker exec "$container_name" psql -U "$db_user" -c "DROP DATABASE IF EXISTS $db_name;"
    docker exec "$container_name" psql -U "$db_user" -c "CREATE DATABASE $db_name;"
    
    # Restore database
    docker exec -i "$container_name" psql -U "$db_user" -d "$db_name" < "$BACKUP_DIR/$backup_file"
    
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}✅ Database restore completed successfully!${NC}"
    else
        echo -e "${RED}❌ Database restore failed!${NC}"
        return 1
    fi
}

# Function to restore uploads
restore_uploads() {
    local env=$1
    local backup_file=$2
    local volume_name=""
    
    case $env in
        "local")
            volume_name="backend_uploads_local"
            ;;
        "prod")
            volume_name="backend_uploads_prod"
            ;;
        *)
            volume_name="backend_uploads"
            ;;
    esac
    
    echo -e "${YELLOW}📁 Restoring uploaded files...${NC}"
    
    # Confirm before proceeding
    echo -e "${RED}⚠️  WARNING: This will replace all existing uploaded files!${NC}"
    read -p "Are you sure you want to continue? (yes/no): " confirm
    if [ "$confirm" != "yes" ]; then
        echo -e "${YELLOW}❌ Uploads restore cancelled.${NC}"
        return 1
    fi
    
    # Create a temporary container to restore the volume
    docker run --rm -v "$volume_name":/data -v "$(pwd)/$BACKUP_DIR":/backup alpine sh -c "rm -rf /data/* && tar xzf /backup/$backup_file -C /data"
    
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}✅ Uploads restore completed successfully!${NC}"
    else
        echo -e "${RED}❌ Uploads restore failed!${NC}"
        return 1
    fi
}

# Main execution
echo -e "${YELLOW}Environment: $ENVIRONMENT${NC}"
echo -e "${YELLOW}Database backup: $DATABASE_BACKUP${NC}"
echo -e "${YELLOW}Uploads backup: $UPLOADS_BACKUP${NC}"

# Restore database
restore_database "$ENVIRONMENT" "$DATABASE_BACKUP"

# Restore uploads
restore_uploads "$ENVIRONMENT" "$UPLOADS_BACKUP"

echo -e "${GREEN}🎉 Data restore completed successfully!${NC}"
echo -e "${YELLOW}💡 You may need to restart your application containers:${NC}"
echo -e "${YELLOW}   docker-compose restart backend frontend${NC}"
