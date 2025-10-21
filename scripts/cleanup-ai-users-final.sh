#!/bin/bash

# Final comprehensive cleanup script for extra AI users
# This handles ALL foreign key constraints properly

echo "⚠️  WARNING: This will permanently delete extra AI users and their data!"
echo "This action cannot be undone!"
echo ""
read -p "Are you sure you want to proceed? (yes/no): " confirm

if [ "$confirm" != "yes" ]; then
    echo "❌ Cleanup cancelled."
    exit 1
fi

echo "🧹 Starting final comprehensive cleanup of extra AI users..."

# Check if local database is running
if docker-compose -f docker-compose.local.yml ps | grep -q "urutte-postgres-local"; then
    echo "📊 Using local database via Docker..."
    
    # Execute comprehensive cleanup SQL commands using Docker
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
    
    -- Get the IDs of users to delete
    \echo 'Getting user IDs to delete...'
    CREATE TEMP TABLE users_to_delete AS
    SELECT id FROM users 
    WHERE (email LIKE '%ai%' OR email LIKE '%assistant%') 
    AND email != 'ai.assistant@urutte.com';
    
    -- Delete in proper order to handle ALL foreign key constraints
    
    -- 1. Delete notifications first (they reference users)
    \echo 'Deleting notifications...'
    DELETE FROM notifications 
    WHERE from_user_id IN (SELECT id FROM users_to_delete)
    OR user_id IN (SELECT id FROM users_to_delete);
    
    -- 2. Delete thread hashtags
    \echo 'Deleting thread hashtags...'
    DELETE FROM thread_hashtags 
    WHERE thread_id IN (
        SELECT t.id FROM threads t 
        JOIN users_to_delete u ON t.user_id = u.id
    );
    
    -- 3. Delete thread mentions
    \echo 'Deleting thread mentions...'
    DELETE FROM thread_mentions 
    WHERE thread_id IN (
        SELECT t.id FROM threads t 
        JOIN users_to_delete u ON t.user_id = u.id
    );
    
    -- 4. Delete thread likes
    \echo 'Deleting thread likes...'
    DELETE FROM thread_likes 
    WHERE thread_id IN (
        SELECT t.id FROM threads t 
        JOIN users_to_delete u ON t.user_id = u.id
    );
    
    -- 5. Delete thread reposts
    \echo 'Deleting thread reposts...'
    DELETE FROM thread_reposts 
    WHERE thread_id IN (
        SELECT t.id FROM threads t 
        JOIN users_to_delete u ON t.user_id = u.id
    );
    
    -- 6. Delete thread bookmarks
    \echo 'Deleting thread bookmarks...'
    DELETE FROM thread_bookmarks 
    WHERE thread_id IN (
        SELECT t.id FROM threads t 
        JOIN users_to_delete u ON t.user_id = u.id
    );
    
    -- 7. Delete thread reactions
    \echo 'Deleting thread reactions...'
    DELETE FROM thread_reactions 
    WHERE thread_id IN (
        SELECT t.id FROM threads t 
        JOIN users_to_delete u ON t.user_id = u.id
    );
    
    -- 8. Delete thread views
    \echo 'Deleting thread views...'
    DELETE FROM thread_views 
    WHERE thread_id IN (
        SELECT t.id FROM threads t 
        JOIN users_to_delete u ON t.user_id = u.id
    );
    
    -- 9. Delete thread media
    \echo 'Deleting thread media...'
    DELETE FROM thread_media 
    WHERE thread_id IN (
        SELECT t.id FROM threads t 
        JOIN users_to_delete u ON t.user_id = u.id
    );
    
    -- 10. Delete AI generated thread records
    \echo 'Deleting AI generated thread records...'
    DELETE FROM ai_generated_threads 
    WHERE thread_id IN (
        SELECT t.id FROM threads t 
        JOIN users_to_delete u ON t.user_id = u.id
    );
    
    -- 11. Delete follow relationships
    \echo 'Deleting follow relationships...'
    DELETE FROM follows 
    WHERE following_id IN (SELECT id FROM users_to_delete)
    OR follower_id IN (SELECT id FROM users_to_delete);
    
    -- 12. Delete follow requests
    \echo 'Deleting follow requests...'
    DELETE FROM follow_requests 
    WHERE requester_id IN (SELECT id FROM users_to_delete)
    OR target_id IN (SELECT id FROM users_to_delete);
    
    -- 13. Delete user topics
    \echo 'Deleting user topics...'
    DELETE FROM user_topics 
    WHERE user_id IN (SELECT id FROM users_to_delete);
    
    -- 14. Delete threads
    \echo 'Deleting threads...'
    DELETE FROM threads 
    WHERE user_id IN (SELECT id FROM users_to_delete);
    
    -- 15. Delete the users themselves
    \echo 'Deleting extra AI users...'
    DELETE FROM users 
    WHERE id IN (SELECT id FROM users_to_delete);
    
    -- Clean up temp table
    DROP TABLE users_to_delete;
    
    -- Show remaining AI users
    \echo 'Remaining AI users:'
    SELECT id, email, name, username, created_at 
    FROM users 
    WHERE email LIKE '%ai%' OR email LIKE '%assistant%';
    
    -- Commit the transaction
    COMMIT;
    
    \echo '✅ Final cleanup completed successfully!'
    
EOF

else
    echo "❌ Local database not running. Please start it with: ./run-local.sh"
    exit 1
fi

echo "✅ Final cleanup script completed!"
echo ""
echo "🔄 Please restart the application to ensure changes take effect:"
echo "   docker-compose -f docker-compose.local.yml restart backend"
