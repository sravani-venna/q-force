package com.testplatform.backend.controller;

import com.testplatform.backend.config.AppProperties;
import com.testplatform.backend.dto.ApiResponse;
import com.testplatform.backend.service.TestGenerationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class HealthController {
    
    @Autowired
    private AppProperties appProperties;
    
    @Autowired
    private TestGenerationService testGenerationService;
    
    /**
     * GET /health - Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "healthy");
        health.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        health.put("version", "1.0.0");
        health.put("environment", "development");
        
        return ResponseEntity.ok(health);
    }
    
    /**
     * GET / - Root endpoint
     */
    @GetMapping("/")
    public ResponseEntity<Map<String, Object>> root() {
        Map<String, Object> info = new HashMap<>();
        info.put("name", "Test Platform Backend API");
        info.put("version", "1.0.0");
        info.put("description", "Centralized test validation and automation platform");
        
        Map<String, String> endpoints = new HashMap<>();
        endpoints.put("health", "/health");
        endpoints.put("auth", appProperties.getApiPrefix() + "/auth");
        endpoints.put("tests", appProperties.getApiPrefix() + "/tests");
        endpoints.put("pullRequests", appProperties.getApiPrefix() + "/pull-requests");
        endpoints.put("dashboard", appProperties.getApiPrefix() + "/dashboard");
        endpoints.put("webhooks", appProperties.getApiPrefix() + "/webhook");
        
        info.put("endpoints", endpoints);
        info.put("documentation", "/api/docs");
        
        return ResponseEntity.ok(info);
    }
    
    /**
     * GET /api/test-suites - Compatibility route for frontend
     */
    @GetMapping("/api/test-suites")
    public ResponseEntity<ApiResponse<List<com.testplatform.backend.dto.TestSuiteDTO>>> getTestSuites() {
        // Forward to the test generation service to get all tests for compatibility
        try {
            List<com.testplatform.backend.model.TestSuite> tests = testGenerationService.getAllTests();
            List<com.testplatform.backend.dto.TestSuiteDTO> testDTOs = tests.stream()
                    .map(com.testplatform.backend.dto.TestSuiteDTO::new)
                    .collect(java.util.stream.Collectors.toList());
            
            return ResponseEntity.ok(ApiResponse.success(testDTOs, testDTOs.size()));
        } catch (Exception error) {
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Failed to fetch test suites"));
        }
    }
}
