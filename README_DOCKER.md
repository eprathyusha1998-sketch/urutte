# Urutte - Quick Docker Setup

Run the entire application (Frontend + Backend + Database) with Docker in 2 simple steps!

## ⚡ Quick Start

### Step 1: Setup Google OAuth
1. Go to https://console.cloud.google.com/apis/credentials
2. Create OAuth 2.0 Client ID
3. Add redirect URI: `http://localhost:8080/login/oauth2/code/google`
4. Copy Client ID and Secret

### Step 2: Run the Application

```bash
# Make sure you have your Google OAuth credentials ready
./docker-start.sh
```

The script will:
- ✅ Create `.env` file if needed
- ✅ Prompt for your Google OAuth credentials
- ✅ Build all services
- ✅ Start PostgreSQL, Backend, and Frontend
- ✅ Wait for everything to be ready
- ✅ Open your browser automatically

**That's it!** 🎉

## 🌐 Access

- **Application**: http://localhost
- **Backend API**: http://localhost:8080/api
- **Database**: localhost:5432

## 📝 Manual Setup (Alternative)

If you prefer manual setup:

```bash
# 1. Create .env file
cp env.docker.example .env

# 2. Edit .env and add your credentials
nano .env

# 3. Start with Docker Compose
docker-compose up --build
```

## 🎮 Commands

```bash
# Start (builds automatically)
docker-compose up --build

# Start in background
docker-compose up -d

# View logs
docker-compose logs -f

# Stop
docker-compose stop

# Remove containers
docker-compose down

# Restart
docker-compose restart
```

## 🔍 Troubleshooting

### Port Already in Use?
```bash
# Kill processes on ports 80, 8080, 5432
lsof -ti:80 | xargs kill -9
lsof -ti:8080 | xargs kill -9
lsof -ti:5432 | xargs kill -9
```

### Can't Connect?
```bash
# Check logs
docker-compose logs backend
docker-compose logs frontend

# Rebuild
docker-compose up --build
```

### OAuth Not Working?
Verify in Google Console:
- Redirect URI: `http://localhost:8080/login/oauth2/code/google`
- Authorized JavaScript origins: `http://localhost`

## 📖 Full Documentation

See **DOCKER_SETUP.md** for:
- Detailed configuration
- Architecture diagrams
- Advanced troubleshooting
- Production deployment
- Security best practices

## 🏗️ What's Running?

| Service | Container | Port | Description |
|---------|-----------|------|-------------|
| Frontend | urutte-frontend | 80 | React + TypeScript + Nginx |
| Backend | urutte-backend | 8080 | Spring Boot + Java 17 |
| Database | urutte-postgres | 5432 | PostgreSQL 15 |

## 🛠️ Tech Stack

**Frontend:**
- React 18
- TypeScript
- Ionic React (Icons)
- Axios (API)
- WebSocket (Real-time)
- Nginx (Server)

**Backend:**
- Spring Boot 3
- Java 17
- Spring Security OAuth2
- PostgreSQL
- WebSocket (STOMP)
- JWT Authentication

**Infrastructure:**
- Docker
- Docker Compose
- PostgreSQL 15

## 📦 Volumes

Data is persisted in Docker volumes:
- `postgres_data` - Database data
- `backend_uploads` - Uploaded files

To backup:
```bash
docker exec urutte-postgres pg_dump -U urutte_user urutte > backup.sql
```

## 🔒 Security

**Default credentials (Change in production!):**
- DB User: `urutte_user`
- DB Password: `urutte_pass`
- DB Name: `urutte`

**Environment Variables:**
All sensitive data is in `.env` file (not committed to git)

## 🚀 Features

✅ Google OAuth2 Login  
✅ Real-time Feed  
✅ Create Posts  
✅ Like Posts  
✅ Comments  
✅ Real-time Notifications  
✅ WebSocket Messages  
✅ User Profiles  
✅ Beautiful UI (Socialite Template)  

## 💡 Tips

- First build takes ~5-10 minutes
- Subsequent starts are much faster
- Use `-d` flag to run in background
- Check logs if something doesn't work
- Frontend rebuilds only when code changes

## 🆘 Need Help?

1. Check **DOCKER_SETUP.md** for detailed docs
2. View logs: `docker-compose logs -f`
3. Verify `.env` file has correct credentials
4. Try clean rebuild: `docker-compose down && docker-compose up --build`

---

**Made with ❤️ using Docker**

Enjoy your social platform! 🎉

