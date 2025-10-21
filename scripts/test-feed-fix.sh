#!/bin/bash

# Script to test the feed fix
# This will check if regular users' public posts and self-posts are now visible

echo "🧪 Testing feed fix - checking if regular users' posts are visible..."

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

# Execute SQL commands on production server to test the feed
ssh -i "$KEY_PATH" "$SERVER_USER@$SERVER_IP" << 'EOF'

echo "📊 Testing feed fix..."

# Connect to production database and test the feed logic
docker compose -f docker-compose.prod.yml exec -T postgres psql -U urutte_user -d urutte_prod << 'SQL_EOF'

-- Test the feed logic for aj@gmail.com user
\echo 'Testing feed logic for aj@gmail.com...'

-- Check what threads should be visible to aj@gmail.com
\echo 'Threads visible to aj@gmail.com (using the fixed logic):'
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
    -- Regular threads (non-AI users) - show ALL public posts from regular users
    (u.email NOT LIKE '%ai%' AND u.email NOT LIKE '%assistant%' AND 
    (t.is_public = true OR 
    (t.is_public = false AND t.reply_permission = 'FOLLOWERS' AND u.id IN 
    (SELECT f.following_id FROM follows f WHERE f.follower_id = (SELECT id FROM users WHERE email = 'aj@gmail.com'))) OR 
    (t.is_public = false AND t.reply_permission = 'FOLLOWING' AND u.id = (SELECT id FROM users WHERE email = 'aj@gmail.com')) OR 
    (t.is_public = false AND t.reply_permission = 'MENTIONED_ONLY' AND t.id IN 
    (SELECT tm.thread_id FROM thread_mentions tm WHERE tm.mentioned_user_id = (SELECT id FROM users WHERE email = 'aj@gmail.com'))))) 
    OR 
    -- AI-generated threads (only if user follows AI user)
    ((u.email LIKE '%ai%' OR u.email LIKE '%assistant%') AND EXISTS 
    (SELECT 1 FROM follows f WHERE f.follower_id = (SELECT id FROM users WHERE email = 'aj@gmail.com') AND f.following_id = u.id))
)
ORDER BY t.created_at DESC
LIMIT 10;

-- Check if sivaprakashniet's posts are visible
\echo 'Checking sivaprakashniet posts:'
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
ORDER BY t.created_at DESC
LIMIT 5;

-- Check if aj@gmail.com own posts are visible
\echo 'Checking aj@gmail.com own posts:'
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
WHERE u.email = 'aj@gmail.com'
AND t.parent_thread_id IS NULL 
AND t.is_deleted = false
ORDER BY t.created_at DESC
LIMIT 5;

-- Check AI posts visibility
\echo 'Checking AI posts visibility:'
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
WHERE u.email = 'ai.assistant@urutte.com'
AND t.parent_thread_id IS NULL 
AND t.is_deleted = false
ORDER BY t.created_at DESC
LIMIT 5;

\echo '✅ Feed test completed!'

SQL_EOF

echo "✅ Feed test completed!"

EOF

echo "✅ Feed test completed!"
echo ""
echo "📊 This test shows:"
echo "1. What threads are visible to aj@gmail.com"
echo "2. If sivaprakashniet's posts are visible"
echo "3. If aj@gmail.com's own posts are visible"
echo "4. If AI posts are visible"
echo ""
echo "🎯 Expected results:"
echo "- Regular users' public posts should be visible"
echo "- User's own posts should be visible"
echo "- AI posts should be visible (if following AI user and liking topics)"
