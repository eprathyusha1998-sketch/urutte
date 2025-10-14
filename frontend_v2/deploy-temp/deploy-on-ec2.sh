#!/bin/bash
set -e

echo "🚀 Deploying Urutte Frontend on EC2..."

# Update system
sudo apt update

# Install nginx if not installed
if ! command -v nginx &> /dev/null; then
    echo "Installing nginx..."
    sudo apt install nginx -y
fi

# Create web directory
sudo mkdir -p /var/www/urutte

# Copy files
sudo cp -r * /var/www/urutte/

# Configure nginx
sudo cp nginx.conf /etc/nginx/sites-available/urutte
sudo ln -sf /etc/nginx/sites-available/urutte /etc/nginx/sites-enabled/
sudo rm -f /etc/nginx/sites-enabled/default

# Test nginx configuration
sudo nginx -t

# Set permissions
sudo chown -R www-data:www-data /var/www/urutte
sudo chmod -R 755 /var/www/urutte

# Restart nginx
sudo systemctl restart nginx
sudo systemctl enable nginx

echo "✅ Deployment completed successfully!"

# Install certbot if not installed
if ! command -v certbot &> /dev/null; then
    echo "Installing SSL certificate tools..."
    sudo apt install certbot python3-certbot-nginx -y
fi

echo "🔒 Setting up SSL certificate..."
sudo certbot --nginx -d urutte.com -d www.urutte.com --non-interactive --agree-tos --email admin@urutte.com || echo "SSL setup may need manual intervention"

echo "🎉 Deployment and SSL setup completed!"
echo "Your site should be accessible at:"
echo "  🌐 HTTP:  http://urutte.com"
echo "  🔒 HTTPS: https://urutte.com"
