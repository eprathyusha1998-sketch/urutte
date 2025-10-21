#!/bin/bash

# Script to execute the cleanup of extra AI users using Docker
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

# Check if local database is running
if docker-compose -f docker-compose.local.yml ps | grep -q "urutte-postgres-local"; then
    echo "📊 Using local database via Docker..."
    
    # Execute cleanup SQL commands using Docker
    docker-compose -f docker-compose.local.yml exec -T postgres psql -U urutte_user -d urutte_local << 'EOF'
    
    -- Start transaction for safety
    BEGIN;
    
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
    echo "❌ Local database not running. Please start it with: ./run-local.sh"
    exit 1
fi

echo "✅ Cleanup script completed!"
echo ""
echo "🔄 Please restart the application to ensure changes take effect:"
echo "   docker-compose -f docker-compose.local.yml restart backend"
