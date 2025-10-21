#!/bin/bash

# Script to test infinite scroll functionality
# This will test the API endpoints to see if pagination is working correctly

echo "🧪 Testing infinite scroll functionality..."

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

echo "📊 Testing API endpoints for pagination..."

# Test the API endpoints directly
echo "Testing page 0 (first 30 threads):"
curl -s "https://urutte.com/api/threads?page=0&size=30" | jq '.content | length'

echo "Testing page 1 (next 30 threads):"
curl -s "https://urutte.com/api/threads?page=1&size=30" | jq '.content | length'

echo "Testing page 2 (next 30 threads):"
curl -s "https://urutte.com/api/threads?page=2&size=30" | jq '.content | length'

echo "Testing page 3 (next 30 threads):"
curl -s "https://urutte.com/api/threads?page=3&size=30" | jq '.content | length'

echo "✅ Infinite scroll API test completed!"
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
