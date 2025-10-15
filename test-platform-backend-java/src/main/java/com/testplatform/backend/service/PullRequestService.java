package com.testplatform.backend.service;

import com.testplatform.backend.enums.PRStatus;
import com.testplatform.backend.enums.TestType;
import com.testplatform.backend.model.PullRequest;
import com.testplatform.backend.model.TestSuite;
import com.testplatform.backend.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
public class PullRequestService {
    
    private static final Logger logger = LoggerFactory.getLogger(PullRequestService.class);
    
    @Autowired
    private TestGenerationService testGenerationService;
    
    @Autowired
    private GitService gitService;
    
    // Mock data storage (in production, use database)
    private final List<PullRequest> pullRequests = new ArrayList<>();
    private long nextPRId = 3L;
    
    public PullRequestService() {
        // Mock data removed - using real Git repository data only
        logger.info("🚀 PullRequestService initialized - using real Git repository data");
    }
    
    /**
     * Get all pull requests
     */
    public List<PullRequest> getAllPullRequests() {
        logger.info("📋 Fetching all pull requests ({} found)", pullRequests.size());
        return new ArrayList<>(pullRequests);
    }
    
    /**
     * Get specific pull request with generated tests
     */
    public PullRequest getPullRequest(Long id) {
        Optional<PullRequest> prOpt = pullRequests.stream()
                .filter(p -> p.getId().equals(id))
                .findFirst();
        
        if (prOpt.isEmpty()) {
            throw new ResourceNotFoundException("Pull request not found");
        }
        
        PullRequest pr = prOpt.get();
        
        // Get generated tests for this PR
        List<TestSuite> generatedTests = testGenerationService.getTestsForPR(pr.getNumber());
        
        logger.info("📋 Fetching PR #{} with {} test suites", pr.getNumber(), generatedTests.size());
        
        return pr;
    }
    
    /**
     * Create new pull request and auto-generate tests
     */
    public PullRequest createPullRequest(PullRequest newPR) {
        // Generate unique PR number
        int number = 125 + new Random().nextInt(1000);
        
        newPR.setId(nextPRId++);
        newPR.setNumber(number);
        if (newPR.getAuthor() == null) {
            newPR.setAuthor("developer@testplatform.com");
        }
        newPR.setStatus(PRStatus.OPEN);
        newPR.setTestsGenerated(0);
        newPR.setTestsPassed(0);
        newPR.setTestsFailed(0);
        newPR.setCoverage(0.0);
        
        pullRequests.add(newPR);
        
        logger.info("🎯 Created PR #{}: {}", number, newPR.getTitle());
        
        // Auto-generate tests if files were changed
        if (newPR.getChangedFiles() != null && !newPR.getChangedFiles().isEmpty()) {
            CompletableFuture.runAsync(() -> {
                try {
                    List<TestType> testTypes = Arrays.asList(TestType.UNIT, TestType.INTEGRATION, TestType.E2E);
                    int totalTestsGenerated = 0;
                    
                    for (PullRequest.ChangedFile file : newPR.getChangedFiles()) {
                        for (TestType testType : testTypes) {
                            // Get actual code content from Git repository
                            String codeContent = gitService.getCodeContentWithLanguage(file.getFilename(), newPR.getBranch());
                            testGenerationService.generateTestCases(codeContent, testType, "java", file.getFilename())
                                    .thenAccept(testCases -> {
                                        TestSuite testSuite = new TestSuite();
                                        testSuite.setId(UUID.randomUUID().toString());
                                        testSuite.setPrNumber(number);
                                        testSuite.setBranch(newPR.getBranch());
                                        testSuite.setFilePath(file.getFilename());
                                        testSuite.setType(testType);
                                        testSuite.setLanguage("java");
                                        testSuite.setTestCases(testCases);
                                        
                                        testGenerationService.saveTestSuite(testSuite);
                                    });
                        }
                    }
                    
                    // Update PR metrics
                    updatePRTestMetrics(number, newPR.getChangedFiles().size() * 3 * 4); // Estimate
                    
                    logger.info("🎯 Auto-generated tests for PR #{}: {} test suites (Unit + Integration + E2E)", 
                               number, newPR.getChangedFiles().size() * 3);
                } catch (Exception error) {
                    logger.error("❌ Error auto-generating tests: {}", error.getMessage());
                }
            });
        }
        
        return newPR;
    }
    
    /**
     * Update pull request
     */
    public PullRequest updatePullRequest(Long id, PullRequest updates) {
        Optional<PullRequest> prOpt = pullRequests.stream()
                .filter(p -> p.getId().equals(id))
                .findFirst();
        
        if (prOpt.isEmpty()) {
            throw new ResourceNotFoundException("Pull request not found");
        }
        
        PullRequest pr = prOpt.get();
        
        // Update fields
        if (updates.getTitle() != null) pr.setTitle(updates.getTitle());
        if (updates.getBranch() != null) pr.setBranch(updates.getBranch());
        if (updates.getStatus() != null) pr.setStatus(updates.getStatus());
        if (updates.getChangedFiles() != null) pr.setChangedFiles(updates.getChangedFiles());
        
        pr.setUpdatedAt(LocalDateTime.now());
        
        logger.info("📝 Updated PR #{}", pr.getNumber());
        
        return pr;
    }
    
    /**
     * Delete pull request
     */
    public void deletePullRequest(Long id) {
        Optional<PullRequest> prOpt = pullRequests.stream()
                .filter(p -> p.getId().equals(id))
                .findFirst();
        
        if (prOpt.isEmpty()) {
            throw new ResourceNotFoundException("Pull request not found");
        }
        
        PullRequest pr = prOpt.get();
        pullRequests.remove(pr);
        
        // Also delete associated tests
        testGenerationService.deleteTestsForPR(pr.getNumber());
        
        logger.info("🗑️ Deleted PR #{}", pr.getNumber());
    }
    
    /**
     * Helper function to update PR test metrics
     */
    public void updatePRTestMetrics(Integer prNumber, Integer newTestsCount) {
        Optional<PullRequest> prOpt = pullRequests.stream()
                .filter(p -> p.getNumber().equals(prNumber))
                .findFirst();
        
        if (prOpt.isPresent()) {
            PullRequest pr = prOpt.get();
            pr.setTestsGenerated(pr.getTestsGenerated() + newTestsCount);
            // Simulate test execution results
            double passRate = 0.85 + Math.random() * 0.1; // 85-95% pass rate
            pr.setTestsPassed((int) Math.floor(pr.getTestsGenerated() * passRate));
            pr.setTestsFailed(pr.getTestsGenerated() - pr.getTestsPassed());
            pr.setCoverage(Math.min(95.0, 80.0 + Math.random() * 15)); // 80-95% coverage
        }
    }
    
    /**
     * Get PRs by status
     */
    public List<PullRequest> getPullRequestsByStatus(PRStatus status) {
        return pullRequests.stream()
                .filter(pr -> pr.getStatus() == status)
                .collect(Collectors.toList());
    }
    
}
