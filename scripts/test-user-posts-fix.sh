#!/bin/bash

# Script to test that logged-in user's posts show in feed regardless of thread type
# This will verify that user's own posts are always visible

echo "🧪 Testing user posts visibility fix..."

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

# Execute SQL commands on production server to test user posts visibility
ssh -i "$KEY_PATH" "$SERVER_USER@$SERVER_IP" << 'EOF'

echo "📊 Testing user posts visibility fix..."

# Connect to production database and test user posts visibility
docker compose -f docker-compose.prod.yml exec -T postgres psql -U urutte_user -d urutte_prod << 'SQL_EOF'

-- Test that logged-in user's posts show regardless of thread type
\echo 'Testing user posts visibility for aj@gmail.com...'

-- Check all posts by aj@gmail.com (regardless of thread type)
\echo 'All posts by aj@gmail.com (should show ALL regardless of thread type):'
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
ORDER BY t.created_at DESC;

-- Test the updated feed query for aj@gmail.com
\echo 'Updated feed query for aj@gmail.com (should include ALL user posts):'
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
    u.id = (SELECT id FROM users WHERE email = 'aj@gmail.com') OR 
    -- Regular threads (non-AI users) - exclude user's own threads to avoid duplication
    (u.email NOT LIKE '%ai%' AND u.email NOT LIKE '%assistant%' AND u.id != (SELECT id FROM users WHERE email = 'aj@gmail.com') AND 
    (t.is_public = true OR 
    (t.is_public = false AND t.reply_permission = 'FOLLOWERS' AND u.id IN 
    (SELECT f.following_id FROM follows f WHERE f.follower_id = (SELECT id FROM users WHERE email = 'aj@gmail.com'))) OR 
    (t.is_public = false AND t.reply_permission = 'FOLLOWING' AND u.id = (SELECT id FROM users WHERE email = 'aj@gmail.com')) OR 
    (t.is_public = false AND t.reply_permission = 'MENTIONED_ONLY' AND t.id IN 
    (SELECT tm.thread_id FROM thread_mentions tm WHERE tm.mentioned_user_id = (SELECT id FROM users WHERE email = 'aj@gmail.com')))) 
    OR 
    -- AI-generated threads (only if user follows AI user)
    ((u.email LIKE '%ai%' OR u.email LIKE '%assistant%') AND EXISTS 
    (SELECT 1 FROM follows f WHERE f.follower_id = (SELECT id FROM users WHERE email = 'aj@gmail.com') AND f.following_id = u.id))
)
ORDER BY t.created_at DESC
LIMIT 15;

-- Count total posts visible to aj@gmail.com
\echo 'Total posts visible to aj@gmail.com:'
SELECT COUNT(*) as total_posts
FROM threads t
JOIN users u ON t.user_id = u.id
WHERE t.parent_thread_id IS NULL 
AND t.is_deleted = false
AND (
    -- Show ALL logged in user created threads in the feed (regardless of thread type)
    u.id = (SELECT id FROM users WHERE email = 'aj@gmail.com') OR 
    -- Regular threads (non-AI users) - exclude user's own threads to avoid duplication
    (u.email NOT LIKE '%ai%' AND u.email NOT LIKE '%assistant%' AND u.id != (SELECT id FROM users WHERE email = 'aj@gmail.com') AND 
    (t.is_public = true OR 
    (t.is_public = false AND t.reply_permission = 'FOLLOWERS' AND u.id IN 
    (SELECT f.following_id FROM follows f WHERE f.follower_id = (SELECT id FROM users WHERE email = 'aj@gmail.com'))) OR 
    (t.is_public = false AND t.reply_permission = 'FOLLOWING' AND u.id = (SELECT id FROM users WHERE email = 'aj@gmail.com')) OR 
    (t.is_public = false AND t.reply_permission = 'MENTIONED_ONLY' AND t.id IN 
    (SELECT tm.thread_id FROM thread_mentions tm WHERE tm.mentioned_user_id = (SELECT id FROM users WHERE email = 'aj@gmail.com')))) 
    OR 
    -- AI-generated threads (only if user follows AI user)
    ((u.email LIKE '%ai%' OR u.email LIKE '%assistant%') AND EXISTS 
    (SELECT 1 FROM follows f WHERE f.follower_id = (SELECT id FROM users WHERE email = 'aj@gmail.com') AND f.following_id = u.id))
);

\echo '✅ User posts visibility test completed!'

SQL_EOF

echo "✅ User posts visibility test completed!"

EOF

echo "✅ User posts visibility test completed!"
echo ""
echo "📊 This test verifies that:"
echo "1. Logged-in user's posts show in feed regardless of thread type (public, private, followers-only, etc.)"
echo "2. User's own posts are always visible in their feed"
echo "3. No duplication of user's own posts"
echo ""
echo "🎯 Expected results:"
echo "- All user's own posts should be visible"
echo "- User's posts should show regardless of privacy settings"
echo "- No duplicate posts in the feed"
