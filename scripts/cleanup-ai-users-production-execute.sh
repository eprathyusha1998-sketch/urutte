#!/bin/bash

# Script to execute cleanup of extra AI users from PRODUCTION database on EC2
# This will permanently delete extra AI users and keep only ai.assistant@urutte.com

echo "⚠️  WARNING: This will permanently delete extra AI users and their data from PRODUCTION!"
echo "This action cannot be undone!"
echo ""
read -p "Are you sure you want to proceed? (yes/no): " confirm

if [ "$confirm" != "yes" ]; then
    echo "❌ Cleanup cancelled."
    exit 1
fi

echo "🧹 Starting cleanup of extra AI users from PRODUCTION database..."

# Load deployment configuration
if [ ! -f "deploy-config.env" ]; then
    echo "❌ Deployment configuration file not found: deploy-config.env"
    exit 1
fi

source deploy-config.env

# Check if required variables are set
if [ -z "$SERVER_IP" ] || [ -z "$SERVER_USER" ] || [ -z "$KEY_PATH" ]; then
    echo "❌ Missing required configuration in deploy-config.env"
    exit 1
fi

echo "📊 Connecting to production server: $SERVER_IP"
echo "🔑 Using key: $KEY_PATH"

# Execute cleanup SQL commands on production server
ssh -i "$KEY_PATH" "$SERVER_USER@$SERVER_IP" << 'EOF'

echo "📊 Starting cleanup on production database..."

# Execute comprehensive cleanup SQL commands using Docker
docker compose -f docker-compose.prod.yml exec -T postgres psql -U urutte_user -d urutte_prod << 'SQL_EOF'

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

\echo '✅ Production cleanup completed successfully!'

SQL_EOF

echo "🔄 Restarting production backend to ensure changes take effect..."
docker compose -f docker-compose.prod.yml restart backend

echo "✅ Production cleanup completed!"

EOF

echo "✅ Production cleanup script completed!"
echo ""
echo "🎉 Production database has been cleaned up!"
echo "📊 Only ai.assistant@urutte.com should remain as the AI user"
