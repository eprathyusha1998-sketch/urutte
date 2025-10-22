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

-- Check ALL threads from sivaprakashniet@gmail.com
\echo 'ALL threads from sivaprakashniet@gmail.com:'
SELECT 
    t.id,
    LEFT(t.content, 100) as content_preview,
    t.is_public,
    t.reply_permission,
    t.parent_thread_id,
    t.is_deleted,
    u.email as user_email,
    u.name as user_name,
    t.created_at
FROM threads t
JOIN users u ON t.user_id = u.id
WHERE u.email = 'sivaprakashniet@gmail.com'
ORDER BY t.created_at DESC;

-- Check threads that match the public criteria step by step
\echo 'Threads with parent_thread_id IS NULL:'
SELECT 
    t.id,
    LEFT(t.content, 100) as content_preview,
    t.is_public,
    t.reply_permission,
    t.parent_thread_id,
    t.is_deleted,
    u.email as user_email,
    u.name as user_name,
    t.created_at
FROM threads t
JOIN users u ON t.user_id = u.id
WHERE u.email = 'sivaprakashniet@gmail.com'
AND t.parent_thread_id IS NULL
ORDER BY t.created_at DESC;

-- Check threads that are not deleted
\echo 'Threads that are not deleted:'
SELECT 
    t.id,
    LEFT(t.content, 100) as content_preview,
    t.is_public,
    t.reply_permission,
    t.parent_thread_id,
    t.is_deleted,
    u.email as user_email,
    u.name as user_name,
    t.created_at
FROM threads t
JOIN users u ON t.user_id = u.id
WHERE u.email = 'sivaprakashniet@gmail.com'
AND t.parent_thread_id IS NULL
AND t.is_deleted = false
ORDER BY t.created_at DESC;

-- Check threads that are public
\echo 'Threads that are public:'
SELECT 
    t.id,
    LEFT(t.content, 100) as content_preview,
    t.is_public,
    t.reply_permission,
    t.parent_thread_id,
    t.is_deleted,
    u.email as user_email,
    u.name as user_name,
    t.created_at
FROM threads t
JOIN users u ON t.user_id = u.id
WHERE u.email = 'sivaprakashniet@gmail.com'
AND t.parent_thread_id IS NULL
AND t.is_deleted = false
AND t.is_public = true
ORDER BY t.created_at DESC;

-- Check if user email matches the NOT LIKE criteria
\echo 'User email check for AI filtering:'
SELECT 
    u.id,
    u.email,
    u.name,
    CASE 
        WHEN u.email NOT LIKE '%ai%' AND u.email NOT LIKE '%assistant%' THEN 'PASSES_AI_FILTER'
        ELSE 'FAILS_AI_FILTER'
    END as ai_filter_result
FROM users u
WHERE u.email = 'sivaprakashniet@gmail.com';

\echo '✅ Detailed public posts debug completed!'

SQL_EOF

echo "✅ Detailed public posts debug completed!"

EOF

echo "✅ Detailed public posts debug completed!"
echo ""
echo "📊 This test verifies:"
echo "1. User IDs for both users"
echo "2. ALL threads from sivaprakashniet@gmail.com"
echo "3. Threads with parent_thread_id IS NULL"
echo "4. Threads that are not deleted"
echo "5. Threads that are public"
echo "6. AI filter check for user email"
echo ""
echo "🎯 This will help identify where the query is failing"
