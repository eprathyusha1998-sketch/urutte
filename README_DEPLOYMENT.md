# 🚀 Urutte.com - AWS EC2 Deployment

A complete guide to deploy your Thread-like social media application on AWS EC2 Ubuntu.

## 📋 Quick Start

### Option 1: Automated Deployment (Recommended)
```bash
# 1. Connect to your EC2 instance
ssh -i "your-key.pem" ubuntu@your-ec2-ip

# 2. Clone and deploy
git clone https://github.com/your-username/urutte.com.git
cd urutte.com
./deploy.sh your-domain.com your-secure-password
```

### Option 2: Manual Deployment
Follow the detailed steps in `DEPLOYMENT_GUIDE.md`

## 🏗️ Architecture

```
Internet → Nginx (Port 80/443) → Frontend (Port 3000) + Backend (Port 8080) → PostgreSQL (Port 5432)
```

## 📁 Project Structure

```
urutte.com/
├── backend/                 # Spring Boot API
│   ├── src/main/java/      # Java source code
│   ├── Dockerfile          # Backend container
│   └── build.gradle        # Dependencies
├── frontend_v2/            # React frontend
│   ├── src/               # React source code
│   ├── Dockerfile         # Frontend container
│   └── nginx.conf         # Nginx config
├── deploy.sh              # Automated deployment script
├── monitor.sh             # Health monitoring script
├── backup.sh              # Database backup script
├── update.sh              # Application update script
└── docker-compose.prod.yml # Production services
```

## 🔧 Prerequisites

### AWS EC2 Instance
- **Instance Type**: t3.medium or larger (2GB+ RAM)
- **OS**: Ubuntu Server 22.04 LTS
- **Storage**: 20GB minimum
- **Security Group**: Allow ports 22, 80, 443, 8080, 3000

### Domain (Optional)
- Point your domain to EC2 public IP
- SSL certificate via Let's Encrypt

## 🚀 Deployment Steps

### 1. Launch EC2 Instance
```bash
# Recommended settings:
# - Instance: t3.medium
# - Storage: 20GB gp3
# - Security Group: HTTP, HTTPS, SSH, Custom TCP (8080, 3000)
```

### 2. Connect and Deploy
```bash
ssh -i "your-key.pem" ubuntu@your-ec2-ip
git clone https://github.com/your-username/urutte.com.git
cd urutte.com
./deploy.sh your-domain.com secure_password_123
```

### 3. Verify Deployment
```bash
# Check services
./monitor.sh

# Test endpoints
curl http://your-domain.com/api/health
curl http://your-domain.com/
```

## 🔐 Security Configuration

### Environment Variables
```bash
# Database
POSTGRES_PASSWORD=your_secure_password_here

# OIDC (if using Google/other OAuth)
OIDC_CLIENT_ID=your_client_id
OIDC_CLIENT_SECRET=your_client_secret
OIDC_ISSUER_URI=your_issuer_uri
```

### SSL Certificate (Production)
```bash
sudo apt install certbot python3-certbot-nginx -y
sudo certbot --nginx -d your-domain.com
```

## 📊 Monitoring & Maintenance

### Health Monitoring
```bash
# Check application health
./monitor.sh

# View logs
docker-compose -f docker-compose.prod.yml logs -f

# Check resource usage
htop
df -h
```

### Backup Strategy
```bash
# Manual backup
./backup.sh

# Automated daily backups
crontab -e
# Add: 0 2 * * * /home/ubuntu/urutte.com/backup.sh
```

### Updates
```bash
# Update application
./update.sh

# Or manual update
git pull
docker-compose -f docker-compose.prod.yml up -d --build
```

## 🛠️ Troubleshooting

### Common Issues

#### Services Not Starting
```bash
# Check logs
docker-compose -f docker-compose.prod.yml logs

# Restart services
docker-compose -f docker-compose.prod.yml restart

# Check disk space
df -h
```

#### Database Connection Issues
```bash
# Check database status
docker exec urutte-postgres-prod pg_isready -U urutte_user

# Check database logs
docker logs urutte-postgres-prod
```

#### Memory Issues
```bash
# Check memory usage
free -h

# Increase EC2 instance size if needed
```

#### Port Conflicts
```bash
# Check what's using ports
sudo netstat -tulpn | grep :80
sudo netstat -tulpn | grep :8080
```

### Useful Commands

```bash
# View all containers
docker ps -a

# View container logs
docker logs container_name

# Restart specific service
docker-compose -f docker-compose.prod.yml restart service_name

# Scale services
docker-compose -f docker-compose.prod.yml up -d --scale backend=2

# Clean up unused resources
docker system prune -a
```

## 📈 Performance Optimization

### Database Optimization
```sql
-- Add indexes for better performance
CREATE INDEX idx_threads_created_at ON threads(created_at);
CREATE INDEX idx_threads_user_id ON threads(user_id);
CREATE INDEX idx_threads_parent_id ON threads(parent_thread_id);
```

### Nginx Optimization
```nginx
# Add to nginx.conf
gzip on;
gzip_types text/plain text/css application/json application/javascript text/xml application/xml;
client_max_body_size 10M;
```

### Docker Optimization
```bash
# Use multi-stage builds
# Optimize image sizes
# Use .dockerignore files
```

## 🔄 CI/CD Pipeline (Optional)

### GitHub Actions
```yaml
name: Deploy to AWS EC2
on:
  push:
    branches: [main]
jobs:
  deploy:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - name: Deploy to EC2
        run: |
          ssh -i ${{ secrets.EC2_KEY }} ubuntu@${{ secrets.EC2_HOST }} 'cd urutte.com && git pull && ./update.sh'
```

## 📞 Support

### Logs Location
- **Application Logs**: `docker-compose -f docker-compose.prod.yml logs`
- **Nginx Logs**: `/var/log/nginx/`
- **System Logs**: `/var/log/syslog`

### Backup Location
- **Database Backups**: `/home/ubuntu/backups/`
- **Upload Files**: `./uploads/`

## 🎯 Production Checklist

- [ ] Strong database password
- [ ] SSL certificate installed
- [ ] Firewall configured
- [ ] Backup strategy in place
- [ ] Monitoring setup
- [ ] Domain configured
- [ ] OIDC credentials set
- [ ] File upload limits configured
- [ ] Error logging enabled
- [ ] Performance monitoring

## 🚀 Go Live!

Your Thread-like social media application is now ready for production!

**Access your app at**: `http://your-domain.com`

**API Documentation**: `http://your-domain.com/api/health`

---

**Happy Threading! 🧵✨**
