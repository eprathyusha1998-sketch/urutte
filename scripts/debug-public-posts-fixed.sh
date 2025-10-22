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

-- Check user IDs for both users
\echo 'User IDs for both users:'
SELECT id, email, name FROM users WHERE email IN ('sivaprakashniet@gmail.com', 'eprathyusha1998@gmail.com');

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

-- Test the feed query for eprathyusha1998@gmail.com (simplified)
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
    u.id = (SELECT id FROM users WHERE email = 'eprathyusha1998@gmail.com') OR 
    (u.email NOT LIKE '%ai%' AND u.email NOT LIKE '%assistant%' AND u.id != (SELECT id FROM users WHERE email = 'eprathyusha1998@gmail.com') AND t.is_public = true)
)
ORDER BY t.created_at DESC
LIMIT 20;

\echo '✅ Public posts debug completed!'

SQL_EOF

echo "✅ Public posts debug completed!"

EOF

echo "✅ Public posts debug completed!"
echo ""
echo "📊 This test verifies:"
echo "1. User IDs for both users"
echo "2. All public posts from regular users"
echo "3. What the feed query returns for eprathyusha1998@gmail.com"
echo ""
echo "🎯 Expected results:"
echo "- Both users should have valid IDs"
echo "- Public posts from regular users should be visible"
echo "- Feed query should return public posts for eprathyusha1998@gmail.com"
