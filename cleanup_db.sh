#!/bin/bash

# Database Cleanup Script
# This script will clean up the database tables

echo "🗑️  Starting database cleanup..."

# Check if Docker is running
if ! docker ps > /dev/null 2>&1; then
    echo "❌ Docker is not running. Please start Docker first."
    exit 1
fi

# Check if the database container is running
if ! docker ps | grep -q "mysql\|mariadb\|postgres"; then
    echo "❌ Database container is not running. Please start your database first."
    exit 1
fi

echo "📋 Choose cleanup option:"
echo "1. Clean data only (keep table structure)"
echo "2. Complete reset (drop and recreate tables)"
echo "3. Exit"
read -p "Enter your choice (1-3): " choice

case $choice in
    1)
        echo "🧹 Cleaning data only..."
        if docker exec -i $(docker ps --format "table {{.Names}}" | grep -E "mysql|mariadb|postgres" | head -1) mysql -u root -p < backend/cleanup_database.sql; then
            echo "✅ Database cleanup completed successfully!"
        else
            echo "❌ Database cleanup failed. Please check your database connection."
        fi
        ;;
    2)
        echo "🔄 Performing complete database reset..."
        read -p "⚠️  This will delete ALL data and table structures. Are you sure? (y/N): " confirm
        if [[ $confirm == [yY] || $confirm == [yY][eE][sS] ]]; then
            if docker exec -i $(docker ps --format "table {{.Names}}" | grep -E "mysql|mariadb|postgres" | head -1) mysql -u root -p < backend/reset_database.sql; then
                echo "✅ Database reset completed successfully!"
            else
                echo "❌ Database reset failed. Please check your database connection."
            fi
        else
            echo "❌ Database reset cancelled."
        fi
        ;;
    3)
        echo "👋 Exiting..."
        exit 0
        ;;
    *)
        echo "❌ Invalid choice. Please run the script again."
        exit 1
        ;;
esac

echo "🎉 Database cleanup process completed!"
