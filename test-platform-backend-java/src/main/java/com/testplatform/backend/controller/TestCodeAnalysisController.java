package com.testplatform.backend.controller;

import com.testplatform.backend.service.GitService;
import com.testplatform.backend.service.LlmService;
import com.testplatform.backend.enums.TestType;
import com.testplatform.backend.model.TestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/test-analysis")
public class TestCodeAnalysisController {
    
    private static final Logger logger = LoggerFactory.getLogger(TestCodeAnalysisController.class);
    
    @Autowired
    private GitService gitService;
    
    @Autowired
    private LlmService llmService;
    
    /**
     * Test endpoint to verify the system can read actual Spring Boot files
     */
    @GetMapping("/test-spring-boot-files")
    public ResponseEntity<String> testSpringBootFiles() {
        try {
            logger.info("🧪 Testing Spring Boot file reading...");
            
            // Test reading actual Spring Boot files
            String usersServiceCode = gitService.getCodeContentFromFile("services/UsersService.java", "cdac-main");
            String bookingServiceCode = gitService.getCodeContentFromFile("services/BookingService.java", "cdac-main");
            String usersEntityCode = gitService.getCodeContentFromFile("Entity/Users.java", "cdac-main");
            
            StringBuilder result = new StringBuilder();
            result.append("✅ Spring Boot File Analysis Test\n\n");
            
            result.append("📁 UsersService.java:\n");
            result.append(usersServiceCode.length() > 0 ? "✅ File read successfully (" + usersServiceCode.length() + " characters)\n" : "❌ File not found or empty\n");
            result.append("Preview: ").append(usersServiceCode.substring(0, Math.min(200, usersServiceCode.length()))).append("...\n\n");
            
            result.append("📁 BookingService.java:\n");
            result.append(bookingServiceCode.length() > 0 ? "✅ File read successfully (" + bookingServiceCode.length() + " characters)\n" : "❌ File not found or empty\n");
            result.append("Preview: ").append(bookingServiceCode.substring(0, Math.min(200, bookingServiceCode.length()))).append("...\n\n");
            
            result.append("📁 Users.java:\n");
            result.append(usersEntityCode.length() > 0 ? "✅ File read successfully (" + usersEntityCode.length() + " characters)\n" : "❌ File not found or empty\n");
            result.append("Preview: ").append(usersEntityCode.substring(0, Math.min(200, usersEntityCode.length()))).append("...\n\n");
            
            return ResponseEntity.ok(result.toString());
            
        } catch (Exception e) {
            logger.error("❌ Error testing Spring Boot files: {}", e.getMessage(), e);
            return ResponseEntity.ok("❌ Error: " + e.getMessage());
        }
    }
    
    /**
     * Test endpoint to generate test cases for actual Spring Boot code
     */
    @PostMapping("/generate-tests-for-spring-boot")
    public ResponseEntity<String> generateTestsForSpringBoot() {
        try {
            logger.info("🤖 Generating test cases for actual Spring Boot code...");
            
            // Read actual Spring Boot files
            String usersServiceCode = gitService.getCodeContentFromFile("services/UsersService.java", "cdac-main");
            
            if (usersServiceCode == null || usersServiceCode.trim().isEmpty()) {
                return ResponseEntity.ok("❌ Could not read UsersService.java - check file path and permissions");
            }
            
            // Generate test cases using LLM
            List<TestCase> testCases = llmService.generateTestCases(usersServiceCode, TestType.UNIT, "java", "services/UsersService.java");
            
            StringBuilder result = new StringBuilder();
            result.append("✅ Generated Test Cases for UsersService.java\n\n");
            result.append("📊 Generated ").append(testCases.size()).append(" test cases\n\n");
            
            for (int i = 0; i < Math.min(5, testCases.size()); i++) {
                TestCase testCase = testCases.get(i);
                result.append("🧪 Test ").append(i + 1).append(": ").append(testCase.getName()).append("\n");
                result.append("   Description: ").append(testCase.getDescription()).append("\n");
                result.append("   Priority: ").append(testCase.getPriority()).append("\n");
                result.append("   Type: ").append(testCase.getType()).append("\n\n");
            }
            
            return ResponseEntity.ok(result.toString());
            
        } catch (Exception e) {
            logger.error("❌ Error generating tests for Spring Boot: {}", e.getMessage(), e);
            return ResponseEntity.ok("❌ Error: " + e.getMessage());
        }
    }
}

