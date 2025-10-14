package com.urutte.service;

import com.urutte.model.Topic;
import com.urutte.repository.TopicRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class TopicService {
    
    private static final Logger logger = LoggerFactory.getLogger(TopicService.class);
    
    @Autowired
    private TopicRepository topicRepository;
    
    /**
     * Initialize default topics
     */
    public void initializeDefaultTopics() {
        logger.info("Initializing default topics");
        
        // Check if topics already exist
        if (topicRepository.countActiveTopics() > 0) {
            logger.info("Topics already exist, skipping initialization");
            return;
        }
        
        // Create default topics
        createDefaultTopics();
        
        logger.info("Default topics initialized successfully");
    }
    
    /**
     * Create default topics
     */
    private void createDefaultTopics() {
        // Technology topics
        createTopic("Artificial Intelligence", "Latest developments in AI, machine learning, and automation", 
                   "Technology", "ai,artificial intelligence,machine learning,automation,chatgpt,gpt,openai", 
                   "artificial intelligence,machine learning,AI news,automation");
        
        createTopic("Web Development", "Frontend, backend, and full-stack development trends", 
                   "Technology", "web development,frontend,backend,react,angular,vue,javascript,typescript", 
                   "web development,frontend development,backend development,react,angular");
        
        createTopic("Mobile Development", "iOS, Android, and cross-platform mobile development", 
                   "Technology", "mobile development,ios,android,react native,flutter,swift,kotlin", 
                   "mobile development,iOS development,Android development,React Native,Flutter");
        
        createTopic("DevOps & Cloud", "DevOps practices, cloud computing, and infrastructure", 
                   "Technology", "devops,cloud computing,aws,azure,gcp,docker,kubernetes,ci/cd", 
                   "DevOps,cloud computing,AWS,Azure,Google Cloud,Docker,Kubernetes");
        
        createTopic("Cybersecurity", "Security threats, best practices, and protection strategies", 
                   "Technology", "cybersecurity,security,hacking,privacy,encryption,malware", 
                   "cybersecurity,information security,privacy,data protection");
        
        // Business topics
        createTopic("Startups", "Startup news, funding, and entrepreneurship", 
                   "Business", "startup,entrepreneurship,funding,venture capital,unicorn,ipo", 
                   "startup news,entrepreneurship,venture capital,funding,IPO");
        
        createTopic("Cryptocurrency", "Bitcoin, blockchain, and digital currencies", 
                   "Business", "cryptocurrency,bitcoin,ethereum,blockchain,defi,nft", 
                   "cryptocurrency,Bitcoin,Ethereum,blockchain,DeFi,NFT");
        
        createTopic("E-commerce", "Online retail, marketplaces, and digital commerce", 
                   "Business", "ecommerce,online retail,amazon,shopify,digital commerce", 
                   "e-commerce,online retail,Amazon,Shopify,digital commerce");
        
        // Science topics
        createTopic("Space & Astronomy", "Space exploration, astronomy, and cosmic discoveries", 
                   "Science", "space,astronomy,nasa,spacex,planets,stars,universe", 
                   "space exploration,astronomy,NASA,SpaceX,planets,stars");
        
        createTopic("Climate Change", "Environmental science, climate action, and sustainability", 
                   "Science", "climate change,global warming,environment,sustainability,renewable energy", 
                   "climate change,global warming,environment,sustainability,renewable energy");
        
        createTopic("Health & Medicine", "Medical breakthroughs, health research, and wellness", 
                   "Science", "medicine,health,medical research,pharmaceuticals,wellness", 
                   "medical research,health,medicine,pharmaceuticals,wellness");
        
        // Entertainment topics
        createTopic("Gaming", "Video games, esports, and gaming industry", 
                   "Entertainment", "gaming,video games,esports,playstation,xbox,nintendo", 
                   "gaming,video games,esports,PlayStation,Xbox,Nintendo");
        
        createTopic("Movies & TV", "Film industry, streaming, and entertainment", 
                   "Entertainment", "movies,tv shows,netflix,disney,hollywood,streaming", 
                   "movies,TV shows,Netflix,Disney,Hollywood,streaming");
        
        createTopic("Music", "Music industry, artists, and streaming platforms", 
                   "Entertainment", "music,spotify,apple music,artists,concerts,albums", 
                   "music,Spotify,Apple Music,artists,concerts,albums");
        
        // Lifestyle topics
        createTopic("Fitness & Health", "Exercise, nutrition, and healthy living", 
                   "Lifestyle", "fitness,health,nutrition,exercise,workout,wellness", 
                   "fitness,health,nutrition,exercise,workout,wellness");
        
        createTopic("Travel", "Travel destinations, tips, and tourism", 
                   "Lifestyle", "travel,tourism,vacation,destinations,hotels,flights", 
                   "travel,tourism,vacation,destinations,hotels,flights");
        
        createTopic("Food & Cooking", "Culinary trends, recipes, and food culture", 
                   "Lifestyle", "food,cooking,recipes,restaurants,culinary,chef", 
                   "food,cooking,recipes,restaurants,culinary,chef");
        
        // Education topics
        createTopic("Online Learning", "E-learning, courses, and educational technology", 
                   "Education", "online learning,education,courses,mooc,edtech,learning", 
                   "online learning,education,courses,MOOC,edtech,learning");
        
        createTopic("Programming", "Coding tutorials, programming languages, and software development", 
                   "Education", "programming,coding,software development,tutorials,algorithms", 
                   "programming,coding,software development,tutorials,algorithms");
        
        // Social topics
        createTopic("Social Media", "Platform updates, trends, and social networking", 
                   "Social", "social media,facebook,twitter,instagram,linkedin,tiktok", 
                   "social media,Facebook,Twitter,Instagram,LinkedIn,TikTok");
        
        createTopic("Remote Work", "Work from home, digital nomads, and remote collaboration", 
                   "Social", "remote work,work from home,digital nomads,telecommuting", 
                   "remote work,work from home,digital nomads,telecommuting");
    }
    
    /**
     * Create a topic
     */
    public Topic createTopic(String name, String description, String category, 
                           String keywords, String searchQueries) {
        Topic topic = new Topic(name, description, category, keywords, searchQueries);
        topic = topicRepository.save(topic);
        
        logger.info("Created topic: {}", topic.getName());
        return topic;
    }
    
    /**
     * Get all active topics
     */
    public List<Topic> getAllActiveTopics() {
        return topicRepository.findByIsActiveTrueOrderByPriorityDesc();
    }
    
    /**
     * Get topics by category
     */
    public List<Topic> getTopicsByCategory(String category) {
        return topicRepository.findByCategoryAndIsActiveTrue(category);
    }
    
    /**
     * Get all categories
     */
    public List<String> getAllCategories() {
        return topicRepository.findDistinctCategories();
    }
    
    /**
     * Get topic by ID
     */
    public Optional<Topic> getTopicById(Long id) {
        return topicRepository.findById(id);
    }
    
    /**
     * Update topic
     */
    public Topic updateTopic(Long id, String name, String description, String category, 
                           String keywords, String searchQueries, Integer priority, Integer threadsPerRun) {
        Optional<Topic> topicOpt = topicRepository.findById(id);
        
        if (topicOpt.isEmpty()) {
            throw new IllegalArgumentException("Topic not found with id: " + id);
        }
        
        Topic topic = topicOpt.get();
        topic.setName(name);
        topic.setDescription(description);
        topic.setCategory(category);
        topic.setKeywords(keywords);
        topic.setSearchQueries(searchQueries);
        topic.setPriority(priority);
        topic.setThreadsPerRun(threadsPerRun);
        
        topic = topicRepository.save(topic);
        logger.info("Updated topic: {}", topic.getName());
        
        return topic;
    }
    
    /**
     * Toggle topic status
     */
    public Topic toggleTopicStatus(Long id) {
        Optional<Topic> topicOpt = topicRepository.findById(id);
        
        if (topicOpt.isEmpty()) {
            throw new IllegalArgumentException("Topic not found with id: " + id);
        }
        
        Topic topic = topicOpt.get();
        topic.setIsActive(!topic.getIsActive());
        
        topic = topicRepository.save(topic);
        logger.info("Toggled topic status: {} (active: {})", topic.getName(), topic.getIsActive());
        
        return topic;
    }
    
    /**
     * Delete topic
     */
    public void deleteTopic(Long id) {
        Optional<Topic> topicOpt = topicRepository.findById(id);
        
        if (topicOpt.isEmpty()) {
            throw new IllegalArgumentException("Topic not found with id: " + id);
        }
        
        Topic topic = topicOpt.get();
        topicRepository.delete(topic);
        
        logger.info("Deleted topic: {}", topic.getName());
    }
    
    /**
     * Search topics
     */
    public List<Topic> searchTopics(String query) {
        return topicRepository.findByNameContainingIgnoreCaseAndIsActiveTrue(query);
    }
    
    /**
     * Get topic statistics
     */
    public TopicStats getTopicStats(Long id) {
        Optional<Topic> topicOpt = topicRepository.findById(id);
        
        if (topicOpt.isEmpty()) {
            throw new IllegalArgumentException("Topic not found with id: " + id);
        }
        
        Topic topic = topicOpt.get();
        
        TopicStats stats = new TopicStats();
        stats.setId(topic.getId());
        stats.setName(topic.getName());
        stats.setCategory(topic.getCategory());
        stats.setIsActive(topic.getIsActive());
        stats.setPriority(topic.getPriority());
        stats.setThreadsPerRun(topic.getThreadsPerRun());
        stats.setTotalThreadsGenerated(topic.getTotalThreadsGenerated());
        stats.setLastGeneratedAt(topic.getLastGeneratedAt());
        stats.setCreatedAt(topic.getCreatedAt());
        
        return stats;
    }
    
    /**
     * Topic statistics data class
     */
    public static class TopicStats {
        private Long id;
        private String name;
        private String category;
        private Boolean isActive;
        private Integer priority;
        private Integer threadsPerRun;
        private Integer totalThreadsGenerated;
        private LocalDateTime lastGeneratedAt;
        private LocalDateTime createdAt;
        
        // Getters and setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        
        public Boolean getIsActive() { return isActive; }
        public void setIsActive(Boolean isActive) { this.isActive = isActive; }
        
        public Integer getPriority() { return priority; }
        public void setPriority(Integer priority) { this.priority = priority; }
        
        public Integer getThreadsPerRun() { return threadsPerRun; }
        public void setThreadsPerRun(Integer threadsPerRun) { this.threadsPerRun = threadsPerRun; }
        
        public Integer getTotalThreadsGenerated() { return totalThreadsGenerated; }
        public void setTotalThreadsGenerated(Integer totalThreadsGenerated) { this.totalThreadsGenerated = totalThreadsGenerated; }
        
        public LocalDateTime getLastGeneratedAt() { return lastGeneratedAt; }
        public void setLastGeneratedAt(LocalDateTime lastGeneratedAt) { this.lastGeneratedAt = lastGeneratedAt; }
        
        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    }
}
