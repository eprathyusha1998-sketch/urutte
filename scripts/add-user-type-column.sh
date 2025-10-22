#!/bin/bash

# Script to add user_type column to users table and set existing values
# This will help migrate from email-based AI detection to proper user types

echo "🔧 Adding user_type column to users table..."

# Load deployment configuration
if [ ! -f "deploy-config.env" ]; then
    echo "❌ Deployment configuration file not found: deploy-config.env"
    exit 1
fi

source deploy-config.env

# Check if required variables are set
if [ -z "$SERVER_IP" ] || [ -z "$SERVER_USER" ] || [ -z "$KEY_PATH" ]; then
    echo "❌ Missing required configuration in deploy-config.env"
    exit 1
fi

echo "📊 Connecting to production server: $SERVER_IP"

# Execute SQL commands on production server to add user_type column
ssh -i "$KEY_PATH" "$SERVER_USER@$SERVER_IP" << 'EOF'

echo "📊 Adding user_type column to users table..."

# Connect to production database and add user_type column
docker compose -f docker-compose.prod.yml exec -T postgres psql -U urutte_user -d urutte_prod << 'SQL_EOF'

-- Add user_type column if it doesn't exist
\echo 'Adding user_type column to users table...'
ALTER TABLE users ADD COLUMN IF NOT EXISTS user_type VARCHAR(20) DEFAULT 'PUBLIC';

-- Update existing users based on email patterns
\echo 'Setting user types for existing users...'

-- Set AI users to ADMIN type
UPDATE users 
SET user_type = 'ADMIN' 
WHERE email LIKE 'ai.assistant@%' OR email LIKE '%@ai.%';

-- Set all other users to PUBLIC type (this is already the default, but being explicit)
UPDATE users 
SET user_type = 'PUBLIC' 
WHERE user_type IS NULL OR user_type = '';

-- Make the column NOT NULL after setting all values
\echo 'Making user_type column NOT NULL...'
ALTER TABLE users ALTER COLUMN user_type SET NOT NULL;

-- Show the results
\echo 'Current user types:'
SELECT 
    email,
    name,
    user_type,
    is_private,
    created_at
FROM users 
ORDER BY user_type, created_at DESC;

\echo 'User type distribution:'
SELECT 
    user_type,
    COUNT(*) as count
FROM users 
GROUP BY user_type
ORDER BY user_type;

\echo '✅ User type column added successfully!'

SQL_EOF

echo "✅ User type column added successfully!"

EOF

echo "✅ User type column added successfully!"
echo ""
echo "📊 This migration:"
echo "1. Adds user_type column to users table"
echo "2. Sets AI users (ai.assistant@%, %@ai.%) to ADMIN type"
echo "3. Sets all other users to PUBLIC type"
echo "4. Makes the column NOT NULL"
echo "5. Shows current user type distribution"
echo ""
echo "🎯 Next steps:"
echo "- Update ThreadRepository queries to use user_type instead of email patterns"
echo "- Update UserService to handle user type logic"
echo "- Implement private profile logic"
