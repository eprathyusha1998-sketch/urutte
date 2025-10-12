-- Complete Thread App Database Schema - PostgreSQL Version
-- This schema handles unlimited thread levels, proper relationships, and all modern thread app features

-- Drop existing tables if they exist (in correct order for foreign keys)
DROP TABLE IF EXISTS thread_likes CASCADE;
DROP TABLE IF EXISTS thread_reposts CASCADE;
DROP TABLE IF EXISTS thread_shares CASCADE;
DROP TABLE IF EXISTS thread_bookmarks CASCADE;
DROP TABLE IF EXISTS thread_mentions CASCADE;
DROP TABLE IF EXISTS thread_hashtags CASCADE;
DROP TABLE IF EXISTS hashtags CASCADE;
DROP TABLE IF EXISTS thread_media CASCADE;
DROP TABLE IF EXISTS thread_polls CASCADE;
DROP TABLE IF EXISTS poll_options CASCADE;
DROP TABLE IF EXISTS poll_votes CASCADE;
DROP TABLE IF EXISTS thread_reactions CASCADE;
DROP TABLE IF EXISTS thread_views CASCADE;
DROP TABLE IF EXISTS thread_reports CASCADE;
DROP TABLE IF EXISTS thread_moderation CASCADE;
DROP TABLE IF EXISTS threads CASCADE;
DROP TABLE IF EXISTS user_follows CASCADE;
DROP TABLE IF EXISTS user_blocks CASCADE;
DROP TABLE IF EXISTS user_mutes CASCADE;
DROP TABLE IF EXISTS user_preferences CASCADE;
DROP TABLE IF EXISTS user_sessions CASCADE;
DROP TABLE IF EXISTS message_reads CASCADE;
DROP TABLE IF EXISTS messages CASCADE;
DROP TABLE IF EXISTS conversation_participants CASCADE;
DROP TABLE IF EXISTS conversations CASCADE;
DROP TABLE IF EXISTS notifications CASCADE;
DROP TABLE IF EXISTS users CASCADE;

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
    followers_count INTEGER DEFAULT 0,
    following_count INTEGER DEFAULT 0,
    threads_count INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_seen_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for users
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_created_at ON users(created_at);

-- =============================================
-- USER RELATIONSHIPS
-- =============================================

CREATE TABLE user_follows (
    id BIGSERIAL PRIMARY KEY,
    follower_id VARCHAR(255) NOT NULL,
    following_id VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    UNIQUE(follower_id, following_id),
    FOREIGN KEY (follower_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (following_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_user_follows_follower ON user_follows(follower_id);
CREATE INDEX idx_user_follows_following ON user_follows(following_id);

CREATE TABLE user_blocks (
    id BIGSERIAL PRIMARY KEY,
    blocker_id VARCHAR(255) NOT NULL,
    blocked_id VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    UNIQUE(blocker_id, blocked_id),
    FOREIGN KEY (blocker_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (blocked_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE user_mutes (
    id BIGSERIAL PRIMARY KEY,
    muter_id VARCHAR(255) NOT NULL,
    muted_id VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    UNIQUE(muter_id, muted_id),
    FOREIGN KEY (muter_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (muted_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE user_preferences (
    id BIGSERIAL PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    preference_key VARCHAR(100) NOT NULL,
    preference_value TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    UNIQUE(user_id, preference_key),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE user_sessions (
    id BIGSERIAL PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    session_token VARCHAR(500) UNIQUE NOT NULL,
    device_info TEXT,
    ip_address VARCHAR(45),
    is_active BOOLEAN DEFAULT TRUE,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_user_sessions_user_id ON user_sessions(user_id);
CREATE INDEX idx_user_sessions_expires_at ON user_sessions(expires_at);

-- =============================================
-- CORE THREAD SYSTEM
-- =============================================

CREATE TYPE thread_type_enum AS ENUM ('original', 'reply', 'quote', 'retweet');

CREATE TABLE threads (
    id BIGSERIAL PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    thread_type thread_type_enum DEFAULT 'original',
    
    -- Thread hierarchy (unlimited levels)
    parent_thread_id BIGINT NULL,
    root_thread_id BIGINT NULL,
    thread_level INTEGER DEFAULT 0,
    thread_path VARCHAR(1000), -- e.g., "1.2.3.4" for unlimited nesting
    
    -- Quote/Retweet specific fields
    quoted_thread_id BIGINT NULL,
    quote_content TEXT NULL,
    
    -- Engagement counts
    likes_count INTEGER DEFAULT 0,
    replies_count INTEGER DEFAULT 0,
    reposts_count INTEGER DEFAULT 0,
    shares_count INTEGER DEFAULT 0,
    views_count INTEGER DEFAULT 0,
    bookmarks_count INTEGER DEFAULT 0,
    
    -- Thread status
    is_deleted BOOLEAN DEFAULT FALSE,
    is_edited BOOLEAN DEFAULT FALSE,
    is_pinned BOOLEAN DEFAULT FALSE,
    is_sensitive BOOLEAN DEFAULT FALSE,
    is_public BOOLEAN DEFAULT TRUE,
    
    -- Timestamps
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    edited_at TIMESTAMP NULL,
    
    -- Foreign keys
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (parent_thread_id) REFERENCES threads(id) ON DELETE CASCADE,
    FOREIGN KEY (root_thread_id) REFERENCES threads(id) ON DELETE CASCADE,
    FOREIGN KEY (quoted_thread_id) REFERENCES threads(id) ON DELETE SET NULL
);

-- Create indexes for threads
CREATE INDEX idx_threads_user_id ON threads(user_id);
CREATE INDEX idx_threads_parent_thread ON threads(parent_thread_id);
CREATE INDEX idx_threads_root_thread ON threads(root_thread_id);
CREATE INDEX idx_threads_thread_level ON threads(thread_level);
CREATE INDEX idx_threads_thread_path ON threads(thread_path);
CREATE INDEX idx_threads_created_at ON threads(created_at);
CREATE INDEX idx_threads_thread_type ON threads(thread_type);
CREATE INDEX idx_threads_is_deleted ON threads(is_deleted);
CREATE INDEX idx_threads_is_public ON threads(is_public);
CREATE INDEX idx_threads_root_level_created ON threads(root_thread_id, thread_level, created_at);
CREATE INDEX idx_threads_user_created ON threads(user_id, created_at);
CREATE INDEX idx_threads_parent_created ON threads(parent_thread_id, created_at);

-- =============================================
-- MEDIA ATTACHMENTS
-- =============================================

CREATE TYPE media_type_enum AS ENUM ('image', 'video', 'gif', 'audio', 'document');

CREATE TABLE thread_media (
    id BIGSERIAL PRIMARY KEY,
    thread_id BIGINT NOT NULL,
    media_type media_type_enum NOT NULL,
    media_url VARCHAR(500) NOT NULL,
    thumbnail_url VARCHAR(500),
    alt_text VARCHAR(500),
    file_size BIGINT,
    duration INTEGER, -- for video/audio in seconds
    width INTEGER,
    height INTEGER,
    display_order INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (thread_id) REFERENCES threads(id) ON DELETE CASCADE
);

CREATE INDEX idx_thread_media_thread_id ON thread_media(thread_id);
CREATE INDEX idx_thread_media_media_type ON thread_media(media_type);

-- =============================================
-- POLLS SYSTEM
-- =============================================

CREATE TABLE thread_polls (
    id BIGSERIAL PRIMARY KEY,
    thread_id BIGINT NOT NULL,
    question TEXT NOT NULL,
    is_multiple_choice BOOLEAN DEFAULT FALSE,
    expires_at TIMESTAMP NULL,
    total_votes INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (thread_id) REFERENCES threads(id) ON DELETE CASCADE
);

CREATE INDEX idx_thread_polls_thread_id ON thread_polls(thread_id);
CREATE INDEX idx_thread_polls_expires_at ON thread_polls(expires_at);

CREATE TABLE poll_options (
    id BIGSERIAL PRIMARY KEY,
    poll_id BIGINT NOT NULL,
    option_text VARCHAR(500) NOT NULL,
    votes_count INTEGER DEFAULT 0,
    display_order INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (poll_id) REFERENCES thread_polls(id) ON DELETE CASCADE
);

CREATE INDEX idx_poll_options_poll_id ON poll_options(poll_id);

CREATE TABLE poll_votes (
    id BIGSERIAL PRIMARY KEY,
    poll_id BIGINT NOT NULL,
    option_id BIGINT NOT NULL,
    user_id VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    UNIQUE(user_id, poll_id),
    FOREIGN KEY (poll_id) REFERENCES thread_polls(id) ON DELETE CASCADE,
    FOREIGN KEY (option_id) REFERENCES poll_options(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- =============================================
-- ENGAGEMENT SYSTEM
-- =============================================

CREATE TABLE thread_likes (
    id BIGSERIAL PRIMARY KEY,
    thread_id BIGINT NOT NULL,
    user_id VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    UNIQUE(thread_id, user_id),
    FOREIGN KEY (thread_id) REFERENCES threads(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_thread_likes_thread_id ON thread_likes(thread_id);
CREATE INDEX idx_thread_likes_user_id ON thread_likes(user_id);

CREATE TYPE repost_type_enum AS ENUM ('repost', 'quote');

CREATE TABLE thread_reposts (
    id BIGSERIAL PRIMARY KEY,
    thread_id BIGINT NOT NULL,
    user_id VARCHAR(255) NOT NULL,
    repost_type repost_type_enum DEFAULT 'repost',
    quote_content TEXT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    UNIQUE(thread_id, user_id),
    FOREIGN KEY (thread_id) REFERENCES threads(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_thread_reposts_thread_id ON thread_reposts(thread_id);
CREATE INDEX idx_thread_reposts_user_id ON thread_reposts(user_id);

CREATE TABLE thread_shares (
    id BIGSERIAL PRIMARY KEY,
    thread_id BIGINT NOT NULL,
    user_id VARCHAR(255) NOT NULL,
    share_platform VARCHAR(50), -- 'twitter', 'facebook', 'linkedin', etc.
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (thread_id) REFERENCES threads(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_thread_shares_thread_id ON thread_shares(thread_id);
CREATE INDEX idx_thread_shares_user_id ON thread_shares(user_id);

CREATE TABLE thread_bookmarks (
    id BIGSERIAL PRIMARY KEY,
    thread_id BIGINT NOT NULL,
    user_id VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    UNIQUE(thread_id, user_id),
    FOREIGN KEY (thread_id) REFERENCES threads(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_thread_bookmarks_thread_id ON thread_bookmarks(thread_id);
CREATE INDEX idx_thread_bookmarks_user_id ON thread_bookmarks(user_id);

CREATE TYPE reaction_type_enum AS ENUM ('like', 'love', 'laugh', 'angry', 'sad', 'wow');

CREATE TABLE thread_reactions (
    id BIGSERIAL PRIMARY KEY,
    thread_id BIGINT NOT NULL,
    user_id VARCHAR(255) NOT NULL,
    reaction_type reaction_type_enum DEFAULT 'like',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    UNIQUE(thread_id, user_id),
    FOREIGN KEY (thread_id) REFERENCES threads(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_thread_reactions_thread_id ON thread_reactions(thread_id);
CREATE INDEX idx_thread_reactions_user_id ON thread_reactions(user_id);

CREATE TABLE thread_views (
    id BIGSERIAL PRIMARY KEY,
    thread_id BIGINT NOT NULL,
    user_id VARCHAR(255) NULL, -- NULL for anonymous views
    ip_address VARCHAR(45),
    user_agent TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (thread_id) REFERENCES threads(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
);

CREATE INDEX idx_thread_views_thread_id ON thread_views(thread_id);
CREATE INDEX idx_thread_views_user_id ON thread_views(user_id);
CREATE INDEX idx_thread_views_created_at ON thread_views(created_at);

-- =============================================
-- MENTIONS AND HASHTAGS
-- =============================================

CREATE TABLE hashtags (
    id BIGSERIAL PRIMARY KEY,
    tag VARCHAR(100) UNIQUE NOT NULL,
    usage_count INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_hashtags_tag ON hashtags(tag);
CREATE INDEX idx_hashtags_usage_count ON hashtags(usage_count);

CREATE TABLE thread_hashtags (
    id BIGSERIAL PRIMARY KEY,
    thread_id BIGINT NOT NULL,
    hashtag_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    UNIQUE(thread_id, hashtag_id),
    FOREIGN KEY (thread_id) REFERENCES threads(id) ON DELETE CASCADE,
    FOREIGN KEY (hashtag_id) REFERENCES hashtags(id) ON DELETE CASCADE
);

CREATE INDEX idx_thread_hashtags_thread_id ON thread_hashtags(thread_id);
CREATE INDEX idx_thread_hashtags_hashtag_id ON thread_hashtags(hashtag_id);

CREATE TABLE thread_mentions (
    id BIGSERIAL PRIMARY KEY,
    thread_id BIGINT NOT NULL,
    mentioned_user_id VARCHAR(255) NOT NULL,
    mention_start INTEGER NOT NULL, -- Position in content where mention starts
    mention_end INTEGER NOT NULL,   -- Position in content where mention ends
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (thread_id) REFERENCES threads(id) ON DELETE CASCADE,
    FOREIGN KEY (mentioned_user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_thread_mentions_thread_id ON thread_mentions(thread_id);
CREATE INDEX idx_thread_mentions_mentioned_user ON thread_mentions(mentioned_user_id);

-- =============================================
-- MODERATION AND REPORTING
-- =============================================

CREATE TYPE report_type_enum AS ENUM ('spam', 'harassment', 'hate_speech', 'violence', 'inappropriate_content', 'other');
CREATE TYPE report_status_enum AS ENUM ('pending', 'reviewed', 'resolved', 'dismissed');
CREATE TYPE moderation_action_enum AS ENUM ('hide', 'delete', 'restrict', 'warn');

CREATE TABLE thread_reports (
    id BIGSERIAL PRIMARY KEY,
    thread_id BIGINT NOT NULL,
    reporter_id VARCHAR(255) NOT NULL,
    report_type report_type_enum NOT NULL,
    report_reason TEXT,
    status report_status_enum DEFAULT 'pending',
    reviewed_by VARCHAR(255) NULL,
    reviewed_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (thread_id) REFERENCES threads(id) ON DELETE CASCADE,
    FOREIGN KEY (reporter_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (reviewed_by) REFERENCES users(id) ON DELETE SET NULL
);

CREATE INDEX idx_thread_reports_thread_id ON thread_reports(thread_id);
CREATE INDEX idx_thread_reports_reporter_id ON thread_reports(reporter_id);
CREATE INDEX idx_thread_reports_status ON thread_reports(status);

CREATE TABLE thread_moderation (
    id BIGSERIAL PRIMARY KEY,
    thread_id BIGINT NOT NULL,
    moderator_id VARCHAR(255) NOT NULL,
    action_type moderation_action_enum NOT NULL,
    reason TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (thread_id) REFERENCES threads(id) ON DELETE CASCADE,
    FOREIGN KEY (moderator_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_thread_moderation_thread_id ON thread_moderation(thread_id);
CREATE INDEX idx_thread_moderation_moderator_id ON thread_moderation(moderator_id);

-- =============================================
-- MESSAGING SYSTEM
-- =============================================

CREATE TYPE conversation_type_enum AS ENUM ('direct', 'group');
CREATE TYPE message_type_enum AS ENUM ('text', 'image', 'video', 'file', 'thread_share');

CREATE TABLE conversations (
    id BIGSERIAL PRIMARY KEY,
    type conversation_type_enum DEFAULT 'direct',
    name VARCHAR(255) NULL, -- For group conversations
    created_by VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_conversations_created_by ON conversations(created_by);
CREATE INDEX idx_conversations_updated_at ON conversations(updated_at);

CREATE TABLE conversation_participants (
    id BIGSERIAL PRIMARY KEY,
    conversation_id BIGINT NOT NULL,
    user_id VARCHAR(255) NOT NULL,
    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    left_at TIMESTAMP NULL,
    is_active BOOLEAN DEFAULT TRUE,
    
    UNIQUE(conversation_id, user_id),
    FOREIGN KEY (conversation_id) REFERENCES conversations(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_conversation_participants_conversation_id ON conversation_participants(conversation_id);
CREATE INDEX idx_conversation_participants_user_id ON conversation_participants(user_id);

CREATE TABLE messages (
    id BIGSERIAL PRIMARY KEY,
    conversation_id BIGINT NOT NULL,
    sender_id VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    message_type message_type_enum DEFAULT 'text',
    thread_id BIGINT NULL, -- For thread shares
    is_edited BOOLEAN DEFAULT FALSE,
    is_deleted BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (conversation_id) REFERENCES conversations(id) ON DELETE CASCADE,
    FOREIGN KEY (sender_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (thread_id) REFERENCES threads(id) ON DELETE SET NULL
);

CREATE INDEX idx_messages_conversation_id ON messages(conversation_id);
CREATE INDEX idx_messages_sender_id ON messages(sender_id);
CREATE INDEX idx_messages_created_at ON messages(created_at);

CREATE TABLE message_reads (
    id BIGSERIAL PRIMARY KEY,
    message_id BIGINT NOT NULL,
    user_id VARCHAR(255) NOT NULL,
    read_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    UNIQUE(message_id, user_id),
    FOREIGN KEY (message_id) REFERENCES messages(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- =============================================
-- NOTIFICATIONS SYSTEM
-- =============================================

CREATE TYPE notification_type_enum AS ENUM ('like', 'repost', 'reply', 'mention', 'follow', 'message', 'system');

CREATE TABLE notifications (
    id BIGSERIAL PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    type notification_type_enum NOT NULL,
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
    FOREIGN KEY (related_message_id) REFERENCES messages(id) ON DELETE CASCADE
);

CREATE INDEX idx_notifications_user_id ON notifications(user_id);
CREATE INDEX idx_notifications_is_read ON notifications(is_read);
CREATE INDEX idx_notifications_created_at ON notifications(created_at);

-- =============================================
-- FUNCTIONS FOR COUNT UPDATES
-- =============================================

-- Function to update thread likes count
CREATE OR REPLACE FUNCTION update_thread_likes_count()
RETURNS TRIGGER AS $$
BEGIN
    IF TG_OP = 'INSERT' THEN
        UPDATE threads SET likes_count = likes_count + 1 WHERE id = NEW.thread_id;
        RETURN NEW;
    ELSIF TG_OP = 'DELETE' THEN
        UPDATE threads SET likes_count = likes_count - 1 WHERE id = OLD.thread_id;
        RETURN OLD;
    END IF;
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

-- Function to update thread reposts count
CREATE OR REPLACE FUNCTION update_thread_reposts_count()
RETURNS TRIGGER AS $$
BEGIN
    IF TG_OP = 'INSERT' THEN
        UPDATE threads SET reposts_count = reposts_count + 1 WHERE id = NEW.thread_id;
        RETURN NEW;
    ELSIF TG_OP = 'DELETE' THEN
        UPDATE threads SET reposts_count = reposts_count - 1 WHERE id = OLD.thread_id;
        RETURN OLD;
    END IF;
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

-- Function to update thread replies count
CREATE OR REPLACE FUNCTION update_thread_replies_count()
RETURNS TRIGGER AS $$
BEGIN
    IF TG_OP = 'INSERT' AND NEW.parent_thread_id IS NOT NULL THEN
        UPDATE threads SET replies_count = replies_count + 1 WHERE id = NEW.parent_thread_id;
    ELSIF TG_OP = 'DELETE' AND OLD.parent_thread_id IS NOT NULL THEN
        UPDATE threads SET replies_count = replies_count - 1 WHERE id = OLD.parent_thread_id;
    END IF;
    RETURN COALESCE(NEW, OLD);
END;
$$ LANGUAGE plpgsql;

-- Function to update user follows count
CREATE OR REPLACE FUNCTION update_user_follows_count()
RETURNS TRIGGER AS $$
BEGIN
    IF TG_OP = 'INSERT' THEN
        UPDATE users SET following_count = following_count + 1 WHERE id = NEW.follower_id;
        UPDATE users SET followers_count = followers_count + 1 WHERE id = NEW.following_id;
        RETURN NEW;
    ELSIF TG_OP = 'DELETE' THEN
        UPDATE users SET following_count = following_count - 1 WHERE id = OLD.follower_id;
        UPDATE users SET followers_count = followers_count - 1 WHERE id = OLD.following_id;
        RETURN OLD;
    END IF;
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

-- Function to update user threads count
CREATE OR REPLACE FUNCTION update_user_threads_count()
RETURNS TRIGGER AS $$
BEGIN
    IF TG_OP = 'INSERT' THEN
        UPDATE users SET threads_count = threads_count + 1 WHERE id = NEW.user_id;
        RETURN NEW;
    ELSIF TG_OP = 'DELETE' THEN
        UPDATE users SET threads_count = threads_count - 1 WHERE id = OLD.user_id;
        RETURN OLD;
    END IF;
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

-- =============================================
-- TRIGGERS FOR COUNT UPDATES
-- =============================================

-- Triggers for thread likes
CREATE TRIGGER trigger_update_thread_likes_count_after_insert
    AFTER INSERT ON thread_likes
    FOR EACH ROW EXECUTE FUNCTION update_thread_likes_count();

CREATE TRIGGER trigger_update_thread_likes_count_after_delete
    AFTER DELETE ON thread_likes
    FOR EACH ROW EXECUTE FUNCTION update_thread_likes_count();

-- Triggers for thread reposts
CREATE TRIGGER trigger_update_thread_reposts_count_after_insert
    AFTER INSERT ON thread_reposts
    FOR EACH ROW EXECUTE FUNCTION update_thread_reposts_count();

CREATE TRIGGER trigger_update_thread_reposts_count_after_delete
    AFTER DELETE ON thread_reposts
    FOR EACH ROW EXECUTE FUNCTION update_thread_reposts_count();

-- Triggers for thread replies
CREATE TRIGGER trigger_update_thread_replies_count_after_insert
    AFTER INSERT ON threads
    FOR EACH ROW EXECUTE FUNCTION update_thread_replies_count();

CREATE TRIGGER trigger_update_thread_replies_count_after_delete
    AFTER DELETE ON threads
    FOR EACH ROW EXECUTE FUNCTION update_thread_replies_count();

-- Triggers for user follows
CREATE TRIGGER trigger_update_user_follows_count_after_insert
    AFTER INSERT ON user_follows
    FOR EACH ROW EXECUTE FUNCTION update_user_follows_count();

CREATE TRIGGER trigger_update_user_follows_count_after_delete
    AFTER DELETE ON user_follows
    FOR EACH ROW EXECUTE FUNCTION update_user_follows_count();

-- Triggers for user threads
CREATE TRIGGER trigger_update_user_threads_count_after_insert
    AFTER INSERT ON threads
    FOR EACH ROW EXECUTE FUNCTION update_user_threads_count();

CREATE TRIGGER trigger_update_user_threads_count_after_delete
    AFTER DELETE ON threads
    FOR EACH ROW EXECUTE FUNCTION update_user_threads_count();

-- =============================================
-- SAMPLE DATA
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
SELECT 'Complete PostgreSQL database schema created successfully!' as Status;
