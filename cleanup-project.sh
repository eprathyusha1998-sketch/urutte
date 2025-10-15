#!/bin/bash

# Urutte Project Cleanup Script
# This script cleans up old files, backups, and unnecessary files from the project
# Usage: ./cleanup-project.sh

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

info() {
    echo -e "${BLUE}ℹ️  $1${NC}"
}

# Configuration
BACKUP_RETENTION_DAYS=7
CLEANUP_DRY_RUN=false

# Parse command line arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        --dry-run)
            CLEANUP_DRY_RUN=true
            shift
            ;;
        --help)
            echo "Usage: $0 [--dry-run] [--help]"
            echo "  --dry-run    Show what would be deleted without actually deleting"
            echo "  --help       Show this help message"
            exit 0
            ;;
        *)
            echo "Unknown option $1"
            exit 1
            ;;
    esac
done

# Function to safely remove files/directories
safe_remove() {
    local path="$1"
    local description="$2"
    
    if [ "$CLEANUP_DRY_RUN" = true ]; then
        if [ -e "$path" ]; then
            info "Would remove: $description ($path)"
        fi
    else
        if [ -e "$path" ]; then
            rm -rf "$path"
            success "Removed: $description"
        fi
    fi
}

# Function to get file size
get_size() {
    local path="$1"
    if [ -e "$path" ]; then
        du -sh "$path" 2>/dev/null | cut -f1
    else
        echo "0B"
    fi
}

# Show cleanup summary
show_summary() {
    local total_size="$1"
    local files_count="$2"
    
    echo -e "\n${BLUE}📊 Cleanup Summary${NC}"
    echo "=================="
    echo "Files/Directories processed: $files_count"
    echo "Total space freed: $total_size"
    
    if [ "$CLEANUP_DRY_RUN" = true ]; then
        echo -e "\n${YELLOW}⚠️  This was a dry run. No files were actually deleted.${NC}"
        echo "Run without --dry-run to perform actual cleanup."
    else
        echo -e "\n${GREEN}✅ Cleanup completed successfully!${NC}"
    fi
}

# Main cleanup function
main() {
    echo -e "${BLUE}"
    echo "🧹 Urutte Project Cleanup"
    echo "========================"
    echo -e "${NC}"
    
    if [ "$CLEANUP_DRY_RUN" = true ]; then
        warning "Running in DRY RUN mode - no files will be deleted"
    fi
    
    local files_processed=0
    local total_size_before=0
    local total_size_after=0
    
    # Calculate initial size
    total_size_before=$(du -sh . 2>/dev/null | cut -f1)
    
    log "Starting project cleanup..."
    
    # 1. Clean up old backup files
    log "Cleaning up old backup files..."
    if [ -d "backups" ]; then
        find backups -type f -name "*.sql" -mtime +$BACKUP_RETENTION_DAYS -exec rm -f {} \; 2>/dev/null || true
        find backups -type f -name "*.tar.gz" -mtime +$BACKUP_RETENTION_DAYS -exec rm -f {} \; 2>/dev/null || true
        find backups -type d -empty -delete 2>/dev/null || true
        success "Old backup files cleaned up"
    fi
    
    # 2. Remove deployment packages
    log "Removing deployment packages..."
    safe_remove "*.tar.gz" "Deployment packages"
    files_processed=$((files_processed + 1))
    
    # 3. Remove test files
    log "Removing test files..."
    safe_remove "test-*.html" "Test HTML files"
    safe_remove "test-*.md" "Test documentation files"
    files_processed=$((files_processed + 2))
    
    # 4. Remove temporary SQL files
    log "Removing temporary SQL files..."
    safe_remove "fix-database-*.sql" "Temporary database fix files"
    safe_remove "*-temp.sql" "Temporary SQL files"
    files_processed=$((files_processed + 2))
    
    # 5. Clean up build directories
    log "Cleaning up build directories..."
    safe_remove "backend/build" "Backend build directory"
    safe_remove "frontend_v2/build" "Frontend build directory"
    safe_remove "frontend_v2/node_modules" "Node modules directory"
    files_processed=$((files_processed + 3))
    
    # 6. Remove old Docker images (keep latest)
    log "Cleaning up old Docker images..."
    if [ "$CLEANUP_DRY_RUN" = false ]; then
        docker images | grep 'urutte-backend-local' | grep -v 'latest' | awk '{print $3}' | xargs -r docker rmi 2>/dev/null || true
        docker images | grep 'urutte-frontend-local' | grep -v 'latest' | awk '{print $3}' | xargs -r docker rmi 2>/dev/null || true
        docker images | grep 'urutte-backend-prod' | grep -v 'latest' | awk '{print $3}' | xargs -r docker rmi 2>/dev/null || true
        docker images | grep 'urutte-frontend-prod' | grep -v 'latest' | awk '{print $3}' | xargs -r docker rmi 2>/dev/null || true
        docker image prune -f 2>/dev/null || true
        success "Old Docker images cleaned up"
    else
        info "Would clean up old Docker images"
    fi
    files_processed=$((files_processed + 1))
    
    # 7. Remove temporary files
    log "Removing temporary files..."
    safe_remove "*.tmp" "Temporary files"
    safe_remove "*.log" "Log files"
    safe_remove ".DS_Store" "macOS system files"
    safe_remove "Thumbs.db" "Windows system files"
    files_processed=$((files_processed + 4))
    
    # 8. Remove old deployment scripts (keep main ones)
    log "Removing old deployment scripts..."
    safe_remove "deploy-*.sh.bak" "Backup deployment scripts"
    safe_remove "*-old.sh" "Old script files"
    files_processed=$((files_processed + 2))
    
    # 9. Remove old nginx configurations
    log "Removing old nginx configurations..."
    safe_remove "nginx-*.conf.bak" "Backup nginx configurations"
    safe_remove "*-old.conf" "Old configuration files"
    files_processed=$((files_processed + 2))
    
    # 10. Remove old environment files
    log "Removing old environment files..."
    safe_remove "*.env.bak" "Backup environment files"
    safe_remove "*-old.env" "Old environment files"
    files_processed=$((files_processed + 2))
    
    # 11. Clean up frontend temporary files
    log "Cleaning up frontend temporary files..."
    safe_remove "frontend_v2/deploy-temp" "Frontend deploy temp directory"
    safe_remove "frontend_v2/*.tar.gz" "Frontend tar files"
    files_processed=$((files_processed + 2))
    
    # 12. Remove old documentation files
    log "Removing old documentation files..."
    safe_remove "*-old.md" "Old documentation files"
    safe_remove "*.md.bak" "Backup documentation files"
    files_processed=$((files_processed + 2))
    
    # Calculate final size
    total_size_after=$(du -sh . 2>/dev/null | cut -f1)
    
    # Show summary
    show_summary "$total_size_before -> $total_size_after" "$files_processed"
    
    log "Project cleanup completed!"
}

# Run main function
main "$@"
