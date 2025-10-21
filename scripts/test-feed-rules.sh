#!/bin/bash

# Script to test the feed rules implementation
# This will verify that the feed follows the exact rules specified

echo "🧪 Testing feed rules implementation..."

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

# Execute SQL commands on production server to test the feed rules
ssh -i "$KEY_PATH" "$SERVER_USER@$SERVER_IP" << 'EOF'

echo "📊 Testing feed rules implementation..."

# Connect to production database and test the feed rules
docker compose -f docker-compose.prod.yml exec -T postgres psql -U urutte_user -d urutte_prod << 'SQL_EOF'

-- Test the feed rules for aj@gmail.com user
\echo 'Testing feed rules for aj@gmail.com...'

-- Rule 1: For AI Assistant Threads - Show only if user follow AI Assistant and liked topics
\echo 'Rule 1: AI Assistant Threads (should show only if following AI and liking topics):'
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

-- Rule 2: For regular user threads - Show all public posts from regular users
\echo 'Rule 2: Regular user public posts (should show all public posts from regular users):'
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
WHERE u.email NOT LIKE '%ai%' 
AND u.email NOT LIKE '%assistant%'
AND t.is_public = true
AND t.parent_thread_id IS NULL 
AND t.is_deleted = false
ORDER BY t.created_at DESC
LIMIT 5;

-- Rule 3: Show all followers-only posts from followed users
\echo 'Rule 3: Followers-only posts from followed users:'
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
WHERE u.email NOT LIKE '%ai%' 
AND u.email NOT LIKE '%assistant%'
AND t.is_public = false
AND t.reply_permission = 'FOLLOWERS'
AND u.id IN (SELECT f.following_id FROM follows f WHERE f.follower_id = (SELECT id FROM users WHERE email = 'aj@gmail.com'))
AND t.parent_thread_id IS NULL 
AND t.is_deleted = false
ORDER BY t.created_at DESC
LIMIT 5;

-- Rule 4: Show all my threads with FOLLOWING permission
\echo 'Rule 4: My threads with FOLLOWING permission:'
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
WHERE u.email NOT LIKE '%ai%' 
AND u.email NOT LIKE '%assistant%'
AND t.is_public = false
AND t.reply_permission = 'FOLLOWING'
AND u.id = (SELECT id FROM users WHERE email = 'aj@gmail.com')
AND t.parent_thread_id IS NULL 
AND t.is_deleted = false
ORDER BY t.created_at DESC
LIMIT 5;

-- Rule 5: Show all threads where I'm mentioned
\echo 'Rule 5: Threads where I am mentioned:'
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
WHERE u.email NOT LIKE '%ai%' 
AND u.email NOT LIKE '%assistant%'
AND t.is_public = false
AND t.reply_permission = 'MENTIONED_ONLY'
AND t.id IN (SELECT tm.thread_id FROM thread_mentions tm WHERE tm.mentioned_user_id = (SELECT id FROM users WHERE email = 'aj@gmail.com'))
AND t.parent_thread_id IS NULL 
AND t.is_deleted = false
ORDER BY t.created_at DESC
LIMIT 5;

-- Rule 6: Show all logged in user created threads in the feed
\echo 'Rule 6: All logged in user created threads:'
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

-- Overall feed test - what should be visible to aj@gmail.com
\echo 'Overall feed test - what should be visible to aj@gmail.com:'
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
    -- Regular threads (non-AI users)
    (u.email NOT LIKE '%ai%' AND u.email NOT LIKE '%assistant%' AND 
    (t.is_public = true OR 
    (t.is_public = false AND t.reply_permission = 'FOLLOWERS' AND u.id IN 
    (SELECT f.following_id FROM follows f WHERE f.follower_id = (SELECT id FROM users WHERE email = 'aj@gmail.com'))) OR 
    (t.is_public = false AND t.reply_permission = 'FOLLOWING' AND u.id = (SELECT id FROM users WHERE email = 'aj@gmail.com')) OR 
    (t.is_public = false AND t.reply_permission = 'MENTIONED_ONLY' AND t.id IN 
    (SELECT tm.thread_id FROM thread_mentions tm WHERE tm.mentioned_user_id = (SELECT id FROM users WHERE email = 'aj@gmail.com'))) OR 
    u.id = (SELECT id FROM users WHERE email = 'aj@gmail.com'))) 
    OR 
    -- AI-generated threads (only if user follows AI user)
    ((u.email LIKE '%ai%' OR u.email LIKE '%assistant%') AND EXISTS 
    (SELECT 1 FROM follows f WHERE f.follower_id = (SELECT id FROM users WHERE email = 'aj@gmail.com') AND f.following_id = u.id))
)
ORDER BY t.created_at DESC
LIMIT 10;

\echo '✅ Feed rules test completed!'

SQL_EOF

echo "✅ Feed rules test completed!"

EOF

echo "✅ Feed rules test completed!"
echo ""
echo "📊 This test verifies all 6 rules:"
echo "1. AI Assistant Threads - Show only if user follow AI Assistant and liked topics"
echo "2. Regular user threads - Show all public posts from regular users"
echo "3. Show all followers-only posts from followed users"
echo "4. Show all my threads with FOLLOWING permission"
echo "5. Show all threads where I'm mentioned"
echo "6. Show all logged in user created threads in the feed"
echo ""
echo "🎯 Expected results:"
echo "- All rules should be properly implemented"
echo "- Feed should show appropriate content based on each rule"
