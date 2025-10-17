package com.urutte.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.urutte.service.ContentGenerationService.NewsItem;
import com.urutte.model.Topic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class EnhancedNewsService {
    
    private static final Logger logger = LoggerFactory.getLogger(EnhancedNewsService.class);
    
    @Autowired
    private RestTemplate restTemplate;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * Fetch news from multiple sources with better coverage
     */
    public List<NewsItem> fetchNewsForTopic(Topic topic) {
        List<NewsItem> newsItems = new ArrayList<>();
        
        try {
            // Use multiple news sources
            newsItems.addAll(fetchFromNewsAPI(topic));
            newsItems.addAll(fetchFromReddit(topic));
            newsItems.addAll(fetchFromHackerNews(topic));
            newsItems.addAll(fetchFromRSSFeeds(topic));
            newsItems.addAll(fetchFromGoogleNews(topic));
            
            // Remove duplicates and sort by relevance
            newsItems = removeDuplicates(newsItems);
            newsItems.sort((a, b) -> {
                int scoreComparison = Integer.compare(b.getScore(), a.getScore());
                if (scoreComparison != 0) return scoreComparison;
                return b.getPublishedAt().compareTo(a.getPublishedAt());
            });
            
            logger.info("Fetched {} news items for topic: {}", newsItems.size(), topic.getName());
            
        } catch (Exception e) {
            logger.error("Error fetching news for topic: {}", topic.getName(), e);
        }
        
        return newsItems;
    }
    
    /**
     * Fetch from NewsAPI (free tier available)
     */
    private List<NewsItem> fetchFromNewsAPI(Topic topic) {
        List<NewsItem> newsItems = new ArrayList<>();
        
        try {
            // NewsAPI free tier - 1000 requests per day
            String apiKey = "your_newsapi_key_here"; // You can get free key from newsapi.org
            String[] keywords = topic.getKeywordsArray();
            
            for (String keyword : keywords) {
                String url = String.format(
                    "https://newsapi.org/v2/everything?q=%s&sortBy=publishedAt&pageSize=10&apiKey=%s",
                    keyword.trim(), apiKey
                );
                
                HttpHeaders headers = new HttpHeaders();
                headers.set("User-Agent", "UrutteBot/1.0");
                HttpEntity<String> entity = new HttpEntity<>(headers);
                
                ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
                
                if (response.getStatusCode() == HttpStatus.OK) {
                    JsonNode root = objectMapper.readTree(response.getBody());
                    JsonNode articles = root.path("articles");
                    
                    for (JsonNode article : articles) {
                        String title = article.path("title").asText();
                        String content = article.path("description").asText();
                        String url_link = article.path("url").asText();
                        String publishedAt = article.path("publishedAt").asText();
                        
                        if (!title.isEmpty() && !content.isEmpty()) {
                            NewsItem item = new NewsItem();
                            item.setTitle(title);
                            item.setContent(content);
                            item.setUrl(url_link);
                            item.setScore(100); // Default score
                            item.setPublishedAt(parseDate(publishedAt));
                            item.setSource("NewsAPI");
                            
                            newsItems.add(item);
                        }
                    }
                }
                
                Thread.sleep(1000); // Rate limiting
            }
            
        } catch (Exception e) {
            logger.error("Error fetching from NewsAPI for topic: {}", topic.getName(), e);
        }
        
        return newsItems;
    }
    
    /**
     * Fetch from Reddit with better error handling
     */
    private List<NewsItem> fetchFromReddit(Topic topic) {
        List<NewsItem> newsItems = new ArrayList<>();
        
        try {
            String[] keywords = topic.getKeywordsArray();
            for (String keyword : keywords) {
                // Try multiple subreddits
                String[] subreddits = {
                    keyword.trim(),
                    "technology",
                    "programming", 
                    "news",
                    "worldnews",
                    "india",
                    "cricket",
                    "bollywood"
                };
                
                for (String subreddit : subreddits) {
                    try {
                        String url = String.format("https://www.reddit.com/r/%s/hot.json?limit=5", subreddit);
                        
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
                                
                                if (!title.isEmpty() && score > 5) {
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
                        
                        Thread.sleep(500); // Rate limiting
                        
                    } catch (Exception e) {
                        logger.warn("Error fetching from subreddit {}: {}", subreddit, e.getMessage());
                    }
                }
            }
            
        } catch (Exception e) {
            logger.error("Error fetching from Reddit for topic: {}", topic.getName(), e);
        }
        
        return newsItems;
    }
    
    /**
     * Fetch from Hacker News
     */
    private List<NewsItem> fetchFromHackerNews(Topic topic) {
        List<NewsItem> newsItems = new ArrayList<>();
        
        try {
            String url = "https://hacker-news.firebaseio.com/v0/topstories.json";
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            
            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode storyIds = objectMapper.readTree(response.getBody());
                
                for (int i = 0; i < Math.min(10, storyIds.size()); i++) {
                    int storyId = storyIds.get(i).asInt();
                    String storyUrl = "https://hacker-news.firebaseio.com/v0/item/" + storyId + ".json";
                    
                    ResponseEntity<String> storyResponse = restTemplate.getForEntity(storyUrl, String.class);
                    
                    if (storyResponse.getStatusCode() == HttpStatus.OK) {
                        JsonNode story = objectMapper.readTree(storyResponse.getBody());
                        
                        String title = story.path("title").asText();
                        String url_link = story.path("url").asText();
                        int score = story.path("score").asInt();
                        long time = story.path("time").asLong() * 1000;
                        
                        if (isRelevantToTopic(title, topic) && score > 20) {
                            NewsItem item = new NewsItem();
                            item.setTitle(title);
                            item.setContent("");
                            item.setUrl(url_link);
                            item.setScore(score);
                            item.setPublishedAt(new Date(time));
                            item.setSource("Hacker News");
                            
                            newsItems.add(item);
                        }
                    }
                    
                    Thread.sleep(100);
                }
            }
            
        } catch (Exception e) {
            logger.error("Error fetching from Hacker News for topic: {}", topic.getName(), e);
        }
        
        return newsItems;
    }
    
    /**
     * Fetch from RSS feeds
     */
    private List<NewsItem> fetchFromRSSFeeds(Topic topic) {
        List<NewsItem> newsItems = new ArrayList<>();
        
        try {
            // Popular RSS feeds
            String[] rssFeeds = {
                "https://feeds.bbci.co.uk/news/technology/rss.xml",
                "https://rss.cnn.com/rss/edition_technology.rss",
                "https://feeds.feedburner.com/oreilly/radar",
                "https://www.theverge.com/rss/index.xml",
                "https://feeds.feedburner.com/venturebeat/SZYF",
                "https://feeds.feedburner.com/TechCrunch/",
                "https://feeds.feedburner.com/oreilly/radar"
            };
            
            for (String feedUrl : rssFeeds) {
                try {
                    ResponseEntity<String> response = restTemplate.getForEntity(feedUrl, String.class);
                    
                    if (response.getStatusCode() == HttpStatus.OK) {
                        // Simple RSS parsing (you might want to use a proper RSS parser)
                        String content = response.getBody();
                        // Parse RSS content and extract relevant items
                        // This is a simplified version - you'd want proper RSS parsing
                    }
                    
                    Thread.sleep(1000);
                    
                } catch (Exception e) {
                    logger.warn("Error fetching RSS feed {}: {}", feedUrl, e.getMessage());
                }
            }
            
        } catch (Exception e) {
            logger.error("Error fetching from RSS feeds for topic: {}", topic.getName(), e);
        }
        
        return newsItems;
    }
    
    /**
     * Fetch from Google News (using RSS)
     */
    private List<NewsItem> fetchFromGoogleNews(Topic topic) {
        List<NewsItem> newsItems = new ArrayList<>();
        
        try {
            String[] keywords = topic.getKeywordsArray();
            
            for (String keyword : keywords) {
                String url = String.format(
                    "https://news.google.com/rss/search?q=%s&hl=en-US&gl=US&ceid=US:en",
                    keyword.trim()
                );
                
                ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
                
                if (response.getStatusCode() == HttpStatus.OK) {
                    // Parse Google News RSS
                    String content = response.getBody();
                    // Extract news items from RSS content
                    // This is simplified - you'd want proper RSS parsing
                }
                
                Thread.sleep(1000);
            }
            
        } catch (Exception e) {
            logger.error("Error fetching from Google News for topic: {}", topic.getName(), e);
        }
        
        return newsItems;
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
     * Remove duplicate news items
     */
    private List<NewsItem> removeDuplicates(List<NewsItem> newsItems) {
        return newsItems.stream()
            .collect(Collectors.toMap(
                NewsItem::getTitle,
                item -> item,
                (existing, replacement) -> existing.getScore() > replacement.getScore() ? existing : replacement
            ))
            .values()
            .stream()
            .collect(Collectors.toList());
    }
    
    /**
     * Parse date string
     */
    private Date parseDate(String dateString) {
        try {
            // Handle different date formats
            return new Date(); // Simplified - you'd want proper date parsing
        } catch (Exception e) {
            return new Date();
        }
    }
}
