package com.testplatform.backend.controller;

import com.testplatform.backend.dto.ApiResponse;
import com.testplatform.backend.dto.DetailedTestCaseDTO;
import com.testplatform.backend.model.TestExecution;
import com.testplatform.backend.service.TestExecutionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/test-executions")
public class TestExecutionController {
    
    @Autowired
    private TestExecutionService testExecutionService;
    
    /**
     * GET /api/test-executions - Get all test executions
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<TestExecution>>> getAllTestExecutions() {
        try {
            List<TestExecution> executions = testExecutionService.getAllExecutions();
            return ResponseEntity.ok(ApiResponse.success(executions, executions.size()));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Failed to fetch test executions"));
        }
    }
    
    /**
     * GET /api/test-executions/test-cases - Get detailed test case results
     */
    @GetMapping("/test-cases")
    public ResponseEntity<ApiResponse<List<DetailedTestCaseDTO>>> getDetailedTestCaseResults(
            @RequestParam(required = false) String status) {
        try {
            List<DetailedTestCaseDTO> results = testExecutionService.getDetailedTestCaseResults(status);
            return ResponseEntity.ok(ApiResponse.success(results, results.size()));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Failed to fetch detailed test case results"));
        }
    }

    /**
     * GET /api/test-executions/test-cases/summary - Get test case results summary
     */
    @GetMapping("/test-cases/summary")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getTestCaseResultsSummary() {
        try {
            Map<String, Object> summary = testExecutionService.getTestCaseResultsSummary();
            return ResponseEntity.ok(ApiResponse.success(summary));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Failed to fetch test case results summary"));
        }
    }

    /**
     * GET /api/test-executions/{id} - Get specific test execution
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TestExecution>> getTestExecution(@PathVariable String id) {
        Optional<TestExecution> execution = testExecutionService.getExecutionStatus(id);
        
        if (execution.isEmpty()) {
            return ResponseEntity.status(404)
                    .body(ApiResponse.error("Test execution not found"));
        }
        
        return ResponseEntity.ok(ApiResponse.success(execution.get()));
    }
    
    /**
     * POST /api/test-executions - Start test execution
     */
    @PostMapping
    public ResponseEntity<ApiResponse<ExecutionResponse>> startTestExecution(@RequestBody TestExecutionRequest request) {
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
                    .body(ApiResponse.error("Failed to start test execution"));
        }
    }
    
    /**
     * POST /api/test-executions/{id}/stop - Stop test execution
     */
    @PostMapping("/{id}/stop")
    public ResponseEntity<ApiResponse<String>> stopTestExecution(@PathVariable String id) {
        try {
            boolean stopped = testExecutionService.stopExecution(id);
            
            if (stopped) {
                return ResponseEntity.ok(ApiResponse.success("Test execution stopped"));
            } else {
                return ResponseEntity.status(404)
                        .body(ApiResponse.error("Test execution not found or already stopped"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Failed to stop test execution"));
        }
    }
    
    // Inner classes for request/response
    public static class TestExecutionRequest {
        private String suiteId;
        private Integer prNumber;
        private String branch;
        private String action;
        
        // Getters and Setters
        public String getSuiteId() { return suiteId; }
        public void setSuiteId(String suiteId) { this.suiteId = suiteId; }
        
        public Integer getPrNumber() { return prNumber; }
        public void setPrNumber(Integer prNumber) { this.prNumber = prNumber; }
        
        public String getBranch() { return branch; }
        public void setBranch(String branch) { this.branch = branch; }
        
        public String getAction() { return action; }
        public void setAction(String action) { this.action = action; }
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