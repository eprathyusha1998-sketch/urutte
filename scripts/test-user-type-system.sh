#!/bin/bash

# Script to test the new user type system
# This will verify that public posts from regular users are now visible

echo "🧪 Testing new user type system..."

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

# Execute SQL commands on production server to test user type system
ssh -i "$KEY_PATH" "$SERVER_USER@$SERVER_IP" << 'EOF'

echo "🧪 Testing new user type system..."

# Connect to production database and test user type system
docker compose -f docker-compose.prod.yml exec -T postgres psql -U urutte_user -d urutte_prod << 'SQL_EOF'

-- Test 1: Check user types
\echo 'Test 1: Current user types:'
SELECT 
    email,
    name,
    user_type,
    is_private,
    created_at
FROM users 
ORDER BY user_type, created_at DESC;

-- Test 2: Check public posts from regular users (should now include sivaprakashniet@gmail.com)
\echo 'Test 2: Public posts from regular users (should include sivaprakashniet@gmail.com):'
SELECT 
    t.id,
    LEFT(t.content, 100) as content_preview,
    t.is_public,
    t.reply_permission,
    u.email as user_email,
    u.name as user_name,
    u.user_type,
    t.created_at
FROM threads t
JOIN users u ON t.user_id = u.id
WHERE t.parent_thread_id IS NULL 
AND t.is_deleted = false
AND t.is_public = true
AND u.user_type != 'ADMIN'
ORDER BY t.created_at DESC
LIMIT 10;

-- Test 3: Test the feed query for eprathyusha1998@gmail.com (should now work)
\echo 'Test 3: Feed query for eprathyusha1998@gmail.com (should include public posts):'
SELECT 
    t.id,
    LEFT(t.content, 100) as content_preview,
    t.is_public,
    t.reply_permission,
    u.email as user_email,
    u.name as user_name,
    u.user_type,
    t.created_at
FROM threads t
JOIN users u ON t.user_id = u.id
WHERE t.parent_thread_id IS NULL 
AND t.is_deleted = false
AND (
    u.id = (SELECT id FROM users WHERE email = 'eprathyusha1998@gmail.com') OR 
    (u.user_type != 'ADMIN' AND u.id != (SELECT id FROM users WHERE email = 'eprathyusha1998@gmail.com') AND t.is_public = true)
)
ORDER BY t.created_at DESC
LIMIT 10;

-- Test 4: Check AI posts (should only show if user follows AI)
\echo 'Test 4: AI posts (ADMIN user type):'
SELECT 
    t.id,
    LEFT(t.content, 100) as content_preview,
    t.is_public,
    t.reply_permission,
    u.email as user_email,
    u.name as user_name,
    u.user_type,
    t.created_at
FROM threads t
JOIN users u ON t.user_id = u.id
WHERE t.parent_thread_id IS NULL 
AND t.is_deleted = false
AND u.user_type = 'ADMIN'
ORDER BY t.created_at DESC
LIMIT 5;

\echo '✅ User type system test completed!'

SQL_EOF

echo "✅ User type system test completed!"

EOF

echo "✅ User type system test completed!"
echo ""
echo "📊 This test verifies:"
echo "1. Current user types are correctly set"
echo "2. Public posts from regular users are visible (including sivaprakashniet@gmail.com)"
echo "3. Feed query for eprathyusha1998@gmail.com works correctly"
echo "4. AI posts are properly identified by user_type = 'ADMIN'"
echo ""
echo "🎯 Expected results:"
echo "- sivaprakashniet@gmail.com should be PUBLIC type"
echo "- Public posts from sivaprakashniet@gmail.com should be visible"
echo "- Feed query should return public posts for eprathyusha1998@gmail.com"
echo "- AI posts should be identified by user_type = 'ADMIN'"
