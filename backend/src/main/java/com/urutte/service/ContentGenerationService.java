package com.urutte.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.urutte.model.*;
import com.urutte.repository.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ContentGenerationService {
    
    private static final Logger logger = LoggerFactory.getLogger(ContentGenerationService.class);
    
    @Autowired
    private TopicRepository topicRepository;
    
    @Autowired
    private AiAdminRepository aiAdminRepository;
    
    @Autowired
    private AiGeneratedThreadRepository aiGeneratedThreadRepository;
    
    @Autowired
    private ThreadRepository threadRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private RestTemplate restTemplate;
    
    @Autowired
    private TrendingNewsService trendingNewsService;
    
    @Value("${app.ai.openai.api-key:}")
    private String openaiApiKey;
    
    @Value("${app.ai.openai.model:gpt-3.5-turbo}")
    private String openaiModel;
    
    @Value("${app.ai.openai.base-url:https://api.openai.com/v1}")
    private String openaiBaseUrl;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * Generate content for high-priority topics only
     */
    public void generateHighPriorityContent() {
        logger.info("Starting high-priority content generation");
        
        // Get AI Admin
        Optional<AiAdmin> aiAdminOpt = aiAdminRepository.findActiveAiAdmin();
        if (aiAdminOpt.isEmpty()) {
            logger.error("No active AI Admin found. Cannot generate content.");
            return;
        }
        
        AiAdmin aiAdmin = aiAdminOpt.get();
        
        // Get high-priority topics (priority >= 8)
        List<Topic> highPriorityTopics = topicRepository.findByIsActiveTrueOrderByPriorityDesc()
            .stream()
            .filter(topic -> topic.getPriority() >= 8)
            .collect(Collectors.toList());
        
        logger.info("Found {} high-priority topics", highPriorityTopics.size());
        
        for (Topic topic : highPriorityTopics) {
            try {
                generateContentForTopic(topic, aiAdmin);
                // Update topic's last generated timestamp
                topic.updateLastGeneratedAt();
                topicRepository.save(topic);
                
                // Add delay between topics
                java.lang.Thread.sleep(3000); // 3 seconds delay
                
            } catch (Exception e) {
                logger.error("Error generating high-priority content for topic: {}", topic.getName(), e);
            }
        }
        
        logger.info("High-priority content generation completed");
    }
    
    /**
     * Clean up old AI-generated content
     */
    public void cleanupOldContent() {
        logger.info("Starting cleanup of old AI-generated content");
        
        try {
            // Delete AI-generated threads older than 30 days
            LocalDateTime cutoffDate = LocalDateTime.now().minusDays(30);
            
            List<AiGeneratedThread> oldThreads = aiGeneratedThreadRepository.findThreadsCreatedSince(cutoffDate);
            
            for (AiGeneratedThread aiThread : oldThreads) {
                // Mark thread as deleted instead of actually deleting
                aiThread.getThread().setIsDeleted(true);
                aiThread.setStatus("deleted");
                
                threadRepository.save(aiThread.getThread());
                aiGeneratedThreadRepository.save(aiThread);
            }
            
            logger.info("Cleaned up {} old AI-generated threads", oldThreads.size());
            
        } catch (Exception e) {
            logger.error("Error during cleanup of old content", e);
        }
    }
    
    /**
     * Generate content for all active topics
     */
    public void generateContentForAllTopics() {
        logger.info("Starting content generation for all topics");
        
        // Get AI Admin
        Optional<AiAdmin> aiAdminOpt = aiAdminRepository.findActiveAiAdmin();
        if (aiAdminOpt.isEmpty()) {
            logger.error("No active AI Admin found. Cannot generate content.");
            return;
        }
        
        AiAdmin aiAdmin = aiAdminOpt.get();
        
        // Get topics ready for generation (not generated in last 2 hours)
        LocalDateTime cutoffTime = LocalDateTime.now().minusHours(2);
        List<Topic> topics = topicRepository.findTopicsReadyForGeneration(cutoffTime);
        
        logger.info("Found {} topics ready for content generation", topics.size());
        
        for (Topic topic : topics) {
            try {
                generateContentForTopic(topic, aiAdmin);
                // Update topic's last generated timestamp
                topic.updateLastGeneratedAt();
                topicRepository.save(topic);
                
                // Add delay between topics to avoid rate limiting
                java.lang.Thread.sleep(5000); // 5 seconds delay
                
            } catch (Exception e) {
                logger.error("Error generating content for topic: {}", topic.getName(), e);
            }
        }
        
        logger.info("Content generation completed for all topics");
    }
    
    /**
     * Generate content for a specific topic
     */
    public void generateContentForTopic(Topic topic, AiAdmin aiAdmin) {
        logger.info("Generating content for topic: {}", topic.getName());
        
        try {
            // Get recent news/stories for this topic
            List<NewsItem> newsItems = fetchRecentNews(topic);
            
            if (newsItems.isEmpty()) {
                logger.warn("No news items found for topic: {}", topic.getName());
                return;
            }
            
            // Generate threads for the top news items
            int threadsToGenerate = Math.min(topic.getThreadsPerRun(), newsItems.size());
            
            for (int i = 0; i < threadsToGenerate; i++) {
                NewsItem newsItem = newsItems.get(i);
                try {
                    generateThreadFromNewsItem(topic, aiAdmin, newsItem);
                    topic.incrementThreadsGenerated();
                    
                    // Add delay between thread creation (30 seconds to 1 minute)
                    if (i < threadsToGenerate - 1) { // Don't delay after the last thread
                        int delayMs = 30000 + (int)(Math.random() * 30000); // 30-60 seconds
                        logger.info("Waiting {} seconds before creating next thread...", delayMs / 1000);
                        java.lang.Thread.sleep(delayMs);
                    }
                } catch (Exception e) {
                    logger.error("Error generating thread from news item: {}", newsItem.getTitle(), e);
                }
            }
            
            topicRepository.save(topic);
            logger.info("Generated {} threads for topic: {}", threadsToGenerate, topic.getName());
            
        } catch (Exception e) {
            logger.error("Error generating content for topic: {}", topic.getName(), e);
        }
    }
    
    /**
     * Fetch recent news for a topic
     */
    private List<NewsItem> fetchRecentNews(Topic topic) {
        List<NewsItem> newsItems = new ArrayList<>();
        
        try {
            // Try external sources first
            newsItems.addAll(fetchFromReddit(topic));
            newsItems.addAll(fetchFromHackerNews(topic));
            newsItems.addAll(fetchFromRSS(topic));
            
            // If no external news found, use trending news generator
            if (newsItems.isEmpty()) {
                logger.info("No external news found for topic: {}, using trending news generator", topic.getName());
                newsItems.addAll(trendingNewsService.generateTrendingNews(topic));
            }
            
            // Sort by relevance and recency
            newsItems.sort((a, b) -> {
                // Prioritize items with higher scores
                int scoreComparison = Integer.compare(b.getScore(), a.getScore());
                if (scoreComparison != 0) return scoreComparison;
                
                // Then by recency
                return b.getPublishedAt().compareTo(a.getPublishedAt());
            });
            
            logger.info("Fetched {} news items for topic: {}", newsItems.size(), topic.getName());
            
        } catch (Exception e) {
            logger.error("Error fetching news for topic: {}, falling back to trending news", topic.getName(), e);
            // Fallback to trending news if everything fails
            try {
                newsItems.addAll(trendingNewsService.generateTrendingNews(topic));
            } catch (Exception fallbackException) {
                logger.error("Error in fallback news generation for topic: {}", topic.getName(), fallbackException);
            }
        }
        
        return newsItems;
    }
    
    /**
     * Fetch news from Reddit
     */
    private List<NewsItem> fetchFromReddit(Topic topic) {
        List<NewsItem> newsItems = new ArrayList<>();
        
        try {
            String[] keywords = topic.getKeywordsArray();
            for (String keyword : keywords) {
                String url = String.format("https://www.reddit.com/r/%s/hot.json?limit=10", keyword.trim());
                
                HttpHeaders headers = new HttpHeaders();
                headers.set("User-Agent", "UrutteBot/1.0");
                HttpEntity<String> entity = new HttpEntity<>(headers);
                
                ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
                
                if (response.getStatusCode() == HttpStatus.OK) {
                    JsonNode root = objectMapper.readTree(response.getBody());
                    JsonNode posts = root.path("data").path("children");
                    
                    for (JsonNode post : posts) {
                        JsonNode data = post.path("data");
                        String title = data.path("title").asText();
                        String content = data.path("selftext").asText();
                        String url_link = data.path("url").asText();
                        int score = data.path("score").asInt();
                        long created = data.path("created_utc").asLong() * 1000;
                        
                        if (!title.isEmpty() && score > 10) { // Only high-quality posts
                            NewsItem item = new NewsItem();
                            item.setTitle(title);
                            item.setContent(content);
                            item.setUrl(url_link);
                            item.setScore(score);
                            item.setPublishedAt(new Date(created));
                            item.setSource("Reddit");
                            
                            newsItems.add(item);
                        }
                    }
                }
                
                java.lang.Thread.sleep(1000); // Rate limiting
            }
            
        } catch (Exception e) {
            logger.error("Error fetching from Reddit for topic: {}", topic.getName(), e);
        }
        
        return newsItems;
    }
    
    /**
     * Fetch news from Hacker News
     */
    private List<NewsItem> fetchFromHackerNews(Topic topic) {
        List<NewsItem> newsItems = new ArrayList<>();
        
        try {
            String url = "https://hacker-news.firebaseio.com/v0/topstories.json";
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            
            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode storyIds = objectMapper.readTree(response.getBody());
                
                // Get top 20 stories
                for (int i = 0; i < Math.min(20, storyIds.size()); i++) {
                    int storyId = storyIds.get(i).asInt();
                    String storyUrl = "https://hacker-news.firebaseio.com/v0/item/" + storyId + ".json";
                    
                    ResponseEntity<String> storyResponse = restTemplate.getForEntity(storyUrl, String.class);
                    
                    if (storyResponse.getStatusCode() == HttpStatus.OK) {
                        JsonNode story = objectMapper.readTree(storyResponse.getBody());
                        
                        String title = story.path("title").asText();
                        String url_link = story.path("url").asText();
                        int score = story.path("score").asInt();
                        long time = story.path("time").asLong() * 1000;
                        
                        // Check if story is relevant to topic
                        if (isRelevantToTopic(title, topic) && score > 50) {
                            NewsItem item = new NewsItem();
                            item.setTitle(title);
                            item.setContent(""); // HN doesn't have content
                            item.setUrl(url_link);
                            item.setScore(score);
                            item.setPublishedAt(new Date(time));
                            item.setSource("Hacker News");
                            
                            newsItems.add(item);
                        }
                    }
                    
                    java.lang.Thread.sleep(100); // Rate limiting
                }
            }
            
        } catch (Exception e) {
            logger.error("Error fetching from Hacker News for topic: {}", topic.getName(), e);
        }
        
        return newsItems;
    }
    
    /**
     * Fetch news from RSS feeds (placeholder - you can implement specific RSS feeds)
     */
    private List<NewsItem> fetchFromRSS(Topic topic) {
        // This is a placeholder - you can implement RSS parsing here
        // For now, return empty list
        return new ArrayList<>();
    }
    
    /**
     * Check if content is relevant to topic
     */
    private boolean isRelevantToTopic(String content, Topic topic) {
        String[] keywords = topic.getKeywordsArray();
        String lowerContent = content.toLowerCase();
        
        for (String keyword : keywords) {
            if (lowerContent.contains(keyword.toLowerCase())) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Generate a thread from a news item using AI
     */
    private void generateThreadFromNewsItem(Topic topic, AiAdmin aiAdmin, NewsItem newsItem) {
        try {
            // Create AI-generated content
            String aiContent = generateAIContent(topic, newsItem);
            
            if (aiContent == null || aiContent.trim().isEmpty()) {
                logger.warn("AI content generation failed for news item: {}", newsItem.getTitle());
                return;
            }
            
            // Create the thread
            com.urutte.model.Thread thread = new com.urutte.model.Thread();
            thread.setContent(aiContent);
            thread.setUser(convertAiAdminToUser(aiAdmin));
            thread.setThreadType(ThreadType.ORIGINAL);
            thread.setThreadLevel(0);
            thread.setIsPublic(true);
            thread.setIsDeleted(false);
            thread.setReplyPermission(ReplyPermission.ANYONE);
            
            thread = threadRepository.save(thread);
            
            // Create AI Generated Thread record
            AiGeneratedThread aiThread = new AiGeneratedThread(
                thread, topic, aiAdmin, newsItem.getContent(),
                newsItem.getUrl(), newsItem.getTitle(), "openai"
            );
            
            aiGeneratedThreadRepository.save(aiThread);
            
            logger.info("Generated thread {} for topic {} from news: {}", 
                       thread.getId(), topic.getName(), newsItem.getTitle());
            
        } catch (Exception e) {
            logger.error("Error generating thread from news item: {}", newsItem.getTitle(), e);
        }
    }
    
    /**
     * Generate AI content using OpenAI API
     */
    private String generateAIContent(Topic topic, NewsItem newsItem) {
        if (openaiApiKey == null || openaiApiKey.trim().isEmpty()) {
            logger.warn("OpenAI API key not configured. Using fallback content generation.");
            return generateFallbackContent(topic, newsItem);
        }
        
        try {
            String prompt = String.format(
                "Create an engaging social media post about this news story. " +
                "Make it conversational, informative, and encourage discussion. " +
                "Keep it under 400 characters and include relevant hashtags. " +
                "ALWAYS include the source link at the end of the post. " +
                "Format: [Your engaging post content] [Source: %s] [Hashtags] " +
                "Topic: %s\n" +
                "News Title: %s\n" +
                "News Content: %s\n" +
                "Source URL: %s",
                newsItem.getUrl(),
                topic.getName(),
                newsItem.getTitle(),
                newsItem.getContent(),
                newsItem.getUrl()
            );
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", openaiModel);
            requestBody.put("messages", Arrays.asList(
                Map.of("role", "system", "content", "You are a social media content creator who creates engaging posts about technology and current events."),
                Map.of("role", "user", "content", prompt)
            ));
            requestBody.put("max_tokens", 200);
            requestBody.put("temperature", 0.7);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + openaiApiKey);
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            
            ResponseEntity<String> response = restTemplate.postForEntity(
                openaiBaseUrl + "/chat/completions", entity, String.class);
            
            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode root = objectMapper.readTree(response.getBody());
                String content = root.path("choices").get(0).path("message").path("content").asText();
                return content.trim();
            }
            
        } catch (Exception e) {
            logger.error("Error calling OpenAI API", e);
        }
        
        return generateFallbackContent(topic, newsItem);
    }
    
    /**
     * Generate fallback content when AI is not available
     */
    private String generateFallbackContent(Topic topic, NewsItem newsItem) {
        String hashtags = Arrays.stream(topic.getKeywordsArray())
            .map(keyword -> "#" + keyword.replaceAll("\\s+", ""))
            .collect(Collectors.joining(" "));
        
        String content = String.format(
            "🔥 %s\n\n%s\n\nWhat are your thoughts on this?\n\nSource: %s\n\n%s",
            newsItem.getTitle(),
            newsItem.getContent().length() > 200 ? 
                newsItem.getContent().substring(0, 200) + "..." : 
                newsItem.getContent(),
            newsItem.getUrl(),
            hashtags
        );
        
        // Ensure content is not too long
        if (content.length() > 800) {
            content = content.substring(0, 797) + "...";
        }
        
        return content;
    }
    
    /**
     * Convert AiAdmin to User for thread creation
     */
    private User convertAiAdminToUser(AiAdmin aiAdmin) {
        // Find or create a user record for the AI Admin
        Optional<User> existingUser = userRepository.findByEmail(aiAdmin.getEmail());
        
        if (existingUser.isPresent()) {
            return existingUser.get();
        }
        
        // Create new user record for AI Admin
        User user = new User();
        user.setId(aiAdmin.getId().toString());
        user.setName(aiAdmin.getName());
        user.setEmail(aiAdmin.getEmail());
        user.setUsername(aiAdmin.getUsername());
        user.setBio(aiAdmin.getBio());
        user.setPicture(aiAdmin.getAvatarUrl());
        user.setIsVerified(true);
        user.setIsActive(true);
        user.setIsPrivate(false);
        user.setIsSuspended(false);
        user.setFollowersCount(aiAdmin.getFollowersCount());
        user.setFollowingCount(aiAdmin.getFollowingCount());
        user.setPostsCount(aiAdmin.getPostsCount());
        
        return userRepository.save(user);
    }
    
    /**
     * News item data class
     */
    public static class NewsItem {
        private String title;
        private String content;
        private String url;
        private int score;
        private Date publishedAt;
        private String source;
        
        // Getters and setters
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        
        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }
        
        public int getScore() { return score; }
        public void setScore(int score) { this.score = score; }
        
        public Date getPublishedAt() { return publishedAt; }
        public void setPublishedAt(Date publishedAt) { this.publishedAt = publishedAt; }
        
        public String getSource() { return source; }
        public void setSource(String source) { this.source = source; }
    }
}
