# 📁 Git Ignore Configuration

This document explains the `.gitignore` files in the Urutte.com project.

## 📋 Files Created

### 1. **`.gitignore`** (Root Level)
Main gitignore file that covers the entire project:
- **Operating System Files**: `.DS_Store`, `Thumbs.db`, etc.
- **IDE Files**: `.vscode/`, `.idea/`, `*.iml`, etc.
- **Java/Spring Boot**: `*.class`, `build/`, `target/`, etc.
- **Node.js/Frontend**: `node_modules/`, `build/`, `dist/`, etc.
- **Database Files**: `*.db`, `*.sqlite`, `*.dump`, etc.
- **Environment Files**: `.env`, `.env.local`, etc.
- **Uploads and Media**: `uploads/`, `media/`, etc.
- **Logs and Monitoring**: `*.log`, `logs/`, etc.
- **Docker Files**: `docker-data/`, `postgres-data/`, etc.
- **SSL Certificates**: `*.pem`, `*.key`, `*.crt`, etc.
- **Backup Files**: `backups/`, `*.backup`, etc.

### 2. **`.dockerignore`** (Root Level)
Optimizes Docker builds by excluding unnecessary files:
- Documentation files
- IDE configurations
- Build outputs
- Log files
- Git files
- Environment files

### 3. **`frontend_v2/.dockerignore`**
Frontend-specific Docker ignore:
- `node_modules/`
- Build outputs
- Test coverage
- IDE files
- Environment files

### 4. **`backend/.dockerignore`**
Backend-specific Docker ignore:
- `build/` and `target/`
- Gradle cache
- IDE files
- Log files
- Environment files

## 🧹 Cleanup Script

### **`cleanup.sh`**
Automated script to remove unwanted files:
```bash
./cleanup.sh
```

**What it removes:**
- `node_modules/` directories
- Build directories (`build/`, `dist/`, `target/`)
- Log files (`*.log`)
- Temporary files (`*.tmp`, `*.swp`, etc.)
- OS files (`.DS_Store`, `Thumbs.db`)
- IDE files (`.vscode/`, `.idea/`, `*.iml`)
- Environment files (`.env`, `.env.local`)
- Coverage directories
- Backup and archive files

## 🎯 What Gets Ignored

### ❌ **Ignored Files/Directories:**
```
# Dependencies
node_modules/
bower_components/

# Build outputs
build/
dist/
target/
out/

# Logs
*.log
logs/
log/

# Environment
.env
.env.local
.env.production

# IDE
.vscode/
.idea/
*.iml

# OS
.DS_Store
Thumbs.db

# Database
*.db
*.sqlite
*.dump

# Uploads
uploads/
media/

# Docker
docker-data/
postgres-data/

# SSL
*.pem
*.key
*.crt

# Backups
backups/
*.backup
```

### ✅ **Kept Files:**
```
# Source code
src/
frontend_v2/src/
backend/src/

# Configuration
package.json
build.gradle
docker-compose.yml
Dockerfile*

# Documentation
README*.md
*.md

# Examples
*.example
*.template
.env.example

# Deployment
deploy.sh
monitor.sh
backup.sh
update.sh
```

## 🚀 Usage

### Before First Commit:
```bash
# 1. Run cleanup script
./cleanup.sh

# 2. Check what will be committed
git status

# 3. Add files to git
git add .

# 4. Commit
git commit -m "Initial commit with proper gitignore"
```

### Regular Development:
```bash
# The .gitignore files will automatically exclude unwanted files
git add .
git commit -m "Your changes"
```

### Docker Builds:
```bash
# .dockerignore files optimize build context
docker build -t urutte-frontend ./frontend_v2
docker build -t urutte-backend ./backend
```

## 🔧 Customization

### Adding New Ignore Patterns:
Edit the appropriate `.gitignore` file:

```bash
# Add to root .gitignore for project-wide
echo "new-pattern" >> .gitignore

# Add to frontend .dockerignore for frontend Docker builds
echo "new-pattern" >> frontend_v2/.dockerignore

# Add to backend .dockerignore for backend Docker builds
echo "new-pattern" >> backend/.dockerignore
```

### Removing Patterns:
```bash
# Remove from .gitignore
sed -i '/pattern-to-remove/d' .gitignore
```

## 📊 Benefits

### **Repository Size:**
- ✅ Smaller repository size
- ✅ Faster clone/pull operations
- ✅ Reduced bandwidth usage

### **Security:**
- ✅ No accidental commits of secrets
- ✅ Environment files protected
- ✅ SSL certificates excluded

### **Performance:**
- ✅ Faster Docker builds
- ✅ Reduced build context
- ✅ Optimized CI/CD pipelines

### **Cleanliness:**
- ✅ No OS-specific files
- ✅ No IDE configurations
- ✅ No temporary files
- ✅ No build artifacts

## 🛠️ Maintenance

### Regular Cleanup:
```bash
# Run cleanup before major commits
./cleanup.sh

# Check for new files that should be ignored
git status
```

### Update Gitignore:
```bash
# Add new patterns as needed
echo "new-pattern" >> .gitignore
git add .gitignore
git commit -m "Update gitignore patterns"
```

---

**Your repository is now properly configured to ignore unwanted files! 🎉**
