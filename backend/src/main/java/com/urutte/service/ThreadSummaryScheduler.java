package com.urutte.service;

import com.urutte.model.Topic;
import com.urutte.model.Thread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.Optional;

@Service
public class ThreadSummaryScheduler {
    
    private static final Logger logger = LoggerFactory.getLogger(ThreadSummaryScheduler.class);
    
    @Autowired
    private TopicService topicService;
    
    @Autowired
    private ThreadSummaryService threadSummaryService;
    
    /**
     * Scheduled task to generate thread summaries every 2 hours
     * Adjusted for developer key limits: 500 calls/day, 1 call/second
     * This replaces the AI content generation with real-time news summaries
     */
    @Scheduled(fixedRate = 2 * 60 * 60 * 1000) // 2 hours in milliseconds
    public void generateScheduledThreadSummaries() {
        logger.info("Starting scheduled thread summary generation...");
        
        try {
            // Get all active topics from database
            List<Topic> activeTopics = topicService.getAllActiveTopics();
            
            if (activeTopics.isEmpty()) {
                logger.warn("No active topics found in database");
                return;
            }
            
            logger.info("Found {} active topics for summary generation", activeTopics.size());
            
            // Generate summaries for each topic sequentially to respect rate limits
            // Developer key: 1 call per second, 500 calls per day
            for (int i = 0; i < activeTopics.size(); i++) {
                Topic topic = activeTopics.get(i);
                try {
                    logger.info("Processing topic {}/{}: {}", i + 1, activeTopics.size(), topic.getName());
                    generateSummariesForTopic(topic);
                    
                    // Add delay between topics to respect rate limits (except for last topic)
                    if (i < activeTopics.size() - 1) {
                        logger.info("Waiting 2 seconds before processing next topic...");
                        java.lang.Thread.sleep(2000); // 2 second delay between topics
                    }
                } catch (Exception e) {
                    logger.error("Error generating summaries for topic: {}", topic.getName(), e);
                }
            }
            
            logger.info("Completed scheduled thread summary generation for all topics");
            
        } catch (Exception e) {
            logger.error("Error in scheduled thread summary generation", e);
        }
    }
    
    /**
     * Generate summaries for a specific topic
     */
    private void generateSummariesForTopic(Topic topic) {
        try {
            logger.info("Generating thread summaries for topic: {}", topic.getName());
            
            // Generate 3 summary posts for this topic
            List<Thread> summaryThreads = threadSummaryService.generateSummaryThread(topic, 3);
            
            if (summaryThreads.isEmpty()) {
                logger.warn("No summary threads generated for topic: {}", topic.getName());
                return;
            }
            
            logger.info("Successfully generated {} summary threads for topic: {}", 
                       summaryThreads.size(), topic.getName());
            
            // Update topic's last generated timestamp
            topic.updateLastGeneratedAt();
            topicService.updateTopic(
                topic.getId(),
                topic.getName(),
                topic.getDescription(),
                topic.getAiPrompt(),
                topic.getCategory(),
                topic.getKeywords(),
                topic.getPriority(),
                topic.getThreadsPerRun(),
                topic.getLastGeneratedAt()
            );
            
        } catch (Exception e) {
            logger.error("Error generating summaries for topic: {}", topic.getName(), e);
        }
    }
    
    /**
     * Manual trigger for thread summary generation (for testing)
     */
    public void generateThreadSummariesNow() {
        logger.info("Manual trigger: Starting thread summary generation...");
        generateScheduledThreadSummaries();
    }
    
    /**
     * Generate summaries for a specific topic (manual trigger)
     */
    public void generateSummariesForTopicNow(String topicName) {
        try {
            var topicOpt = topicService.getTopicByName(topicName);
            if (topicOpt.isEmpty()) {
                logger.warn("Topic not found: {}", topicName);
                return;
            }
            
            Topic topic = topicOpt.get();
            generateSummariesForTopic(topic);
            
        } catch (Exception e) {
            logger.error("Error generating summaries for topic: {}", topicName, e);
        }
    }
}
