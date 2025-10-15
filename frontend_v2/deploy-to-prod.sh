#!/bin/bash

# Deploy Frontend to Production EC2
# This script uploads the built frontend to the EC2 instance

set -e

# Configuration
EC2_HOST="18.191.252.18"
EC2_USER="ubuntu"
EC2_KEY="/Users/prakashperumal/Downloads/urutte_v2.pem"
REMOTE_PATH="/var/www/urutte-frontend"
LOCAL_BUILD_PATH="./build"

echo "🚀 Starting deployment to production..."

# Check if build directory exists
if [ ! -d "$LOCAL_BUILD_PATH" ]; then
    echo "❌ Build directory not found. Please run 'npm run build' first."
    exit 1
fi

# Check if EC2 key exists
if [ ! -f "$EC2_KEY" ]; then
    echo "❌ EC2 key file not found at $EC2_KEY"
    exit 1
fi

echo "📦 Creating deployment archive..."
# Create a tar archive of the build directory
tar -czf urutte-frontend-deploy.tar.gz -C build .

echo "📤 Uploading to EC2..."
# Upload the archive to EC2
scp -i "$EC2_KEY" urutte-frontend-deploy.tar.gz "$EC2_USER@$EC2_HOST:/tmp/"

echo "🔧 Deploying on EC2..."
# SSH into EC2 and deploy
ssh -i "$EC2_KEY" "$EC2_USER@$EC2_HOST" << 'EOF'
    echo "📁 Extracting files..."
    sudo mkdir -p /var/www/urutte-frontend
    sudo tar -xzf /tmp/urutte-frontend-deploy.tar.gz -C /var/www/urutte-frontend/
    
    echo "🔧 Setting permissions..."
    sudo chown -R www-data:www-data /var/www/urutte-frontend
    sudo chmod -R 755 /var/www/urutte-frontend
    
    echo "🔄 Restarting nginx..."
    sudo systemctl reload nginx
    
    echo "🧹 Cleaning up..."
    rm /tmp/urutte-frontend-deploy.tar.gz
    
    echo "✅ Deployment completed successfully!"
EOF

# Clean up local archive
rm urutte-frontend-deploy.tar.gz

echo "🎉 Frontend deployed successfully to production!"
echo "🌐 Your site should be available at: http://$EC2_HOST"
