#!/bin/bash

# Script to check what topics AI posts are using
# This will help us understand why no AI posts are showing

echo "🔍 Checking AI posts and their topics..."

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

# Execute SQL commands on production server
ssh -i "$KEY_PATH" "$SERVER_USER@$SERVER_IP" << 'EOF'

echo "📊 Checking AI posts and their topics..."

# Connect to production database and check AI posts
docker compose -f docker-compose.prod.yml exec -T postgres psql -U urutte_user -d urutte_prod << 'SQL_EOF'

-- Check AI posts and their topics
\echo 'AI posts and their topics:'
SELECT 
    t.id,
    LEFT(t.content, 100) as content_preview,
    t.created_at,
    agt.topic_id,
    top.name as topic_name,
    top.description
FROM threads t
JOIN users u ON t.user_id = u.id
JOIN ai_generated_threads agt ON t.id = agt.thread_id
JOIN topics top ON agt.topic_id = top.id
WHERE u.email = 'ai.assistant@urutte.com'
ORDER BY t.created_at DESC
LIMIT 10;

-- Check what topics are most common in AI posts
\echo 'Most common topics in AI posts:'
SELECT 
    top.name as topic_name,
    COUNT(*) as post_count
FROM threads t
JOIN users u ON t.user_id = u.id
JOIN ai_generated_threads agt ON t.id = agt.thread_id
JOIN topics top ON agt.topic_id = top.id
WHERE u.email = 'ai.assistant@urutte.com'
GROUP BY top.id, top.name
ORDER BY post_count DESC
LIMIT 10;

-- Check what topics the user has liked
\echo 'Topics liked by aj@gmail.com:'
SELECT 
    t.id,
    t.name,
    t.description
FROM topics t
JOIN user_topics ut ON t.id = ut.topic_id
JOIN users u ON ut.user_id = u.id
WHERE u.email = 'aj@gmail.com'
ORDER BY t.name;

-- Check if there are any matches
\echo 'AI posts that match user topics:'
SELECT 
    t.id,
    LEFT(t.content, 100) as content_preview,
    t.created_at,
    agt.topic_id,
    top.name as topic_name
FROM threads t
JOIN users u ON t.user_id = u.id
JOIN ai_generated_threads agt ON t.id = agt.thread_id
JOIN topics top ON agt.topic_id = top.id
JOIN user_topics ut ON top.id = ut.topic_id
JOIN users u2 ON ut.user_id = u2.id
WHERE u.email = 'ai.assistant@urutte.com'
AND u2.email = 'aj@gmail.com'
ORDER BY t.created_at DESC
LIMIT 5;

SQL_EOF

echo "✅ AI posts analysis completed!"

EOF

echo "✅ Analysis completed!"
echo ""
echo "📊 This will show you:"
echo "1. What topics AI posts are using"
echo "2. What topics the user has liked"
echo "3. If there are any matches"
