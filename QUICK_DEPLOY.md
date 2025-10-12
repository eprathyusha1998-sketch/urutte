# Quick Deploy to AWS EC2

## Prerequisites
- AWS EC2 Ubuntu 22.04 instance (t3.medium or larger)
- Security group with ports 22, 80, 443, 8080, 3000 open
- SSH access to your EC2 instance

## Quick Start (5 minutes)

### 1. Connect to your EC2 instance
```bash
ssh -i "your-key.pem" ubuntu@your-ec2-ip
```

### 2. Clone and deploy
```bash
# Clone the repository
git clone https://github.com/your-username/urutte.com.git
cd urutte.com

# Run the automated deployment script
./deploy.sh your-domain.com your-secure-db-password

# Or for localhost testing:
./deploy.sh localhost secure_password_123
```

### 3. Access your app
- **Frontend**: http://your-ec2-ip or http://your-domain.com
- **API**: http://your-ec2-ip/api or http://your-domain.com/api
- **Health Check**: http://your-ec2-ip/api/health

## What the script does automatically:
✅ Installs Docker, Docker Compose, Nginx  
✅ Creates production environment files  
✅ Builds and starts all services  
✅ Configures Nginx reverse proxy  
✅ Sets up firewall rules  
✅ Creates backup and update scripts  

## Post-deployment (Optional)

### SSL Certificate (for production domains)
```bash
sudo apt install certbot python3-certbot-nginx -y
sudo certbot --nginx -d your-domain.com
```

### Daily backups
```bash
crontab -e
# Add: 0 2 * * * /home/ubuntu/urutte.com/backup.sh
```

## Troubleshooting

### Check service status
```bash
docker-compose -f docker-compose.prod.yml ps
docker-compose -f docker-compose.prod.yml logs
```

### Restart services
```bash
docker-compose -f docker-compose.prod.yml restart
```

### Update application
```bash
./update.sh
```

## Manual deployment (if script fails)

Follow the detailed steps in `DEPLOYMENT_GUIDE.md` for manual deployment.

---

**That's it! Your Thread-like app should be running on AWS EC2! 🚀**
