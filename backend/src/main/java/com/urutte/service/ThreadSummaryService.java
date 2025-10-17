package com.urutte.service;

import com.urutte.model.Thread;
import com.urutte.model.Topic;
import com.urutte.model.User;
import com.urutte.service.ContentGenerationService.NewsItem;
import com.urutte.repository.ThreadRepository;
import com.urutte.repository.TopicRepository;
import com.urutte.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.util.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
public class ThreadSummaryService {
    
    private static final Logger logger = LoggerFactory.getLogger(ThreadSummaryService.class);
    
    @Autowired
    private ThreadRepository threadRepository;
    
    @Autowired
    private TopicRepository topicRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private RestTemplate restTemplate;
    
    @Value("${app.ai.openai.api-key:}")
    private String openaiApiKey;
    
    @Value("${app.ai.openai.model:gpt-3.5-turbo}")
    private String openaiModel;
    
    @Value("${app.ai.openai.base-url:https://api.openai.com/v1}")
    private String openaiBaseUrl;
    
    /**
     * Generate a summary thread with 3 posts based on a topic
     */
    public List<Thread> generateSummaryThread(Topic topic, int numberOfPosts) {
        List<Thread> summaryThreads = new ArrayList<>();
        
        try {
            logger.info("Generating summary thread for topic: {} with {} posts", topic.getName(), numberOfPosts);
            
            // Fetch recent news for the topic using Guardian API
            List<NewsItem> newsItems = fetchNewsForTopic(topic);
            
            if (newsItems.isEmpty()) {
                logger.warn("No news items found for topic: {}", topic.getName());
                return summaryThreads;
            }
            
            // Take the top 3 most relevant news items
            List<NewsItem> topNews = newsItems.stream()
                .limit(numberOfPosts)
                .toList();
            
            // Generate summary posts for each news item
            for (int i = 0; i < topNews.size(); i++) {
                NewsItem newsItem = topNews.get(i);
                Thread summaryThread = createSummaryPost(topic, newsItem, i + 1, topNews.size());
                if (summaryThread != null) {
                    summaryThreads.add(summaryThread);
                }
            }
            
            logger.info("Generated {} summary threads for topic: {}", summaryThreads.size(), topic.getName());
            
        } catch (Exception e) {
            logger.error("Error generating summary thread for topic: {}", topic.getName(), e);
        }
        
        return summaryThreads;
    }
    
    /**
     * Create a summary post for a news item
     */
    private Thread createSummaryPost(Topic topic, NewsItem newsItem, int postNumber, int totalPosts) {
        try {
        // Get AI Assistant user - try by email first, then create if not found
        Optional<User> aiUserOpt = userRepository.findByEmail("ai.assistant@urutte.com");
        if (aiUserOpt.isEmpty()) {
            // Create AI Assistant user if not found
            User aiUser = new User();
            aiUser.setId("ai-assistant-001");
            aiUser.setEmail("ai.assistant@urutte.com");
            aiUser.setPassword("$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi"); // password: "password"
            aiUser.setName("AI Assistant");
            aiUser.setUsername("ai_assistant_bot");
            aiUser.setIsActive(true);
            aiUser.setCreatedAt(java.time.Instant.now());
            aiUser.setUpdatedAt(java.time.Instant.now());
            
            aiUser = userRepository.save(aiUser);
            aiUserOpt = Optional.of(aiUser);
            logger.info("Created new AI Assistant user: {}", aiUser.getId());
        }
            
            User aiUser = aiUserOpt.get();
            logger.info("Using AI user: {} ({}) for post creation", aiUser.getName(), aiUser.getEmail());
            
            // Generate summary content with source link
            String summaryContent = generateSummaryContent(topic, newsItem, postNumber, totalPosts);
            
            // Create thread with user
            Thread thread = new Thread(summaryContent, aiUser);
            thread.setCreatedAt(LocalDateTime.now());
            thread.setUpdatedAt(LocalDateTime.now());
            
            // Save the thread
            Thread savedThread = threadRepository.save(thread);
            logger.info("Created summary post {} for topic: {}", postNumber, topic.getName());
            
            return savedThread;
            
        } catch (Exception e) {
            logger.error("Error creating summary post for news item: {}", newsItem.getTitle(), e);
            return null;
        }
    }
    
    /**
     * Generate summary content with source link
     */
    private String generateSummaryContent(Topic topic, NewsItem newsItem, int postNumber, int totalPosts) {
        try {
            // Create a prompt for generating summary content
            String prompt = String.format(
                "Create a concise social media post summary for this news story. " +
                "Make it engaging and informative. " +
                "Include relevant hashtags and ALWAYS include the source link at the end. " +
                "Format: [Summary content] [Source: %s] [Hashtags] " +
                "Keep it under 300 characters total. " +
                "This is post %d of %d in a thread about %s.\n\n" +
                "News Title: %s\n" +
                "News Content: %s\n" +
                "Source URL: %s",
                newsItem.getUrl(),
                postNumber,
                totalPosts,
                topic.getName(),
                newsItem.getTitle(),
                newsItem.getContent(),
                newsItem.getUrl()
            );
            
            // Try to use OpenAI for better content generation
            if (openaiApiKey != null && !openaiApiKey.isEmpty()) {
                try {
                    String aiContent = generateWithOpenAI(prompt);
                    if (aiContent != null && !aiContent.trim().isEmpty()) {
                        return aiContent;
                    }
                } catch (Exception e) {
                    logger.warn("OpenAI generation failed, using fallback: {}", e.getMessage());
                }
            }
            
            // Fallback content generation
            return generateFallbackSummary(topic, newsItem, postNumber, totalPosts);
            
        } catch (Exception e) {
            logger.error("Error generating summary content: {}", e.getMessage());
            return generateFallbackSummary(topic, newsItem, postNumber, totalPosts);
        }
    }
    
    /**
     * Generate content using OpenAI
     */
    private String generateWithOpenAI(String prompt) {
        try {
            String url = openaiBaseUrl + "/chat/completions";
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + openaiApiKey);
            headers.set("Content-Type", "application/json");
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", openaiModel);
            requestBody.put("max_tokens", 150);
            requestBody.put("temperature", 0.7);
            
            List<Map<String, String>> messages = new ArrayList<>();
            Map<String, String> message = new HashMap<>();
            message.put("role", "user");
            message.put("content", prompt);
            messages.add(message);
            requestBody.put("messages", messages);
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, request, Map.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                List<Map<String, Object>> choices = (List<Map<String, Object>>) responseBody.get("choices");
                if (choices != null && !choices.isEmpty()) {
                    Map<String, Object> firstChoice = choices.get(0);
                    Map<String, Object> messageContent = (Map<String, Object>) firstChoice.get("message");
                    return (String) messageContent.get("content");
                }
            }
            
        } catch (Exception e) {
            logger.error("Error calling OpenAI API: {}", e.getMessage());
        }
        
        return null;
    }
    
    /**
     * Generate fallback summary content
     */
    private String generateFallbackSummary(Topic topic, NewsItem newsItem, int postNumber, int totalPosts) {
        // Create a more engaging and human-like summary (all in English)
        String[] engagingStarters = {
            "Just came across this interesting update:",
            "This caught my attention today:",
            "Here's something worth discussing:",
            "Interesting development in the news:",
            "Wanted to share this with you all:",
            "This is quite significant:",
            "Thought this was worth highlighting:",
            "Breaking news that's worth noting:"
        };
        
        String starter = engagingStarters[postNumber % engagingStarters.length];
        
        // Create hashtags based on topic
        String hashtags = generateHashtags(topic);
        
        // Format the content to be more conversational and natural
        String content = String.format(
            "%s\n\n%s\n\n%s\n\nWhat are your thoughts on this?\n\n%s\n\nSource: %s",
            starter,
            newsItem.getTitle(),
            newsItem.getContent().length() > 200 ? 
                newsItem.getContent().substring(0, 200) + "..." : 
                newsItem.getContent(),
            hashtags,
            newsItem.getUrl()
        );
        
        // Ensure content is not too long
        if (content.length() > 800) {
            content = content.substring(0, 797) + "...";
        }
        
        return content;
    }
    
    
    /**
     * Generate hashtags based on topic
     */
    private String generateHashtags(Topic topic) {
        String topicName = topic.getName().toLowerCase();
        String keywords = topic.getKeywords().toLowerCase();
        
        // Limit to max 2 hashtags
        List<String> hashtags = new ArrayList<>();
        
        // Add topic-specific hashtags (max 2)
        if (topicName.contains("india")) {
            hashtags.add("#India");
            if (hashtags.size() < 2) hashtags.add("#News");
        } else if (topicName.contains("cricket")) {
            hashtags.add("#Cricket");
            if (hashtags.size() < 2) hashtags.add("#Sports");
        } else if (topicName.contains("bollywood")) {
            hashtags.add("#Bollywood");
            if (hashtags.size() < 2) hashtags.add("#Entertainment");
        } else if (topicName.contains("technology")) {
            hashtags.add("#Technology");
            if (hashtags.size() < 2) hashtags.add("#Innovation");
        } else if (topicName.contains("tamil")) {
            hashtags.add("#TamilNadu");
            if (hashtags.size() < 2) hashtags.add("#News");
        } else if (topicName.contains("gaming")) {
            hashtags.add("#Gaming");
            if (hashtags.size() < 2) hashtags.add("#Entertainment");
        } else if (topicName.contains("health")) {
            hashtags.add("#Health");
            if (hashtags.size() < 2) hashtags.add("#Wellness");
        } else {
            // Default hashtags
            hashtags.add("#News");
            if (hashtags.size() < 2) hashtags.add("#Updates");
        }
        
        // Ensure we have at least 1 hashtag
        if (hashtags.isEmpty()) {
            hashtags.add("#News");
        }
        
        return String.join(" ", hashtags);
    }
    
    /**
     * Fetch news for a topic using Guardian API
     */
    private List<NewsItem> fetchNewsForTopic(Topic topic) {
        List<NewsItem> newsItems = new ArrayList<>();
        
        try {
            // Use Guardian API to fetch news
            String apiKey = "cca24b40-7d6e-44c8-ba61-1b9039ff961f";
            String baseUrl = "https://content.guardianapis.com";
            
            // Determine search query based on topic keywords
            String searchQuery = topic.getName();
            if (topic.getKeywords() != null && !topic.getKeywords().isEmpty()) {
                String[] keywords = topic.getKeywords().split(",");
                if (keywords.length > 0) {
                    // Use the first keyword that's not too generic
                    for (String keyword : keywords) {
                        keyword = keyword.trim();
                        if (keyword.length() > 3 && !keyword.toLowerCase().contains("news")) {
                            searchQuery = keyword;
                            break;
                        }
                    }
                }
            }
            
            logger.info("Searching Guardian API for topic: {} with query: {}", topic.getName(), searchQuery);
            
            // Search Guardian API
            String url = baseUrl + "/search";
            String fullUrl = url + "?api-key=" + apiKey + 
                           "&q=" + searchQuery.replace(" ", "%20") +
                           "&page-size=5" +
                           "&show-fields=headline,trailText,thumbnail" +
                           "&order-by=newest";
            
            logger.info("Making request to Guardian API: {}", fullUrl);
            
            // Rate limiting: Wait 1 second between API calls (developer key limit)
            try {
                java.lang.Thread.sleep(1000);
            } catch (InterruptedException e) {
                java.lang.Thread.currentThread().interrupt();
                logger.warn("Rate limiting sleep interrupted");
            }
            
            ResponseEntity<Map> response = restTemplate.getForEntity(fullUrl, Map.class);
            
            logger.info("Guardian API response status: {}", response.getStatusCode());
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                Map<String, Object> responseData = (Map<String, Object>) responseBody.get("response");
                
                if (responseData != null) {
                    List<Map<String, Object>> results = (List<Map<String, Object>>) responseData.get("results");
                    
                    if (results != null) {
                        logger.info("Found {} articles from Guardian API", results.size());
                        for (Map<String, Object> article : results) {
                            NewsItem newsItem = new NewsItem();
                            newsItem.setTitle((String) article.get("webTitle"));
                            newsItem.setUrl((String) article.get("webUrl"));
                            
                            Map<String, Object> fields = (Map<String, Object>) article.get("fields");
                            if (fields != null) {
                                newsItem.setContent((String) fields.get("trailText"));
                                
                                // TODO: Add thumbnail support later
                            }
                            
                            if (newsItem.getContent() == null || newsItem.getContent().isEmpty()) {
                                newsItem.setContent(newsItem.getTitle());
                            }
                            
                            newsItem.setSource("The Guardian");
                            newsItem.setScore(calculateRelevanceScore(newsItem, topic));
                            newsItem.setPublishedAt(new Date());
                            
                            newsItems.add(newsItem);
                        }
                    }
                }
            }
            
            // Sort by relevance score
            newsItems.sort((a, b) -> Integer.compare(b.getScore(), a.getScore()));
            
        } catch (Exception e) {
            logger.error("Error fetching news for topic: {}", topic.getName(), e);
        }
        
        return newsItems;
    }
    
    /**
     * Calculate relevance score for news item
     */
    private int calculateRelevanceScore(NewsItem newsItem, Topic topic) {
        int score = 50; // Base score
        
        String title = newsItem.getTitle().toLowerCase();
        String content = newsItem.getContent().toLowerCase();
        String keywords = topic.getKeywords().toLowerCase();
        String topicName = topic.getName().toLowerCase();
        
        // Check for keyword matches in title (higher weight)
        String[] keywordArray = keywords.split(",");
        for (String keyword : keywordArray) {
            keyword = keyword.trim();
            if (title.contains(keyword)) {
                score += 20;
            }
            if (content.contains(keyword)) {
                score += 10;
            }
        }
        
        // Check for topic name matches
        if (title.contains(topicName)) {
            score += 15;
        }
        if (content.contains(topicName)) {
            score += 10;
        }
        
        return Math.min(100, Math.max(0, score));
    }
    
    /**
     * NewsItem inner class
     */
    public static class NewsItem {
        private String title;
        private String content;
        private String url;
        private String source;
        private int score;
        private Date publishedAt;
        
        // Getters and setters
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        
        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }
        
        public String getSource() { return source; }
        public void setSource(String source) { this.source = source; }
        
        public int getScore() { return score; }
        public void setScore(int score) { this.score = score; }
        
        public Date getPublishedAt() { return publishedAt; }
        public void setPublishedAt(Date publishedAt) { this.publishedAt = publishedAt; }
    }
}
