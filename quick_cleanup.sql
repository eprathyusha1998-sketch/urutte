-- Quick Database Cleanup
-- Run this to quickly clean all data from tables

-- Disable foreign key checks
SET FOREIGN_KEY_CHECKS = 0;

-- Delete all data from tables
TRUNCATE TABLE comment_likes;
TRUNCATE TABLE comment_replies;
TRUNCATE TABLE comments;
TRUNCATE TABLE likes;
TRUNCATE TABLE reposts;
TRUNCATE TABLE posts;
TRUNCATE TABLE follows;
TRUNCATE TABLE messages;
TRUNCATE TABLE notifications;
TRUNCATE TABLE users;

-- Re-enable foreign key checks
SET FOREIGN_KEY_CHECKS = 1;

-- Show result
SELECT 'Quick cleanup completed!' as Status;
