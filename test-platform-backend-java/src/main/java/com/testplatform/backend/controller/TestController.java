package com.testplatform.backend.controller;

import com.testplatform.backend.dto.ApiResponse;
import com.testplatform.backend.dto.TestSuiteDTO;
import com.testplatform.backend.enums.TestType;
import com.testplatform.backend.model.TestExecution;
import com.testplatform.backend.model.TestSuite;
import com.testplatform.backend.service.TestGenerationService;
import com.testplatform.backend.service.TestExecutionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/tests")
public class TestController {
    
    @Autowired
    private TestGenerationService testGenerationService;
    
    @Autowired
    private TestExecutionService testExecutionService;
    
    /**
     * GET /api/tests/suites - Get all test suites
     */
    @GetMapping("/suites")
    public ResponseEntity<ApiResponse<List<TestSuiteDTO>>> getAllTestSuites() {
        List<TestSuite> tests = testGenerationService.getAllTests();
        List<TestSuiteDTO> testDTOs = tests.stream()
                .map(TestSuiteDTO::new)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponse.success(testDTOs, testDTOs.size()));
    }
    
    /**
     * POST /api/tests/generate - Generate tests
     */
    @PostMapping("/generate")
    public ResponseEntity<ApiResponse<List<Object>>> generateTests(@RequestBody GenerateTestsRequest request) {
        try {
            CompletableFuture<List<com.testplatform.backend.model.TestCase>> testCasesFuture = 
                testGenerationService.generateTestCases(
                    request.getCode(), 
                    request.getType(), 
                    request.getLanguage(), 
                    request.getFilePath()
                );
            
            List<com.testplatform.backend.model.TestCase> testCases = testCasesFuture.get();
            
            return ResponseEntity.ok(ApiResponse.success(
                testCases.stream().collect(Collectors.toList()),
                String.format("Generated %d test cases", testCases.size())
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Failed to generate tests"));
        }
    }
    
    /**
     * POST /api/tests/execute - Execute tests
     */
    @PostMapping("/execute")
    public ResponseEntity<ApiResponse<ExecutionResponse>> executeTests(@RequestBody ExecuteTestsRequest request) {
        try {
            CompletableFuture<String> executionFuture;
            
            if (request.getPrNumber() != null) {
                executionFuture = testExecutionService.executeTestsForPR(request.getPrNumber(), request.getBranch());
            } else if (request.getSuiteId() != null) {
                executionFuture = testExecutionService.executeTestSuite(request.getSuiteId());
            } else {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Either suiteId or prNumber is required"));
            }
            
            String executionId = executionFuture.get();
            ExecutionResponse response = new ExecutionResponse(executionId);
            
            return ResponseEntity.ok(ApiResponse.success(response, "Test execution started"));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Failed to execute tests"));
        }
    }
    
    /**
     * GET /api/tests/executions/:id - Get execution status
     */
    @GetMapping("/executions/{id}")
    public ResponseEntity<ApiResponse<TestExecution>> getExecutionStatus(@PathVariable String id) {
        Optional<TestExecution> execution = testExecutionService.getExecutionStatus(id);
        
        if (execution.isEmpty()) {
            return ResponseEntity.status(404)
                    .body(ApiResponse.error("Execution not found"));
        }
        
        return ResponseEntity.ok(ApiResponse.success(execution.get()));
    }
    
    /**
     * GET /api/tests/stats - Get test statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getTestStats() {
        try {
            Map<String, Object> generationStats = testGenerationService.getTestStats();
            Map<String, Object> executionStats = testExecutionService.getExecutionStats();
            
            Map<String, Object> stats = Map.of(
                "generation", generationStats,
                "execution", executionStats
            );
            
            return ResponseEntity.ok(ApiResponse.success(stats));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Failed to fetch test statistics"));
        }
    }
    
    /**
     * POST /api/tests/regenerate - Regenerate all test cases from current codebase
     */
    @PostMapping("/regenerate")
    public ResponseEntity<ApiResponse<Map<String, Object>>> regenerateTests() {
        try {
            CompletableFuture<Map<String, Object>> regenerationFuture = testGenerationService.regenerateAllTests();
            Map<String, Object> result = regenerationFuture.get();
            
            return ResponseEntity.ok(ApiResponse.success(result, "Test cases regenerated successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Failed to regenerate tests: " + e.getMessage()));
        }
    }
    
    // Inner classes for request/response
    public static class GenerateTestsRequest {
        private String code;
        private TestType type;
        private String language;
        private String filePath;
        
        // Getters and Setters
        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
        
        public TestType getType() { return type; }
        public void setType(TestType type) { this.type = type; }
        
        public String getLanguage() { return language; }
        public void setLanguage(String language) { this.language = language; }
        
        public String getFilePath() { return filePath; }
        public void setFilePath(String filePath) { this.filePath = filePath; }
    }
    
    public static class ExecuteTestsRequest {
        private String suiteId;
        private Integer prNumber;
        private String branch;
        
        // Getters and Setters
        public String getSuiteId() { return suiteId; }
        public void setSuiteId(String suiteId) { this.suiteId = suiteId; }
        
        public Integer getPrNumber() { return prNumber; }
        public void setPrNumber(Integer prNumber) { this.prNumber = prNumber; }
        
        public String getBranch() { return branch; }
        public void setBranch(String branch) { this.branch = branch; }
    }
    
    public static class ExecutionResponse {
        private String executionId;
        
        public ExecutionResponse(String executionId) {
            this.executionId = executionId;
        }
        
        // Getters and Setters
        public String getExecutionId() { return executionId; }
        public void setExecutionId(String executionId) { this.executionId = executionId; }
    }
}
