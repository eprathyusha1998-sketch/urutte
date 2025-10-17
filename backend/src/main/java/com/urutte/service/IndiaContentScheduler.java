package com.urutte.service;

import com.urutte.model.Topic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class IndiaContentScheduler {
    
    private static final Logger logger = LoggerFactory.getLogger(IndiaContentScheduler.class);
    
    @Autowired
    private ContentGenerationService contentGenerationService;
    
    @Autowired
    private AiAdminService aiAdminService;
    
    @Autowired
    private TopicService topicService;
    
    // India-focused topic keywords that should generate content hourly
    private static final List<String> INDIA_TOPIC_KEYWORDS = Arrays.asList(
        "India", "Tamil Nadu", "Cricket", "Bollywood", "South Indian"
    );
    
    /**
     * Run content generation every hour for India-focused topics
     * Cron expression: 0 0 * * * * (every hour at minute 0)
     */
    @Scheduled(cron = "0 0 * * * *")
    public void generateIndiaContentHourly() {
        logger.info("Starting hourly India content generation");
        
        try {
            // Ensure AI Admin exists
            aiAdminService.ensureAiAdminExists();
            
            // Generate content for India-focused topics
            generateContentForIndiaTopics();
            
            logger.info("Hourly India content generation completed successfully");
            
        } catch (Exception e) {
            logger.error("Error in hourly India content generation", e);
        }
    }
    
    /**
     * Generate content for India-focused topics
     */
    private void generateContentForIndiaTopics() {
        logger.info("Generating content for India-focused topics");
        
        // Get all active topics and filter for India-related ones
        List<Topic> allTopics = topicService.getAllActiveTopics();
        List<Topic> indiaTopics = allTopics.stream()
            .filter(topic -> INDIA_TOPIC_KEYWORDS.stream()
                .anyMatch(keyword -> topic.getName().toLowerCase().contains(keyword.toLowerCase()) ||
                                   topic.getKeywords().toLowerCase().contains(keyword.toLowerCase())))
            .collect(java.util.stream.Collectors.toList());
        
        logger.info("Found {} India-focused topics: {}", 
                   indiaTopics.size(), 
                   indiaTopics.stream().map(Topic::getName).collect(java.util.stream.Collectors.toList()));
        
        for (Topic topic : indiaTopics) {
            try {
                logger.info("Generating content for topic: {}", topic.getName());
                aiAdminService.getActiveAiAdmin().ifPresent(aiAdmin -> {
                    contentGenerationService.generateContentForTopic(topic, aiAdmin);
                });
            } catch (Exception e) {
                logger.error("Error generating content for topic: {}", topic.getName(), e);
            }
        }
    }
    
    /**
     * Generate content for India topics immediately (for testing or manual triggers)
     */
    public void generateIndiaContentNow() {
        logger.info("Manual India content generation triggered");
        
        try {
            // Ensure AI Admin exists
            aiAdminService.ensureAiAdminExists();
            
            // Generate content for India-focused topics
            generateContentForIndiaTopics();
            
            logger.info("Manual India content generation completed successfully");
            
        } catch (Exception e) {
            logger.error("Error in manual India content generation", e);
        }
    }
}
