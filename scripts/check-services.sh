#!/bin/bash

# Script to check if local services are running

echo "🔍 Checking local services..."

echo ""
echo "📊 Frontend (React):"
if curl -s "http://localhost:3000" > /dev/null; then
    echo "✅ Frontend is running at http://localhost:3000"
else
    echo "❌ Frontend is not running"
fi

echo ""
echo "🔧 Backend (Spring Boot):"
if curl -s "http://localhost:8080/api/threads?page=0&size=1" > /dev/null; then
    echo "✅ Backend is running at http://localhost:8080"
else
    echo "❌ Backend is not running"
fi

echo ""
echo "🗄️ Database (PostgreSQL):"
if curl -s "http://localhost:8080/api/actuator/health" | grep -q "UP"; then
    echo "✅ Database connection is healthy"
else
    echo "❌ Database connection issues"
fi

echo ""
echo "🧪 Test URLs:"
echo "Frontend: http://localhost:3000/feed"
echo "Backend API: http://localhost:8080/api/threads?page=0&size=30"
echo "Test Page: file://$(pwd)/test-infinite-scroll.html"

echo ""
echo "📝 Next Steps:"
echo "1. Visit http://localhost:3000/feed"
echo "2. Look for the blue dashed loading box"
echo "3. Click 'Load More (Manual)' button"
echo "4. Check browser console for debug logs"
