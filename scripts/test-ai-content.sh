#!/bin/bash

# Test AI Content Generation Script
# This script demonstrates what the AI Assistant will post

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${GREEN}🤖 AI Content Generation Test${NC}"
echo ""

# Check if backend is running
if ! curl -s http://localhost:8080/actuator/health > /dev/null; then
    echo -e "${RED}❌ Backend is not running. Please start it first:${NC}"
    echo -e "${YELLOW}   docker-compose up -d backend${NC}"
    exit 1
fi

echo -e "${BLUE}🔍 Checking AI Assistant...${NC}"
ai_admin=$(curl -s http://localhost:8080/api/ai-content/ai-admin)

if echo "$ai_admin" | grep -q "ai_assistant"; then
    echo -e "${GREEN}✅ AI Assistant is ready${NC}"
    echo -e "${BLUE}   Username: @ai_assistant${NC}"
    echo -e "${BLUE}   Avatar: Custom robot avatar${NC}"
else
    echo -e "${YELLOW}⚠️  AI Assistant not found. Running setup first...${NC}"
    ./scripts/setup-ai-content.sh
fi

echo ""
echo -e "${BLUE}📋 Available Topics:${NC}"
topics=$(curl -s http://localhost:8080/api/ai-content/topics | jq -r '.[] | "\(.name) (\(.category)) - Priority: \(.priority)"' 2>/dev/null || echo "Topics not available")

if [ "$topics" != "Topics not available" ]; then
    echo "$topics" | head -10
    echo -e "${BLUE}   ... and more topics${NC}"
else
    echo -e "${YELLOW}   Topics not available (jq not installed or API error)${NC}"
fi

echo ""
echo -e "${BLUE}🧪 Testing Content Generation...${NC}"
echo -e "${YELLOW}This will generate sample content for demonstration${NC}"

read -p "Do you want to generate test content now? (y/n): " generate_test

if [ "$generate_test" = "y" ] || [ "$generate_test" = "Y" ]; then
    echo -e "${BLUE}🚀 Generating test content...${NC}"
    
    # Generate content
    response=$(curl -s -X POST http://localhost:8080/api/ai-content/generate)
    
    if echo "$response" | grep -q "success"; then
        echo -e "${GREEN}✅ Test content generation completed${NC}"
        
        # Wait a moment for content to be processed
        echo -e "${YELLOW}⏳ Waiting for content to be processed...${NC}"
        sleep 5
        
        # Show what was generated
        echo ""
        echo -e "${BLUE}📰 Generated Content Preview:${NC}"
        echo -e "${YELLOW}The AI Assistant (@ai_assistant) will post threads like:${NC}"
        echo ""
        echo -e "${BLUE}Example Thread 1:${NC}"
        echo -e "🤖 @ai_assistant"
        echo -e "🔥 Breaking: New AI model achieves 99% accuracy in medical diagnosis!"
        echo -e ""
        echo -e "This breakthrough could revolutionize healthcare. What are your thoughts on AI in medicine? #AI #Healthcare #Innovation"
        echo ""
        
        echo -e "${BLUE}Example Thread 2:${NC}"
        echo -e "🤖 @ai_assistant"
        echo -e "💻 React 19 is here with amazing new features!"
        echo -e ""
        echo -e "Server components, improved performance, and better developer experience. Who's excited to try it out? #React #WebDev #JavaScript"
        echo ""
        
        echo -e "${BLUE}Example Thread 3:${NC}"
        echo -e "🤖 @ai_assistant"
        echo -e "🚀 SpaceX successfully launches 50 satellites into orbit!"
        echo -e ""
        echo -e "Another milestone in space exploration. The future of satellite internet is looking bright! #SpaceX #Space #Technology"
        echo ""
        
    else
        echo -e "${YELLOW}⚠️  Test generation response: $response${NC}"
    fi
fi

echo ""
echo -e "${GREEN}🎉 AI Content Generation Test Complete!${NC}"
echo ""
echo -e "${BLUE}📋 What you'll see in your app:${NC}"
echo -e "   🤖 Posts from @ai_assistant with robot avatar"
echo -e "   📰 Fresh content every 2 hours automatically"
echo -e "   🎯 Topics covering Technology, Business, Science, etc."
echo -e "   💬 Engaging posts that encourage discussion"
echo -e "   🔗 Source attribution for transparency"
echo ""
echo -e "${BLUE}🔧 To monitor the system:${NC}"
echo -e "   • Check logs: docker-compose logs -f backend"
echo -e "   • View stats: curl http://localhost:8080/api/ai-content/ai-admin/stats"
echo -e "   • Generate now: curl -X POST http://localhost:8080/api/ai-content/generate"
echo ""
echo -e "${GREEN}🚀 Your AI Assistant is ready to keep your platform active!${NC}"
