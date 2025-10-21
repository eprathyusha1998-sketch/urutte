#!/bin/bash

# Script to generate AI content on production server
# This will create AI posts for the ai.assistant@urutte.com user

echo "🤖 Generating AI content on production server..."

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

# Execute AI content generation on production server
ssh -i "$KEY_PATH" "$SERVER_USER@$SERVER_IP" << 'EOF'

echo "📊 Generating AI content on production..."

# Check if backend is running
if ! curl -s http://localhost:8080/actuator/health > /dev/null; then
    echo "❌ Backend is not running. Please start it first."
    exit 1
fi

echo "✅ Backend is running and healthy"

# Initialize AI content system
echo "🚀 Initializing AI content system..."
response=$(curl -s -X POST http://localhost:8080/api/ai-content/initialize)

if echo "$response" | grep -q "success"; then
    echo "✅ AI content system initialized successfully"
else
    echo "⚠️  AI content system initialization response: $response"
fi

# Check AI Admin
echo "👤 Checking AI Admin..."
ai_admin=$(curl -s http://localhost:8080/api/ai-content/ai-admin)

if echo "$ai_admin" | grep -q "ai_assistant"; then
    echo "✅ AI Admin is ready"
else
    echo "⚠️  AI Admin check response: $ai_admin"
fi

# Check topics
echo "📋 Checking topics..."
topics_count=$(curl -s http://localhost:8080/api/ai-content/topics | jq '. | length' 2>/dev/null || echo "0")

if [ "$topics_count" -gt 0 ]; then
    echo "✅ $topics_count topics available"
else
    echo "⚠️  No topics found"
fi

# Generate AI content
echo "🧪 Generating AI content..."
echo "This will create AI posts for ai.assistant@urutte.com"

# Generate content
response=$(curl -s -X POST http://localhost:8080/api/ai-content/generate)

if echo "$response" | grep -q "success"; then
    echo "✅ AI content generation completed"
    
    # Wait a moment for content to be processed
    echo "⏳ Waiting for content to be processed..."
    sleep 5
    
    # Check how many AI posts were created
    echo "📊 Checking generated AI posts..."
    
    # Connect to database to check AI posts
    docker compose -f docker-compose.prod.yml exec -T postgres psql -U urutte_user -d urutte_prod << 'SQL_EOF'
    
    -- Check AI posts count
    \echo 'AI posts count:'
    SELECT COUNT(*) as ai_posts_count
    FROM threads t
    JOIN users u ON t.user_id = u.id
    WHERE u.email = 'ai.assistant@urutte.com';
    
    -- Show recent AI posts
    \echo 'Recent AI posts:'
    SELECT 
        t.id,
        LEFT(t.content, 100) as content_preview,
        t.created_at
    FROM threads t
    JOIN users u ON t.user_id = u.id
    WHERE u.email = 'ai.assistant@urutte.com'
    ORDER BY t.created_at DESC
    LIMIT 5;
    
    -- Show AI posts with topics
    \echo 'AI posts with topics:'
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
    LIMIT 5;
    
    SQL_EOF
    
else
    echo "⚠️  AI content generation response: $response"
fi

echo "✅ AI content generation completed on production!"

EOF

echo "✅ AI content generation completed!"
echo ""
echo "🎉 AI posts should now be available for users who follow ai.assistant@urutte.com and like matching topics!"
echo "📊 Check the production database to see the generated AI posts"
