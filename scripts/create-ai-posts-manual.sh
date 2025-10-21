#!/bin/bash

# Script to manually create AI posts in the production database
# This will create sample AI posts for testing

echo "🤖 Creating AI posts manually in production database..."

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

# Execute SQL commands on production server to create AI posts
ssh -i "$KEY_PATH" "$SERVER_USER@$SERVER_IP" << 'EOF'

echo "📊 Creating AI posts manually..."

# Connect to production database and create AI posts
docker compose -f docker-compose.prod.yml exec -T postgres psql -U urutte_user -d urutte_prod << 'SQL_EOF'

-- Get the AI user ID
\echo 'Getting AI user ID...'
SELECT id, email, name FROM users WHERE email = 'ai.assistant@urutte.com';

-- Get some topics
\echo 'Available topics:'
SELECT id, name FROM topics WHERE is_active = true LIMIT 5;

-- Create sample AI posts
\echo 'Creating sample AI posts...'

-- Post 1: Artificial Intelligence topic
INSERT INTO threads (id, content, user_id, thread_type, thread_level, is_public, is_deleted, reply_permission, created_at, updated_at)
VALUES (
    gen_random_uuid(),
    '🤖 Exciting news in AI! New breakthroughs in machine learning are revolutionizing how we approach complex problems. What are your thoughts on the future of AI? #AI #MachineLearning #Innovation',
    (SELECT id FROM users WHERE email = 'ai.assistant@urutte.com'),
    'ORIGINAL',
    0,
    true,
    false,
    'ANYONE',
    NOW(),
    NOW()
);

-- Post 2: Startups topic
INSERT INTO threads (id, content, user_id, thread_type, thread_level, is_public, is_deleted, reply_permission, created_at, updated_at)
VALUES (
    gen_random_uuid(),
    '🚀 The startup ecosystem is buzzing with innovation! From fintech to healthtech, entrepreneurs are solving real-world problems. What startup trends are you most excited about? #Startups #Entrepreneurship #Innovation',
    (SELECT id FROM users WHERE email = 'ai.assistant@urutte.com'),
    'ORIGINAL',
    0,
    true,
    false,
    'ANYONE',
    NOW(),
    NOW()
);

-- Post 3: India News topic
INSERT INTO threads (id, content, user_id, thread_type, thread_level, is_public, is_deleted, reply_permission, created_at, updated_at)
VALUES (
    gen_random_uuid(),
    '🇮🇳 India is making waves in the global tech scene! From digital transformation to space exploration, the country is setting new benchmarks. What Indian innovation are you most proud of? #India #Technology #Innovation',
    (SELECT id FROM users WHERE email = 'ai.assistant@urutte.com'),
    'ORIGINAL',
    0,
    true,
    false,
    'ANYONE',
    NOW(),
    NOW()
);

-- Post 4: South Indian Movies topic
INSERT INTO threads (id, content, user_id, thread_type, thread_level, is_public, is_deleted, reply_permission, created_at, updated_at)
VALUES (
    gen_random_uuid(),
    '🎬 South Indian cinema is breaking barriers and reaching global audiences! From compelling stories to technical excellence, the industry is setting new standards. What''s your favorite South Indian movie? #SouthIndianCinema #Movies #Entertainment',
    (SELECT id FROM users WHERE email = 'ai.assistant@urutte.com'),
    'ORIGINAL',
    0,
    true,
    false,
    'ANYONE',
    NOW(),
    NOW()
);

-- Post 5: Tamil Nadu News topic
INSERT INTO threads (id, content, user_id, thread_type, thread_level, is_public, is_deleted, reply_permission, created_at, updated_at)
VALUES (
    gen_random_uuid(),
    '🏛️ Tamil Nadu continues to lead in innovation and development! From IT hubs to manufacturing excellence, the state is a powerhouse of growth. What makes Tamil Nadu special to you? #TamilNadu #Development #Innovation',
    (SELECT id FROM users WHERE email = 'ai.assistant@urutte.com'),
    'ORIGINAL',
    0,
    true,
    false,
    'ANYONE',
    NOW(),
    NOW()
);

-- Create AI Generated Thread records for each post
\echo 'Creating AI Generated Thread records...'

-- Get the thread IDs we just created
CREATE TEMP TABLE ai_threads AS
SELECT id, created_at
FROM threads 
WHERE user_id = (SELECT id FROM users WHERE email = 'ai.assistant@urutte.com')
ORDER BY created_at DESC
LIMIT 5;

-- Create AI Generated Thread records
INSERT INTO ai_generated_threads (id, thread_id, topic_id, ai_admin_id, source_content, source_url, source_title, ai_model, created_at, updated_at)
SELECT 
    gen_random_uuid(),
    t.id,
    CASE 
        WHEN t.content LIKE '%AI%' OR t.content LIKE '%machine learning%' THEN (SELECT id FROM topics WHERE name = 'Artificial Intelligence')
        WHEN t.content LIKE '%startup%' OR t.content LIKE '%entrepreneur%' THEN (SELECT id FROM topics WHERE name = 'Startups')
        WHEN t.content LIKE '%India%' AND t.content NOT LIKE '%Tamil Nadu%' THEN (SELECT id FROM topics WHERE name = 'India News')
        WHEN t.content LIKE '%South Indian%' OR t.content LIKE '%cinema%' THEN (SELECT id FROM topics WHERE name = 'South Indian Movies')
        WHEN t.content LIKE '%Tamil Nadu%' THEN (SELECT id FROM topics WHERE name = 'Tamil Nadu News')
        ELSE (SELECT id FROM topics WHERE name = 'Artificial Intelligence')
    END,
    (SELECT id FROM ai_admin WHERE email = 'ai.assistant@urutte.com' LIMIT 1),
    t.content,
    'https://example.com/source',
    'AI Generated Content',
    'openai',
    NOW(),
    NOW()
FROM ai_threads t;

-- Show the created AI posts
\echo 'Created AI posts:'
SELECT 
    t.id,
    LEFT(t.content, 100) as content_preview,
    t.created_at,
    agt.topic_id,
    top.name as topic_name
FROM threads t
JOIN users u ON t.user_id = u.id
LEFT JOIN ai_generated_threads agt ON t.id = agt.thread_id
LEFT JOIN topics top ON agt.topic_id = top.id
WHERE u.email = 'ai.assistant@urutte.com'
ORDER BY t.created_at DESC
LIMIT 10;

-- Clean up temp table
DROP TABLE ai_threads;

\echo '✅ AI posts created successfully!'

SQL_EOF

echo "✅ AI posts created manually!"

EOF

echo "✅ AI posts creation completed!"
echo ""
echo "🎉 AI posts have been created for ai.assistant@urutte.com!"
echo "📊 Users who follow ai.assistant@urutte.com and like matching topics should now see these posts"
