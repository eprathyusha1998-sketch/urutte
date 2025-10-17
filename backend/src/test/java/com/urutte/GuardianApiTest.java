package com.urutte;

import com.urutte.service.GuardianNewsService;
import com.urutte.model.Topic;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@SpringBootTest
@TestPropertySource(properties = {
    "app.guardian.api-key=test-key",
    "app.guardian.base-url=https://content.guardianapis.com"
})
public class GuardianApiTest {
    
    @Autowired
    private GuardianNewsService guardianNewsService;
    
    @Test
    public void testGuardianApiConnection() {
        // Test API connection
        boolean isConnected = guardianNewsService.testApiConnection();
        System.out.println("Guardian API Connection: " + isConnected);
        
        if (isConnected) {
            // Test news fetching for India topic
            Topic indiaTopic = new Topic();
            indiaTopic.setName("India News");
            indiaTopic.setKeywords("India,politics,economy,social issues,national news");
            
            List<com.urutte.service.ContentGenerationService.NewsItem> newsItems = 
                guardianNewsService.fetchNewsForTopic(indiaTopic);
            
            System.out.println("Fetched " + newsItems.size() + " news items for India topic");
            
            // Print first few items
            newsItems.stream().limit(3).forEach(item -> {
                System.out.println("Title: " + item.getTitle());
                System.out.println("URL: " + item.getUrl());
                System.out.println("Score: " + item.getScore());
                System.out.println("---");
            });
        }
    }
}
