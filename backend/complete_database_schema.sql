-- Complete Thread App Database Schema
-- This schema handles unlimited thread levels, proper relationships, and all modern thread app features

-- Drop existing tables if they exist
DROP TABLE IF EXISTS thread_likes;
DROP TABLE IF EXISTS thread_reposts;
DROP TABLE IF EXISTS thread_shares;
DROP TABLE IF EXISTS thread_bookmarks;
DROP TABLE IF EXISTS thread_mentions;
DROP TABLE IF EXISTS thread_hashtags;
DROP TABLE IF EXISTS hashtags;
DROP TABLE IF EXISTS thread_media;
DROP TABLE IF EXISTS thread_polls;
DROP TABLE IF EXISTS poll_options;
DROP TABLE IF EXISTS poll_votes;
DROP TABLE IF EXISTS thread_reactions;
DROP TABLE IF EXISTS thread_views;
DROP TABLE IF EXISTS thread_reports;
DROP TABLE IF EXISTS thread_moderation;
DROP TABLE IF EXISTS threads;
DROP TABLE IF EXISTS user_follows;
DROP TABLE IF EXISTS user_blocks;
DROP TABLE IF EXISTS user_mutes;
DROP TABLE IF EXISTS user_preferences;
DROP TABLE IF EXISTS user_sessions;
DROP TABLE IF EXISTS users;

-- =============================================
-- CORE USER MANAGEMENT
-- =============================================

CREATE TABLE users (
    id VARCHAR(255) PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    display_name VARCHAR(100) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    bio TEXT,
    location VARCHAR(100),
    website VARCHAR(255),
    birth_date DATE,
    profile_image_url VARCHAR(500),
    cover_image_url VARCHAR(500),
    is_verified BOOLEAN DEFAULT FALSE,
    is_private BOOLEAN DEFAULT FALSE,
    is_active BOOLEAN DEFAULT TRUE,
    is_suspended BOOLEAN DEFAULT FALSE,
    followers_count INT DEFAULT 0,
    following_count INT DEFAULT 0,
    threads_count INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    last_seen_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    INDEX idx_username (username),
    INDEX idx_email (email),
    INDEX idx_created_at (created_at)
);

-- =============================================
-- USER RELATIONSHIPS
-- =============================================

CREATE TABLE user_follows (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    follower_id VARCHAR(255) NOT NULL,
    following_id VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    UNIQUE KEY unique_follow (follower_id, following_id),
    FOREIGN KEY (follower_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (following_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_follower (follower_id),
    INDEX idx_following (following_id)
);

CREATE TABLE user_blocks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    blocker_id VARCHAR(255) NOT NULL,
    blocked_id VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    UNIQUE KEY unique_block (blocker_id, blocked_id),
    FOREIGN KEY (blocker_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (blocked_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE user_mutes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    muter_id VARCHAR(255) NOT NULL,
    muted_id VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    UNIQUE KEY unique_mute (muter_id, muted_id),
    FOREIGN KEY (muter_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (muted_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE user_preferences (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    preference_key VARCHAR(100) NOT NULL,
    preference_value TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    UNIQUE KEY unique_user_preference (user_id, preference_key),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE user_sessions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    session_token VARCHAR(500) NOT NULL,
    device_info TEXT,
    ip_address VARCHAR(45),
    is_active BOOLEAN DEFAULT TRUE,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    UNIQUE KEY unique_session_token (session_token),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_expires_at (expires_at)
);

-- =============================================
-- CORE THREAD SYSTEM
-- =============================================

CREATE TABLE threads (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    thread_type ENUM('original', 'reply', 'quote', 'retweet') DEFAULT 'original',
    
    -- Thread hierarchy (unlimited levels)
    parent_thread_id BIGINT NULL,
    root_thread_id BIGINT NULL,
    thread_level INT DEFAULT 0,
    thread_path VARCHAR(1000), -- e.g., "1.2.3.4" for unlimited nesting
    
    -- Quote/Retweet specific fields
    quoted_thread_id BIGINT NULL,
    quote_content TEXT NULL,
    
    -- Engagement counts
    likes_count INT DEFAULT 0,
    replies_count INT DEFAULT 0,
    reposts_count INT DEFAULT 0,
    shares_count INT DEFAULT 0,
    views_count INT DEFAULT 0,
    bookmarks_count INT DEFAULT 0,
    
    -- Thread status
    is_deleted BOOLEAN DEFAULT FALSE,
    is_edited BOOLEAN DEFAULT FALSE,
    is_pinned BOOLEAN DEFAULT FALSE,
    is_sensitive BOOLEAN DEFAULT FALSE,
    is_public BOOLEAN DEFAULT TRUE,
    
    -- Timestamps
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    edited_at TIMESTAMP NULL,
    
    -- Foreign keys
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (parent_thread_id) REFERENCES threads(id) ON DELETE CASCADE,
    FOREIGN KEY (root_thread_id) REFERENCES threads(id) ON DELETE CASCADE,
    FOREIGN KEY (quoted_thread_id) REFERENCES threads(id) ON DELETE SET NULL,
    
    -- Indexes for performance
    INDEX idx_user_id (user_id),
    INDEX idx_parent_thread (parent_thread_id),
    INDEX idx_root_thread (root_thread_id),
    INDEX idx_thread_level (thread_level),
    INDEX idx_thread_path (thread_path),
    INDEX idx_created_at (created_at),
    INDEX idx_thread_type (thread_type),
    INDEX idx_is_deleted (is_deleted),
    INDEX idx_is_public (is_public),
    
    -- Composite indexes for common queries
    INDEX idx_root_level_created (root_thread_id, thread_level, created_at),
    INDEX idx_user_created (user_id, created_at),
    INDEX idx_parent_created (parent_thread_id, created_at)
);

-- =============================================
-- MEDIA ATTACHMENTS
-- =============================================

CREATE TABLE thread_media (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    thread_id BIGINT NOT NULL,
    media_type ENUM('image', 'video', 'gif', 'audio', 'document') NOT NULL,
    media_url VARCHAR(500) NOT NULL,
    thumbnail_url VARCHAR(500),
    alt_text VARCHAR(500),
    file_size BIGINT,
    duration INT, -- for video/audio in seconds
    width INT,
    height INT,
    display_order INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (thread_id) REFERENCES threads(id) ON DELETE CASCADE,
    INDEX idx_thread_id (thread_id),
    INDEX idx_media_type (media_type)
);

-- =============================================
-- POLLS SYSTEM
-- =============================================

CREATE TABLE thread_polls (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    thread_id BIGINT NOT NULL,
    question TEXT NOT NULL,
    is_multiple_choice BOOLEAN DEFAULT FALSE,
    expires_at TIMESTAMP NULL,
    total_votes INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (thread_id) REFERENCES threads(id) ON DELETE CASCADE,
    INDEX idx_thread_id (thread_id),
    INDEX idx_expires_at (expires_at)
);

CREATE TABLE poll_options (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    poll_id BIGINT NOT NULL,
    option_text VARCHAR(500) NOT NULL,
    votes_count INT DEFAULT 0,
    display_order INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (poll_id) REFERENCES thread_polls(id) ON DELETE CASCADE,
    INDEX idx_poll_id (poll_id)
);

CREATE TABLE poll_votes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    poll_id BIGINT NOT NULL,
    option_id BIGINT NOT NULL,
    user_id VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    UNIQUE KEY unique_user_poll_vote (user_id, poll_id),
    FOREIGN KEY (poll_id) REFERENCES thread_polls(id) ON DELETE CASCADE,
    FOREIGN KEY (option_id) REFERENCES poll_options(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- =============================================
-- ENGAGEMENT SYSTEM
-- =============================================

CREATE TABLE thread_likes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    thread_id BIGINT NOT NULL,
    user_id VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    UNIQUE KEY unique_thread_like (thread_id, user_id),
    FOREIGN KEY (thread_id) REFERENCES threads(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_thread_id (thread_id),
    INDEX idx_user_id (user_id)
);

CREATE TABLE thread_reposts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    thread_id BIGINT NOT NULL,
    user_id VARCHAR(255) NOT NULL,
    repost_type ENUM('repost', 'quote') DEFAULT 'repost',
    quote_content TEXT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    UNIQUE KEY unique_thread_repost (thread_id, user_id),
    FOREIGN KEY (thread_id) REFERENCES threads(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_thread_id (thread_id),
    INDEX idx_user_id (user_id)
);

CREATE TABLE thread_shares (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    thread_id BIGINT NOT NULL,
    user_id VARCHAR(255) NOT NULL,
    share_platform VARCHAR(50), -- 'twitter', 'facebook', 'linkedin', etc.
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (thread_id) REFERENCES threads(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_thread_id (thread_id),
    INDEX idx_user_id (user_id)
);

CREATE TABLE thread_bookmarks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    thread_id BIGINT NOT NULL,
    user_id VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    UNIQUE KEY unique_thread_bookmark (thread_id, user_id),
    FOREIGN KEY (thread_id) REFERENCES threads(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_thread_id (thread_id),
    INDEX idx_user_id (user_id)
);

CREATE TABLE thread_reactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    thread_id BIGINT NOT NULL,
    user_id VARCHAR(255) NOT NULL,
    reaction_type ENUM('like', 'love', 'laugh', 'angry', 'sad', 'wow') DEFAULT 'like',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    UNIQUE KEY unique_thread_reaction (thread_id, user_id),
    FOREIGN KEY (thread_id) REFERENCES threads(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_thread_id (thread_id),
    INDEX idx_user_id (user_id)
);

CREATE TABLE thread_views (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    thread_id BIGINT NOT NULL,
    user_id VARCHAR(255) NULL, -- NULL for anonymous views
    ip_address VARCHAR(45),
    user_agent TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (thread_id) REFERENCES threads(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_thread_id (thread_id),
    INDEX idx_user_id (user_id),
    INDEX idx_created_at (created_at)
);

-- =============================================
-- MENTIONS AND HASHTAGS
-- =============================================

CREATE TABLE hashtags (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tag VARCHAR(100) UNIQUE NOT NULL,
    usage_count INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_tag (tag),
    INDEX idx_usage_count (usage_count)
);

CREATE TABLE thread_hashtags (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    thread_id BIGINT NOT NULL,
    hashtag_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    UNIQUE KEY unique_thread_hashtag (thread_id, hashtag_id),
    FOREIGN KEY (thread_id) REFERENCES threads(id) ON DELETE CASCADE,
    FOREIGN KEY (hashtag_id) REFERENCES hashtags(id) ON DELETE CASCADE,
    INDEX idx_thread_id (thread_id),
    INDEX idx_hashtag_id (hashtag_id)
);

CREATE TABLE thread_mentions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    thread_id BIGINT NOT NULL,
    mentioned_user_id VARCHAR(255) NOT NULL,
    mention_start INT NOT NULL, -- Position in content where mention starts
    mention_end INT NOT NULL,   -- Position in content where mention ends
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (thread_id) REFERENCES threads(id) ON DELETE CASCADE,
    FOREIGN KEY (mentioned_user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_thread_id (thread_id),
    INDEX idx_mentioned_user (mentioned_user_id)
);

-- =============================================
-- MODERATION AND REPORTING
-- =============================================

CREATE TABLE thread_reports (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    thread_id BIGINT NOT NULL,
    reporter_id VARCHAR(255) NOT NULL,
    report_type ENUM('spam', 'harassment', 'hate_speech', 'violence', 'inappropriate_content', 'other') NOT NULL,
    report_reason TEXT,
    status ENUM('pending', 'reviewed', 'resolved', 'dismissed') DEFAULT 'pending',
    reviewed_by VARCHAR(255) NULL,
    reviewed_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (thread_id) REFERENCES threads(id) ON DELETE CASCADE,
    FOREIGN KEY (reporter_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (reviewed_by) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_thread_id (thread_id),
    INDEX idx_reporter_id (reporter_id),
    INDEX idx_status (status)
);

CREATE TABLE thread_moderation (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    thread_id BIGINT NOT NULL,
    moderator_id VARCHAR(255) NOT NULL,
    action_type ENUM('hide', 'delete', 'restrict', 'warn') NOT NULL,
    reason TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (thread_id) REFERENCES threads(id) ON DELETE CASCADE,
    FOREIGN KEY (moderator_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_thread_id (thread_id),
    INDEX idx_moderator_id (moderator_id)
);

-- =============================================
-- MESSAGING SYSTEM
-- =============================================

CREATE TABLE conversations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    type ENUM('direct', 'group') DEFAULT 'direct',
    name VARCHAR(255) NULL, -- For group conversations
    created_by VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_created_by (created_by),
    INDEX idx_updated_at (updated_at)
);

CREATE TABLE conversation_participants (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    conversation_id BIGINT NOT NULL,
    user_id VARCHAR(255) NOT NULL,
    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    left_at TIMESTAMP NULL,
    is_active BOOLEAN DEFAULT TRUE,
    
    UNIQUE KEY unique_conversation_user (conversation_id, user_id),
    FOREIGN KEY (conversation_id) REFERENCES conversations(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_conversation_id (conversation_id),
    INDEX idx_user_id (user_id)
);

CREATE TABLE messages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    conversation_id BIGINT NOT NULL,
    sender_id VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    message_type ENUM('text', 'image', 'video', 'file', 'thread_share') DEFAULT 'text',
    thread_id BIGINT NULL, -- For thread shares
    is_edited BOOLEAN DEFAULT FALSE,
    is_deleted BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (conversation_id) REFERENCES conversations(id) ON DELETE CASCADE,
    FOREIGN KEY (sender_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (thread_id) REFERENCES threads(id) ON DELETE SET NULL,
    INDEX idx_conversation_id (conversation_id),
    INDEX idx_sender_id (sender_id),
    INDEX idx_created_at (created_at)
);

CREATE TABLE message_reads (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    message_id BIGINT NOT NULL,
    user_id VARCHAR(255) NOT NULL,
    read_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    UNIQUE KEY unique_message_read (message_id, user_id),
    FOREIGN KEY (message_id) REFERENCES messages(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- =============================================
-- NOTIFICATIONS SYSTEM
-- =============================================

CREATE TABLE notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    type ENUM('like', 'repost', 'reply', 'mention', 'follow', 'message', 'system') NOT NULL,
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    is_read BOOLEAN DEFAULT FALSE,
    related_user_id VARCHAR(255) NULL,
    related_thread_id BIGINT NULL,
    related_message_id BIGINT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (related_user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (related_thread_id) REFERENCES threads(id) ON DELETE CASCADE,
    FOREIGN KEY (related_message_id) REFERENCES messages(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_is_read (is_read),
    INDEX idx_created_at (created_at)
);

-- =============================================
-- TRIGGERS FOR COUNT UPDATES
-- =============================================

-- Update thread counts when likes are added/removed
DELIMITER //
CREATE TRIGGER update_thread_likes_count_after_insert
AFTER INSERT ON thread_likes
FOR EACH ROW
BEGIN
    UPDATE threads SET likes_count = likes_count + 1 WHERE id = NEW.thread_id;
END//

CREATE TRIGGER update_thread_likes_count_after_delete
AFTER DELETE ON thread_likes
FOR EACH ROW
BEGIN
    UPDATE threads SET likes_count = likes_count - 1 WHERE id = OLD.thread_id;
END//

-- Update thread counts when reposts are added/removed
CREATE TRIGGER update_thread_reposts_count_after_insert
AFTER INSERT ON thread_reposts
FOR EACH ROW
BEGIN
    UPDATE threads SET reposts_count = reposts_count + 1 WHERE id = NEW.thread_id;
END//

CREATE TRIGGER update_thread_reposts_count_after_delete
AFTER DELETE ON thread_reposts
FOR EACH ROW
BEGIN
    UPDATE threads SET reposts_count = reposts_count - 1 WHERE id = OLD.thread_id;
END//

-- Update thread counts when replies are added
CREATE TRIGGER update_thread_replies_count_after_insert
AFTER INSERT ON threads
FOR EACH ROW
BEGIN
    IF NEW.parent_thread_id IS NOT NULL THEN
        UPDATE threads SET replies_count = replies_count + 1 WHERE id = NEW.parent_thread_id;
    END IF;
END//

-- Update user counts when follows are added/removed
CREATE TRIGGER update_user_follows_count_after_insert
AFTER INSERT ON user_follows
FOR EACH ROW
BEGIN
    UPDATE users SET following_count = following_count + 1 WHERE id = NEW.follower_id;
    UPDATE users SET followers_count = followers_count + 1 WHERE id = NEW.following_id;
END//

CREATE TRIGGER update_user_follows_count_after_delete
AFTER DELETE ON user_follows
FOR EACH ROW
BEGIN
    UPDATE users SET following_count = following_count - 1 WHERE id = OLD.follower_id;
    UPDATE users SET followers_count = followers_count - 1 WHERE id = OLD.following_id;
END//

-- Update user thread count
CREATE TRIGGER update_user_threads_count_after_insert
AFTER INSERT ON threads
FOR EACH ROW
BEGIN
    UPDATE users SET threads_count = threads_count + 1 WHERE id = NEW.user_id;
END//

CREATE TRIGGER update_user_threads_count_after_delete
AFTER DELETE ON threads
FOR EACH ROW
BEGIN
    UPDATE users SET threads_count = threads_count - 1 WHERE id = OLD.user_id;
END//

DELIMITER ;

-- =============================================
-- SAMPLE DATA (Optional)
-- =============================================

-- Insert sample users
INSERT INTO users (id, username, display_name, email, bio, is_verified) VALUES
('user1', 'john_doe', 'John Doe', 'john@example.com', 'Software developer and tech enthusiast', TRUE),
('user2', 'jane_smith', 'Jane Smith', 'jane@example.com', 'Designer and creative thinker', FALSE),
('user3', 'tech_guru', 'Tech Guru', 'tech@example.com', 'Technology news and insights', TRUE);

-- Insert sample hashtags
INSERT INTO hashtags (tag, usage_count) VALUES
('technology', 0),
('programming', 0),
('design', 0),
('ai', 0),
('webdev', 0);

-- Success message
SELECT 'Complete database schema created successfully!' as Status;
