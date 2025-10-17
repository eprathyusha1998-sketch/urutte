package com.urutte.service;

import com.urutte.service.ContentGenerationService.NewsItem;
import com.urutte.model.Topic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientException;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
public class NewsScrapingService {
    
    private static final Logger logger = LoggerFactory.getLogger(NewsScrapingService.class);
    
    @Autowired
    private RestTemplate restTemplate;
    
    // RSS Feed URLs for different news sources
    private static final Map<String, String[]> NEWS_SOURCES = new HashMap<>();
    
    static {
        // India News Sources
        NEWS_SOURCES.put("India", new String[]{
            "https://timesofindia.indiatimes.com/rssfeeds/-2128936835.cms", // Times of India - India
            "https://www.hindustantimes.com/rss/india/rssfeed.xml", // Hindustan Times - India
            "https://feeds.feedburner.com/ndtvnews-india-news", // NDTV India
            "https://www.thehindu.com/news/national/feeder/default.rss", // The Hindu - National
            "https://www.indiatoday.in/rss/1206514", // India Today - India
            "https://www.firstpost.com/rss/india.xml" // Firstpost - India
        });
        
        // Tamil Nadu News Sources
        NEWS_SOURCES.put("Tamil Nadu", new String[]{
            "https://www.thehindu.com/news/cities/chennai/feeder/default.rss", // The Hindu - Chennai
            "https://www.dinamalar.com/rss.asp", // Dinamalar Tamil News
            "https://www.dinakaran.com/rss.asp", // Dinakaran Tamil News
            "https://www.vikatan.com/rss.xml" // Vikatan Tamil News
        });
        
        // Cricket News Sources
        NEWS_SOURCES.put("Cricket", new String[]{
            "https://feeds.feedburner.com/ndtvnews-sports-news", // NDTV Sports
            "https://www.espncricinfo.com/rss/content/story/feeds/6.xml", // ESPN Cricinfo
            "https://www.cricbuzz.com/rss-feeds", // Cricbuzz
            "https://timesofindia.indiatimes.com/rssfeeds/4719148.cms" // TOI Sports
        });
        
        // Bollywood News Sources
        NEWS_SOURCES.put("Bollywood", new String[]{
            "https://feeds.feedburner.com/ndtvnews-entertainment-news", // NDTV Entertainment
            "https://timesofindia.indiatimes.com/rssfeeds/1081479906.cms", // TOI Entertainment
            "https://www.hindustantimes.com/rss/entertainment/rssfeed.xml", // HT Entertainment
            "https://www.bollywoodhungama.com/rss.xml" // Bollywood Hungama
        });
        
        // Global News Sources
        NEWS_SOURCES.put("Global", new String[]{
            "http://feeds.bbci.co.uk/news/rss.xml", // BBC News
            "https://feeds.reuters.com/reuters/topNews", // Reuters Top News
            "http://rss.cnn.com/rss/edition.rss", // CNN International
            "https://feeds.npr.org/1001/rss.xml", // NPR News
            "https://feeds.feedburner.com/ndtvnews-top-stories" // NDTV Top Stories
        });
        
        // Technology News Sources
        NEWS_SOURCES.put("Technology", new String[]{
            "https://feeds.feedburner.com/oreilly/radar", // O'Reilly Radar
            "https://techcrunch.com/feed/", // TechCrunch
            "https://feeds.feedburner.com/ndtvnews-technology-news", // NDTV Technology
            "https://timesofindia.indiatimes.com/rssfeeds/66949542.cms" // TOI Technology
        });
    }
    
    /**
     * Scrape news for a specific topic
     */
    public List<NewsItem> scrapeNewsForTopic(Topic topic) {
        List<NewsItem> newsItems = new ArrayList<>();
        
        try {
            String topicName = topic.getName().toLowerCase();
            String keywords = topic.getKeywords().toLowerCase();
            
            // Determine which news sources to use based on topic
            List<String> sourcesToUse = determineNewsSources(topicName, keywords);
            
            for (String source : sourcesToUse) {
                try {
                    List<NewsItem> items = scrapeRSSFeed(source, topic);
                    newsItems.addAll(items);
                    logger.info("Scraped {} items from source: {}", items.size(), source);
                } catch (Exception e) {
                    logger.warn("Failed to scrape source {}: {}", source, e.getMessage());
                }
            }
            
            // Sort by recency and relevance
            newsItems.sort((a, b) -> {
                int scoreComparison = Integer.compare(b.getScore(), a.getScore());
                if (scoreComparison != 0) return scoreComparison;
                return b.getPublishedAt().compareTo(a.getPublishedAt());
            });
            
            logger.info("Total scraped {} news items for topic: {}", newsItems.size(), topic.getName());
            
        } catch (Exception e) {
            logger.error("Error scraping news for topic: {}", topic.getName(), e);
        }
        
        return newsItems;
    }
    
    /**
     * Determine which news sources to use based on topic
     */
    private List<String> determineNewsSources(String topicName, String keywords) {
        List<String> sources = new ArrayList<>();
        
        // Add India sources if topic is India-related
        if (topicName.contains("india") || topicName.contains("indian") || keywords.contains("india")) {
            sources.addAll(Arrays.asList(NEWS_SOURCES.get("India")));
        }
        
        // Add Tamil Nadu sources if topic is Tamil-related
        if (topicName.contains("tamil") || keywords.contains("tamil")) {
            sources.addAll(Arrays.asList(NEWS_SOURCES.get("Tamil Nadu")));
        }
        
        // Add Cricket sources if topic is cricket-related
        if (topicName.contains("cricket") || keywords.contains("cricket")) {
            sources.addAll(Arrays.asList(NEWS_SOURCES.get("Cricket")));
        }
        
        // Add Bollywood sources if topic is entertainment-related
        if (topicName.contains("bollywood") || topicName.contains("entertainment") || 
            keywords.contains("bollywood") || keywords.contains("entertainment")) {
            sources.addAll(Arrays.asList(NEWS_SOURCES.get("Bollywood")));
        }
        
        // Add Technology sources if topic is tech-related
        if (topicName.contains("technology") || topicName.contains("tech") || 
            keywords.contains("technology") || keywords.contains("tech")) {
            sources.addAll(Arrays.asList(NEWS_SOURCES.get("Technology")));
        }
        
        // Always add some global sources for broader coverage
        sources.addAll(Arrays.asList(NEWS_SOURCES.get("Global")));
        
        return sources;
    }
    
    /**
     * Scrape RSS feed
     */
    private List<NewsItem> scrapeRSSFeed(String feedUrl, Topic topic) {
        List<NewsItem> items = new ArrayList<>();
        
        try {
            String rssContent = restTemplate.getForObject(feedUrl, String.class);
            if (rssContent != null) {
                items = parseRSSContent(rssContent, topic);
            }
        } catch (RestClientException e) {
            logger.warn("Failed to fetch RSS feed from {}: {}", feedUrl, e.getMessage());
        }
        
        return items;
    }
    
    /**
     * Parse RSS content
     */
    private List<NewsItem> parseRSSContent(String rssContent, Topic topic) {
        List<NewsItem> items = new ArrayList<>();
        
        try {
            // Simple RSS parsing using regex (for production, consider using a proper RSS parser)
            Pattern itemPattern = Pattern.compile("<item>(.*?)</item>", Pattern.DOTALL);
            Matcher itemMatcher = itemPattern.matcher(rssContent);
            
            while (itemMatcher.find() && items.size() < 10) { // Limit to 10 items per source
                String itemContent = itemMatcher.group(1);
                
                NewsItem item = parseRSSItem(itemContent, topic);
                if (item != null) {
                    items.add(item);
                }
            }
            
        } catch (Exception e) {
            logger.error("Error parsing RSS content: {}", e.getMessage());
        }
        
        return items;
    }
    
    /**
     * Parse individual RSS item
     */
    private NewsItem parseRSSItem(String itemContent, Topic topic) {
        try {
            NewsItem item = new NewsItem();
            
            // Extract title
            Pattern titlePattern = Pattern.compile("<title><!\\[CDATA\\[(.*?)\\]\\]></title>|<title>(.*?)</title>");
            Matcher titleMatcher = titlePattern.matcher(itemContent);
            if (titleMatcher.find()) {
                String title = titleMatcher.group(1) != null ? titleMatcher.group(1) : titleMatcher.group(2);
                item.setTitle(cleanHtml(title));
            } else {
                return null; // Skip items without title
            }
            
            // Extract description/content
            Pattern descPattern = Pattern.compile("<description><!\\[CDATA\\[(.*?)\\]\\]></description>|<description>(.*?)</description>");
            Matcher descMatcher = descPattern.matcher(itemContent);
            if (descMatcher.find()) {
                String description = descMatcher.group(1) != null ? descMatcher.group(1) : descMatcher.group(2);
                item.setContent(cleanHtml(description));
            } else {
                item.setContent(item.getTitle()); // Use title as content if no description
            }
            
            // Extract link
            Pattern linkPattern = Pattern.compile("<link>(.*?)</link>");
            Matcher linkMatcher = linkPattern.matcher(itemContent);
            if (linkMatcher.find()) {
                item.setUrl(linkMatcher.group(1));
            } else {
                item.setUrl("https://news.source.com/" + System.currentTimeMillis());
            }
            
            // Extract publication date
            Pattern datePattern = Pattern.compile("<pubDate>(.*?)</pubDate>");
            Matcher dateMatcher = datePattern.matcher(itemContent);
            if (dateMatcher.find()) {
                try {
                    // Simple date parsing (for production, use proper date parsing)
                    item.setPublishedAt(new Date());
                } catch (Exception e) {
                    item.setPublishedAt(new Date());
                }
            } else {
                item.setPublishedAt(new Date());
            }
            
            // Set score based on relevance to topic
            item.setScore(calculateRelevanceScore(item, topic));
            item.setSource("RSS Feed Scraper");
            
            return item;
            
        } catch (Exception e) {
            logger.error("Error parsing RSS item: {}", e.getMessage());
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
        String topicName = topic.getName().toLowerCase();
        if (title.contains(topicName)) {
            score += 15;
        }
        if (content.contains(topicName)) {
            score += 10;
        }
        
        // Ensure score is within bounds
        return Math.min(100, Math.max(0, score));
    }
    
    /**
     * Clean HTML tags from text
     */
    private String cleanHtml(String html) {
        if (html == null) return "";
        
        // Remove HTML tags
        html = html.replaceAll("<[^>]+>", "");
        
        // Decode HTML entities
        html = html.replace("&amp;", "&");
        html = html.replace("&lt;", "<");
        html = html.replace("&gt;", ">");
        html = html.replace("&quot;", "\"");
        html = html.replace("&#39;", "'");
        html = html.replace("&nbsp;", " ");
        
        // Clean up extra whitespace
        html = html.replaceAll("\\s+", " ").trim();
        
        return html;
    }
    
    /**
     * Get news from specific category
     */
    public List<NewsItem> getNewsByCategory(String category) {
        List<NewsItem> newsItems = new ArrayList<>();
        
        String[] sources = NEWS_SOURCES.get(category);
        if (sources != null) {
            for (String source : sources) {
                try {
                    List<NewsItem> items = scrapeRSSFeed(source, createDummyTopic(category));
                    newsItems.addAll(items);
                } catch (Exception e) {
                    logger.warn("Failed to scrape source {}: {}", source, e.getMessage());
                }
            }
        }
        
        return newsItems;
    }
    
    /**
     * Create a dummy topic for category-based scraping
     */
    private Topic createDummyTopic(String category) {
        Topic topic = new Topic();
        topic.setName(category);
        topic.setKeywords(category.toLowerCase());
        return topic;
    }
}
