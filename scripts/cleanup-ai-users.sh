#!/bin/bash

# Script to clean up extra AI users and keep only ai.assistant@urutte.com
# This script will:
# 1. Remove all AI users except ai.assistant@urutte.com
# 2. Remove their threads and related data
# 3. Clean up follow relationships

echo "🧹 Cleaning up extra AI users from database..."

# Database connection details
DB_HOST="localhost"
DB_PORT="5432"
DB_NAME="urutte"
DB_USER="urutte_user"

# Check if we're running locally or on production
if [ -f "docker-compose.local.yml" ] && docker-compose -f docker-compose.local.yml ps | grep -q "urutte-postgres-local"; then
    echo "📊 Using local database..."
    DB_HOST="localhost"
    DB_PORT="5432"
    DB_NAME="urutte"
    DB_USER="urutte_user"
    DB_PASSWORD="urutte_password"
    
    # Execute SQL commands on local database
    PGPASSWORD=$DB_PASSWORD psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME << 'EOF'
    
    -- First, let's see what AI users exist
    SELECT 'Current AI users:' as info;
    SELECT id, email, name, username, created_at FROM users WHERE email LIKE '%ai%' OR email LIKE '%assistant%' ORDER BY created_at;
    
    -- Get the ID of the main AI user we want to keep
    SELECT 'Main AI user to keep:' as info;
    SELECT id, email, name FROM users WHERE email = 'ai.assistant@urutte.com';
    
    -- Count threads by AI users
    SELECT 'Threads by AI users:' as info;
    SELECT u.email, COUNT(t.id) as thread_count 
    FROM users u 
    LEFT JOIN threads t ON u.id = t.user_id 
    WHERE u.email LIKE '%ai%' OR u.email LIKE '%assistant%'
    GROUP BY u.id, u.email;
    
    -- Count follow relationships involving AI users
    SELECT 'Follow relationships involving AI users:' as info;
    SELECT 
        'Followers of AI users' as type,
        COUNT(*) as count
    FROM follows f 
    JOIN users u ON f.following_id = u.id 
    WHERE u.email LIKE '%ai%' OR u.email LIKE '%assistant%'
    UNION ALL
    SELECT 
        'AI users following others' as type,
        COUNT(*) as count
    FROM follows f 
    JOIN users u ON f.follower_id = u.id 
    WHERE u.email LIKE '%ai%' OR u.email LIKE '%assistant%';
    
EOF

else
    echo "📊 Using production database..."
    # For production, we'll need to SSH into the server
    echo "⚠️  Production cleanup requires manual database access"
    echo "Please run the following SQL commands on the production database:"
    echo ""
    echo "-- First, check what AI users exist:"
    echo "SELECT id, email, name, username, created_at FROM users WHERE email LIKE '%ai%' OR email LIKE '%assistant%' ORDER BY created_at;"
    echo ""
    echo "-- Get the ID of the main AI user:"
    echo "SELECT id, email, name FROM users WHERE email = 'ai.assistant@urutte.com';"
    echo ""
    echo "-- Count threads by AI users:"
    echo "SELECT u.email, COUNT(t.id) as thread_count FROM users u LEFT JOIN threads t ON u.id = t.user_id WHERE u.email LIKE '%ai%' OR u.email LIKE '%assistant%' GROUP BY u.id, u.email;"
    echo ""
    echo "-- Count follow relationships:"
    echo "SELECT 'Followers of AI users' as type, COUNT(*) as count FROM follows f JOIN users u ON f.following_id = u.id WHERE u.email LIKE '%ai%' OR u.email LIKE '%assistant%' UNION ALL SELECT 'AI users following others' as type, COUNT(*) as count FROM follows f JOIN users u ON f.follower_id = u.id WHERE u.email LIKE '%ai%' OR u.email LIKE '%assistant%';"
    echo ""
    echo "⚠️  After reviewing the data, run the cleanup commands manually"
fi

echo "✅ Database analysis complete!"
echo ""
echo "📋 Next steps:"
echo "1. Review the AI users and their data above"
echo "2. If you want to proceed with cleanup, run:"
echo "   ./scripts/cleanup-ai-users-execute.sh"
echo ""
echo "⚠️  WARNING: This will permanently delete extra AI users and their data!"
