package com.urutte.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class AiContentScheduler {
    
    private static final Logger logger = LoggerFactory.getLogger(AiContentScheduler.class);
    
    @Autowired
    private ContentGenerationService contentGenerationService;
    
    @Autowired
    private AiAdminService aiAdminService;
    
    /**
     * Run content generation every 4 hours
     * Cron expression: 0 0 *\/4 * * * (every 4 hours at minute 0)
     */
    @Scheduled(cron = "0 0 */4 * * *")
    public void generateContentScheduled() {
        logger.info("Starting scheduled AI content generation");
        
        try {
            // Ensure AI Admin exists
            aiAdminService.ensureAiAdminExists();
            
            // Generate content for all topics
            contentGenerationService.generateContentForAllTopics();
            
            logger.info("Scheduled AI content generation completed successfully");
            
        } catch (Exception e) {
            logger.error("Error in scheduled AI content generation", e);
        }
    }
    
    /**
     * Run content generation every 2 hours for high-priority topics
     * Cron expression: 0 0 *\/2 * * * (every 2 hours at minute 0)
     */
    @Scheduled(cron = "0 0 */2 * * *")
    public void generateHighPriorityContent() {
        logger.info("Starting high-priority AI content generation");
        
        try {
            // Ensure AI Admin exists
            aiAdminService.ensureAiAdminExists();
            
            // Generate content for high-priority topics only
            contentGenerationService.generateHighPriorityContent();
            
            logger.info("High-priority AI content generation completed successfully");
            
        } catch (Exception e) {
            logger.error("Error in high-priority AI content generation", e);
        }
    }
    
    /**
     * Clean up old AI-generated content (run daily at 2 AM)
     * Cron expression: 0 0 2 daily at 2 AM
     */
    @Scheduled(cron = "0 0 2 * * *")
    public void cleanupOldContent() {
        logger.info("Starting cleanup of old AI-generated content");
        
        try {
            contentGenerationService.cleanupOldContent();
            logger.info("Cleanup of old AI-generated content completed successfully");
            
        } catch (Exception e) {
            logger.error("Error in cleanup of old AI-generated content", e);
        }
    }
    
    /**
     * Generate content immediately (for testing or manual triggers)
     */
    public void generateContentNow() {
        logger.info("Manual AI content generation triggered");
        
        try {
            // Ensure AI Admin exists
            aiAdminService.ensureAiAdminExists();
            
            // Generate content for all topics
            contentGenerationService.generateContentForAllTopics();
            
            logger.info("Manual AI content generation completed successfully");
            
        } catch (Exception e) {
            logger.error("Error in manual AI content generation", e);
        }
    }
}
