package com.urutte.service;

import com.urutte.service.ContentGenerationService.NewsItem;
import com.urutte.model.Topic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class TrendingNewsService {
    
    private static final Logger logger = LoggerFactory.getLogger(TrendingNewsService.class);
    
    /**
     * Generate trending news items for a topic without external APIs
     */
    public List<NewsItem> generateTrendingNews(Topic topic) {
        List<NewsItem> newsItems = new ArrayList<>();
        
        try {
            // Generate news items based on topic keywords and current trends
            String[] keywords = topic.getKeywordsArray();
            
            for (String keyword : keywords) {
                List<NewsItem> items = generateNewsForKeyword(keyword, topic);
                newsItems.addAll(items);
            }
            
            // Limit to top 10 items
            newsItems.sort((a, b) -> Integer.compare(b.getScore(), a.getScore()));
            newsItems = newsItems.subList(0, Math.min(10, newsItems.size()));
            
            logger.info("Generated {} trending news items for topic: {}", newsItems.size(), topic.getName());
            
        } catch (Exception e) {
            logger.error("Error generating trending news for topic: {}", topic.getName(), e);
        }
        
        return newsItems;
    }
    
    /**
     * Generate news items for a specific keyword
     */
    private List<NewsItem> generateNewsForKeyword(String keyword, Topic topic) {
        List<NewsItem> items = new ArrayList<>();
        
        // Generate multiple news items based on the keyword
        String[] newsTemplates = getNewsTemplates(keyword, topic.getCategory());
        
        for (String template : newsTemplates) {
            NewsItem item = new NewsItem();
            item.setTitle(template);
            item.setContent(generateContentForTitle(template, keyword));
            item.setUrl(generateUrlForTitle(template));
            item.setScore(50 + (int)(Math.random() * 50)); // Random score 50-100
            item.setPublishedAt(new Date());
            item.setSource("Trending News Generator");
            
            items.add(item);
        }
        
        return items;
    }
    
    /**
     * Get news templates based on keyword and category
     */
    private String[] getNewsTemplates(String keyword, String category) {
        Map<String, String[]> templates = new HashMap<>();
        
        // Technology templates
        templates.put("Technology", new String[]{
            "Latest breakthrough in " + keyword + " technology",
            "New " + keyword + " framework released",
            "Major update to " + keyword + " platform",
            "Security vulnerability found in " + keyword,
            "Startup raises millions for " + keyword + " innovation"
        });
        
        // News templates
        templates.put("News", new String[]{
            "Breaking: Major development in " + keyword,
            "Government announces new " + keyword + " policy",
            "International " + keyword + " summit concludes",
            "Economic impact of " + keyword + " changes",
            "Public opinion shifts on " + keyword + " issue"
        });
        
        // Sports templates
        templates.put("Sports", new String[]{
            "Exciting " + keyword + " match results",
            "New " + keyword + " tournament announced",
            "Player transfer news in " + keyword,
            "Controversy in " + keyword + " league",
            "Upcoming " + keyword + " championship"
        });
        
        // Entertainment templates
        templates.put("Entertainment", new String[]{
            "New " + keyword + " movie release",
            "Celebrity news in " + keyword + " industry",
            "Box office success for " + keyword + " film",
            "Award nominations for " + keyword + " content",
            "Streaming platform adds " + keyword + " content"
        });
        
        // Business templates
        templates.put("Business", new String[]{
            "Market trends in " + keyword + " sector",
            "New " + keyword + " business model",
            "Investment opportunities in " + keyword,
            "Merger and acquisition in " + keyword + " industry",
            "Startup funding for " + keyword + " companies"
        });
        
        // Science templates
        templates.put("Science", new String[]{
            "Scientific breakthrough in " + keyword + " research",
            "New study reveals " + keyword + " findings",
            "Medical advancement in " + keyword + " treatment",
            "Environmental impact of " + keyword + " technology",
            "Space exploration and " + keyword + " discovery"
        });
        
        return templates.getOrDefault(category, new String[]{
            "Latest updates on " + keyword,
            "New developments in " + keyword,
            "Trending news about " + keyword,
            "Recent changes in " + keyword + " industry",
            "Breaking news: " + keyword + " update"
        });
    }
    
    /**
     * Generate content for a news title
     */
    private String generateContentForTitle(String title, String keyword) {
        String[] contentTemplates = {
            "This development in " + keyword + " represents a significant milestone in the industry. Experts believe this could have far-reaching implications for the future of " + keyword + " technology and applications.",
            
            "The announcement has generated significant interest among " + keyword + " professionals and enthusiasts. Industry leaders are closely watching how this will impact the competitive landscape.",
            
            "This news comes at a time when the " + keyword + " sector is experiencing rapid growth and transformation. Stakeholders are optimistic about the potential benefits this could bring.",
            
            "The development has sparked discussions among " + keyword + " communities worldwide. Many are excited about the possibilities this opens up for innovation and progress.",
            
            "Industry analysts are predicting that this could be a game-changer for " + keyword + ". The implications extend beyond immediate benefits to long-term strategic advantages."
        };
        
        return contentTemplates[(int)(Math.random() * contentTemplates.length)];
    }
    
    /**
     * Generate a URL for a news title
     */
    private String generateUrlForTitle(String title) {
        // Generate a realistic-looking URL
        String baseUrl = "https://news.example.com/";
        String slug = title.toLowerCase()
            .replaceAll("[^a-z0-9\\s]", "")
            .replaceAll("\\s+", "-")
            .substring(0, Math.min(50, title.length()));
        
        return baseUrl + slug + "-" + System.currentTimeMillis();
    }
}
