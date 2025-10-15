# Urutte Deployment Guide

This guide explains how to deploy and manage the Urutte application using the new deployment system.

## 🚀 Quick Start

### Local Development
```bash
# Run the application locally
./run-local.sh
```

### Production Deployment
```bash
# Deploy to EC2 production server
./deploy-production.sh
```

## 📁 Project Structure

```
urutte/
├── config/                    # Environment configurations
│   ├── local.env             # Local development settings
│   └── production.env        # Production settings
├── scripts/                  # Utility scripts
├── run-local.sh             # Local development runner
├── deploy-production.sh     # Production deployment script
├── cleanup-project.sh       # Project cleanup utility
└── deploy-config.env        # Deployment configuration
```

## 🔧 Configuration

### 1. Deployment Configuration (`deploy-config.env`)

```bash
# Server details
SERVER_IP=18.191.252.18
SERVER_USER=ubuntu
KEY_PATH=/Users/prakashperumal/Downloads/urutte_v2.pem
DOMAIN=urutte.com
EMAIL=your-email@example.com
```

### 2. Environment Files

The system uses separate environment files for different deployments:

- **`config/local.env`** - Local development settings
- **`config/production.env`** - Production settings

## 🏠 Local Development

### Prerequisites
- Docker and Docker Compose
- Node.js (for frontend development)
- Java 17+ (for backend development)

### Running Locally

```bash
# Start local development environment
./run-local.sh
```

This will:
- ✅ Build backend and frontend
- ✅ Start PostgreSQL database
- ✅ Start backend API server
- ✅ Start frontend development server
- ✅ Set up proper networking

### Access Points
- **Frontend**: http://localhost:3000
- **Backend API**: http://localhost:8080/api
- **Database**: localhost:5432

### Stopping Local Environment
```bash
docker-compose -f docker-compose.local.yml down
```

## 🌐 Production Deployment

### Prerequisites
- Docker and Docker Compose
- SSH access to EC2 server
- SSL certificates (Let's Encrypt)

### Deploying to Production

```bash
# Deploy to production server
./deploy-production.sh
```

This will:
- ✅ Build production images
- ✅ Upload to EC2 server
- ✅ Deploy with zero downtime
- ✅ Preserve database and upload data
- ✅ Clean up old files automatically

### Production Features
- **Data Preservation**: Database and uploads are preserved across deployments
- **Zero Downtime**: Rolling deployment with health checks
- **Automatic Cleanup**: Old images and files are cleaned up
- **SSL Support**: HTTPS with Let's Encrypt certificates

## 🧹 Project Cleanup

### Automatic Cleanup
Both deployment scripts automatically clean up:
- Old Docker images
- Temporary build files
- Old deployment packages

### Manual Cleanup
```bash
# Clean up project files
./cleanup-project.sh

# Dry run to see what would be deleted
./cleanup-project.sh --dry-run
```

### What Gets Cleaned Up
- Old backup files (>7 days)
- Deployment packages (*.tar.gz)
- Test files (test-*.html, test-*.md)
- Build directories
- Old Docker images
- Temporary files
- Old configuration files

## 🔒 Data Preservation

### What's Preserved
- ✅ **Database Data**: All user data, threads, comments
- ✅ **Upload Files**: Images, videos, profile pictures
- ✅ **Configuration**: SSL certificates, nginx configs
- ✅ **Logs**: Application and nginx logs

### What's Updated
- 🔄 **Application Code**: Backend and frontend code
- 🔄 **Dependencies**: Updated packages and libraries
- 🔄 **Configuration**: Environment variables and settings

## 🐛 Troubleshooting

### Common Issues

#### 1. URL Mismatch Issues
**Problem**: Frontend calling wrong API URLs
**Solution**: The new system uses proper environment files to ensure correct URLs

#### 2. Database Connection Issues
**Problem**: Backend can't connect to database
**Solution**: Check environment variables in config files

#### 3. SSL Certificate Issues
**Problem**: HTTPS not working
**Solution**: Ensure Let's Encrypt certificates are properly mounted

#### 4. Permission Issues
**Problem**: Can't access files or directories
**Solution**: Check file permissions and ownership

### Debugging Commands

```bash
# Check service status
docker-compose -f docker-compose.prod.yml ps

# View logs
docker-compose -f docker-compose.prod.yml logs -f

# Check backend health
curl https://urutte.com/api/actuator/health

# Check database connection
docker exec -it urutte-postgres-prod psql -U urutte_user -d urutte_prod
```

## 📊 Monitoring

### Health Checks
- **Backend**: `/api/actuator/health`
- **Frontend**: `/health`
- **Database**: PostgreSQL health check

### Logs
- **Application Logs**: `./logs/`
- **Nginx Logs**: `./logs/nginx/`
- **Docker Logs**: `docker-compose logs -f`

## 🔄 Maintenance

### Regular Tasks
1. **Weekly**: Run cleanup script
2. **Monthly**: Check disk space and logs
3. **Quarterly**: Update dependencies

### Backup Strategy
- **Database**: Automated daily backups
- **Uploads**: Preserved in Docker volumes
- **Configuration**: Version controlled

## 🚨 Emergency Procedures

### Rollback Deployment
```bash
# SSH to server
ssh -i /path/to/key.pem ubuntu@server-ip

# Stop current services
docker compose -f docker-compose.prod.yml stop

# Load previous images
docker load < backend-prod-latest.tar.gz
docker load < frontend-prod-latest.tar.gz

# Start services
docker compose -f docker-compose.prod.yml up -d
```

### Database Recovery
```bash
# Restore from backup
docker exec -i urutte-postgres-prod psql -U urutte_user -d urutte_prod < backup.sql
```

## 📞 Support

For issues or questions:
1. Check the logs first
2. Review this documentation
3. Check the troubleshooting section
4. Contact the development team

---

**Last Updated**: October 2025
**Version**: 2.0
