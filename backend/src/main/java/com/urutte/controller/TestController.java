package com.urutte.controller;

import com.urutte.service.NewsScrapingService;
import com.urutte.service.IndiaNewsService;
import com.urutte.service.GuardianNewsService;
import com.urutte.service.ContentGenerationService.NewsItem;
import com.urutte.model.Topic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
public class TestController {
    
    @Autowired
    private NewsScrapingService newsScrapingService;
    
    @Autowired
    private IndiaNewsService indiaNewsService;
    
    @Autowired
    private GuardianNewsService guardianNewsService;
    
    @GetMapping("/scrape/{category}")
    public ResponseEntity<Map<String, Object>> testScraping(@PathVariable String category) {
        try {
            List<NewsItem> newsItems = newsScrapingService.getNewsByCategory(category);
            
            Map<String, Object> response = new HashMap<>();
            response.put("category", category);
            response.put("count", newsItems.size());
            response.put("newsItems", newsItems.stream().limit(3).map(item -> {
                Map<String, Object> itemMap = new HashMap<>();
                itemMap.put("title", item.getTitle());
                itemMap.put("content", item.getContent().length() > 150 ? 
                    item.getContent().substring(0, 150) + "..." : item.getContent());
                itemMap.put("url", item.getUrl());
                itemMap.put("score", item.getScore());
                itemMap.put("source", item.getSource());
                return itemMap;
            }).toList());
            response.put("status", "success");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Error testing news scraping: " + e.getMessage());
            response.put("status", "error");
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    @GetMapping("/india-news")
    public ResponseEntity<Map<String, Object>> testIndiaNews() {
        try {
            // Create a dummy India topic
            Topic indiaTopic = new Topic();
            indiaTopic.setName("India Top News");
            indiaTopic.setKeywords("India,politics,economy,social issues,national news");
            
            List<NewsItem> newsItems = indiaNewsService.generateIndiaNews(indiaTopic);
            
            Map<String, Object> response = new HashMap<>();
            response.put("topic", "India Top News");
            response.put("count", newsItems.size());
            response.put("newsItems", newsItems.stream().limit(3).map(item -> {
                Map<String, Object> itemMap = new HashMap<>();
                itemMap.put("title", item.getTitle());
                itemMap.put("content", item.getContent().length() > 150 ? 
                    item.getContent().substring(0, 150) + "..." : item.getContent());
                itemMap.put("url", item.getUrl());
                itemMap.put("score", item.getScore());
                itemMap.put("source", item.getSource());
                return itemMap;
            }).toList());
            response.put("status", "success");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Error testing India news: " + e.getMessage());
            response.put("status", "error");
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    @GetMapping("/guardian")
    public ResponseEntity<Map<String, Object>> testGuardianApi() {
        try {
            // Test API connection first
            boolean isConnected = guardianNewsService.testApiConnection();
            
            Map<String, Object> response = new HashMap<>();
            response.put("api_connection", isConnected);
            
            if (isConnected) {
                // Create a dummy topic to test news fetching
                Topic testTopic = new Topic();
                testTopic.setName("India News");
                testTopic.setKeywords("India,politics,economy,social issues,national news");
                
                List<NewsItem> newsItems = guardianNewsService.fetchNewsForTopic(testTopic);
                
                response.put("topic", "India News");
                response.put("count", newsItems.size());
                response.put("newsItems", newsItems.stream().limit(3).map(item -> {
                    Map<String, Object> itemMap = new HashMap<>();
                    itemMap.put("title", item.getTitle());
                    itemMap.put("content", item.getContent().length() > 150 ? 
                        item.getContent().substring(0, 150) + "..." : item.getContent());
                    itemMap.put("url", item.getUrl());
                    itemMap.put("score", item.getScore());
                    itemMap.put("source", item.getSource());
                    return itemMap;
                }).toList());
            } else {
                response.put("message", "Guardian API connection failed. Check API key configuration.");
            }
            
            response.put("status", "success");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Error testing Guardian API: " + e.getMessage());
            response.put("status", "error");
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    @GetMapping("/guardian/{topicName}")
    public ResponseEntity<Map<String, Object>> testGuardianForTopic(@PathVariable String topicName) {
        try {
            // Create a topic based on the path parameter
            Topic testTopic = new Topic();
            testTopic.setName(topicName);
            
            // Set appropriate keywords based on topic
            switch (topicName.toLowerCase()) {
                case "india":
                    testTopic.setKeywords("India,politics,economy,social issues,national news");
                    break;
                case "cricket":
                    testTopic.setKeywords("cricket,IPL,BCCI,Indian cricket,international cricket");
                    break;
                case "bollywood":
                    testTopic.setKeywords("Bollywood,Hindi movies,celebrity news,film industry");
                    break;
                case "technology":
                    testTopic.setKeywords("technology,AI,software,tech news,innovation");
                    break;
                default:
                    testTopic.setKeywords(topicName + ",news,updates");
            }
            
            List<NewsItem> newsItems = guardianNewsService.fetchNewsForTopic(testTopic);
            
            Map<String, Object> response = new HashMap<>();
            response.put("topic", topicName);
            response.put("count", newsItems.size());
            response.put("newsItems", newsItems.stream().limit(5).map(item -> {
                Map<String, Object> itemMap = new HashMap<>();
                itemMap.put("title", item.getTitle());
                itemMap.put("content", item.getContent().length() > 150 ? 
                    item.getContent().substring(0, 150) + "..." : item.getContent());
                itemMap.put("url", item.getUrl());
                itemMap.put("score", item.getScore());
                itemMap.put("source", item.getSource());
                return itemMap;
            }).toList());
            response.put("status", "success");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Error testing Guardian API for topic " + topicName + ": " + e.getMessage());
            response.put("status", "error");
            return ResponseEntity.internalServerError().body(response);
        }
    }
}
