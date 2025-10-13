package com.testplatform.backend;

import com.testplatform.backend.config.AppProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableConfigurationProperties(AppProperties.class)
@EnableAsync
public class TestPlatformBackendApplication implements CommandLineRunner {
    
    @Autowired
    private Environment environment;
    
    @Autowired
    private AppProperties appProperties;
    
    public static void main(String[] args) {
        SpringApplication.run(TestPlatformBackendApplication.class, args);
    }
    
    @Override
    public void run(String... args) throws Exception {
        String port = environment.getProperty("server.port", "8080");
        String env = environment.getProperty("spring.profiles.active", "development");
        
        System.out.println("🚀 Test Platform Backend Server Started!");
        System.out.println("========================================");
        System.out.println("✅ Server running on: http://localhost:" + port);
        System.out.println("✅ Health check: http://localhost:" + port + "/health");
        System.out.println("✅ Environment: " + env);
        System.out.println("📋 Available Endpoints:");
        System.out.println("• POST " + appProperties.getApiPrefix() + "/auth/login");
        System.out.println("• GET  " + appProperties.getApiPrefix() + "/tests/suites");
        System.out.println("• POST " + appProperties.getApiPrefix() + "/tests/generate");
        System.out.println("• POST " + appProperties.getApiPrefix() + "/tests/execute");
        System.out.println("• GET  " + appProperties.getApiPrefix() + "/pull-requests");
        System.out.println("• POST " + appProperties.getApiPrefix() + "/pull-requests");
        System.out.println("• GET  " + appProperties.getApiPrefix() + "/dashboard/stats");
        System.out.println("• POST " + appProperties.getApiPrefix() + "/webhook/pr-created");
        System.out.println("🔐 Test Credentials:");
        System.out.println("• Email: admin@testplatform.com");
        System.out.println("• Password: admin123");
        System.out.println("The backend is now ready to serve requests!");
    }
}
