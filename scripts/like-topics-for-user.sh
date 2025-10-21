#!/bin/bash

# Script to help a user like topics so they can see AI posts
# Usage: ./like-topics-for-user.sh <user_email>

if [ -z "$1" ]; then
    echo "❌ Usage: ./like-topics-for-user.sh <user_email>"
    echo "Example: ./like-topics-for-user.sh aj@gmail.com"
    exit 1
fi

USER_EMAIL="$1"
echo "🎯 Helping user $USER_EMAIL like topics to see AI posts..."

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

# Execute SQL commands on production server to help user like topics
ssh -i "$KEY_PATH" "$SERVER_USER@$SERVER_IP" << EOF

echo "📊 Helping user $USER_EMAIL like topics..."

# Connect to production database and help user like topics
docker compose -f docker-compose.prod.yml exec -T postgres psql -U urutte_user -d urutte_prod << 'SQL_EOF'

-- Get user ID
\echo 'Getting user ID for $USER_EMAIL...'
SELECT id, email, name FROM users WHERE email = '$USER_EMAIL';

-- Get available topics
\echo 'Available topics:'
SELECT id, name, description FROM topics WHERE is_active = true ORDER BY name;

-- Get user's current liked topics
\echo 'User current liked topics:'
SELECT t.id, t.name, t.description 
FROM topics t 
JOIN user_topics ut ON t.id = ut.topic_id 
JOIN users u ON ut.user_id = u.id 
WHERE u.email = '$USER_EMAIL';

-- Like some popular topics for the user (if they haven't liked them already)
\echo 'Liking topics for user...'

-- Get user ID
CREATE TEMP TABLE target_user AS
SELECT id FROM users WHERE email = '$USER_EMAIL';

-- Like Stock Market topic (if not already liked)
INSERT INTO user_topics (user_id, topic_id, created_at)
SELECT tu.id, t.id, NOW()
FROM target_user tu
CROSS JOIN topics t
WHERE t.name = 'Stock Market' 
AND NOT EXISTS (
    SELECT 1 FROM user_topics ut 
    WHERE ut.user_id = tu.id AND ut.topic_id = t.id
);

-- Like Sports topic (if not already liked)
INSERT INTO user_topics (user_id, topic_id, created_at)
SELECT tu.id, t.id, NOW()
FROM target_user tu
CROSS JOIN topics t
WHERE t.name = 'Sports' 
AND NOT EXISTS (
    SELECT 1 FROM user_topics ut 
    WHERE ut.user_id = tu.id AND ut.topic_id = t.id
);

-- Like US Top News topic (if not already liked)
INSERT INTO user_topics (user_id, topic_id, created_at)
SELECT tu.id, t.id, NOW()
FROM target_user tu
CROSS JOIN topics t
WHERE t.name = 'US Top News' 
AND NOT EXISTS (
    SELECT 1 FROM user_topics ut 
    WHERE ut.user_id = tu.id AND ut.topic_id = t.id
);

-- Show updated liked topics
\echo 'User updated liked topics:'
SELECT t.id, t.name, t.description 
FROM topics t 
JOIN user_topics ut ON t.id = ut.topic_id 
JOIN users u ON ut.user_id = u.id 
WHERE u.email = '$USER_EMAIL';

-- Show AI posts that should now be visible
\echo 'AI posts that should now be visible:'
SELECT t.id, t.content, t.created_at, agt.topic_id, top.name as topic_name
FROM threads t
JOIN users u ON t.user_id = u.id
JOIN ai_generated_threads agt ON t.id = agt.thread_id
JOIN topics top ON agt.topic_id = top.id
JOIN user_topics ut ON top.id = ut.topic_id
WHERE u.email = 'ai.assistant@urutte.com'
AND ut.user_id = (SELECT id FROM users WHERE email = '$USER_EMAIL')
ORDER BY t.created_at DESC
LIMIT 5;

-- Clean up temp table
DROP TABLE target_user;

\echo '✅ Topics liked successfully!'

SQL_EOF

echo "✅ User $USER_EMAIL should now be able to see AI posts!"

EOF

echo "✅ Topic liking completed!"
echo ""
echo "🎉 User $USER_EMAIL should now be able to see AI posts!"
echo "📊 The user has liked topics that match AI-generated content"
echo "🔄 Try refreshing the feed to see AI posts"
