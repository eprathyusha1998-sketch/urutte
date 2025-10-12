# Urutte - Comprehensive Database Design

## 📋 Overview

This document outlines the complete database schema for Urutte, a scalable social media platform designed to handle 500k+ daily active users with room for future growth.

## 🎯 Design Principles

1. **Scalability First**: Designed for horizontal scaling with proper indexing
2. **Performance**: Denormalized counters for fast reads
3. **Flexibility**: Support for multiple OAuth providers
4. **Future-Proof**: Schema accommodates premium features, verification, moderation
5. **Data Integrity**: Foreign keys, constraints, and cascading deletes

---

## 📊 Core Tables

### 1. **users** (Main User Table)

Stores all user account information, profile data, and settings.

```sql
CREATE TABLE users (
    -- Identity
    id VARCHAR(255) PRIMARY KEY,
    username VARCHAR(50) UNIQUE,
    email VARCHAR(255) UNIQUE NOT NULL,
    
    -- Profile
    name VARCHAR(255) NOT NULL,
    picture TEXT,
    cover_photo TEXT,
    bio TEXT,
    location VARCHAR(255),
    website VARCHAR(255),
    phone_number VARCHAR(20),
    date_of_birth VARCHAR(10),
    gender VARCHAR(20),
    
    -- OAuth Integration
    google_id VARCHAR(255) UNIQUE,
    github_id VARCHAR(255) UNIQUE,
    facebook_id VARCHAR(255) UNIQUE,
    twitter_id VARCHAR(255) UNIQUE,
    
    -- Account Status
    is_verified BOOLEAN DEFAULT FALSE,
    is_private BOOLEAN DEFAULT FALSE,
    is_active BOOLEAN DEFAULT TRUE,
    is_suspended BOOLEAN DEFAULT FALSE,
    
    -- Performance Counters (Denormalized)
    followers_count INTEGER DEFAULT 0,
    following_count INTEGER DEFAULT 0,
    posts_count INTEGER DEFAULT 0,
    
    -- Premium Features
    is_premium BOOLEAN DEFAULT FALSE,
    premium_expires_at TIMESTAMP,
    
    -- Timestamps
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_login_at TIMESTAMP,
    email_verified_at TIMESTAMP,
    
    -- Indexes
    INDEX idx_email (email),
    INDEX idx_username (username),
    INDEX idx_google_id (google_id),
    INDEX idx_created_at (created_at)
);
```

**Fields Explained:**
- **username**: Public handle like `@johndoe`
- **cover_photo**: Banner/header image
- **OAuth IDs**: Support multiple login methods
- **is_verified**: Blue checkmark (verified accounts)
- **is_private**: Private account (followers only)
- **Counters**: Cached for fast profile page loads
- **last_login_at**: Track user activity

---

### 2. **posts**

All user-generated content (posts, replies, quotes).

```sql
CREATE TABLE posts (
    id BIGSERIAL PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    
    -- Content
    content TEXT NOT NULL,
    media_url TEXT,
    media_type VARCHAR(20),  -- 'image', 'video', 'gif'
    
    -- Post Type
    is_reply BOOLEAN DEFAULT FALSE,
    reply_to_post_id BIGINT,
    is_repost BOOLEAN DEFAULT FALSE,
    repost_of_post_id BIGINT,
    is_quote BOOLEAN DEFAULT FALSE,
    quoted_post_id BIGINT,
    
    -- Engagement (Denormalized)
    likes_count INTEGER DEFAULT 0,
    reposts_count INTEGER DEFAULT 0,
    quotes_count INTEGER DEFAULT 0,
    replies_count INTEGER DEFAULT 0,
    views_count INTEGER DEFAULT 0,
    
    -- Moderation
    is_deleted BOOLEAN DEFAULT FALSE,
    is_hidden BOOLEAN DEFAULT FALSE,
    deleted_at TIMESTAMP,
    
    -- Timestamps
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Foreign Keys
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (reply_to_post_id) REFERENCES posts(id) ON DELETE SET NULL,
    FOREIGN KEY (repost_of_post_id) REFERENCES posts(id) ON DELETE SET NULL,
    FOREIGN KEY (quoted_post_id) REFERENCES posts(id) ON DELETE SET NULL,
    
    -- Indexes
    INDEX idx_user_id (user_id),
    INDEX idx_created_at (created_at),
    INDEX idx_reply_to_post_id (reply_to_post_id),
    INDEX idx_is_reply (is_reply),
    INDEX idx_user_created (user_id, created_at)
);
```

**Features:**
- Supports replies, reposts, and quotes
- Media attachments (images/videos)
- Engagement metrics cached
- Soft deletes for moderation

---

### 3. **likes**

User likes/favorites on posts.

```sql
CREATE TABLE likes (
    id BIGSERIAL PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    post_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE,
    
    UNIQUE (user_id, post_id),
    INDEX idx_post_id (post_id),
    INDEX idx_user_id (user_id),
    INDEX idx_created_at (created_at)
);
```

---

### 4. **follows**

User follow relationships.

```sql
CREATE TABLE follows (
    id BIGSERIAL PRIMARY KEY,
    follower_id VARCHAR(255) NOT NULL,  -- Who is following
    following_id VARCHAR(255) NOT NULL, -- Who is being followed
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (follower_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (following_id) REFERENCES users(id) ON DELETE CASCADE,
    
    UNIQUE (follower_id, following_id),
    INDEX idx_follower_id (follower_id),
    INDEX idx_following_id (following_id)
);
```

---

### 5. **reposts**

Track who reposted what.

```sql
CREATE TABLE reposts (
    id BIGSERIAL PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    post_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE,
    
    UNIQUE (user_id, post_id),
    INDEX idx_post_id (post_id),
    INDEX idx_user_id (user_id)
);
```

---

### 6. **notifications**

All user notifications (likes, follows, replies, mentions).

```sql
CREATE TABLE notifications (
    id BIGSERIAL PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,       -- Recipient
    sender_id VARCHAR(255),               -- Who triggered it
    type VARCHAR(50) NOT NULL,           -- 'LIKE', 'FOLLOW', 'REPLY', 'MENTION', 'REPOST'
    
    -- Reference to entity
    post_id BIGINT,
    
    content TEXT,
    is_read BOOLEAN DEFAULT FALSE,
    
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    read_at TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (sender_id) REFERENCES users(id) ON DELETE SET NULL,
    FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE,
    
    INDEX idx_user_id (user_id),
    INDEX idx_user_read (user_id, is_read),
    INDEX idx_created_at (created_at)
);
```

**Notification Types:**
- `LIKE`: Someone liked your post
- `FOLLOW`: Someone followed you
- `REPLY`: Someone replied to your post
- `MENTION`: Someone mentioned you (@username)
- `REPOST`: Someone reposted your post
- `QUOTE`: Someone quoted your post

---

### 7. **messages**

Direct messages between users.

```sql
CREATE TABLE messages (
    id BIGSERIAL PRIMARY KEY,
    sender_id VARCHAR(255) NOT NULL,
    receiver_id VARCHAR(255) NOT NULL,
    
    content TEXT NOT NULL,
    media_url TEXT,
    media_type VARCHAR(20),
    
    is_read BOOLEAN DEFAULT FALSE,
    is_deleted BOOLEAN DEFAULT FALSE,
    
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    read_at TIMESTAMP,
    
    FOREIGN KEY (sender_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (receiver_id) REFERENCES users(id) ON DELETE CASCADE,
    
    INDEX idx_sender_id (sender_id),
    INDEX idx_receiver_id (receiver_id),
    INDEX idx_conversation (sender_id, receiver_id, created_at),
    INDEX idx_created_at (created_at)
);
```

---

## 🛒 Marketplace Tables

### 8. **products**

Marketplace items for sale.

```sql
CREATE TABLE products (
    id BIGSERIAL PRIMARY KEY,
    seller_id VARCHAR(255) NOT NULL,
    
    title VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    currency VARCHAR(3) DEFAULT 'USD',
    
    category VARCHAR(100),
    condition VARCHAR(50),  -- 'NEW', 'LIKE_NEW', 'USED', 'FOR_PARTS'
    location VARCHAR(255),
    
    image_urls TEXT[],  -- Array of image URLs
    
    is_available BOOLEAN DEFAULT TRUE,
    is_sold BOOLEAN DEFAULT FALSE,
    
    views_count INTEGER DEFAULT 0,
    favorites_count INTEGER DEFAULT 0,
    
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    sold_at TIMESTAMP,
    
    FOREIGN KEY (seller_id) REFERENCES users(id) ON DELETE CASCADE,
    
    INDEX idx_seller_id (seller_id),
    INDEX idx_category (category),
    INDEX idx_price (price),
    INDEX idx_created_at (created_at),
    INDEX idx_is_available (is_available)
);
```

---

### 9. **product_reviews**

Reviews and ratings for products.

```sql
CREATE TABLE product_reviews (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL,
    user_id VARCHAR(255) NOT NULL,
    
    rating INTEGER NOT NULL CHECK (rating >= 1 AND rating <= 5),
    comment TEXT,
    
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    
    UNIQUE (product_id, user_id),
    INDEX idx_product_id (product_id),
    INDEX idx_user_id (user_id)
);
```

---

### 10. **product_favorites**

Users can favorite/save products.

```sql
CREATE TABLE product_favorites (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL,
    user_id VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    
    UNIQUE (product_id, user_id),
    INDEX idx_product_id (product_id),
    INDEX idx_user_id (user_id)
);
```

---

## 📅 Events Tables

### 11. **events**

User-created events.

```sql
CREATE TABLE events (
    id BIGSERIAL PRIMARY KEY,
    creator_id VARCHAR(255) NOT NULL,
    
    title VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    location VARCHAR(255),
    event_date TIMESTAMP NOT NULL,
    end_date TIMESTAMP,
    
    cover_image TEXT,
    category VARCHAR(100),
    
    is_online BOOLEAN DEFAULT FALSE,
    meeting_url TEXT,
    
    max_attendees INTEGER,
    attendees_count INTEGER DEFAULT 0,
    
    is_private BOOLEAN DEFAULT FALSE,
    is_cancelled BOOLEAN DEFAULT FALSE,
    
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (creator_id) REFERENCES users(id) ON DELETE CASCADE,
    
    INDEX idx_creator_id (creator_id),
    INDEX idx_event_date (event_date),
    INDEX idx_category (category),
    INDEX idx_is_private (is_private)
);
```

---

### 12. **event_attendees**

Track who's attending events.

```sql
CREATE TABLE event_attendees (
    id BIGSERIAL PRIMARY KEY,
    event_id BIGINT NOT NULL,
    user_id VARCHAR(255) NOT NULL,
    
    status VARCHAR(20) DEFAULT 'GOING',  -- 'GOING', 'INTERESTED', 'NOT_GOING'
    
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (event_id) REFERENCES events(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    
    UNIQUE (event_id, user_id),
    INDEX idx_event_id (event_id),
    INDEX idx_user_id (user_id)
);
```

---

## 🔐 Security & Moderation Tables

### 13. **blocked_users**

User blocking for privacy.

```sql
CREATE TABLE blocked_users (
    id BIGSERIAL PRIMARY KEY,
    blocker_id VARCHAR(255) NOT NULL,  -- Who blocked
    blocked_id VARCHAR(255) NOT NULL,  -- Who was blocked
    
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (blocker_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (blocked_id) REFERENCES users(id) ON DELETE CASCADE,
    
    UNIQUE (blocker_id, blocked_id),
    INDEX idx_blocker_id (blocker_id),
    INDEX idx_blocked_id (blocked_id)
);
```

---

### 14. **reports**

Content and user reports for moderation.

```sql
CREATE TABLE reports (
    id BIGSERIAL PRIMARY KEY,
    reporter_id VARCHAR(255) NOT NULL,
    
    -- What's being reported
    reported_user_id VARCHAR(255),
    reported_post_id BIGINT,
    
    reason VARCHAR(100) NOT NULL,  -- 'SPAM', 'HARASSMENT', 'HATE_SPEECH', 'VIOLENCE', etc.
    description TEXT,
    
    status VARCHAR(20) DEFAULT 'PENDING',  -- 'PENDING', 'REVIEWED', 'ACTIONED', 'DISMISSED'
    
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    reviewed_at TIMESTAMP,
    reviewed_by VARCHAR(255),
    
    FOREIGN KEY (reporter_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (reported_user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (reported_post_id) REFERENCES posts(id) ON DELETE CASCADE,
    
    INDEX idx_reporter_id (reporter_id),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
);
```

---

### 15. **hashtags**

Trending topics and hashtags.

```sql
CREATE TABLE hashtags (
    id BIGSERIAL PRIMARY KEY,
    tag VARCHAR(255) UNIQUE NOT NULL,
    
    posts_count INTEGER DEFAULT 0,
    last_used_at TIMESTAMP,
    
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    INDEX idx_tag (tag),
    INDEX idx_posts_count (posts_count DESC),
    INDEX idx_last_used_at (last_used_at DESC)
);
```

---

### 16. **post_hashtags**

Link posts to hashtags (many-to-many).

```sql
CREATE TABLE post_hashtags (
    id BIGSERIAL PRIMARY KEY,
    post_id BIGINT NOT NULL,
    hashtag_id BIGINT NOT NULL,
    
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE,
    FOREIGN KEY (hashtag_id) REFERENCES hashtags(id) ON DELETE CASCADE,
    
    UNIQUE (post_id, hashtag_id),
    INDEX idx_post_id (post_id),
    INDEX idx_hashtag_id (hashtag_id)
);
```

---

## 📊 Analytics Tables

### 17. **user_activity**

Track user engagement for analytics.

```sql
CREATE TABLE user_activity (
    id BIGSERIAL PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    
    activity_type VARCHAR(50) NOT NULL,  -- 'LOGIN', 'POST', 'LIKE', 'FOLLOW', etc.
    metadata JSONB,
    
    ip_address VARCHAR(45),
    user_agent TEXT,
    
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    
    INDEX idx_user_id (user_id),
    INDEX idx_activity_type (activity_type),
    INDEX idx_created_at (created_at)
);
```

---

## 🔄 Optimization Strategies

### 1. **Read Replicas**
- Master for writes
- Multiple read replicas for queries
- Route feed queries to read replicas

### 2. **Sharding Strategy**
- Shard by `user_id` for user-specific data
- Shard by time for posts (recent data on faster storage)
- Geographic sharding for global users

### 3. **Caching**
- Redis for:
  - User sessions
  - Feed cache (last 100 posts)
  - Trending hashtags
  - Notification counts
  - Online user status

### 4. **Denormalization**
- Counter fields (`followers_count`, `posts_count`, etc.)
- Update via triggers or application logic
- Trade write complexity for read speed

### 5. **Partitioning**
```sql
-- Partition posts by month
CREATE TABLE posts_2025_01 PARTITION OF posts
FOR VALUES FROM ('2025-01-01') TO ('2025-02-01');
```

### 6. **Indexes**
- Composite indexes for common queries
- Partial indexes for filtered queries
- Cover indexes for specific use cases

---

## 📈 Scaling Milestones

### Phase 1: 0-100k Users
- Single PostgreSQL instance
- Redis for sessions
- Basic indexes

### Phase 2: 100k-500k Users
- Master-Replica setup
- Connection pooling (PgBouncer)
- CDN for media files
- Full-text search (Elasticsearch)

### Phase 3: 500k-2M Users
- Horizontal sharding
- Message queue (RabbitMQ/Kafka)
- Separate analytics database
- Microservices architecture

### Phase 4: 2M+ Users
- Multi-region deployment
- GraphQL federation
- Event sourcing for critical data
- CQRS pattern for reads/writes

---

## 🎯 Future Enhancements

### Short Term (3-6 months)
- [ ] Bookmarks/Save posts
- [ ] Lists (custom feeds)
- [ ] Polls
- [ ] Spaces (audio rooms)
- [ ] Communities/Topics

### Medium Term (6-12 months)
- [ ] Live streaming
- [ ] Stories (24-hour posts)
- [ ] Monetization (subscriptions, tips)
- [ ] Advanced analytics dashboard
- [ ] Creator tools

### Long Term (12+ months)
- [ ] NFT integration
- [ ] Decentralized identity
- [ ] AI content moderation
- [ ] Multi-language support
- [ ] Native mobile apps

---

## 📝 Notes

1. **UUID vs Auto-Increment**
   - Users: UUID for distributed systems
   - Posts: BIGSERIAL for performance

2. **Soft Deletes**
   - Keep deleted content for compliance
   - Actual deletion after retention period

3. **Compliance**
   - GDPR: User data export/deletion
   - CCPA: Data privacy rights
   - User consent tracking

4. **Backup Strategy**
   - Daily full backups
   - Hourly incremental backups
   - Point-in-time recovery
   - Geographic replication

---

**Database Version**: PostgreSQL 15+  
**Last Updated**: 2025-10-10  
**Maintainer**: Urutte Development Team

