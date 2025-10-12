#!/bin/bash

# New Database Setup Script
# This script helps you set up the new comprehensive database schema

echo "🚀 Thread App Database Setup"
echo "=============================="
echo ""

# Check if Docker is running
if ! docker ps > /dev/null 2>&1; then
    echo "❌ Docker is not running. Please start Docker first."
    exit 1
fi

# Check if the database container is running
DB_CONTAINER=$(docker ps --format "table {{.Names}}" | grep -E "mysql|mariadb|postgres" | head -1)
if [ -z "$DB_CONTAINER" ]; then
    echo "❌ Database container is not running. Please start your database first."
    exit 1
fi

echo "📋 Database Setup Options:"
echo "1. 🆕 Create new database schema (recommended for fresh start)"
echo "2. 🔄 Migrate from old schema to new schema"
echo "3. 🧹 Clean up old database and create new schema"
echo "4. 📊 View current database status"
echo "5. ❌ Exit"
echo ""

read -p "Enter your choice (1-5): " choice

case $choice in
    1)
        echo "🆕 Creating new database schema..."
        echo "⚠️  This will create a completely new schema with sample data."
        read -p "Are you sure? (y/N): " confirm
        if [[ $confirm == [yY] || $confirm == [yY][eE][sS] ]]; then
            if docker exec -i $DB_CONTAINER mysql -u root -p < backend/complete_database_schema.sql; then
                echo "✅ New database schema created successfully!"
                echo "📊 Sample users and hashtags have been added."
            else
                echo "❌ Failed to create new schema. Please check your database connection."
            fi
        else
            echo "❌ Schema creation cancelled."
        fi
        ;;
    2)
        echo "🔄 Migrating from old schema to new schema..."
        echo "⚠️  This will migrate your existing data to the new schema."
        read -p "Are you sure? (y/N): " confirm
        if [[ $confirm == [yY] || $confirm == [yY][eE][sS] ]]; then
            if docker exec -i $DB_CONTAINER mysql -u root -p < backend/migrate_to_new_schema.sql; then
                echo "✅ Migration completed successfully!"
                echo "📊 Your existing data has been migrated to the new schema."
            else
                echo "❌ Migration failed. Please check your database connection and existing data."
            fi
        else
            echo "❌ Migration cancelled."
        fi
        ;;
    3)
        echo "🧹 Cleaning up old database and creating new schema..."
        echo "⚠️  This will DELETE ALL existing data and create a fresh schema."
        read -p "Are you sure? This cannot be undone! (y/N): " confirm
        if [[ $confirm == [yY] || $confirm == [yY][eE][sS] ]]; then
            echo "🗑️  Cleaning up old data..."
            if docker exec -i $DB_CONTAINER mysql -u root -p < quick_cleanup.sql; then
                echo "✅ Old data cleaned up."
                echo "🆕 Creating new schema..."
                if docker exec -i $DB_CONTAINER mysql -u root -p < backend/complete_database_schema.sql; then
                    echo "✅ New database schema created successfully!"
                else
                    echo "❌ Failed to create new schema."
                fi
            else
                echo "❌ Failed to clean up old data."
            fi
        else
            echo "❌ Cleanup and schema creation cancelled."
        fi
        ;;
    4)
        echo "📊 Current database status:"
        echo "=========================="
        docker exec -i $DB_CONTAINER mysql -u root -p -e "
            SELECT 
                TABLE_NAME,
                TABLE_ROWS,
                AUTO_INCREMENT
            FROM 
                INFORMATION_SCHEMA.TABLES 
            WHERE 
                TABLE_SCHEMA = DATABASE()
                AND TABLE_TYPE = 'BASE TABLE'
            ORDER BY 
                TABLE_NAME;
        "
        ;;
    5)
        echo "👋 Exiting..."
        exit 0
        ;;
    *)
        echo "❌ Invalid choice. Please run the script again."
        exit 1
        ;;
esac

echo ""
echo "🎉 Database setup process completed!"
echo ""
echo "📚 Next steps:"
echo "1. Update your backend models to match the new schema"
echo "2. Update your API endpoints to use the new table structure"
echo "3. Test the new features (unlimited threads, reactions, etc.)"
echo "4. Update your frontend to handle the new data structure"
echo ""
echo "📖 For more information, see DATABASE_FEATURES.md"
