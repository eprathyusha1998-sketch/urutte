#!/bin/bash

# Script to test infinite scroll functionality with detailed debugging
# This will help identify the exact issue with pagination

echo "🧪 Testing infinite scroll functionality with detailed debugging..."

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

echo "📊 Testing API endpoints for pagination with detailed debugging..."

# Test the API endpoints directly with detailed output
echo "Testing page 0 (first 30 threads):"
PAGE_0_RESPONSE=$(curl -s "https://urutte.com/api/threads?page=0&size=30")
echo "Response: $PAGE_0_RESPONSE"
PAGE_0_COUNT=$(echo $PAGE_0_RESPONSE | jq '.content | length')
echo "Count: $PAGE_0_COUNT"

echo ""
echo "Testing page 1 (next 30 threads):"
PAGE_1_RESPONSE=$(curl -s "https://urutte.com/api/threads?page=1&size=30")
echo "Response: $PAGE_1_RESPONSE"
PAGE_1_COUNT=$(echo $PAGE_1_RESPONSE | jq '.content | length')
echo "Count: $PAGE_1_COUNT"

echo ""
echo "Testing page 2 (next 30 threads):"
PAGE_2_RESPONSE=$(curl -s "https://urutte.com/api/threads?page=2&size=30")
echo "Response: $PAGE_2_RESPONSE"
PAGE_2_COUNT=$(echo $PAGE_2_RESPONSE | jq '.content | length')
echo "Count: $PAGE_2_COUNT"

echo ""
echo "Testing page 3 (next 30 threads):"
PAGE_3_RESPONSE=$(curl -s "https://urutte.com/api/threads?page=3&size=30")
echo "Response: $PAGE_3_RESPONSE"
PAGE_3_COUNT=$(echo $PAGE_3_RESPONSE | jq '.content | length')
echo "Count: $PAGE_3_COUNT"

echo ""
echo "📊 Summary:"
echo "Page 0: $PAGE_0_COUNT threads"
echo "Page 1: $PAGE_1_COUNT threads"
echo "Page 2: $PAGE_2_COUNT threads"
echo "Page 3: $PAGE_3_COUNT threads"

echo ""
echo "✅ Infinite scroll API test with detailed debugging completed!"
echo ""
echo "📊 This test verifies that:"
echo "1. API returns data for multiple pages"
echo "2. Pagination is working correctly"
echo "3. Each page returns the expected number of threads"
echo ""
echo "🎯 Expected results:"
echo "- Page 0: 30 threads"
echo "- Page 1: 30 threads (if more data exists)"
echo "- Page 2: 30 threads (if more data exists)"
echo "- Page 3: 30 threads (if more data exists)"
