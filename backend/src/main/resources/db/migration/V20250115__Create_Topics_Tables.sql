-- Create topics table
CREATE TABLE IF NOT EXISTS topics (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    description TEXT,
    ai_prompt TEXT,
    category VARCHAR(255),
    keywords TEXT,
    priority INTEGER DEFAULT 5,
    threads_per_run INTEGER DEFAULT 3,
    threads_generated INTEGER DEFAULT 0,
    last_generated_at TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Create user_topics table
CREATE TABLE IF NOT EXISTS user_topics (
    id BIGSERIAL PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    topic_id BIGINT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(user_id, topic_id),
    FOREIGN KEY (topic_id) REFERENCES topics(id) ON DELETE CASCADE
);

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_user_topics_user_id ON user_topics(user_id);
CREATE INDEX IF NOT EXISTS idx_user_topics_topic_id ON user_topics(topic_id);
CREATE INDEX IF NOT EXISTS idx_topics_is_active ON topics(is_active);

-- Insert default topics
INSERT INTO topics (name, description, ai_prompt, category, keywords) VALUES
('Stock Market', 'Latest updates on AMD, NVIDIA, Apple, Meta, SOFI, NBIS, HIMS and top stock market news including Fed meetings', 'Generate content about stock market trends, earnings reports, and financial news focusing on AMD, NVIDIA, Apple, Meta, SOFI, NBIS, HIMS and Federal Reserve meetings', 'Finance', 'AMD,NVIDIA,Apple,Meta,SOFI,NBIS,HIMS,stock market,earnings,Fed meetings'),
('Sports', 'Cricket, football, basketball and other sports news and updates', 'Generate content about sports news, match results, player updates, and analysis covering cricket, football, basketball and other major sports', 'Sports', 'cricket,football,basketball,sports news,match results,player updates'),
('US Top News', 'Latest breaking news and important updates from the United States', 'Generate content about top US news, political developments, social issues, and major events happening in the United States', 'News', 'US news,politics,social issues,breaking news'),
('Tamil Nadu India News', 'Latest news and updates from Tamil Nadu, India', 'Generate content about Tamil Nadu politics, development, culture, and important news from the state', 'Regional', 'Tamil Nadu,politics,development,culture,India'),
('India Top News', 'Latest national news and updates from India', 'Generate content about Indian politics, economy, social issues, and major national developments', 'News', 'India,politics,economy,social issues,national news'),
('World Cinema', 'Latest updates from global cinema, movies, and entertainment industry', 'Generate content about new movie releases, film industry news, celebrity updates, and entertainment trends worldwide', 'Entertainment', 'movies,cinema,entertainment,celebrity,film industry'),
('White House', 'Updates on Trump, Senate, Congress and US political developments', 'Generate content about US political developments, White House news, Trump updates, Senate proceedings, and Congressional activities', 'Politics', 'Trump,Senate,Congress,White House,US politics'),
('AI Innovation', 'Latest developments in artificial intelligence and technology innovation', 'Generate content about AI breakthroughs, tech innovations, machine learning developments, and emerging technologies', 'Technology', 'AI,artificial intelligence,technology,innovation,machine learning'),
('TVK Tamil Politics', 'Updates on Tamil politics and TVK party developments', 'Generate content about Tamil political developments, TVK party news, and Tamil political landscape', 'Politics', 'TVK,Tamil politics,political developments'),
('Top US News', 'Breaking news and important updates from across the United States', 'Generate content about major US news stories, political developments, and significant events across the country', 'News', 'US news,breaking news,political developments')
ON CONFLICT (name) DO NOTHING;
