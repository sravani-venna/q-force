package com.testplatform.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.testplatform.backend.config.AppProperties;
import com.testplatform.backend.enums.TestType;
import com.testplatform.backend.model.TestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class LlmService {
    
    private static final Logger logger = LoggerFactory.getLogger(LlmService.class);
    
    @Autowired
    private AppProperties appProperties;
    
    @Autowired
    private CodeAnalyzerService codeAnalyzerService;
    
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * Generate test cases using LLM based on code content
     */
    public List<TestCase> generateTestCases(String code, TestType testType, String language, String filePath) {
        try {
            logger.info("🤖 Generating {} tests using LLM for {}", testType, filePath);
            
            // Analyze the code first to get better context
            CodeAnalyzerService.CodeAnalysisResult analysis = codeAnalyzerService.analyzeCode(code, filePath, language);
            
            String prompt = buildEnhancedPrompt(code, testType, language, filePath, analysis);
            String response = callLlmApi(prompt);
            return parseLlmResponse(response, testType, language, filePath);
            
        } catch (Exception e) {
            logger.error("❌ Error generating tests with LLM: {}", e.getMessage(), e);
            // Fallback to basic test cases if LLM fails
            return createFallbackTestCases(testType, language, filePath);
        }
    }
    
    /**
     * Build enhanced prompt with code analysis
     */
    private String buildEnhancedPrompt(String code, TestType testType, String language, String filePath, 
                                     CodeAnalyzerService.CodeAnalysisResult analysis) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("You are an expert software testing engineer. Generate comprehensive test cases for the following code.\n\n");
        
        // Add code analysis context
        prompt.append("Code Analysis:\n");
        prompt.append(analysis.getContext()).append("\n\n");
        
        prompt.append("Code to test:\n");
        prompt.append("```").append(language).append("\n");
        prompt.append(code);
        prompt.append("\n```\n\n");
        
        prompt.append("Test Requirements:\n");
        prompt.append("- Test Type: ").append(testType).append("\n");
        prompt.append("- Language: ").append(language).append("\n");
        prompt.append("- File: ").append(filePath).append("\n");
        prompt.append("- Testable Methods: ").append(String.join(", ", analysis.getTestableMethods())).append("\n");
        prompt.append("- Dependencies: ").append(String.join(", ", analysis.getDependencies())).append("\n");
        prompt.append("- Business Logic Patterns: ").append(String.join(", ", analysis.getBusinessLogic())).append("\n\n");
        
        prompt.append("Generate 8-15 comprehensive, high-quality test cases that cover:\n");
        
        switch (testType) {
            case UNIT:
                prompt.append("- Method functionality and behavior\n");
                prompt.append("- Edge cases and boundary conditions\n");
                prompt.append("- Exception handling and error scenarios\n");
                prompt.append("- Input validation and parameter checks\n");
                prompt.append("- Return value validation\n");
                prompt.append("- Null and empty input handling\n");
                prompt.append("- Performance edge cases\n");
                prompt.append("- Concurrency and thread safety\n");
                prompt.append("- Memory usage and resource cleanup\n");
                if (!analysis.getDependencies().isEmpty()) {
                    prompt.append("- Mock external dependencies: ").append(String.join(", ", analysis.getDependencies())).append("\n");
                }
                break;
            case INTEGRATION:
                prompt.append("- Component interactions\n");
                prompt.append("- API endpoint testing\n");
                prompt.append("- Database operations\n");
                prompt.append("- External service integrations\n");
                prompt.append("- Data flow validation\n");
                prompt.append("- Error propagation and handling\n");
                prompt.append("- Timeout and retry scenarios\n");
                prompt.append("- Data consistency and transactions\n");
                prompt.append("- Security and authentication flows\n");
                break;
            case E2E:
                prompt.append("- Complete user workflows\n");
                prompt.append("- End-to-end scenarios\n");
                prompt.append("- User interface interactions\n");
                prompt.append("- System integration points\n");
                prompt.append("- Business process validation\n");
                prompt.append("- Cross-browser compatibility\n");
                prompt.append("- Performance under load\n");
                prompt.append("- Error recovery and user experience\n");
                prompt.append("- Data persistence across sessions\n");
                break;
            case PERFORMANCE:
                prompt.append("- Load testing and stress testing\n");
                prompt.append("- Response time validation\n");
                prompt.append("- Throughput and capacity testing\n");
                prompt.append("- Memory usage and optimization\n");
                prompt.append("- CPU utilization monitoring\n");
                prompt.append("- Database query performance\n");
                prompt.append("- Network latency testing\n");
                prompt.append("- Resource leak detection\n");
                prompt.append("- Scalability and concurrency testing\n");
                break;
            case SECURITY:
                prompt.append("- Authentication and authorization\n");
                prompt.append("- Input validation and sanitization\n");
                prompt.append("- SQL injection prevention\n");
                prompt.append("- Cross-site scripting (XSS) protection\n");
                prompt.append("- CSRF token validation\n");
                prompt.append("- Data encryption and secure storage\n");
                prompt.append("- Session management security\n");
                prompt.append("- API security and rate limiting\n");
                prompt.append("- Vulnerability scanning and penetration testing\n");
                break;
        }
        
        prompt.append("\nIMPORTANT: Respond with ONLY a valid JSON object. No markdown, no backticks, no additional text.\n");
        prompt.append("Return the response in the following JSON format:\n");
        prompt.append("{\n");
        prompt.append("  \"testCases\": [\n");
        prompt.append("    {\n");
        prompt.append("      \"name\": \"Test case name\",\n");
        prompt.append("      \"description\": \"Detailed description of what this test validates\",\n");
        prompt.append("      \"priority\": \"HIGH|MEDIUM|LOW\",\n");
        prompt.append("      \"testCode\": \"Generated test code snippet\"\n");
        prompt.append("    }\n");
        prompt.append("  ]\n");
        prompt.append("}\n");
        prompt.append("\nCRITICAL: Return ONLY the JSON object above, nothing else. No explanations, no markdown formatting.");
        
        return prompt.toString();
    }
    
    /**
     * Build the prompt for LLM based on code and test type (legacy method)
     */
    private String buildPrompt(String code, TestType testType, String language, String filePath) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("You are an expert software testing engineer. Generate comprehensive test cases for the following code.\n\n");
        prompt.append("Code to test:\n");
        prompt.append("```").append(language).append("\n");
        prompt.append(code);
        prompt.append("\n```\n\n");
        
        prompt.append("Test Requirements:\n");
        prompt.append("- Test Type: ").append(testType).append("\n");
        prompt.append("- Language: ").append(language).append("\n");
        prompt.append("- File: ").append(filePath).append("\n\n");
        
        prompt.append("Generate 8-15 comprehensive, high-quality test cases that cover:\n");
        
        switch (testType) {
            case UNIT:
                prompt.append("- Method functionality and behavior\n");
                prompt.append("- Edge cases and boundary conditions\n");
                prompt.append("- Exception handling and error scenarios\n");
                prompt.append("- Input validation and parameter checks\n");
                prompt.append("- Return value validation\n");
                prompt.append("- Null and empty input handling\n");
                prompt.append("- Performance edge cases\n");
                prompt.append("- Concurrency and thread safety\n");
                break;
            case INTEGRATION:
                prompt.append("- Component interactions\n");
                prompt.append("- API endpoint testing\n");
                prompt.append("- Database operations\n");
                prompt.append("- External service integrations\n");
                prompt.append("- Data flow validation\n");
                prompt.append("- Error propagation and handling\n");
                prompt.append("- Timeout and retry scenarios\n");
                prompt.append("- Data consistency and transactions\n");
                break;
            case E2E:
                prompt.append("- Complete user workflows\n");
                prompt.append("- End-to-end scenarios\n");
                prompt.append("- User interface interactions\n");
                prompt.append("- System integration points\n");
                prompt.append("- Business process validation\n");
                prompt.append("- Cross-browser compatibility\n");
                prompt.append("- Performance under load\n");
                prompt.append("- Error recovery and user experience\n");
                break;
            case PERFORMANCE:
                prompt.append("- Load testing and stress testing\n");
                prompt.append("- Response time validation\n");
                prompt.append("- Throughput and capacity testing\n");
                prompt.append("- Memory usage and optimization\n");
                prompt.append("- CPU utilization monitoring\n");
                prompt.append("- Database query performance\n");
                prompt.append("- Network latency testing\n");
                prompt.append("- Resource leak detection\n");
                prompt.append("- Scalability and concurrency testing\n");
                break;
            case SECURITY:
                prompt.append("- Authentication and authorization\n");
                prompt.append("- Input validation and sanitization\n");
                prompt.append("- SQL injection prevention\n");
                prompt.append("- Cross-site scripting (XSS) protection\n");
                prompt.append("- CSRF token validation\n");
                prompt.append("- Data encryption and secure storage\n");
                prompt.append("- Session management security\n");
                prompt.append("- API security and rate limiting\n");
                prompt.append("- Vulnerability scanning and penetration testing\n");
                break;
        }
        
        prompt.append("\nIMPORTANT: Respond with ONLY a valid JSON object. No markdown, no backticks, no additional text.\n");
        prompt.append("Return the response in the following JSON format:\n");
        prompt.append("{\n");
        prompt.append("  \"testCases\": [\n");
        prompt.append("    {\n");
        prompt.append("      \"name\": \"Test case name\",\n");
        prompt.append("      \"description\": \"Detailed description of what this test validates\",\n");
        prompt.append("      \"priority\": \"HIGH|MEDIUM|LOW\",\n");
        prompt.append("      \"testCode\": \"Generated test code snippet\"\n");
        prompt.append("    }\n");
        prompt.append("  ]\n");
        prompt.append("}\n");
        prompt.append("\nCRITICAL: Return ONLY the JSON object above, nothing else. No explanations, no markdown formatting.");
        
        return prompt.toString();
    }
    
    /**
     * Call the local LLM API (Ollama or similar)
     */
    private String callLlmApi(String prompt) {
        try {
            String url = appProperties.getLlm().getBaseUrl() + "/api/generate";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", appProperties.getLlm().getModel());
            requestBody.put("prompt", prompt);
            requestBody.put("stream", false);
            requestBody.put("options", Map.of(
                "temperature", appProperties.getLlm().getTemperature(),
                "num_predict", appProperties.getLlm().getMaxTokens()
            ));
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.POST, entity, String.class
            );
            
            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode responseJson = objectMapper.readTree(response.getBody());
                JsonNode responseNode = responseJson.get("response");
                if (responseNode != null) {
                    return responseNode.asText();
                }
                throw new RuntimeException("No valid response from local LLM API");
            } else {
                throw new RuntimeException("Local LLM API call failed with status: " + response.getStatusCode());
            }
            
        } catch (Exception e) {
            logger.error("❌ Error calling local LLM API: {}", e.getMessage(), e);
            return "Error: Failed to call local LLM API - " + e.getMessage();
        }
    }
    
    /**
     * Parse LLM response and convert to TestCase objects
     */
    private List<TestCase> parseLlmResponse(String response, TestType testType, String language, String filePath) {
        try {
            // Clean the response - remove backticks, markdown formatting, and extract JSON
            String cleanedResponse = response
                .replaceAll("```json", "")
                .replaceAll("```", "")
                .replaceAll("`", "")
                .trim();
            
            // Extract JSON from the response (remove any text before the JSON)
            String jsonResponse = cleanedResponse;
            int jsonStart = cleanedResponse.indexOf("{");
            if (jsonStart > 0) {
                jsonResponse = cleanedResponse.substring(jsonStart);
            }
            
            // Find the end of the JSON object
            int braceCount = 0;
            int jsonEnd = jsonStart;
            for (int i = jsonStart; i < cleanedResponse.length(); i++) {
                if (cleanedResponse.charAt(i) == '{') {
                    braceCount++;
                } else if (cleanedResponse.charAt(i) == '}') {
                    braceCount--;
                    if (braceCount == 0) {
                        jsonEnd = i + 1;
                        break;
                    }
                }
            }
            
            if (jsonEnd > jsonStart) {
                jsonResponse = cleanedResponse.substring(jsonStart, jsonEnd);
            }
            
            JsonNode responseJson = objectMapper.readTree(jsonResponse);
            JsonNode testCasesNode = responseJson.get("testCases");
            
            List<TestCase> testCases = new ArrayList<>();
            
            if (testCasesNode != null && testCasesNode.isArray()) {
                for (JsonNode testCaseNode : testCasesNode) {
                    TestCase testCase = new TestCase();
                    testCase.setId(UUID.randomUUID().toString());
                    testCase.setName(testCaseNode.get("name").asText());
                    testCase.setDescription(testCaseNode.get("description").asText());
                    testCase.setType(testType);
                    testCase.setLanguage(language);
                    testCase.setFilePath(filePath);
                    testCase.setCode(testCaseNode.get("testCode").asText());
                    
                    // Parse priority
                    String priorityStr = testCaseNode.get("priority").asText();
                    switch (priorityStr.toUpperCase()) {
                        case "HIGH":
                            testCase.setPriority(com.testplatform.backend.enums.TestPriority.HIGH);
                            break;
                        case "MEDIUM":
                            testCase.setPriority(com.testplatform.backend.enums.TestPriority.MEDIUM);
                            break;
                        case "LOW":
                            testCase.setPriority(com.testplatform.backend.enums.TestPriority.LOW);
                            break;
                        default:
                            testCase.setPriority(com.testplatform.backend.enums.TestPriority.MEDIUM);
                    }
                    
                    testCase.setStatus(com.testplatform.backend.enums.TestStatus.PENDING);
                    testCases.add(testCase);
                }
            }
            
            logger.info("✅ Generated {} test cases using LLM", testCases.size());
            return testCases;
            
        } catch (Exception e) {
            logger.error("❌ Error parsing LLM response: {}", e.getMessage(), e);
            // Return fallback test cases if parsing fails
            return createFallbackTestCases(testType, language, filePath);
        }
    }
    
    /**
     * Create fallback test cases if LLM fails
     */
    private List<TestCase> createFallbackTestCases(TestType testType, String language, String filePath) {
        List<TestCase> fallbackTests = new ArrayList<>();
        
        TestCase fallbackTest = new TestCase();
        fallbackTest.setId(UUID.randomUUID().toString());
        fallbackTest.setName("LLM Generated Test - " + testType);
        fallbackTest.setDescription("Test case generated by LLM (fallback mode)");
        fallbackTest.setType(testType);
        fallbackTest.setLanguage(language);
        fallbackTest.setFilePath(filePath);
        fallbackTest.setCode("// LLM generated test code would go here");
        fallbackTest.setPriority(com.testplatform.backend.enums.TestPriority.MEDIUM);
        fallbackTest.setStatus(com.testplatform.backend.enums.TestStatus.PENDING);
        
        fallbackTests.add(fallbackTest);
        return fallbackTests;
    }
}
