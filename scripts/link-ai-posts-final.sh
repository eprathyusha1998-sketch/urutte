#!/bin/bash

# Script to link existing AI posts to topics (final working version)
# This will create ai_generated_threads records for existing AI posts

echo "🔗 Linking AI posts to topics (final version)..."

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

echo "📊 Linking AI posts to topics (final version)..."

# Connect to production database and link AI posts to topics
docker compose -f docker-compose.prod.yml exec -T postgres psql -U urutte_user -d urutte_prod << 'SQL_EOF'

-- Check ai_admins table
\echo 'Checking ai_admins table:'
SELECT id, email, name FROM ai_admins LIMIT 5;

-- Get AI posts that don't have ai_generated_threads records
\echo 'AI posts without topic links:'
SELECT COUNT(*) as unlinked_posts
FROM threads t
JOIN users u ON t.user_id = u.id
WHERE u.email = 'ai.assistant@urutte.com'
AND NOT EXISTS (
    SELECT 1 FROM ai_generated_threads agt 
    WHERE agt.thread_id = t.id
);

-- Create ai_generated_threads records for existing AI posts (using correct table and column names)
\echo 'Creating ai_generated_threads records...'

-- Get AI posts that need linking (limit to 10 for testing)
CREATE TEMP TABLE ai_posts_to_link AS
SELECT t.id as thread_id, t.content, t.created_at
FROM threads t
JOIN users u ON t.user_id = u.id
WHERE u.email = 'ai.assistant@urutte.com'
AND NOT EXISTS (
    SELECT 1 FROM ai_generated_threads agt 
    WHERE agt.thread_id = t.id
)
ORDER BY t.created_at DESC
LIMIT 10;

-- Link posts to topics based on content (using correct table and column names)
INSERT INTO ai_generated_threads (thread_id, topic_id, ai_admin_id, generation_method, status, created_at, updated_at)
SELECT 
    t.thread_id,
    CASE 
        WHEN t.content ILIKE '%AI%' OR t.content ILIKE '%artificial intelligence%' OR t.content ILIKE '%machine learning%' THEN (SELECT id FROM topics WHERE name = 'Artificial Intelligence')
        WHEN t.content ILIKE '%startup%' OR t.content ILIKE '%entrepreneur%' OR t.content ILIKE '%business%' THEN (SELECT id FROM topics WHERE name = 'Startups')
        WHEN t.content ILIKE '%India%' AND t.content NOT ILIKE '%Tamil Nadu%' THEN (SELECT id FROM topics WHERE name = 'India News')
        WHEN t.content ILIKE '%South Indian%' OR t.content ILIKE '%cinema%' OR t.content ILIKE '%movie%' THEN (SELECT id FROM topics WHERE name = 'South Indian Movies')
        WHEN t.content ILIKE '%Tamil Nadu%' THEN (SELECT id FROM topics WHERE name = 'Tamil Nadu News')
        WHEN t.content ILIKE '%news%' OR t.content ILIKE '%breaking%' THEN (SELECT id FROM topics WHERE name = 'India News')
        ELSE (SELECT id FROM topics WHERE name = 'Artificial Intelligence')
    END,
    (SELECT id FROM ai_admins WHERE email = 'ai.assistant@urutte.com' LIMIT 1),
    'openai',
    'completed',
    NOW(),
    NOW()
FROM ai_posts_to_link t;

-- Show linked AI posts
\echo 'Linked AI posts:'
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
WHERE u.email = 'ai.assistant@urutte.com'
ORDER BY t.created_at DESC
LIMIT 10;

-- Show AI posts that should now be visible to aj@gmail.com
\echo 'AI posts visible to aj@gmail.com:'
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

-- Clean up temp table
DROP TABLE ai_posts_to_link;

\echo '✅ AI posts linked to topics successfully!'

SQL_EOF

echo "✅ AI posts linked to topics!"

EOF

echo "✅ AI posts linking completed!"
echo ""
echo "🎉 AI posts are now properly linked to topics!"
echo "📊 Users who follow ai.assistant@urutte.com and like matching topics should now see these posts"
