package com.testplatform.backend.controller;

import com.testplatform.backend.dto.ApiResponse;
import com.testplatform.backend.dto.PullRequestDTO;
import com.testplatform.backend.dto.TestSuiteDTO;
import com.testplatform.backend.enums.TestType;
import com.testplatform.backend.model.PullRequest;
import com.testplatform.backend.model.TestSuite;
import com.testplatform.backend.service.PullRequestService;
import com.testplatform.backend.service.TestGenerationService;
import com.testplatform.backend.service.TestExecutionService;
import com.testplatform.backend.service.GitService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/pull-requests")
public class PullRequestController {
    
    private static final Logger logger = LoggerFactory.getLogger(PullRequestController.class);
    
    @Autowired
    private PullRequestService pullRequestService;
    
    @Autowired
    private TestGenerationService testGenerationService;
    
    @Autowired
    private GitService gitService;
    
    @Autowired
    private TestExecutionService testExecutionService;
    
    /**
     * GET /api/pull-requests - Get all pull requests
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<PullRequestDTO>>> getAllPullRequests() {
        List<PullRequest> prs = pullRequestService.getAllPullRequests();
        List<PullRequestDTO> prDTOs = prs.stream()
                .map(PullRequestDTO::new)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponse.success(prDTOs, prDTOs.size()));
    }
    
    /**
     * GET /api/pull-requests/:id - Get specific pull request with generated tests
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PullRequestDTO>> getPullRequest(@PathVariable Long id) {
        PullRequest pr = pullRequestService.getPullRequest(id);
        PullRequestDTO prDTO = new PullRequestDTO(pr);
        
        // Get generated tests for this PR
        List<TestSuite> generatedTests = testGenerationService.getTestsForPR(pr.getNumber());
        List<TestSuiteDTO> testDTOs = generatedTests.stream()
                .map(TestSuiteDTO::new)
                .collect(Collectors.toList());
        prDTO.setGeneratedTests(testDTOs);
        
        return ResponseEntity.ok(ApiResponse.success(prDTO));
    }
    
    /**
     * POST /api/pull-requests - Create new pull request and auto-generate tests
     */
    @PostMapping
    public ResponseEntity<ApiResponse<PullRequestDTO>> createPullRequest(@Valid @RequestBody PullRequest pullRequest) {
        PullRequest createdPR = pullRequestService.createPullRequest(pullRequest);
        PullRequestDTO prDTO = new PullRequestDTO(createdPR);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(prDTO, "Pull request created and test generation started"));
    }
    
    /**
     * PUT /api/pull-requests/:id - Update pull request
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PullRequestDTO>> updatePullRequest(
            @PathVariable Long id, 
            @RequestBody PullRequest updates) {
        PullRequest updatedPR = pullRequestService.updatePullRequest(id, updates);
        PullRequestDTO prDTO = new PullRequestDTO(updatedPR);
        
        return ResponseEntity.ok(ApiResponse.success(prDTO));
    }
    
    /**
     * DELETE /api/pull-requests/:id - Delete pull request
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Object>> deletePullRequest(@PathVariable Long id) {
        pullRequestService.deletePullRequest(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Pull request deleted successfully"));
    }
    
    /**
     * POST /api/pull-requests/:id/tests/generate - Generate tests for specific PR
     */
    @PostMapping("/{id}/tests/generate")
    public ResponseEntity<ApiResponse<List<TestSuiteDTO>>> generateTests(
            @PathVariable Long id,
            @RequestBody(required = false) GenerateTestsRequest request) {
        
        PullRequest pr = pullRequestService.getPullRequest(id);
        String language = request != null ? request.getLanguage() : "java";
        TestType testType = request != null ? request.getTestType() : TestType.UNIT;
        
        List<TestSuite> testSuites = pr.getChangedFiles().stream()
                .map(file -> {
                    // Get actual code content from Git repository
                    String codeContent = gitService.getCodeContentWithLanguage(file.getFilename(), pr.getBranch());
                    
                    TestSuite testSuite = new TestSuite();
                    testSuite.setPrNumber(pr.getNumber());
                    testSuite.setBranch(pr.getBranch());
                    testSuite.setFilePath(file.getFilename());
                    testSuite.setType(testType);
                    testSuite.setLanguage(language);
                    
                    // Generate test cases using LLM with real code content
                    try {
                        testSuite.setTestCases(testGenerationService.generateTestCases(codeContent, testType, language, file.getFilename()).get());
                    } catch (Exception e) {
                        logger.error("Error generating test cases for {}: {}", file.getFilename(), e.getMessage());
                    }
                    
                    return testGenerationService.saveTestSuite(testSuite);
                })
                .collect(Collectors.toList());
        
        List<TestSuiteDTO> testDTOs = testSuites.stream()
                .map(TestSuiteDTO::new)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponse.success(testDTOs, 
                String.format("Generated %d test suites", testSuites.size())));
    }
    
    
    /**
     * POST /api/pull-requests/:id/tests/execute - Execute tests for specific PR
     */
    @PostMapping("/{id}/tests/execute")
    public ResponseEntity<ApiResponse<ExecutionResponse>> executeTests(@PathVariable Long id) {
        PullRequest pr = pullRequestService.getPullRequest(id);
        
        CompletableFuture<String> executionFuture = testExecutionService.executeTestsForPR(pr.getNumber(), pr.getBranch());
        
        try {
            String executionId = executionFuture.get();
            ExecutionResponse response = new ExecutionResponse(executionId, "3-5 seconds");
            
            return ResponseEntity.ok(ApiResponse.success(response, "Test execution started"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to execute tests"));
        }
    }
    
    // Inner classes for request/response
    public static class GenerateTestsRequest {
        private String language = "java";
        private TestType testType = TestType.UNIT;
        
        // Getters and Setters
        public String getLanguage() { return language; }
        public void setLanguage(String language) { this.language = language; }
        
        public TestType getTestType() { return testType; }
        public void setTestType(TestType testType) { this.testType = testType; }
    }
    
    public static class ExecutionResponse {
        private String executionId;
        private String estimatedDuration;
        
        public ExecutionResponse(String executionId, String estimatedDuration) {
            this.executionId = executionId;
            this.estimatedDuration = estimatedDuration;
        }
        
        // Getters and Setters
        public String getExecutionId() { return executionId; }
        public void setExecutionId(String executionId) { this.executionId = executionId; }
        
        public String getEstimatedDuration() { return estimatedDuration; }
        public void setEstimatedDuration(String estimatedDuration) { this.estimatedDuration = estimatedDuration; }
    }
}
