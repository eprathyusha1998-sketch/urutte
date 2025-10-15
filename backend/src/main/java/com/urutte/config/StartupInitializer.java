package com.urutte.config;

import com.urutte.service.AiAdminService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class StartupInitializer implements CommandLineRunner {
    
    private static final Logger logger = LoggerFactory.getLogger(StartupInitializer.class);
    
    @Autowired
    private AiAdminService aiAdminService;
    
    @Override
    public void run(String... args) throws Exception {
        logger.info("Starting application initialization...");
        
        try {
            // Ensure AI Admin exists and has a password
            aiAdminService.ensureAiAdminExists();
            logger.info("AI Admin initialization completed successfully");
            
        } catch (Exception e) {
            logger.error("Error during AI Admin initialization", e);
        }
        
        logger.info("Application initialization completed");
    }
}
