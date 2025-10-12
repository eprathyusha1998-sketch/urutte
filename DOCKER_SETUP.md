# Docker Setup Guide

This guide will help you run the entire Urutte application (Frontend, Backend, and Database) using Docker.

## 📋 Prerequisites

1. **Docker** installed on your system
   - Download from: https://www.docker.com/products/docker-desktop
   - Version: 20.10 or higher

2. **Docker Compose** installed
   - Comes with Docker Desktop
   - Version: 2.0 or higher

3. **Google OAuth2 Credentials**
   - Get from: https://console.cloud.google.com/apis/credentials
   - Create OAuth 2.0 Client ID
   - Add authorized redirect URI: `http://localhost:8080/login/oauth2/code/google`

## 🚀 Quick Start

### Step 1: Setup Environment Variables

Create a `.env` file in the project root:

```bash
cp env.docker.example .env
```

Edit `.env` and add your Google OAuth credentials:

```env
GOOGLE_CLIENT_ID=your-actual-client-id.apps.googleusercontent.com
GOOGLE_CLIENT_SECRET=your-actual-client-secret
```

### Step 2: Build and Run

**Option A: Build and start all services**
```bash
docker-compose up --build
```

**Option B: Build and start in detached mode (background)**
```bash
docker-compose up --build -d
```

**Option C: Build without cache (clean build)**
```bash
docker-compose build --no-cache
docker-compose up
```

### Step 3: Access the Application

- **Frontend**: http://localhost (port 80)
- **Backend API**: http://localhost:8080/api
- **Database**: localhost:5432

### Step 4: Test the Application

1. Open browser: `http://localhost`
2. You should see the login page
3. Click "Sign in" or Google OAuth button
4. Authenticate with Google
5. You'll be redirected to the feed page

## 🐳 Docker Services

### 1. PostgreSQL Database
- **Container**: `urutte-postgres`
- **Port**: 5432
- **Database**: urutte
- **User**: urutte_user
- **Password**: urutte_pass
- **Volume**: `postgres_data` (persistent storage)

### 2. Spring Boot Backend
- **Container**: `urutte-backend`
- **Port**: 8080
- **Base URL**: http://localhost:8080
- **API Base**: http://localhost:8080/api
- **WebSocket**: http://localhost:8080/ws
- **Volume**: `backend_uploads` (for file uploads)

### 3. React Frontend (frontend_v2)
- **Container**: `urutte-frontend`
- **Port**: 80
- **Base URL**: http://localhost
- **Web Server**: Nginx
- **Built with**: React 18, TypeScript

## 📝 Docker Commands

### View Running Containers
```bash
docker-compose ps
```

### View Logs
```bash
# All services
docker-compose logs -f

# Specific service
docker-compose logs -f backend
docker-compose logs -f frontend
docker-compose logs -f postgres
```

### Stop Services
```bash
docker-compose stop
```

### Start Services (without rebuilding)
```bash
docker-compose start
```

### Restart Services
```bash
docker-compose restart
```

### Stop and Remove Containers
```bash
docker-compose down
```

### Stop and Remove Containers + Volumes (⚠️ Deletes data!)
```bash
docker-compose down -v
```

### Rebuild Specific Service
```bash
# Rebuild frontend only
docker-compose build frontend
docker-compose up -d frontend

# Rebuild backend only
docker-compose build backend
docker-compose up -d backend
```

### Execute Commands in Container
```bash
# Access backend container shell
docker exec -it urutte-backend sh

# Access frontend container shell
docker exec -it urutte-frontend sh

# Access PostgreSQL
docker exec -it urutte-postgres psql -U urutte_user -d urutte
```

## 🔧 Configuration

### Environment Variables

#### Backend Environment Variables
Set in `docker-compose.yml` under `backend` service:

```yaml
environment:
  SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/urutte
  SPRING_DATASOURCE_USERNAME: urutte_user
  SPRING_DATASOURCE_PASSWORD: urutte_pass
  SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_GOOGLE_CLIENT_ID: ${GOOGLE_CLIENT_ID}
  SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_GOOGLE_CLIENT_SECRET: ${GOOGLE_CLIENT_SECRET}
  APP_OAUTH2_REDIRECT_URI: http://localhost/login?token={token}
  APP_CORS_ALLOWED_ORIGINS: http://localhost
```

#### Frontend Build Arguments
Set in `docker-compose.yml` under `frontend` service:

```yaml
build:
  args:
    REACT_APP_API_URL: http://localhost:8080/api
    REACT_APP_WS_URL: http://localhost:8080/ws
```

### Port Configuration

To change ports, edit `docker-compose.yml`:

```yaml
services:
  backend:
    ports:
      - "8080:8080"  # Change first number for host port
  
  frontend:
    ports:
      - "80:80"      # Change first number for host port
  
  postgres:
    ports:
      - "5432:5432"  # Change first number for host port
```

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────┐
│                   Docker Host                    │
│                                                  │
│  ┌──────────────┐  ┌──────────────┐  ┌────────┐│
│  │   Frontend   │  │   Backend    │  │Postgres││
│  │   (Nginx)    │  │(Spring Boot) │  │   DB   ││
│  │              │  │              │  │        ││
│  │  Port: 80    │  │  Port: 8080  │  │Port:   ││
│  │              │  │              │  │ 5432   ││
│  └──────┬───────┘  └──────┬───────┘  └───┬────┘│
│         │                 │               │     │
│         └─────────────────┴───────────────┘     │
│              urutte-network (bridge)            │
└─────────────────────────────────────────────────┘
         │                 │               │
         ↓                 ↓               ↓
    localhost:80   localhost:8080   localhost:5432
```

## 🔍 Troubleshooting

### Issue: "Port already in use"

**Solution 1:** Stop the service using the port
```bash
# Find process using port 80
lsof -ti:80 | xargs kill -9

# Find process using port 8080
lsof -ti:8080 | xargs kill -9

# Find process using port 5432
lsof -ti:5432 | xargs kill -9
```

**Solution 2:** Change port in `docker-compose.yml`
```yaml
ports:
  - "8081:8080"  # Use port 8081 instead of 8080
```

### Issue: "Cannot connect to backend"

**Check backend logs:**
```bash
docker-compose logs backend
```

**Common causes:**
1. Backend failed to start - check logs for errors
2. Database not ready - wait for health check to pass
3. Wrong OAuth credentials - check `.env` file

**Solution:**
```bash
# Restart backend
docker-compose restart backend

# Or rebuild
docker-compose up --build backend
```

### Issue: "OAuth redirect not working"

**Verify:**
1. Google Console redirect URI: `http://localhost:8080/login/oauth2/code/google`
2. Frontend redirect in `.env`: `http://localhost/login?token={token}`
3. CORS origins include `http://localhost`

**Fix:**
Update `docker-compose.yml`:
```yaml
APP_OAUTH2_REDIRECT_URI: http://localhost/login?token={token}
APP_CORS_ALLOWED_ORIGINS: http://localhost,http://localhost:80
```

### Issue: "Database connection failed"

**Check database status:**
```bash
docker-compose ps postgres
docker-compose logs postgres
```

**Solution:**
```bash
# Restart database
docker-compose restart postgres

# Or remove and recreate
docker-compose down
docker-compose up -d postgres
```

### Issue: "Frontend shows blank page"

**Check frontend logs:**
```bash
docker-compose logs frontend
```

**Solution:**
```bash
# Rebuild frontend
docker-compose build --no-cache frontend
docker-compose up -d frontend
```

### Issue: "CORS errors in browser console"

**Update CORS in docker-compose.yml:**
```yaml
APP_CORS_ALLOWED_ORIGINS: http://localhost,http://localhost:80,http://127.0.0.1
```

Then restart:
```bash
docker-compose restart backend
```

## 🧹 Maintenance

### View Disk Usage
```bash
docker system df
```

### Clean Up Unused Images
```bash
docker image prune -a
```

### Clean Up Unused Volumes
```bash
docker volume prune
```

### Complete Cleanup (⚠️ Removes everything!)
```bash
docker system prune -a --volumes
```

### Backup Database
```bash
# Create backup
docker exec urutte-postgres pg_dump -U urutte_user urutte > backup.sql

# Restore backup
docker exec -i urutte-postgres psql -U urutte_user urutte < backup.sql
```

## 🔒 Security Notes

1. **Change default credentials** in production:
   - Database password
   - Add secret keys for JWT

2. **Use environment variables** for sensitive data:
   - Never commit `.env` file
   - Use `.env.example` as template

3. **Enable HTTPS** in production:
   - Use reverse proxy (Nginx/Traefik)
   - Get SSL certificate (Let's Encrypt)

4. **Restrict CORS** in production:
   - Only allow your domain
   - Don't use wildcards

## 📊 Health Checks

All services have health checks configured:

### Backend Health
```bash
curl http://localhost:8080/actuator/health
```

### Frontend Health
```bash
curl http://localhost/health
```

### Database Health
```bash
docker exec urutte-postgres pg_isready -U urutte_user
```

## 🚀 Production Deployment

### Using Docker Swarm
```bash
docker swarm init
docker stack deploy -c docker-compose.yml urutte
```

### Using Kubernetes
Convert docker-compose.yml to k8s manifests:
```bash
kompose convert
kubectl apply -f .
```

### Using Cloud Providers
- **AWS**: ECS with Fargate
- **Google Cloud**: Cloud Run or GKE
- **Azure**: Container Instances or AKS
- **DigitalOcean**: App Platform or Kubernetes

## 📚 Additional Resources

- Docker Documentation: https://docs.docker.com/
- Docker Compose: https://docs.docker.com/compose/
- Spring Boot with Docker: https://spring.io/guides/gs/spring-boot-docker/
- React with Docker: https://create-react-app.dev/docs/deployment/#docker

## 🆘 Getting Help

If you encounter issues:

1. Check logs: `docker-compose logs -f`
2. Verify environment variables in `.env`
3. Ensure ports are not in use
4. Try rebuilding: `docker-compose up --build`
5. Check this guide's troubleshooting section

---

**Ready to go!** 🎉

Run: `docker-compose up --build`

Access: http://localhost

