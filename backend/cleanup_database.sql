-- Database Cleanup Script
-- This script will clean up all tables and reset the database to a clean state
-- WARNING: This will delete ALL data from the database

-- Disable foreign key checks temporarily
SET FOREIGN_KEY_CHECKS = 0;

-- Clean up all tables in the correct order to avoid foreign key constraints
-- Delete from tables that reference other tables first

-- Delete from comment_likes (references comments)
DELETE FROM comment_likes;

-- Delete from comment_replies (references comments)
DELETE FROM comment_replies;

-- Delete from comments (references posts and users)
DELETE FROM comments;

-- Delete from likes (references posts and users)
DELETE FROM likes;

-- Delete from reposts (references posts and users)
DELETE FROM reposts;

-- Delete from posts (references users)
DELETE FROM posts;

-- Delete from follows (references users)
DELETE FROM follows;

-- Delete from messages (references users)
DELETE FROM messages;

-- Delete from notifications (references users)
DELETE FROM notifications;

-- Delete from users (main table)
DELETE FROM users;

-- Reset auto-increment counters
ALTER TABLE users AUTO_INCREMENT = 1;
ALTER TABLE posts AUTO_INCREMENT = 1;
ALTER TABLE comments AUTO_INCREMENT = 1;
ALTER TABLE likes AUTO_INCREMENT = 1;
ALTER TABLE reposts AUTO_INCREMENT = 1;
ALTER TABLE follows AUTO_INCREMENT = 1;
ALTER TABLE messages AUTO_INCREMENT = 1;
ALTER TABLE notifications AUTO_INCREMENT = 1;
ALTER TABLE comment_likes AUTO_INCREMENT = 1;
ALTER TABLE comment_replies AUTO_INCREMENT = 1;

-- Re-enable foreign key checks
SET FOREIGN_KEY_CHECKS = 1;

-- Show table status to confirm cleanup
SELECT 
    TABLE_NAME,
    TABLE_ROWS,
    AUTO_INCREMENT
FROM 
    INFORMATION_SCHEMA.TABLES 
WHERE 
    TABLE_SCHEMA = DATABASE()
    AND TABLE_TYPE = 'BASE TABLE'
ORDER BY 
    TABLE_NAME;

-- Success message
SELECT 'Database cleanup completed successfully!' as Status;
