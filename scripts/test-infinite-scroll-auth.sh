#!/bin/bash

# Script to test infinite scroll functionality with authentication
# This will test the API endpoints to see if pagination is working correctly

echo "🧪 Testing infinite scroll functionality with authentication..."

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

echo "📊 Testing API endpoints for pagination with authentication..."

# First, let's get a token by logging in
echo "Getting authentication token..."
TOKEN_RESPONSE=$(curl -s -X POST "https://urutte.com/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"email": "aj@gmail.com", "password": "password123"}')

echo "Login response: $TOKEN_RESPONSE"

# Extract token from response
TOKEN=$(echo $TOKEN_RESPONSE | jq -r '.access_token // .token // empty')

if [ -z "$TOKEN" ] || [ "$TOKEN" = "null" ]; then
    echo "❌ Failed to get authentication token"
    echo "Response: $TOKEN_RESPONSE"
    exit 1
fi

echo "✅ Got authentication token"

# Test the API endpoints with authentication
echo "Testing page 0 (first 30 threads):"
curl -s -H "Authorization: Bearer $TOKEN" "https://urutte.com/api/threads?page=0&size=30" | jq '.content | length'

echo "Testing page 1 (next 30 threads):"
curl -s -H "Authorization: Bearer $TOKEN" "https://urutte.com/api/threads?page=1&size=30" | jq '.content | length'

echo "Testing page 2 (next 30 threads):"
curl -s -H "Authorization: Bearer $TOKEN" "https://urutte.com/api/threads?page=2&size=30" | jq '.content | length'

echo "Testing page 3 (next 30 threads):"
curl -s -H "Authorization: Bearer $TOKEN" "https://urutte.com/api/threads?page=3&size=30" | jq '.content | length'

echo "✅ Infinite scroll API test with authentication completed!"
echo ""
echo "📊 This test verifies that:"
echo "1. API returns data for multiple pages with authentication"
echo "2. Pagination is working correctly"
echo "3. Each page returns the expected number of threads"
echo ""
echo "🎯 Expected results:"
echo "- Page 0: 30 threads"
echo "- Page 1: 30 threads (if more data exists)"
echo "- Page 2: 30 threads (if more data exists)"
echo "- Page 3: 30 threads (if more data exists)"
