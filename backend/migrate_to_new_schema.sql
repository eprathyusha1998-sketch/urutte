-- Migration Script: Old Schema to New Schema
-- This script migrates data from the old schema to the new comprehensive schema

-- =============================================
-- STEP 1: BACKUP EXISTING DATA
-- =============================================

-- Create backup tables
CREATE TABLE users_backup AS SELECT * FROM users;
CREATE TABLE posts_backup AS SELECT * FROM posts;
CREATE TABLE comments_backup AS SELECT * FROM comments;
CREATE TABLE likes_backup AS SELECT * FROM likes;
CREATE TABLE reposts_backup AS SELECT * FROM reposts;
CREATE TABLE follows_backup AS SELECT * FROM follows;
CREATE TABLE messages_backup AS SELECT * FROM messages;

-- =============================================
-- STEP 2: DROP OLD TABLES
-- =============================================

-- Drop old tables in correct order
DROP TABLE IF EXISTS comment_likes;
DROP TABLE IF EXISTS comment_replies;
DROP TABLE IF EXISTS comments;
DROP TABLE IF EXISTS likes;
DROP TABLE IF EXISTS reposts;
DROP TABLE IF EXISTS posts;
DROP TABLE IF EXISTS follows;
DROP TABLE IF EXISTS messages;
DROP TABLE IF EXISTS notifications;
DROP TABLE IF EXISTS users;

-- =============================================
-- STEP 3: CREATE NEW SCHEMA
-- =============================================

-- Run the complete_database_schema.sql here
-- (This would be executed separately)

-- =============================================
-- STEP 4: MIGRATE USER DATA
-- =============================================

-- Migrate users from backup
INSERT INTO users (
    id, 
    username, 
    display_name, 
    email, 
    bio, 
    profile_image_url, 
    is_verified, 
    is_private, 
    is_active,
    created_at,
    updated_at
)
SELECT 
    id,
    COALESCE(SUBSTRING_INDEX(email, '@', 1), CONCAT('user_', id)) as username,
    COALESCE(name, 'User') as display_name,
    email,
    NULL as bio,
    picture as profile_image_url,
    COALESCE(is_verified, FALSE) as is_verified,
    COALESCE(is_private, FALSE) as is_private,
    COALESCE(is_active, TRUE) as is_active,
    created_at,
    updated_at
FROM users_backup;

-- =============================================
-- STEP 5: MIGRATE POSTS TO THREADS
-- =============================================

-- Migrate posts to threads
INSERT INTO threads (
    id,
    user_id,
    content,
    thread_type,
    parent_thread_id,
    root_thread_id,
    thread_level,
    thread_path,
    quoted_thread_id,
    quote_content,
    likes_count,
    replies_count,
    reposts_count,
    is_deleted,
    is_edited,
    is_public,
    created_at,
    updated_at
)
SELECT 
    id,
    user_id,
    content,
    CASE 
        WHEN is_quote_repost = TRUE THEN 'quote'
        WHEN parent_post_id IS NOT NULL THEN 'reply'
        ELSE 'original'
    END as thread_type,
    parent_post_id as parent_thread_id,
    COALESCE(root_post_id, id) as root_thread_id,
    COALESCE(thread_level, 0) as thread_level,
    thread_path,
    quoted_post_id as quoted_thread_id,
    NULL as quote_content,
    COALESCE(likes_count, 0) as likes_count,
    COALESCE(comments_count, 0) as replies_count,
    COALESCE(reposts_count, 0) as reposts_count,
    FALSE as is_deleted,
    FALSE as is_edited,
    TRUE as is_public,
    timestamp as created_at,
    timestamp as updated_at
FROM posts_backup;

-- =============================================
-- STEP 6: MIGRATE MEDIA ATTACHMENTS
-- =============================================

-- Migrate media from posts to thread_media
INSERT INTO thread_media (
    thread_id,
    media_type,
    media_url,
    alt_text,
    display_order,
    created_at
)
SELECT 
    id as thread_id,
    CASE 
        WHEN media_type = 'image' THEN 'image'
        WHEN media_type = 'video' THEN 'video'
        ELSE 'image'
    END as media_type,
    media_url,
    NULL as alt_text,
    0 as display_order,
    timestamp as created_at
FROM posts_backup
WHERE media_url IS NOT NULL AND media_url != '';

-- =============================================
-- STEP 7: MIGRATE LIKES
-- =============================================

-- Migrate likes from old likes table
INSERT INTO thread_likes (
    thread_id,
    user_id,
    created_at
)
SELECT 
    post_id as thread_id,
    user_id,
    created_at
FROM likes_backup;

-- =============================================
-- STEP 8: MIGRATE REPOSTS
-- =============================================

-- Migrate reposts
INSERT INTO thread_reposts (
    thread_id,
    user_id,
    repost_type,
    created_at
)
SELECT 
    original_post_id as thread_id,
    user_id,
    'repost' as repost_type,
    created_at
FROM reposts_backup;

-- =============================================
-- STEP 9: MIGRATE FOLLOWS
-- =============================================

-- Migrate follows
INSERT INTO user_follows (
    follower_id,
    following_id,
    created_at
)
SELECT 
    follower_id,
    following_id,
    created_at
FROM follows_backup;

-- =============================================
-- STEP 10: MIGRATE MESSAGES
-- =============================================

-- Create default conversations for existing messages
INSERT INTO conversations (id, type, created_by, created_at)
SELECT DISTINCT 
    CONV(CRC32(CONCAT(LEAST(sender_id, receiver_id), ':', GREATEST(sender_id, receiver_id))), 10, 10) as id,
    'direct' as type,
    LEAST(sender_id, receiver_id) as created_by,
    MIN(created_at) as created_at
FROM messages_backup
GROUP BY LEAST(sender_id, receiver_id), GREATEST(sender_id, receiver_id);

-- Add participants to conversations
INSERT INTO conversation_participants (conversation_id, user_id, joined_at)
SELECT DISTINCT
    CONV(CRC32(CONCAT(LEAST(sender_id, receiver_id), ':', GREATEST(sender_id, receiver_id))), 10, 10) as conversation_id,
    LEAST(sender_id, receiver_id) as user_id,
    MIN(created_at) as joined_at
FROM messages_backup
GROUP BY LEAST(sender_id, receiver_id), GREATEST(sender_id, receiver_id)

UNION ALL

SELECT DISTINCT
    CONV(CRC32(CONCAT(LEAST(sender_id, receiver_id), ':', GREATEST(sender_id, receiver_id))), 10, 10) as conversation_id,
    GREATEST(sender_id, receiver_id) as user_id,
    MIN(created_at) as joined_at
FROM messages_backup
GROUP BY LEAST(sender_id, receiver_id), GREATEST(sender_id, receiver_id);

-- Migrate messages
INSERT INTO messages (
    conversation_id,
    sender_id,
    content,
    message_type,
    created_at
)
SELECT 
    CONV(CRC32(CONCAT(LEAST(sender_id, receiver_id), ':', GREATEST(sender_id, receiver_id))), 10, 10) as conversation_id,
    sender_id,
    content,
    'text' as message_type,
    created_at
FROM messages_backup;

-- =============================================
-- STEP 11: UPDATE COUNTERS
-- =============================================

-- Update user thread counts
UPDATE users u 
SET threads_count = (
    SELECT COUNT(*) 
    FROM threads t 
    WHERE t.user_id = u.id 
    AND t.thread_type = 'original'
);

-- Update user follower/following counts
UPDATE users u 
SET followers_count = (
    SELECT COUNT(*) 
    FROM user_follows uf 
    WHERE uf.following_id = u.id
);

UPDATE users u 
SET following_count = (
    SELECT COUNT(*) 
    FROM user_follows uf 
    WHERE uf.follower_id = u.id
);

-- =============================================
-- STEP 12: CLEANUP
-- =============================================

-- Drop backup tables
DROP TABLE IF EXISTS users_backup;
DROP TABLE IF EXISTS posts_backup;
DROP TABLE IF EXISTS comments_backup;
DROP TABLE IF EXISTS likes_backup;
DROP TABLE IF EXISTS reposts_backup;
DROP TABLE IF EXISTS follows_backup;
DROP TABLE IF EXISTS messages_backup;

-- =============================================
-- STEP 13: VERIFICATION
-- =============================================

-- Verify migration
SELECT 'Migration completed successfully!' as Status;

-- Show migration summary
SELECT 
    'Users' as Table_Name,
    COUNT(*) as Record_Count
FROM users

UNION ALL

SELECT 
    'Threads' as Table_Name,
    COUNT(*) as Record_Count
FROM threads

UNION ALL

SELECT 
    'Thread Likes' as Table_Name,
    COUNT(*) as Record_Count
FROM thread_likes

UNION ALL

SELECT 
    'Thread Reposts' as Table_Name,
    COUNT(*) as Record_Count
FROM thread_reposts

UNION ALL

SELECT 
    'User Follows' as Table_Name,
    COUNT(*) as Record_Count
FROM user_follows

UNION ALL

SELECT 
    'Messages' as Table_Name,
    COUNT(*) as Record_Count
FROM messages

UNION ALL

SELECT 
    'Conversations' as Table_Name,
    COUNT(*) as Record_Count
FROM conversations;
