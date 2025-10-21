#!/bin/bash

# Script to execute the cleanup of extra AI users
# This will permanently delete extra AI users and keep only ai.assistant@urutte.com

echo "⚠️  WARNING: This will permanently delete extra AI users and their data!"
echo "This action cannot be undone!"
echo ""
read -p "Are you sure you want to proceed? (yes/no): " confirm

if [ "$confirm" != "yes" ]; then
    echo "❌ Cleanup cancelled."
    exit 1
fi

echo "🧹 Starting cleanup of extra AI users..."

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
    
    # Execute cleanup SQL commands on local database
    PGPASSWORD=$DB_PASSWORD psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME << 'EOF'
    
    -- Start transaction for safety
    BEGIN;
    
    -- Get the ID of the main AI user we want to keep
    \echo 'Getting main AI user ID...'
    \set main_ai_user_id `SELECT id FROM users WHERE email = 'ai.assistant@urutte.com'`
    
    -- Show what we're about to delete
    \echo 'AI users to be deleted:'
    SELECT id, email, name, username, created_at 
    FROM users 
    WHERE (email LIKE '%ai%' OR email LIKE '%assistant%') 
    AND email != 'ai.assistant@urutte.com'
    ORDER BY created_at;
    
    -- Delete follow relationships where extra AI users are involved
    \echo 'Deleting follow relationships involving extra AI users...'
    DELETE FROM follows 
    WHERE following_id IN (
        SELECT id FROM users 
        WHERE (email LIKE '%ai%' OR email LIKE '%assistant%') 
        AND email != 'ai.assistant@urutte.com'
    ) OR follower_id IN (
        SELECT id FROM users 
        WHERE (email LIKE '%ai%' OR email LIKE '%assistant%') 
        AND email != 'ai.assistant@urutte.com'
    );
    
    -- Delete AI generated thread records for extra AI users
    \echo 'Deleting AI generated thread records...'
    DELETE FROM ai_generated_threads 
    WHERE thread_id IN (
        SELECT t.id FROM threads t 
        JOIN users u ON t.user_id = u.id 
        WHERE (u.email LIKE '%ai%' OR u.email LIKE '%assistant%') 
        AND u.email != 'ai.assistant@urutte.com'
    );
    
    -- Delete threads by extra AI users
    \echo 'Deleting threads by extra AI users...'
    DELETE FROM threads 
    WHERE user_id IN (
        SELECT id FROM users 
        WHERE (email LIKE '%ai%' OR email LIKE '%assistant%') 
        AND email != 'ai.assistant@urutte.com'
    );
    
    -- Delete the extra AI users themselves
    \echo 'Deleting extra AI users...'
    DELETE FROM users 
    WHERE (email LIKE '%ai%' OR email LIKE '%assistant%') 
    AND email != 'ai.assistant@urutte.com';
    
    -- Show remaining AI users
    \echo 'Remaining AI users:'
    SELECT id, email, name, username, created_at 
    FROM users 
    WHERE email LIKE '%ai%' OR email LIKE '%assistant%';
    
    -- Commit the transaction
    COMMIT;
    
    \echo '✅ Cleanup completed successfully!'
    
EOF

else
    echo "📊 Production cleanup requires manual execution"
    echo "Please run the following SQL commands on the production database:"
    echo ""
    echo "-- Start transaction for safety"
    echo "BEGIN;"
    echo ""
    echo "-- Get the ID of the main AI user we want to keep"
    echo "SELECT id FROM users WHERE email = 'ai.assistant@urutte.com';"
    echo ""
    echo "-- Show what we're about to delete"
    echo "SELECT id, email, name, username, created_at FROM users WHERE (email LIKE '%ai%' OR email LIKE '%assistant%') AND email != 'ai.assistant@urutte.com' ORDER BY created_at;"
    echo ""
    echo "-- Delete follow relationships where extra AI users are involved"
    echo "DELETE FROM follows WHERE following_id IN (SELECT id FROM users WHERE (email LIKE '%ai%' OR email LIKE '%assistant%') AND email != 'ai.assistant@urutte.com') OR follower_id IN (SELECT id FROM users WHERE (email LIKE '%ai%' OR email LIKE '%assistant%') AND email != 'ai.assistant@urutte.com');"
    echo ""
    echo "-- Delete AI generated thread records for extra AI users"
    echo "DELETE FROM ai_generated_threads WHERE thread_id IN (SELECT t.id FROM threads t JOIN users u ON t.user_id = u.id WHERE (u.email LIKE '%ai%' OR u.email LIKE '%assistant%') AND u.email != 'ai.assistant@urutte.com');"
    echo ""
    echo "-- Delete threads by extra AI users"
    echo "DELETE FROM threads WHERE user_id IN (SELECT id FROM users WHERE (email LIKE '%ai%' OR email LIKE '%assistant%') AND email != 'ai.assistant@urutte.com');"
    echo ""
    echo "-- Delete the extra AI users themselves"
    echo "DELETE FROM users WHERE (email LIKE '%ai%' OR email LIKE '%assistant%') AND email != 'ai.assistant@urutte.com';"
    echo ""
    echo "-- Show remaining AI users"
    echo "SELECT id, email, name, username, created_at FROM users WHERE email LIKE '%ai%' OR email LIKE '%assistant%';"
    echo ""
    echo "-- Commit the transaction"
    echo "COMMIT;"
    echo ""
    echo "⚠️  After running these commands, restart the application"
fi

echo "✅ Cleanup script completed!"
