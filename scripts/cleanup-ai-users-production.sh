#!/bin/bash

# Script to clean up extra AI users from PRODUCTION database on EC2
# This script will analyze and clean up extra AI users on the production server

echo "🧹 Analyzing AI users in PRODUCTION database..."

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
echo "🔑 Using key: $KEY_PATH"

# Execute SQL commands on production server
ssh -i "$KEY_PATH" "$SERVER_USER@$SERVER_IP" << 'EOF'

echo "📊 Analyzing AI users in production database..."

# Connect to production database and analyze
docker compose -f docker-compose.prod.yml exec -T postgres psql -U urutte_user -d urutte_prod << 'SQL_EOF'

-- First, let's see what AI users exist
SELECT 'Current AI users:' as info;
SELECT id, email, name, username, created_at FROM users WHERE email LIKE '%ai%' OR email LIKE '%assistant%' ORDER BY created_at;

-- Get the ID of the main AI user we want to keep
SELECT 'Main AI user to keep:' as info;
SELECT id, email, name FROM users WHERE email = 'ai.assistant@urutte.com';

-- Count threads by AI users
SELECT 'Threads by AI users:' as info;
SELECT u.email, COUNT(t.id) as thread_count 
FROM users u 
LEFT JOIN threads t ON u.id = t.user_id 
WHERE u.email LIKE '%ai%' OR u.email LIKE '%assistant%'
GROUP BY u.id, u.email;

-- Count follow relationships involving AI users
SELECT 'Follow relationships involving AI users:' as info;
SELECT 
    'Followers of AI users' as type,
    COUNT(*) as count
FROM follows f 
JOIN users u ON f.following_id = u.id 
WHERE u.email LIKE '%ai%' OR u.email LIKE '%assistant%'
UNION ALL
SELECT 
    'AI users following others' as type,
    COUNT(*) as count
FROM follows f 
JOIN users u ON f.follower_id = u.id 
WHERE u.email LIKE '%ai%' OR u.email LIKE '%assistant%';

SQL_EOF

EOF

echo "✅ Production database analysis complete!"
echo ""
echo "📋 Next steps:"
echo "1. Review the AI users and their data above"
echo "2. If you want to proceed with cleanup, run:"
echo "   ./scripts/cleanup-ai-users-production-execute.sh"
echo ""
echo "⚠️  WARNING: This will permanently delete extra AI users and their data from PRODUCTION!"
