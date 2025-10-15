#!/bin/bash

# AI Content Generation Setup Script - FREE CREDITS OPTIMIZED
# This script sets up the AI content generation system optimized for free credits

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${GREEN}🤖 Setting up AI Content Generation System (FREE CREDITS OPTIMIZED)${NC}"
echo ""
echo -e "${BLUE}📋 What this script will do:${NC}"
echo -e "   ✅ Create AI Assistant user (@ai_assistant)"
echo -e "   ✅ Set up 20+ topics for content generation"
echo -e "   ✅ Create database tables for AI content tracking"
echo -e "   ✅ Start scheduled jobs (every 6 hours - FREE CREDITS OPTIMIZED)"
echo -e "   ✅ Generate threads automatically in AI Assistant's name"
echo -e "   ✅ Use custom AI Assistant avatar"
echo -e "   ✅ Optimized for free OpenAI credits (reduced frequency & threads)"
echo ""
echo -e "${YELLOW}💰 FREE CREDITS OPTIMIZATION:${NC}"
echo -e "   • Generation frequency: Every 6 hours (instead of 2 hours)"
echo -e "   • High priority topics: Every 3 hours (instead of 1 hour)"
echo -e "   • Max threads per topic: 1-2 (instead of 3-5)"
echo -e "   • Estimated daily cost: $0.50-2.00 (instead of $3-10)"
echo -e "   • Monthly cost: $15-60 (instead of $90-300)"
echo ""

# Check if we're in the right directory
if [ ! -f "docker-compose.yml" ]; then
    echo -e "${RED}❌ Please run this script from the project root directory${NC}"
    exit 1
fi

# Check if backend is running
echo -e "${BLUE}🔍 Checking if backend is running...${NC}"
if ! curl -s http://localhost:8080/actuator/health > /dev/null; then
    echo -e "${YELLOW}⚠️  Backend is not running. Starting backend...${NC}"
    docker-compose up -d backend
    echo -e "${YELLOW}⏳ Waiting for backend to start...${NC}"
    sleep 30
fi

# Check if backend is healthy
if ! curl -s http://localhost:8080/actuator/health > /dev/null; then
    echo -e "${RED}❌ Backend is not responding. Please check the logs:${NC}"
    echo -e "${YELLOW}   docker-compose logs backend${NC}"
    exit 1
fi

echo -e "${GREEN}✅ Backend is running and healthy${NC}"

# Run database migration
echo -e "${BLUE}🗄️  Setting up database tables...${NC}"
if [ -f "backend/create_ai_content_tables.sql" ]; then
    echo -e "${YELLOW}Running database migration...${NC}"
    
    # Execute the SQL script
    docker exec -i urutte-postgres-local psql -U urutte_user -d urutte_local < backend/create_ai_content_tables.sql
    
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}✅ Database tables created successfully${NC}"
    else
        echo -e "${RED}❌ Database migration failed${NC}"
        exit 1
    fi
else
    echo -e "${RED}❌ Database migration script not found${NC}"
    exit 1
fi

# Initialize the AI content system
echo -e "${BLUE}🚀 Initializing AI content system...${NC}"
response=$(curl -s -X POST http://localhost:8080/api/ai-content/initialize)

if echo "$response" | grep -q "success"; then
    echo -e "${GREEN}✅ AI content system initialized successfully${NC}"
else
    echo -e "${YELLOW}⚠️  AI content system initialization response: $response${NC}"
fi

# Check AI Admin
echo -e "${BLUE}👤 Checking AI Admin...${NC}"
ai_admin=$(curl -s http://localhost:8080/api/ai-content/ai-admin)

if echo "$ai_admin" | grep -q "ai_assistant"; then
    echo -e "${GREEN}✅ AI Admin created successfully${NC}"
    echo -e "${BLUE}   Username: ai_assistant${NC}"
    echo -e "${BLUE}   Email: ai@urutte.com${NC}"
    echo -e "${BLUE}   Avatar: Custom AI Assistant robot avatar${NC}"
    echo -e "${BLUE}   Bio: 🤖 AI-powered content curator${NC}"
else
    echo -e "${YELLOW}⚠️  AI Admin check response: $ai_admin${NC}"
fi

# Check topics
echo -e "${BLUE}📋 Checking topics...${NC}"
topics_count=$(curl -s http://localhost:8080/api/ai-content/topics | jq '. | length' 2>/dev/null || echo "0")

if [ "$topics_count" -gt 0 ]; then
    echo -e "${GREEN}✅ $topics_count topics created successfully${NC}"
else
    echo -e "${YELLOW}   Topics not available (jq not installed or API error)${NC}"
fi

# Test content generation (optional)
echo ""
echo -e "${BLUE}🧪 Testing content generation...${NC}"
echo -e "${YELLOW}This will generate sample content for demonstration${NC}"

read -p "Do you want to test content generation now? (y/n): " test_generation

if [ "$test_generation" = "y" ] || [ "$test_generation" = "Y" ]; then
    echo -e "${YELLOW}Generating test content...${NC}"
    test_response=$(curl -s -X POST http://localhost:8080/api/ai-content/generate)
    
    if echo "$test_response" | grep -q "success"; then
        echo -e "${GREEN}✅ Test content generation completed${NC}"
    else
        echo -e "${YELLOW}⚠️  Test generation response: $test_response${NC}"
    fi
fi

# Show configuration
echo ""
echo -e "${GREEN}🎉 AI Content Generation System Setup Complete! (FREE CREDITS OPTIMIZED)${NC}"
echo ""
echo -e "${BLUE}📋 Configuration Summary:${NC}"
echo -e "   • AI Admin: @ai_assistant (ai@urutte.com)"
echo -e "   • Avatar: Custom robot avatar with blue gradient"
echo -e "   • Topics: $topics_count active topics"
echo -e "   • Generation Schedule: Every 6 hours (FREE CREDITS OPTIMIZED)"
echo -e "   • High Priority: Every 3 hours (FREE CREDITS OPTIMIZED)"
echo -e "   • Cleanup: Daily at 2 AM"
echo -e "   • Thread Creation: Automatic in AI Assistant's name"
echo -e "   • Max Threads per Topic: 1-2 (FREE CREDITS OPTIMIZED)"
echo ""
echo -e "${BLUE}💰 Cost Optimization:${NC}"
echo -e "   • Daily estimated cost: $0.50-2.00"
echo -e "   • Monthly estimated cost: $15-60"
echo -e "   • Free credits will last: 2-4 months"
echo -e "   • OpenAI API Key: Set OPENAI_API_KEY environment variable"
echo -e "   • Model: gpt-3.5-turbo (most cost-effective)"
echo ""
echo -e "${BLUE}🔧 Configuration:${NC}"
echo -e "   • OpenAI API Key: Set OPENAI_API_KEY environment variable"
echo -e "   • Model: gpt-3.5-turbo (configurable)"
echo -e "   • Max threads per topic: 1-2 (configurable per topic)"
echo ""
echo -e "${BLUE}📡 API Endpoints:${NC}"
echo -e "   • GET  /api/ai-content/ai-admin - Get AI Admin info"
echo -e "   • GET  /api/ai-content/topics - List all topics"
echo -e "   • POST /api/ai-content/generate - Generate content now"
echo -e "   • POST /api/ai-content/initialize - Initialize system"
echo ""
echo -e "${BLUE}🛠️  Management:${NC}"
echo -e "   • View logs: docker-compose logs backend"
echo -e "   • Check status: curl http://localhost:8080/api/ai-content/ai-admin/stats"
echo -e "   • Generate content: curl -X POST http://localhost:8080/api/ai-content/generate"
echo ""
echo -e "${YELLOW}💡 What happens next:${NC}"
echo -e "   🤖 AI Assistant will start posting threads automatically"
echo -e "   📰 Content will be generated from Reddit, Hacker News, and other sources"
echo -e "   🎯 Topics include: AI, Web Dev, Startups, Crypto, Gaming, and more"
echo -e "   ⏰ First content generation will happen within 6 hours"
echo -e "   👥 Users will see posts from @ai_assistant with robot avatar"
echo -e "   💰 Optimized for free credits - will last 2-4 months"
echo ""
echo -e "${YELLOW}💡 Next Steps:${NC}"
echo -e "   1. Set OPENAI_API_KEY environment variable for AI content generation"
echo -e "   2. Monitor the system logs for content generation"
echo -e "   3. Check your OpenAI usage dashboard regularly"
echo -e "   4. Customize topics and priorities as needed"
echo -e "   5. Check the generated content in your application"
echo ""
echo -e "${GREEN}🚀 Your AI content generation system is ready! (FREE CREDITS OPTIMIZED)${NC}"
