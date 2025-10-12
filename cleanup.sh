#!/bin/bash

# Urutte.com - Cleanup Script
# Remove unwanted files before committing to git

echo "🧹 Cleaning up unwanted files..."

# Remove node_modules directories
echo "Removing node_modules directories..."
find . -name "node_modules" -type d -exec rm -rf {} + 2>/dev/null || true

# Remove build directories
echo "Removing build directories..."
find . -name "build" -type d -exec rm -rf {} + 2>/dev/null || true
find . -name "dist" -type d -exec rm -rf {} + 2>/dev/null || true
find . -name "target" -type d -exec rm -rf {} + 2>/dev/null || true

# Remove log files
echo "Removing log files..."
find . -name "*.log" -type f -delete 2>/dev/null || true

# Remove temporary files
echo "Removing temporary files..."
find . -name "*.tmp" -type f -delete 2>/dev/null || true
find . -name "*.temp" -type f -delete 2>/dev/null || true
find . -name "*.swp" -type f -delete 2>/dev/null || true
find . -name "*.swo" -type f -delete 2>/dev/null || true

# Remove OS files
echo "Removing OS files..."
find . -name ".DS_Store" -type f -delete 2>/dev/null || true
find . -name "Thumbs.db" -type f -delete 2>/dev/null || true

# Remove IDE files
echo "Removing IDE files..."
find . -name ".vscode" -type d -exec rm -rf {} + 2>/dev/null || true
find . -name ".idea" -type d -exec rm -rf {} + 2>/dev/null || true
find . -name "*.iml" -type f -delete 2>/dev/null || true

# Remove environment files (keep examples)
echo "Removing environment files..."
find . -name ".env" -type f -delete 2>/dev/null || true
find . -name ".env.local" -type f -delete 2>/dev/null || true
find . -name ".env.development" -type f -delete 2>/dev/null || true
find . -name ".env.production" -type f -delete 2>/dev/null || true

# Remove coverage directories
echo "Removing coverage directories..."
find . -name "coverage" -type d -exec rm -rf {} + 2>/dev/null || true
find . -name ".nyc_output" -type d -exec rm -rf {} + 2>/dev/null || true

# Remove backup files
echo "Removing backup files..."
find . -name "*.backup" -type f -delete 2>/dev/null || true
find . -name "*.bak" -type f -delete 2>/dev/null || true

# Remove archive files
echo "Removing archive files..."
find . -name "*.zip" -type f -delete 2>/dev/null || true
find . -name "*.tar.gz" -type f -delete 2>/dev/null || true
find . -name "*.rar" -type f -delete 2>/dev/null || true

echo "✅ Cleanup completed!"
echo ""
echo "📋 Files that should be ignored by git:"
echo "- node_modules/ directories"
echo "- build/ and dist/ directories"
echo "- *.log files"
echo "- .DS_Store and Thumbs.db"
echo "- .vscode/ and .idea/ directories"
echo "- .env files (except .env.example)"
echo "- coverage/ directories"
echo "- backup and archive files"
echo ""
echo "💡 Run 'git status' to see what files are ready to commit."
