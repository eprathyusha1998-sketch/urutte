-- Migration script to fix null threadLevel values in existing posts
-- Run this script to update existing posts with proper thread hierarchy

-- First, add the new columns if they don't exist
ALTER TABLE posts ADD COLUMN IF NOT EXISTS root_post_id BIGINT;
ALTER TABLE posts ADD COLUMN IF NOT EXISTS thread_level INTEGER DEFAULT 0;
ALTER TABLE posts ADD COLUMN IF NOT EXISTS thread_path VARCHAR(500);
ALTER TABLE posts ADD COLUMN IF NOT EXISTS quoted_post_id BIGINT;
ALTER TABLE posts ADD COLUMN IF NOT EXISTS is_quote_repost BOOLEAN DEFAULT FALSE;

-- Update existing posts to have proper thread hierarchy
-- Set thread_level to 0 for all main posts (posts without parent_post_id)
UPDATE posts 
SET thread_level = 0, 
    root_post_id = NULL, 
    thread_path = NULL
WHERE parent_post_id IS NULL 
  AND thread_level IS NULL;

-- Set thread_level to 1 for all first-level replies
UPDATE posts 
SET thread_level = 1,
    root_post_id = parent_post_id,
    thread_path = CAST(parent_post_id AS VARCHAR)
WHERE parent_post_id IS NOT NULL 
  AND thread_level IS NULL
  AND parent_post_id IN (
    SELECT id FROM posts WHERE parent_post_id IS NULL
  );

-- For deeper levels, we need to calculate recursively
-- This is a simplified approach - for complex hierarchies, you might need a more sophisticated script

-- Set default values for any remaining null values
UPDATE posts 
SET thread_level = COALESCE(thread_level, 0),
    is_quote_repost = COALESCE(is_quote_repost, FALSE)
WHERE thread_level IS NULL OR is_quote_repost IS NULL;

-- Add foreign key constraints (if they don't exist)
-- Note: These might fail if there are orphaned references
-- ALTER TABLE posts ADD CONSTRAINT fk_posts_root_post FOREIGN KEY (root_post_id) REFERENCES posts(id);
-- ALTER TABLE posts ADD CONSTRAINT fk_posts_quoted_post FOREIGN KEY (quoted_post_id) REFERENCES posts(id);

-- Add indexes for performance
CREATE INDEX IF NOT EXISTS idx_posts_root_post ON posts(root_post_id);
CREATE INDEX IF NOT EXISTS idx_posts_thread_path ON posts(thread_path);
CREATE INDEX IF NOT EXISTS idx_posts_quoted_post ON posts(quoted_post_id);
CREATE INDEX IF NOT EXISTS idx_posts_thread_level ON posts(thread_level);

-- Verify the migration
SELECT 
    COUNT(*) as total_posts,
    COUNT(CASE WHEN thread_level = 0 THEN 1 END) as main_posts,
    COUNT(CASE WHEN thread_level > 0 THEN 1 END) as replies,
    COUNT(CASE WHEN thread_level IS NULL THEN 1 END) as null_thread_levels
FROM posts;
