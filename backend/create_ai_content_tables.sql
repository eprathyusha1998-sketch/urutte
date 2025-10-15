-- AI Content Generation Tables
-- Run this script to create the necessary tables for AI content generation

-- AI Admins table
CREATE TABLE IF NOT EXISTS ai_admins (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    username VARCHAR(255) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    bio TEXT,
    avatar_url TEXT,
    is_active BOOLEAN NOT NULL DEFAULT true,
    is_verified BOOLEAN NOT NULL DEFAULT true,
    posts_count INTEGER NOT NULL DEFAULT 0,
    followers_count INTEGER NOT NULL DEFAULT 0,
    following_count INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Topics table
CREATE TABLE IF NOT EXISTS topics (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    description TEXT,
    category VARCHAR(100) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT true,
    priority INTEGER NOT NULL DEFAULT 1,
    threads_per_run INTEGER NOT NULL DEFAULT 3,
    keywords TEXT,
    search_queries TEXT,
    last_generated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    total_threads_generated INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- AI Generated Threads table
CREATE TABLE IF NOT EXISTS ai_generated_threads (
    id BIGSERIAL PRIMARY KEY,
    thread_id BIGINT NOT NULL REFERENCES threads(id) ON DELETE CASCADE,
    topic_id BIGINT NOT NULL REFERENCES topics(id) ON DELETE CASCADE,
    ai_admin_id BIGINT NOT NULL REFERENCES ai_admins(id) ON DELETE CASCADE,
    original_content TEXT,
    source_url TEXT,
    source_title TEXT,
    generation_method VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'active',
    metadata TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_ai_admins_username ON ai_admins(username);
CREATE INDEX IF NOT EXISTS idx_ai_admins_email ON ai_admins(email);
CREATE INDEX IF NOT EXISTS idx_ai_admins_active ON ai_admins(is_active);

CREATE INDEX IF NOT EXISTS idx_topics_name ON topics(name);
CREATE INDEX IF NOT EXISTS idx_topics_category ON topics(category);
CREATE INDEX IF NOT EXISTS idx_topics_active ON topics(is_active);
CREATE INDEX IF NOT EXISTS idx_topics_priority ON topics(priority);
CREATE INDEX IF NOT EXISTS idx_topics_last_generated ON topics(last_generated_at);

CREATE INDEX IF NOT EXISTS idx_ai_generated_threads_thread_id ON ai_generated_threads(thread_id);
CREATE INDEX IF NOT EXISTS idx_ai_generated_threads_topic_id ON ai_generated_threads(topic_id);
CREATE INDEX IF NOT EXISTS idx_ai_generated_threads_ai_admin_id ON ai_generated_threads(ai_admin_id);
CREATE INDEX IF NOT EXISTS idx_ai_generated_threads_status ON ai_generated_threads(status);
CREATE INDEX IF NOT EXISTS idx_ai_generated_threads_created_at ON ai_generated_threads(created_at);

-- Insert default AI Admin
INSERT INTO ai_admins (name, username, email, bio, avatar_url, is_active, is_verified, followers_count, following_count, posts_count, created_at, updated_at)
VALUES (
    'AI Assistant',
    'ai_assistant',
    'ai@urutte.com',
    '🤖 AI-powered content curator bringing you the latest trends and discussions from across the web. Always learning, always sharing!',
    'http://localhost/assets/images/avatars/avatar-1.jpg',
    true,
    true,
    0,
    0,
    0,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (username) DO NOTHING;

-- Insert default topics (Free Credits Optimized - Reduced threads per run)
INSERT INTO topics (name, description, category, keywords, search_queries, priority, threads_per_run, is_active, total_threads_generated, created_at, updated_at, last_generated_at) VALUES
-- Technology topics (High Priority)
('Artificial Intelligence', 'Latest developments in AI, machine learning, and automation', 'Technology', 'ai,artificial intelligence,machine learning,automation,chatgpt,gpt,openai', 'artificial intelligence,machine learning,AI news,automation', 10, 2, true, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP - INTERVAL '3 hours'),
('Web Development', 'Frontend, backend, and full-stack development trends', 'Technology', 'web development,frontend,backend,react,angular,vue,javascript,typescript', 'web development,frontend development,backend development,react,angular', 9, 2, true, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP - INTERVAL '3 hours'),
('Mobile Development', 'iOS, Android, and cross-platform mobile development', 'Technology', 'mobile development,ios,android,react native,flutter,swift,kotlin', 'mobile development,iOS development,Android development,React Native,Flutter', 8, 1, true, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP - INTERVAL '3 hours'),
('DevOps & Cloud', 'DevOps practices, cloud computing, and infrastructure', 'Technology', 'devops,cloud computing,aws,azure,gcp,docker,kubernetes,ci/cd', 'DevOps,cloud computing,AWS,Azure,Google Cloud,Docker,Kubernetes', 8, 1, true, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP - INTERVAL '3 hours'),
('Cybersecurity', 'Security threats, best practices, and protection strategies', 'Technology', 'cybersecurity,security,hacking,privacy,encryption,malware', 'cybersecurity,information security,privacy,data protection', 7, 1, true, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP - INTERVAL '3 hours'),

-- Business topics
('Startups', 'Startup news, funding, and entrepreneurship', 'Business', 'startup,entrepreneurship,funding,venture capital,unicorn,ipo', 'startup news,entrepreneurship,venture capital,funding,IPO', 9, 2, true, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP - INTERVAL '3 hours'),
('Cryptocurrency', 'Bitcoin, blockchain, and digital currencies', 'Business', 'cryptocurrency,bitcoin,ethereum,blockchain,defi,nft', 'cryptocurrency,Bitcoin,Ethereum,blockchain,DeFi,NFT', 8, 1, true, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP - INTERVAL '3 hours'),
('E-commerce', 'Online retail, marketplaces, and digital commerce', 'Business', 'ecommerce,online retail,amazon,shopify,digital commerce', 'e-commerce,online retail,Amazon,Shopify,digital commerce', 6, 1, true, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP - INTERVAL '3 hours'),

-- Science topics
('Space & Astronomy', 'Space exploration, astronomy, and cosmic discoveries', 'Science', 'space,astronomy,nasa,spacex,planets,stars,universe', 'space exploration,astronomy,NASA,SpaceX,planets,stars', 7, 1, true, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP - INTERVAL '3 hours'),
('Climate Change', 'Environmental science, climate action, and sustainability', 'Science', 'climate change,global warming,environment,sustainability,renewable energy', 'climate change,global warming,environment,sustainability,renewable energy', 8, 1, true, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP - INTERVAL '3 hours'),
('Health & Medicine', 'Medical breakthroughs, health research, and wellness', 'Science', 'medicine,health,medical research,pharmaceuticals,wellness', 'medical research,health,medicine,pharmaceuticals,wellness', 7, 1, true, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP - INTERVAL '3 hours'),

-- Entertainment topics
('Gaming', 'Video games, esports, and gaming industry', 'Entertainment', 'gaming,video games,esports,playstation,xbox,nintendo', 'gaming,video games,esports,PlayStation,Xbox,Nintendo', 6, 1, true, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP - INTERVAL '3 hours'),
('Movies & TV', 'Film industry, streaming, and entertainment', 'Entertainment', 'movies,tv shows,netflix,disney,hollywood,streaming', 'movies,TV shows,Netflix,Disney,Hollywood,streaming', 5, 1, true, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP - INTERVAL '3 hours'),
('Music', 'Music industry, artists, and streaming platforms', 'Entertainment', 'music,spotify,apple music,artists,concerts,albums', 'music,Spotify,Apple Music,artists,concerts,albums', 5, 1, true, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP - INTERVAL '3 hours'),

-- Lifestyle topics
('Fitness & Health', 'Exercise, nutrition, and healthy living', 'Lifestyle', 'fitness,health,nutrition,exercise,workout,wellness', 'fitness,health,nutrition,exercise,workout,wellness', 4, 1, true, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP - INTERVAL '3 hours'),
('Travel', 'Travel destinations, tips, and tourism', 'Lifestyle', 'travel,tourism,vacation,destinations,hotels,flights', 'travel,tourism,vacation,destinations,hotels,flights', 4, 1, true, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP - INTERVAL '3 hours'),
('Food & Cooking', 'Culinary trends, recipes, and food culture', 'Lifestyle', 'food,cooking,recipes,restaurants,culinary,chef', 'food,cooking,recipes,restaurants,culinary,chef', 3, 1, true, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP - INTERVAL '3 hours'),

-- Education topics
('Online Learning', 'E-learning, courses, and educational technology', 'Education', 'online learning,education,courses,mooc,edtech,learning', 'online learning,education,courses,MOOC,edtech,learning', 6, 1, true, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP - INTERVAL '3 hours'),
('Programming', 'Coding tutorials, programming languages, and software development', 'Education', 'programming,coding,software development,tutorials,algorithms', 'programming,coding,software development,tutorials,algorithms', 7, 1, true, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP - INTERVAL '3 hours'),

-- Social topics
('Social Media', 'Platform updates, trends, and social networking', 'Social', 'social media,facebook,twitter,instagram,linkedin,tiktok', 'social media,Facebook,Twitter,Instagram,LinkedIn,TikTok', 5, 1, true, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP - INTERVAL '3 hours'),
('Remote Work', 'Work from home, digital nomads, and remote collaboration', 'Social', 'remote work,work from home,digital nomads,telecommuting', 'remote work,work from home,digital nomads,telecommuting', 6, 1, true, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP - INTERVAL '3 hours')
ON CONFLICT (name) DO NOTHING;

-- Create a user record for the AI Admin (if it doesn't exist)
INSERT INTO users (id, name, email, username, bio, picture, is_verified, is_active, is_private, is_suspended, followers_count, following_count, posts_count, created_at, updated_at)
SELECT 
    ai_admins.id::text,
    ai_admins.name,
    ai_admins.email,
    ai_admins.username,
    ai_admins.bio,
    ai_admins.avatar_url,
    ai_admins.is_verified,
    ai_admins.is_active,
    false,
    false,
    ai_admins.followers_count,
    ai_admins.following_count,
    ai_admins.posts_count,
    ai_admins.created_at,
    ai_admins.updated_at
FROM ai_admins 
WHERE ai_admins.username = 'ai_assistant'
AND NOT EXISTS (
    SELECT 1 FROM users WHERE users.email = ai_admins.email
)
ON CONFLICT (email) DO NOTHING;

-- Update the last_generated_at to be old so topics are ready for generation
UPDATE topics SET last_generated_at = CURRENT_TIMESTAMP - INTERVAL '3 hours';

COMMIT;
