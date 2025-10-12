#!/bin/bash

# Urutte.com Monitoring Script
# This script monitors the health of your application

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}🔍 Urutte.com Health Monitor${NC}"
echo "=================================="

# Check if services are running
echo -e "\n${BLUE}📊 Service Status:${NC}"
docker-compose -f docker-compose.prod.yml ps

# Check disk space
echo -e "\n${BLUE}💾 Disk Usage:${NC}"
df -h | grep -E "(Filesystem|/dev/)"

# Check memory usage
echo -e "\n${BLUE}🧠 Memory Usage:${NC}"
free -h

# Check application health
echo -e "\n${BLUE}🏥 Application Health:${NC}"

# Check frontend
if curl -s -f http://localhost:3000/health > /dev/null; then
    echo -e "${GREEN}✅ Frontend: Healthy${NC}"
else
    echo -e "${RED}❌ Frontend: Unhealthy${NC}"
fi

# Check backend
if curl -s -f http://localhost:8080/api/health > /dev/null; then
    echo -e "${GREEN}✅ Backend: Healthy${NC}"
else
    echo -e "${RED}❌ Backend: Unhealthy${NC}"
fi

# Check database
if docker exec urutte-postgres-prod pg_isready -U urutte_user -d urutte_prod > /dev/null 2>&1; then
    echo -e "${GREEN}✅ Database: Healthy${NC}"
else
    echo -e "${RED}❌ Database: Unhealthy${NC}"
fi

# Check Nginx
if systemctl is-active --quiet nginx; then
    echo -e "${GREEN}✅ Nginx: Running${NC}"
else
    echo -e "${RED}❌ Nginx: Not Running${NC}"
fi

# Show recent logs
echo -e "\n${BLUE}📝 Recent Logs (last 10 lines):${NC}"
docker-compose -f docker-compose.prod.yml logs --tail=10

# Show resource usage
echo -e "\n${BLUE}📈 Resource Usage:${NC}"
echo "CPU Usage:"
top -bn1 | grep "Cpu(s)" | awk '{print $2}' | cut -d'%' -f1

echo -e "\n${BLUE}🔗 Application URLs:${NC}"
echo "Frontend: http://$(curl -s ifconfig.me)"
echo "API: http://$(curl -s ifconfig.me)/api"
echo "Health: http://$(curl -s ifconfig.me)/api/health"

echo -e "\n${GREEN}✅ Health check completed!${NC}"
