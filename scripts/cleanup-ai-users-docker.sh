#!/bin/bash

# Script to clean up extra AI users using Docker
# This script will analyze and clean up extra AI users

echo "🧹 Analyzing AI users in database..."

# Check if local database is running
if docker-compose -f docker-compose.local.yml ps | grep -q "urutte-postgres-local"; then
    echo "📊 Using local database via Docker..."
    
    # Execute SQL commands using Docker
    docker-compose -f docker-compose.local.yml exec -T postgres psql -U urutte_user -d urutte_local << 'EOF'
    
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
    
EOF

else
    echo "❌ Local database not running. Please start it with: ./run-local.sh"
    exit 1
fi

echo "✅ Database analysis complete!"
echo ""
echo "📋 Next steps:"
echo "1. Review the AI users and their data above"
echo "2. If you want to proceed with cleanup, run:"
echo "   ./scripts/cleanup-ai-users-execute-docker.sh"
echo ""
echo "⚠️  WARNING: This will permanently delete extra AI users and their data!"
