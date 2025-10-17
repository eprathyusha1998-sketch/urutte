package com.urutte.service;

import com.urutte.service.ContentGenerationService.NewsItem;
import com.urutte.model.Topic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.util.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Service
public class GuardianNewsService {
    
    private static final Logger logger = LoggerFactory.getLogger(GuardianNewsService.class);
    
    @Autowired
    private RestTemplate restTemplate;
    
    @Value("${app.guardian.api-key:}")
    private String guardianApiKey;
    
    @Value("${app.guardian.base-url:https://content.guardianapis.com}")
    private String guardianBaseUrl;
    
    // Guardian API sections for different topics
    private static final Map<String, String[]> TOPIC_SECTIONS = new HashMap<>();
    
    static {
        // India-related sections
        TOPIC_SECTIONS.put("India", new String[]{
            "world/india", "world/asia", "world", "politics", "business", "sport"
        });
        
        // Tamil Nadu specific
        TOPIC_SECTIONS.put("Tamil Nadu", new String[]{
            "world/india", "world/asia", "world", "politics"
        });
        
        // Cricket
        TOPIC_SECTIONS.put("Cricket", new String[]{
            "sport/cricket", "sport", "world/india", "world/asia"
        });
        
        // Bollywood/Entertainment
        TOPIC_SECTIONS.put("Bollywood", new String[]{
            "film", "culture", "world/india", "world/asia"
        });
        
        // Global news
        TOPIC_SECTIONS.put("Global", new String[]{
            "world", "politics", "business", "technology", "sport", "culture"
        });
        
        // Technology
        TOPIC_SECTIONS.put("Technology", new String[]{
            "technology", "business", "world", "science"
        });
        
        // Politics
        TOPIC_SECTIONS.put("Politics", new String[]{
            "politics", "world", "uk-news", "us-news"
        });
        
        // Sports
        TOPIC_SECTIONS.put("Sports", new String[]{
            "sport", "sport/cricket", "sport/football", "sport/tennis"
        });
    }
    
    /**
     * Fetch news from Guardian API for a specific topic
     */
    public List<NewsItem> fetchNewsForTopic(Topic topic) {
        List<NewsItem> newsItems = new ArrayList<>();
        
        try {
            String topicName = topic.getName().toLowerCase();
            String keywords = topic.getKeywords().toLowerCase();
            
            // Determine which sections to search
            List<String> sectionsToSearch = determineSections(topicName, keywords);
            
            // Search in each section
            for (String section : sectionsToSearch) {
                try {
                    List<NewsItem> sectionItems = fetchFromSection(section, topic);
                    newsItems.addAll(sectionItems);
                    logger.info("Fetched {} items from Guardian section: {}", sectionItems.size(), section);
                } catch (Exception e) {
                    logger.warn("Failed to fetch from Guardian section {}: {}", section, e.getMessage());
                }
            }
            
            // Also do keyword-based search
            try {
                List<NewsItem> keywordItems = searchByKeywords(topic);
                newsItems.addAll(keywordItems);
                logger.info("Fetched {} items from Guardian keyword search", keywordItems.size());
            } catch (Exception e) {
                logger.warn("Failed Guardian keyword search: {}", e.getMessage());
            }
            
            // Remove duplicates and sort by relevance
            newsItems = removeDuplicates(newsItems);
            newsItems.sort((a, b) -> {
                int scoreComparison = Integer.compare(b.getScore(), a.getScore());
                if (scoreComparison != 0) return scoreComparison;
                return b.getPublishedAt().compareTo(a.getPublishedAt());
            });
            
            logger.info("Total Guardian news items for topic {}: {}", topic.getName(), newsItems.size());
            
        } catch (Exception e) {
            logger.error("Error fetching Guardian news for topic: {}", topic.getName(), e);
        }
        
        return newsItems;
    }
    
    /**
     * Determine which Guardian sections to search based on topic
     */
    private List<String> determineSections(String topicName, String keywords) {
        List<String> sections = new ArrayList<>();
        
        // Check for specific topic matches
        for (Map.Entry<String, String[]> entry : TOPIC_SECTIONS.entrySet()) {
            String key = entry.getKey().toLowerCase();
            if (topicName.contains(key) || keywords.contains(key)) {
                sections.addAll(Arrays.asList(entry.getValue()));
            }
        }
        
        // Default sections if no specific match
        if (sections.isEmpty()) {
            sections.addAll(Arrays.asList("world", "politics", "business", "technology"));
        }
        
        // Remove duplicates and limit to avoid too many API calls
        return sections.stream().distinct().limit(3).toList();
    }
    
    /**
     * Fetch news from a specific Guardian section
     */
    private List<NewsItem> fetchFromSection(String section, Topic topic) {
        List<NewsItem> items = new ArrayList<>();
        
        try {
            String url = buildSectionUrl(section);
            GuardianResponse response = restTemplate.getForObject(url, GuardianResponse.class);
            
            if (response != null && response.getResponse() != null && response.getResponse().getResults() != null) {
                for (GuardianArticle article : response.getResponse().getResults()) {
                    NewsItem item = convertToNewsItem(article, topic);
                    if (item != null) {
                        items.add(item);
                    }
                }
            }
            
        } catch (RestClientException e) {
            logger.warn("Failed to fetch from Guardian section {}: {}", section, e.getMessage());
        }
        
        return items;
    }
    
    /**
     * Search Guardian API by keywords
     */
    private List<NewsItem> searchByKeywords(Topic topic) {
        List<NewsItem> items = new ArrayList<>();
        
        try {
            // Extract key keywords from topic
            String[] keywords = topic.getKeywords().split(",");
            String searchQuery = String.join(" OR ", Arrays.stream(keywords)
                .map(String::trim)
                .filter(k -> k.length() > 2)
                .limit(3)
                .toArray(String[]::new));
            
            if (searchQuery.isEmpty()) {
                searchQuery = topic.getName();
            }
            
            String url = buildSearchUrl(searchQuery);
            GuardianResponse response = restTemplate.getForObject(url, GuardianResponse.class);
            
            if (response != null && response.getResponse() != null && response.getResponse().getResults() != null) {
                for (GuardianArticle article : response.getResponse().getResults()) {
                    NewsItem item = convertToNewsItem(article, topic);
                    if (item != null) {
                        items.add(item);
                    }
                }
            }
            
        } catch (RestClientException e) {
            logger.warn("Failed Guardian keyword search: {}", e.getMessage());
        }
        
        return items;
    }
    
    /**
     * Build URL for section-based search
     */
    private String buildSectionUrl(String section) {
        StringBuilder url = new StringBuilder(guardianBaseUrl);
        url.append("/").append(section);
        url.append("?api-key=").append(guardianApiKey);
        url.append("&show-fields=headline,trailText,body,thumbnail,byline");
        url.append("&page-size=10");
        url.append("&order-by=newest");
        
        return url.toString();
    }
    
    /**
     * Build URL for keyword-based search
     */
    private String buildSearchUrl(String query) {
        StringBuilder url = new StringBuilder(guardianBaseUrl);
        url.append("/search");
        url.append("?api-key=").append(guardianApiKey);
        url.append("&q=").append(query.replace(" ", "%20"));
        url.append("&show-fields=headline,trailText,body,thumbnail,byline");
        url.append("&page-size=10");
        url.append("&order-by=newest");
        
        return url.toString();
    }
    
    /**
     * Convert Guardian article to NewsItem
     */
    private NewsItem convertToNewsItem(GuardianArticle article, Topic topic) {
        try {
            NewsItem item = new NewsItem();
            
            // Set title
            String title = article.getWebTitle();
            if (title == null || title.trim().isEmpty()) {
                return null; // Skip articles without titles
            }
            item.setTitle(title);
            
            // Set content
            String content = article.getFields() != null ? article.getFields().getTrailText() : "";
            if (content == null || content.trim().isEmpty()) {
                content = title; // Use title as content if no trail text
            }
            item.setContent(content);
            
            // Set URL
            item.setUrl(article.getWebUrl());
            
            // Set publication date
            try {
                if (article.getWebPublicationDate() != null) {
                    LocalDateTime dateTime = LocalDateTime.parse(
                        article.getWebPublicationDate().substring(0, 19),
                        DateTimeFormatter.ISO_LOCAL_DATE_TIME
                    );
                    item.setPublishedAt(Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant()));
                } else {
                    item.setPublishedAt(new Date());
                }
            } catch (Exception e) {
                item.setPublishedAt(new Date());
            }
            
            // Calculate relevance score
            item.setScore(calculateRelevanceScore(item, topic));
            
            // Set source
            item.setSource("The Guardian");
            
            return item;
            
        } catch (Exception e) {
            logger.warn("Error converting Guardian article: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * Calculate relevance score for news item
     */
    private int calculateRelevanceScore(NewsItem item, Topic topic) {
        int score = 50; // Base score
        
        String title = item.getTitle().toLowerCase();
        String content = item.getContent().toLowerCase();
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
        
        // Boost score for recent articles
        long hoursSincePublished = (System.currentTimeMillis() - item.getPublishedAt().getTime()) / (1000 * 60 * 60);
        if (hoursSincePublished < 24) {
            score += 10;
        } else if (hoursSincePublished < 48) {
            score += 5;
        }
        
        // Ensure score is within bounds
        return Math.min(100, Math.max(0, score));
    }
    
    /**
     * Remove duplicate news items
     */
    private List<NewsItem> removeDuplicates(List<NewsItem> items) {
        Set<String> seenUrls = new HashSet<>();
        return items.stream()
            .filter(item -> seenUrls.add(item.getUrl()))
            .toList();
    }
    
    /**
     * Test Guardian API connectivity
     */
    public boolean testApiConnection() {
        try {
            String testUrl = guardianBaseUrl + "/search?api-key=" + guardianApiKey + "&q=test&page-size=1";
            GuardianResponse response = restTemplate.getForObject(testUrl, GuardianResponse.class);
            return response != null && response.getResponse() != null;
        } catch (Exception e) {
            logger.error("Guardian API connection test failed: {}", e.getMessage());
            return false;
        }
    }
    
    // Guardian API Response Classes
    public static class GuardianResponse {
        private GuardianResponseData response;
        
        public GuardianResponseData getResponse() { return response; }
        public void setResponse(GuardianResponseData response) { this.response = response; }
    }
    
    public static class GuardianResponseData {
        private List<GuardianArticle> results;
        
        public List<GuardianArticle> getResults() { return results; }
        public void setResults(List<GuardianArticle> results) { this.results = results; }
    }
    
    public static class GuardianArticle {
        private String id;
        private String webTitle;
        private String webUrl;
        private String webPublicationDate;
        private GuardianFields fields;
        
        // Getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        
        public String getWebTitle() { return webTitle; }
        public void setWebTitle(String webTitle) { this.webTitle = webTitle; }
        
        public String getWebUrl() { return webUrl; }
        public void setWebUrl(String webUrl) { this.webUrl = webUrl; }
        
        public String getWebPublicationDate() { return webPublicationDate; }
        public void setWebPublicationDate(String webPublicationDate) { this.webPublicationDate = webPublicationDate; }
        
        public GuardianFields getFields() { return fields; }
        public void setFields(GuardianFields fields) { this.fields = fields; }
    }
    
    public static class GuardianFields {
        private String headline;
        private String trailText;
        private String body;
        private String thumbnail;
        private String byline;
        
        // Getters and setters
        public String getHeadline() { return headline; }
        public void setHeadline(String headline) { this.headline = headline; }
        
        public String getTrailText() { return trailText; }
        public void setTrailText(String trailText) { this.trailText = trailText; }
        
        public String getBody() { return body; }
        public void setBody(String body) { this.body = body; }
        
        public String getThumbnail() { return thumbnail; }
        public void setThumbnail(String thumbnail) { this.thumbnail = thumbnail; }
        
        public String getByline() { return byline; }
        public void setByline(String byline) { this.byline = byline; }
    }
}
