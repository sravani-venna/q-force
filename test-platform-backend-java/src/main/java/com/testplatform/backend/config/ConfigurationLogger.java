package com.testplatform.backend.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class ConfigurationLogger {
    
    private static final Logger logger = LoggerFactory.getLogger(ConfigurationLogger.class);
    
    @Autowired
    private AppProperties appProperties;
    
    @EventListener(ApplicationReadyEvent.class)
    public void logConfiguration() {
        logger.info("=== Application Configuration ===");
        logger.info("API Prefix: {}", appProperties.getApiPrefix());
        
        if (appProperties.getGithub() != null) {
            String token = appProperties.getGithub().getToken();
            if (token != null && !token.isEmpty()) {
                logger.info("✅ GitHub Token: Configured (length: {} chars)", token.length());
                logger.info("✅ GitHub Token prefix: {}...", token.substring(0, Math.min(10, token.length())));
            } else {
                logger.warn("⚠️ GitHub Token: NOT CONFIGURED - PR Review will not work!");
            }
        } else {
            logger.error("❌ GitHub configuration object is null!");
        }
        
        logger.info("=== End Configuration ===");
    }
}

