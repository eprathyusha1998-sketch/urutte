#!/bin/bash

# Script to debug public posts visibility issue
# This will help identify why public posts from regular users aren't visible

echo "🔍 Debugging public posts visibility issue..."

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

# Execute SQL commands on production server to debug public posts
ssh -i "$KEY_PATH" "$SERVER_USER@$SERVER_IP" << 'EOF'

echo "📊 Debugging public posts visibility..."

# Connect to production database and debug public posts
docker compose -f docker-compose.prod.yml exec -T postgres psql -U urutte_user -d urutte_prod << 'SQL_EOF'

-- Check if sivaprakashniet@gmail.com has any public posts
\echo 'Checking public posts from sivaprakashniet@gmail.com:'
SELECT 
    t.id,
    LEFT(t.content, 100) as content_preview,
    t.is_public,
    t.reply_permission,
    u.email as user_email,
    u.name as user_name,
    t.created_at
FROM threads t
JOIN users u ON t.user_id = u.id
WHERE u.email = 'sivaprakashniet@gmail.com'
AND t.parent_thread_id IS NULL 
AND t.is_deleted = false
ORDER BY t.created_at DESC;

-- Check what the feed query should return for eprathyusha1998@gmail.com
\echo 'Testing feed query for eprathyusha1998@gmail.com:'
SELECT 
    t.id,
    LEFT(t.content, 100) as content_preview,
    t.is_public,
    t.reply_permission,
    u.email as user_email,
    u.name as user_name,
    t.created_at
FROM threads t
JOIN users u ON t.user_id = u.id
WHERE t.parent_thread_id IS NULL 
AND t.is_deleted = false
AND (
    -- Show ALL logged in user created threads in the feed (regardless of thread type)
    u.id = (SELECT id FROM users WHERE email = 'eprathyusha1998@gmail.com') OR 
    -- Regular threads (non-AI users) - exclude user's own threads to avoid duplication
    (u.email NOT LIKE '%ai%' AND u.email NOT LIKE '%assistant%' AND u.id != (SELECT id FROM users WHERE email = 'eprathyusha1998@gmail.com') AND 
    (t.is_public = true OR 
    (t.is_public = false AND t.reply_permission = 'FOLLOWERS' AND u.id IN 
    (SELECT f.following_id FROM follows f WHERE f.follower_id = (SELECT id FROM users WHERE email = 'eprathyusha1998@gmail.com'))) OR 
    (t.is_public = false AND t.reply_permission = 'FOLLOWING' AND u.id = (SELECT id FROM users WHERE email = 'eprathyusha1998@gmail.com')) OR 
    (t.is_public = false AND t.reply_permission = 'MENTIONED_ONLY' AND t.id IN 
    (SELECT tm.thread_id FROM thread_mentions tm WHERE tm.mentioned_user_id = (SELECT id FROM users WHERE email = 'eprathyusha1998@gmail.com')))) 
    OR 
    -- AI-generated threads (only if user follows AI user)
    ((u.email LIKE '%ai%' OR u.email LIKE '%assistant%') AND EXISTS 
    (SELECT 1 FROM follows f WHERE f.follower_id = (SELECT id FROM users WHERE email = 'eprathyusha1998@gmail.com') AND f.following_id = u.id))
)
ORDER BY t.created_at DESC
LIMIT 20;

-- Check if there are any public posts from regular users
\echo 'All public posts from regular users:'
SELECT 
    t.id,
    LEFT(t.content, 100) as content_preview,
    t.is_public,
    t.reply_permission,
    u.email as user_email,
    u.name as user_name,
    t.created_at
FROM threads t
JOIN users u ON t.user_id = u.id
WHERE t.parent_thread_id IS NULL 
AND t.is_deleted = false
AND t.is_public = true
AND u.email NOT LIKE '%ai%' 
AND u.email NOT LIKE '%assistant%'
ORDER BY t.created_at DESC
LIMIT 10;

-- Check user IDs for both users
\echo 'User IDs for both users:'
SELECT id, email, name FROM users WHERE email IN ('sivaprakashniet@gmail.com', 'eprathyusha1998@gmail.com');

\echo '✅ Public posts debug completed!'

SQL_EOF

echo "✅ Public posts debug completed!"

EOF

echo "✅ Public posts debug completed!"
echo ""
echo "📊 This test verifies:"
echo "1. If sivaprakashniet@gmail.com has public posts"
echo "2. What the feed query returns for eprathyusha1998@gmail.com"
echo "3. All public posts from regular users"
echo "4. User IDs for both users"
echo ""
echo "🎯 Expected results:"
echo "- sivaprakashniet@gmail.com should have public posts"
echo "- Feed query should return those public posts for eprathyusha1998@gmail.com"
echo "- All public posts from regular users should be visible"
