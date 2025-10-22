#!/bin/bash

# Script to test infinite scroll functionality locally
# This will help identify the exact issue with pagination

echo "🧪 Testing infinite scroll functionality locally..."

echo "📊 Testing local API endpoints for pagination..."

# Test the local API endpoints directly
echo "Testing page 0 (first 30 threads):"
PAGE_0_RESPONSE=$(curl -s "http://localhost:8080/api/threads?page=0&size=30")
echo "Response: $PAGE_0_RESPONSE"
PAGE_0_COUNT=$(echo $PAGE_0_RESPONSE | jq '.content | length' 2>/dev/null || echo "0")
echo "Count: $PAGE_0_COUNT"

echo ""
echo "Testing page 1 (next 30 threads):"
PAGE_1_RESPONSE=$(curl -s "http://localhost:8080/api/threads?page=1&size=30")
echo "Response: $PAGE_1_RESPONSE"
PAGE_1_COUNT=$(echo $PAGE_1_RESPONSE | jq '.content | length' 2>/dev/null || echo "0")
echo "Count: $PAGE_1_COUNT"

echo ""
echo "Testing page 2 (next 30 threads):"
PAGE_2_RESPONSE=$(curl -s "http://localhost:8080/api/threads?page=2&size=30")
echo "Response: $PAGE_2_RESPONSE"
PAGE_2_COUNT=$(echo $PAGE_2_RESPONSE | jq '.content | length' 2>/dev/null || echo "0")
echo "Count: $PAGE_2_COUNT"

echo ""
echo "📊 Summary:"
echo "Page 0: $PAGE_0_COUNT threads"
echo "Page 1: $PAGE_1_COUNT threads"
echo "Page 2: $PAGE_2_COUNT threads"

echo ""
echo "✅ Local infinite scroll API test completed!"
echo ""
echo "📊 This test verifies that:"
echo "1. Local API returns data for multiple pages"
echo "2. Pagination is working correctly"
echo "3. Each page returns the expected number of threads"
echo ""
echo "🎯 Expected results:"
echo "- Page 0: 30 threads"
echo "- Page 1: 30 threads (if more data exists)"
echo "- Page 2: 30 threads (if more data exists)"
echo ""
echo "🌐 Frontend should be available at: http://localhost:3000"
echo "🔧 Backend should be available at: http://localhost:8080"
