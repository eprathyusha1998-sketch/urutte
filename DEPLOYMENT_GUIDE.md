# AWS EC2 Deployment Guide for Urutte.com

This guide will help you deploy your Thread-like social media app to AWS EC2 Ubuntu instance.

## Prerequisites

1. AWS Account with EC2 access
2. Domain name (optional but recommended)
3. SSH key pair for EC2 access

## Step 1: Launch EC2 Instance

### 1.1 Create EC2 Instance
```bash
# Recommended instance type: t3.medium or larger
# AMI: Ubuntu Server 22.04 LTS
# Storage: 20GB minimum (gp3)
# Security Group: Allow HTTP (80), HTTPS (443), SSH (22)
```

### 1.2 Configure Security Group
```
Inbound Rules:
- SSH (22) - Your IP
- HTTP (80) - 0.0.0.0/0
- HTTPS (443) - 0.0.0.0/0
- Custom TCP (8080) - 0.0.0.0/0 (for Spring Boot)
- Custom TCP (3000) - 0.0.0.0/0 (for React dev)
```

## Step 2: Connect to EC2 Instance

```bash
# Replace with your key file and instance IP
ssh -i "your-key.pem" ubuntu@your-ec2-ip
```

## Step 3: Install Dependencies

### 3.1 Update System
```bash
sudo apt update && sudo apt upgrade -y
```

### 3.2 Install Docker and Docker Compose
```bash
# Install Docker
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh
sudo usermod -aG docker ubuntu

# Install Docker Compose
sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose

# Logout and login again to apply docker group changes
exit
# SSH back in
```

### 3.3 Install Java 17 (for backend)
```bash
sudo apt install openjdk-17-jdk -y
java -version
```

### 3.4 Install Node.js 18+ (for frontend)
```bash
curl -fsSL https://deb.nodesource.com/setup_18.x | sudo -E bash -
sudo apt-get install -y nodejs
node --version
npm --version
```

### 3.5 Install Nginx (for reverse proxy)
```bash
sudo apt install nginx -y
sudo systemctl start nginx
sudo systemctl enable nginx
```

## Step 4: Clone and Setup Application

### 4.1 Clone Repository
```bash
cd /home/ubuntu
git clone https://github.com/your-username/urutte.com.git
cd urutte.com
```

### 4.2 Create Production Environment File
```bash
# Create production environment file
cat > .env.production << EOF
# Database
POSTGRES_DB=urutte_prod
POSTGRES_USER=urutte_user
POSTGRES_PASSWORD=your_secure_password_here
POSTGRES_HOST=localhost
POSTGRES_PORT=5432

# Backend
SPRING_PROFILES_ACTIVE=prod
SERVER_PORT=8080

# Frontend
REACT_APP_API_URL=http://your-domain.com/api
REACT_APP_WS_URL=ws://your-domain.com/ws

# OIDC (if using)
OIDC_CLIENT_ID=your_oidc_client_id
OIDC_CLIENT_SECRET=your_oidc_client_secret
OIDC_ISSUER_URI=your_oidc_issuer_uri
EOF
```

### 4.3 Update Docker Compose for Production
```bash
# Create production docker-compose file
cat > docker-compose.prod.yml << EOF
version: '3.8'

services:
  postgres:
    image: postgres:15
    container_name: urutte-postgres-prod
    environment:
      POSTGRES_DB: urutte_prod
      POSTGRES_USER: urutte_user
      POSTGRES_PASSWORD: your_secure_password_here
    volumes:
      - postgres_data:/var/lib/postgresql/data
      - ./backend/init_database.sql:/docker-entrypoint-initdb.d/init_database.sql
    ports:
      - "5432:5432"
    restart: unless-stopped

  backend:
    build:
      context: ./backend
      dockerfile: Dockerfile
    container_name: urutte-backend-prod
    environment:
      SPRING_PROFILES_ACTIVE: prod
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/urutte_prod
      SPRING_DATASOURCE_USERNAME: urutte_user
      SPRING_DATASOURCE_PASSWORD: your_secure_password_here
    ports:
      - "8080:8080"
    depends_on:
      - postgres
    restart: unless-stopped
    volumes:
      - ./uploads:/app/uploads

  frontend:
    build:
      context: ./frontend_v2
      dockerfile: Dockerfile
      args:
        - REACT_APP_API_URL=http://your-domain.com/api
    container_name: urutte-frontend-prod
    ports:
      - "3000:80"
    depends_on:
      - backend
    restart: unless-stopped

volumes:
  postgres_data:
EOF
```

## Step 5: Build and Deploy

### 5.1 Build Frontend for Production
```bash
cd frontend_v2
npm install
npm run build
cd ..
```

### 5.2 Start Services
```bash
# Start all services
docker-compose -f docker-compose.prod.yml up -d --build

# Check status
docker-compose -f docker-compose.prod.yml ps
```

## Step 6: Configure Nginx Reverse Proxy

### 6.1 Create Nginx Configuration
```bash
sudo cat > /etc/nginx/sites-available/urutte.com << EOF
server {
    listen 80;
    server_name your-domain.com www.your-domain.com;

    # Frontend (React)
    location / {
        proxy_pass http://localhost:3000;
        proxy_http_version 1.1;
        proxy_set_header Upgrade \$http_upgrade;
        proxy_set_header Connection 'upgrade';
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto \$scheme;
        proxy_cache_bypass \$http_upgrade;
    }

    # Backend API
    location /api/ {
        proxy_pass http://localhost:8080/api/;
        proxy_http_version 1.1;
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto \$scheme;
    }

    # WebSocket support
    location /ws/ {
        proxy_pass http://localhost:8080/ws/;
        proxy_http_version 1.1;
        proxy_set_header Upgrade \$http_upgrade;
        proxy_set_header Connection "upgrade";
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto \$scheme;
    }

    # Static files
    location /uploads/ {
        proxy_pass http://localhost:8080/uploads/;
    }
}
EOF
```

### 6.2 Enable Site and Restart Nginx
```bash
sudo ln -s /etc/nginx/sites-available/urutte.com /etc/nginx/sites-enabled/
sudo nginx -t
sudo systemctl restart nginx
```

## Step 7: SSL Certificate (Optional but Recommended)

### 7.1 Install Certbot
```bash
sudo apt install certbot python3-certbot-nginx -y
```

### 7.2 Get SSL Certificate
```bash
sudo certbot --nginx -d your-domain.com -d www.your-domain.com
```

## Step 8: Configure Firewall

```bash
sudo ufw allow ssh
sudo ufw allow 'Nginx Full'
sudo ufw --force enable
```

## Step 9: Setup Monitoring and Logs

### 9.1 Create Log Rotation
```bash
sudo cat > /etc/logrotate.d/urutte << EOF
/home/ubuntu/urutte.com/logs/*.log {
    daily
    missingok
    rotate 52
    compress
    delaycompress
    notifempty
    create 644 ubuntu ubuntu
}
EOF
```

### 9.2 Create Systemd Service (Optional)
```bash
sudo cat > /etc/systemd/system/urutte.service << EOF
[Unit]
Description=Urutte.com Application
After=docker.service
Requires=docker.service

[Service]
Type=oneshot
RemainAfterExit=yes
WorkingDirectory=/home/ubuntu/urutte.com
ExecStart=/usr/local/bin/docker-compose -f docker-compose.prod.yml up -d
ExecStop=/usr/local/bin/docker-compose -f docker-compose.prod.yml down
TimeoutStartSec=0

[Install]
WantedBy=multi-user.target
EOF

sudo systemctl enable urutte.service
```

## Step 10: Database Migration and Setup

### 10.1 Run Database Migrations
```bash
# Connect to backend container
docker exec -it urutte-backend-prod bash

# Run any database migrations
# (Add your migration commands here)

exit
```

## Step 11: Testing

### 11.1 Test Application
```bash
# Check if all services are running
docker-compose -f docker-compose.prod.yml ps

# Check logs
docker-compose -f docker-compose.prod.yml logs

# Test endpoints
curl http://localhost/api/health
curl http://localhost/
```

## Step 12: Backup Strategy

### 12.1 Create Backup Script
```bash
cat > backup.sh << EOF
#!/bin/bash
DATE=\$(date +%Y%m%d_%H%M%S)
BACKUP_DIR="/home/ubuntu/backups"

mkdir -p \$BACKUP_DIR

# Backup database
docker exec urutte-postgres-prod pg_dump -U urutte_user urutte_prod > \$BACKUP_DIR/db_backup_\$DATE.sql

# Backup uploads
tar -czf \$BACKUP_DIR/uploads_backup_\$DATE.tar.gz uploads/

# Keep only last 7 days of backups
find \$BACKUP_DIR -name "*.sql" -mtime +7 -delete
find \$BACKUP_DIR -name "*.tar.gz" -mtime +7 -delete
EOF

chmod +x backup.sh
```

### 12.2 Setup Cron Job for Backups
```bash
crontab -e
# Add this line for daily backups at 2 AM
0 2 * * * /home/ubuntu/urutte.com/backup.sh
```

## Troubleshooting

### Common Issues:

1. **Port Conflicts**: Make sure ports 80, 443, 8080, 3000 are not used by other services
2. **Memory Issues**: Increase EC2 instance size if you get out of memory errors
3. **Database Connection**: Check if PostgreSQL is running and accessible
4. **File Permissions**: Ensure uploads directory has proper permissions

### Useful Commands:

```bash
# View logs
docker-compose -f docker-compose.prod.yml logs -f

# Restart services
docker-compose -f docker-compose.prod.yml restart

# Update application
git pull
docker-compose -f docker-compose.prod.yml up -d --build

# Check disk space
df -h

# Check memory usage
free -h

# Check running processes
htop
```

## Security Considerations

1. **Change default passwords** in environment files
2. **Use strong database passwords**
3. **Enable firewall** and only open necessary ports
4. **Keep system updated** regularly
5. **Monitor logs** for suspicious activity
6. **Use SSL certificates** for HTTPS
7. **Regular backups** of database and files

## Performance Optimization

1. **Use larger EC2 instance** for better performance
2. **Enable database connection pooling**
3. **Use CDN** for static assets
4. **Implement caching** strategies
5. **Monitor resource usage** and scale accordingly

Your application should now be accessible at `http://your-domain.com` or `http://your-ec2-ip`!
