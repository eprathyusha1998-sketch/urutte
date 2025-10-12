# Docker Permissions Fix for Ubuntu 24

This guide helps resolve the Docker permissions issue you encountered when running `deploy.sh` on Ubuntu 24.

## 🚨 The Problem

When running `deploy.sh` on Ubuntu 24, you might see this error:

```
To run Docker as a non-privileged user, consider setting up the
Docker daemon in rootless mode for your user:

    dockerd-rootless-setuptool.sh install

Visit https://docs.docker.com/go/rootless/ to learn about rootless mode.
```

This happens because:
1. Docker is installed but the user isn't in the `docker` group
2. Docker daemon isn't running
3. User permissions haven't been applied after adding to docker group

## 🔧 Quick Fix

### Option 1: Use the Fix Script (Recommended)

```bash
# Run the permissions fix script
./fix-docker-permissions.sh

# Then run deployment
./deploy.sh your-domain.com your-db-password
```

### Option 2: Manual Fix

```bash
# 1. Start Docker service
sudo systemctl start docker
sudo systemctl enable docker

# 2. Add user to docker group
sudo usermod -aG docker $USER

# 3. Apply group changes (choose one):
# Option A: Logout and login again
# Option B: Run this command:
newgrp docker

# 4. Verify Docker works
docker info

# 5. Run deployment
./deploy.sh your-domain.com your-db-password
```

### Option 3: Temporary Fix (Not Recommended for Production)

```bash
# Run deployment with sudo (temporary solution)
sudo ./deploy.sh your-domain.com your-db-password
```

## 🔍 Troubleshooting Steps

### Step 1: Check Docker Installation

```bash
# Check if Docker is installed
docker --version

# If not installed, install it:
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh
rm get-docker.sh
```

### Step 2: Check Docker Service Status

```bash
# Check if Docker daemon is running
sudo systemctl status docker

# Start Docker if not running
sudo systemctl start docker
sudo systemctl enable docker
```

### Step 3: Check User Groups

```bash
# Check if user is in docker group
groups $USER

# If not in docker group, add user:
sudo usermod -aG docker $USER
```

### Step 4: Apply Group Changes

After adding user to docker group, you need to apply the changes:

```bash
# Method 1: Logout and login again (recommended)
exit
# Then SSH back in

# Method 2: Use newgrp command
newgrp docker

# Method 3: Start a new shell session
bash
```

### Step 5: Verify Docker Access

```bash
# Test Docker access
docker info

# If successful, you should see Docker system information
# If failed, repeat steps 3-4
```

## 🐛 Common Issues and Solutions

### Issue: "Cannot connect to the Docker daemon"

**Solution:**
```bash
# Start Docker service
sudo systemctl start docker

# Check if user is in docker group
groups $USER | grep docker

# If not, add user to docker group
sudo usermod -aG docker $USER

# Apply changes
newgrp docker
```

### Issue: "Permission denied while trying to connect to the Docker daemon socket"

**Solution:**
```bash
# Check socket permissions
ls -la /var/run/docker.sock

# Should show: srw-rw---- 1 root docker
# If not, fix permissions:
sudo chmod 666 /var/run/docker.sock
sudo chown root:docker /var/run/docker.sock
```

### Issue: "Docker daemon not running"

**Solution:**
```bash
# Start Docker service
sudo systemctl start docker
sudo systemctl enable docker

# Check status
sudo systemctl status docker
```

### Issue: "User still can't access Docker after adding to group"

**Solution:**
```bash
# Verify user is in docker group
id $USER

# If not showing docker group, try:
sudo gpasswd -a $USER docker

# Apply changes
newgrp docker

# Or logout and login again
```

## 🔄 Alternative: Rootless Docker (Advanced)

If you prefer rootless Docker (more secure but more complex):

```bash
# Install rootless Docker
dockerd-rootless-setuptool.sh install

# Follow the setup instructions
# Note: This requires additional configuration
```

## ✅ Verification

After fixing permissions, verify everything works:

```bash
# 1. Check Docker access
docker info

# 2. Test Docker Compose
docker-compose --version
# or
docker compose version

# 3. Run a test container
docker run hello-world

# 4. Run deployment
./deploy.sh your-domain.com your-db-password
```

## 📝 What the Updated deploy.sh Does

The updated `deploy.sh` script now:

1. **Checks Docker installation** and installs if missing
2. **Starts Docker service** automatically
3. **Verifies user permissions** before proceeding
4. **Handles both old and new Docker Compose syntax**
5. **Provides clear error messages** and next steps
6. **Exits gracefully** if permissions need to be applied

## 🚀 Next Steps

After fixing Docker permissions:

1. **Run the deployment script:**
   ```bash
   ./deploy.sh your-domain.com your-secure-password
   ```

2. **Monitor the deployment:**
   ```bash
   # Watch logs
   docker-compose -f docker-compose.prod.yml logs -f
   
   # Check service status
   docker-compose -f docker-compose.prod.yml ps
   ```

3. **Access your application:**
   - Frontend: `http://your-domain.com`
   - Backend API: `http://your-domain.com/api`

## 🆘 Still Having Issues?

If you're still experiencing problems:

1. **Check the logs:**
   ```bash
   ./deploy.sh your-domain.com your-password 2>&1 | tee deployment.log
   ```

2. **Run the fix script:**
   ```bash
   ./fix-docker-permissions.sh
   ```

3. **Try manual Docker commands:**
   ```bash
   docker run hello-world
   docker-compose --version
   ```

4. **Check system resources:**
   ```bash
   # Ensure you have enough disk space and memory
   df -h
   free -h
   ```

---

**Remember:** After adding a user to the docker group, you must logout and login again (or use `newgrp docker`) for the changes to take effect.
