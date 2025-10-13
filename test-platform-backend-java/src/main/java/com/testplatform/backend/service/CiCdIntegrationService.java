package com.testplatform.backend.service;

import com.testplatform.backend.config.AppProperties;
import com.testplatform.backend.enums.TestType;
import com.testplatform.backend.model.TestCase;
import com.testplatform.backend.model.PullRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.CompletableFuture;

@Service
public class CiCdIntegrationService {
    
    private static final Logger logger = LoggerFactory.getLogger(CiCdIntegrationService.class);
    
    @Autowired
    private AppProperties appProperties;
    
    @Autowired
    private TestGenerationService testGenerationService;
    
    @Autowired
    private TestExecutionEngine testExecutionEngine;
    
    @Autowired
    private MultiRepositoryService multiRepositoryService;
    
    private final RestTemplate restTemplate = new RestTemplate();
    
    /**
     * Handle GitHub webhook
     */
    public CompletableFuture<WebhookResult> handleGitHubWebhook(GitHubWebhookPayload payload) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                logger.info("🔄 Processing GitHub webhook for PR #{}", payload.getPullRequest().getNumber());
                
                if (!"opened".equals(payload.getAction()) && !"synchronize".equals(payload.getAction())) {
                    logger.info("Skipping webhook action: {}", payload.getAction());
                    return new WebhookResult(false, "Action not supported: " + payload.getAction());
                }
                
                // Generate tests for changed files
                List<TestCase> generatedTests = generateTestsForPullRequest(payload);
                
                // Execute tests
                TestExecutionEngine.TestExecutionResult executionResult = 
                    executeTestsForPullRequest(payload, generatedTests);
                
                // Post results to PR
                postTestResultsToPR(payload, executionResult);
                
                return new WebhookResult(true, "Tests generated and executed successfully");
                
            } catch (Exception e) {
                logger.error("❌ Error processing GitHub webhook: {}", e.getMessage(), e);
                return new WebhookResult(false, "Error: " + e.getMessage());
            }
        });
    }
    
    /**
     * Handle GitLab webhook
     */
    public CompletableFuture<WebhookResult> handleGitLabWebhook(GitLabWebhookPayload payload) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                logger.info("🔄 Processing GitLab webhook for MR #{}", payload.getMergeRequest().getIid());
                
                if (!"open".equals(payload.getMergeRequest().getState()) && 
                    !"reopened".equals(payload.getMergeRequest().getState())) {
                    logger.info("Skipping webhook state: {}", payload.getMergeRequest().getState());
                    return new WebhookResult(false, "State not supported: " + payload.getMergeRequest().getState());
                }
                
                // Generate tests for changed files
                List<TestCase> generatedTests = generateTestsForMergeRequest(payload);
                
                // Execute tests
                TestExecutionEngine.TestExecutionResult executionResult = 
                    executeTestsForMergeRequest(payload, generatedTests);
                
                // Post results to MR
                postTestResultsToMR(payload, executionResult);
                
                return new WebhookResult(true, "Tests generated and executed successfully");
                
            } catch (Exception e) {
                logger.error("❌ Error processing GitLab webhook: {}", e.getMessage(), e);
                return new WebhookResult(false, "Error: " + e.getMessage());
            }
        });
    }
    
    /**
     * Handle Bitbucket webhook
     */
    public CompletableFuture<WebhookResult> handleBitbucketWebhook(BitbucketWebhookPayload payload) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                logger.info("🔄 Processing Bitbucket webhook for PR #{}", payload.getPullRequest().getId());
                
                if (!"opened".equals(payload.getPullRequest().getState()) && 
                    !"updated".equals(payload.getPullRequest().getState())) {
                    logger.info("Skipping webhook state: {}", payload.getPullRequest().getState());
                    return new WebhookResult(false, "State not supported: " + payload.getPullRequest().getState());
                }
                
                // Generate tests for changed files
                List<TestCase> generatedTests = generateTestsForBitbucketPR(payload);
                
                // Execute tests
                TestExecutionEngine.TestExecutionResult executionResult = 
                    executeTestsForBitbucketPR(payload, generatedTests);
                
                // Post results to PR
                postTestResultsToBitbucketPR(payload, executionResult);
                
                return new WebhookResult(true, "Tests generated and executed successfully");
                
            } catch (Exception e) {
                logger.error("❌ Error processing Bitbucket webhook: {}", e.getMessage(), e);
                return new WebhookResult(false, "Error: " + e.getMessage());
            }
        });
    }
    
    /**
     * Generate tests for GitHub pull request
     */
    private List<TestCase> generateTestsForPullRequest(GitHubWebhookPayload payload) {
        List<TestCase> allTests = new ArrayList<>();
        
        try {
            GitHubPullRequest pr = payload.getPullRequest();
            List<GitHubFile> changedFiles = pr.getChangedFiles();
            
            for (GitHubFile file : changedFiles) {
                if (isTestableFile(file.getFilename())) {
                    // Get code content
                    String codeContent = multiRepositoryService.getCodeContent(
                        "default", file.getFilename(), pr.getHead().getRef());
                    
                    if (codeContent != null) {
                        // Generate tests for each test type
                        for (TestType testType : Arrays.asList(TestType.UNIT, TestType.INTEGRATION)) {
                            try {
                                List<TestCase> tests = testGenerationService.generateTestCases(
                                    codeContent, testType, detectLanguage(file.getFilename()), file.getFilename())
                                    .get();
                                allTests.addAll(tests);
                            } catch (Exception e) {
                                logger.warn("Failed to generate tests for {}: {}", file.getFilename(), e.getMessage());
                            }
                        }
                    }
                }
            }
            
            logger.info("✅ Generated {} tests for GitHub PR #{}", allTests.size(), pr.getNumber());
            
        } catch (Exception e) {
            logger.error("❌ Error generating tests for GitHub PR: {}", e.getMessage(), e);
        }
        
        return allTests;
    }
    
    /**
     * Generate tests for GitLab merge request
     */
    private List<TestCase> generateTestsForMergeRequest(GitLabWebhookPayload payload) {
        List<TestCase> allTests = new ArrayList<>();
        
        try {
            GitLabMergeRequest mr = payload.getMergeRequest();
            List<GitLabFile> changedFiles = mr.getChangedFiles();
            
            for (GitLabFile file : changedFiles) {
                if (isTestableFile(file.getNewPath())) {
                    // Get code content
                    String codeContent = multiRepositoryService.getCodeContent(
                        "default", file.getNewPath(), mr.getSourceBranch());
                    
                    if (codeContent != null) {
                        // Generate tests for each test type
                        for (TestType testType : Arrays.asList(TestType.UNIT, TestType.INTEGRATION)) {
                            try {
                                List<TestCase> tests = testGenerationService.generateTestCases(
                                    codeContent, testType, detectLanguage(file.getNewPath()), file.getNewPath())
                                    .get();
                                allTests.addAll(tests);
                            } catch (Exception e) {
                                logger.warn("Failed to generate tests for {}: {}", file.getNewPath(), e.getMessage());
                            }
                        }
                    }
                }
            }
            
            logger.info("✅ Generated {} tests for GitLab MR #{}", allTests.size(), mr.getIid());
            
        } catch (Exception e) {
            logger.error("❌ Error generating tests for GitLab MR: {}", e.getMessage(), e);
        }
        
        return allTests;
    }
    
    /**
     * Generate tests for Bitbucket pull request
     */
    private List<TestCase> generateTestsForBitbucketPR(BitbucketWebhookPayload payload) {
        List<TestCase> allTests = new ArrayList<>();
        
        try {
            BitbucketPullRequest pr = payload.getPullRequest();
            List<BitbucketFile> changedFiles = pr.getChangedFiles();
            
            for (BitbucketFile file : changedFiles) {
                if (isTestableFile(file.getPath())) {
                    // Get code content
                    String codeContent = multiRepositoryService.getCodeContent(
                        "default", file.getPath(), pr.getSource().getBranch().getName());
                    
                    if (codeContent != null) {
                        // Generate tests for each test type
                        for (TestType testType : Arrays.asList(TestType.UNIT, TestType.INTEGRATION)) {
                            try {
                                List<TestCase> tests = testGenerationService.generateTestCases(
                                    codeContent, testType, detectLanguage(file.getPath()), file.getPath())
                                    .get();
                                allTests.addAll(tests);
                            } catch (Exception e) {
                                logger.warn("Failed to generate tests for {}: {}", file.getPath(), e.getMessage());
                            }
                        }
                    }
                }
            }
            
            logger.info("✅ Generated {} tests for Bitbucket PR #{}", allTests.size(), pr.getId());
            
        } catch (Exception e) {
            logger.error("❌ Error generating tests for Bitbucket PR: {}", e.getMessage(), e);
        }
        
        return allTests;
    }
    
    /**
     * Execute tests for GitHub pull request
     */
    private TestExecutionEngine.TestExecutionResult executeTestsForPullRequest(
            GitHubWebhookPayload payload, List<TestCase> tests) {
        try {
            return testExecutionEngine.executeTests(
                "default", "java", "spring-boot", tests).get();
        } catch (Exception e) {
            logger.error("❌ Error executing tests for GitHub PR: {}", e.getMessage(), e);
            return new TestExecutionEngine.TestExecutionResult(0, tests.size(), tests.size(), 
                Collections.singletonList("Test execution failed: " + e.getMessage()));
        }
    }
    
    /**
     * Execute tests for GitLab merge request
     */
    private TestExecutionEngine.TestExecutionResult executeTestsForMergeRequest(
            GitLabWebhookPayload payload, List<TestCase> tests) {
        try {
            return testExecutionEngine.executeTests(
                "default", "java", "spring-boot", tests).get();
        } catch (Exception e) {
            logger.error("❌ Error executing tests for GitLab MR: {}", e.getMessage(), e);
            return new TestExecutionEngine.TestExecutionResult(0, tests.size(), tests.size(), 
                Collections.singletonList("Test execution failed: " + e.getMessage()));
        }
    }
    
    /**
     * Execute tests for Bitbucket pull request
     */
    private TestExecutionEngine.TestExecutionResult executeTestsForBitbucketPR(
            BitbucketWebhookPayload payload, List<TestCase> tests) {
        try {
            return testExecutionEngine.executeTests(
                "default", "java", "spring-boot", tests).get();
        } catch (Exception e) {
            logger.error("❌ Error executing tests for Bitbucket PR: {}", e.getMessage(), e);
            return new TestExecutionEngine.TestExecutionResult(0, tests.size(), tests.size(), 
                Collections.singletonList("Test execution failed: " + e.getMessage()));
        }
    }
    
    /**
     * Post test results to GitHub PR
     */
    private void postTestResultsToPR(GitHubWebhookPayload payload, 
                                   TestExecutionEngine.TestExecutionResult result) {
        try {
            GitHubPullRequest pr = payload.getPullRequest();
            String comment = generateTestResultComment(result);
            
            String url = String.format("https://api.github.com/repos/%s/issues/%s/comments",
                payload.getRepository().getFullName(), pr.getNumber());
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "token " + getGitHubToken());
            
            Map<String, String> body = Map.of("body", comment);
            HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, headers);
            
            restTemplate.postForEntity(url, entity, String.class);
            
            logger.info("✅ Posted test results to GitHub PR #{}", pr.getNumber());
            
        } catch (Exception e) {
            logger.error("❌ Error posting results to GitHub PR: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Post test results to GitLab MR
     */
    private void postTestResultsToMR(GitLabWebhookPayload payload, 
                                   TestExecutionEngine.TestExecutionResult result) {
        try {
            GitLabMergeRequest mr = payload.getMergeRequest();
            String comment = generateTestResultComment(result);
            
            String url = String.format("https://gitlab.com/api/v4/projects/%s/merge_requests/%s/notes",
                payload.getProject().getId(), mr.getIid());
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("PRIVATE-TOKEN", getGitLabToken());
            
            Map<String, String> body = Map.of("body", comment);
            HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, headers);
            
            restTemplate.postForEntity(url, entity, String.class);
            
            logger.info("✅ Posted test results to GitLab MR #{}", mr.getIid());
            
        } catch (Exception e) {
            logger.error("❌ Error posting results to GitLab MR: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Post test results to Bitbucket PR
     */
    private void postTestResultsToBitbucketPR(BitbucketWebhookPayload payload, 
                                            TestExecutionEngine.TestExecutionResult result) {
        try {
            BitbucketPullRequest pr = payload.getPullRequest();
            String comment = generateTestResultComment(result);
            
            String url = String.format("https://api.bitbucket.org/2.0/repositories/%s/pullrequests/%s/comments",
                payload.getRepository().getFullName(), pr.getId());
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + getBitbucketToken());
            
            Map<String, Object> body = new HashMap<>();
            Map<String, String> content = new HashMap<>();
            content.put("raw", comment);
            body.put("content", content);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            
            restTemplate.postForEntity(url, entity, String.class);
            
            logger.info("✅ Posted test results to Bitbucket PR #{}", pr.getId());
            
        } catch (Exception e) {
            logger.error("❌ Error posting results to Bitbucket PR: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Generate test result comment
     */
    private String generateTestResultComment(TestExecutionEngine.TestExecutionResult result) {
        StringBuilder comment = new StringBuilder();
        
        comment.append("## 🤖 AI-Generated Test Results\n\n");
        comment.append("**Test Execution Summary:**\n");
        comment.append("- ✅ **Passed:** ").append(result.getPassedCount()).append("\n");
        comment.append("- ❌ **Failed:** ").append(result.getFailedCount()).append("\n");
        comment.append("- 📊 **Total:** ").append(result.getTotalCount()).append("\n");
        comment.append("- 🎯 **Success Rate:** ").append(String.format("%.1f%%", result.getSuccessRate() * 100)).append("\n");
        
        if (!result.getFailures().isEmpty()) {
            comment.append("\n**Failures:**\n");
            for (String failure : result.getFailures()) {
                comment.append("- ").append(failure).append("\n");
            }
        }
        
        comment.append("\n---\n");
        comment.append("*Generated by AI Test Platform*");
        
        return comment.toString();
    }
    
    /**
     * Check if file is testable
     */
    private boolean isTestableFile(String filename) {
        if (filename == null) return false;
        
        String lowerFilename = filename.toLowerCase();
        return lowerFilename.endsWith(".java") ||
               lowerFilename.endsWith(".js") ||
               lowerFilename.endsWith(".ts") ||
               lowerFilename.endsWith(".py") ||
               lowerFilename.endsWith(".cs") ||
               lowerFilename.endsWith(".go") ||
               lowerFilename.endsWith(".rs");
    }
    
    /**
     * Detect language from filename
     */
    private String detectLanguage(String filename) {
        if (filename == null) return "unknown";
        
        String lowerFilename = filename.toLowerCase();
        if (lowerFilename.endsWith(".java")) return "java";
        if (lowerFilename.endsWith(".js")) return "javascript";
        if (lowerFilename.endsWith(".ts")) return "typescript";
        if (lowerFilename.endsWith(".py")) return "python";
        if (lowerFilename.endsWith(".cs")) return "csharp";
        if (lowerFilename.endsWith(".go")) return "go";
        if (lowerFilename.endsWith(".rs")) return "rust";
        
        return "unknown";
    }
    
    /**
     * Get GitHub token
     */
    private String getGitHubToken() {
        return System.getenv("GITHUB_TOKEN");
    }
    
    /**
     * Get GitLab token
     */
    private String getGitLabToken() {
        return System.getenv("GITLAB_TOKEN");
    }
    
    /**
     * Get Bitbucket token
     */
    private String getBitbucketToken() {
        return System.getenv("BITBUCKET_TOKEN");
    }
    
    /**
     * Webhook result
     */
    public static class WebhookResult {
        private final boolean success;
        private final String message;
        
        public WebhookResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
        
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
    }
    
    // Webhook payload classes (simplified)
    public static class GitHubWebhookPayload {
        private String action;
        private GitHubPullRequest pullRequest;
        private GitHubRepository repository;
        
        // Getters and setters
        public String getAction() { return action; }
        public void setAction(String action) { this.action = action; }
        public GitHubPullRequest getPullRequest() { return pullRequest; }
        public void setPullRequest(GitHubPullRequest pullRequest) { this.pullRequest = pullRequest; }
        public GitHubRepository getRepository() { return repository; }
        public void setRepository(GitHubRepository repository) { this.repository = repository; }
    }
    
    public static class GitHubPullRequest {
        private int number;
        private GitHubHead head;
        private List<GitHubFile> changedFiles;
        
        // Getters and setters
        public int getNumber() { return number; }
        public void setNumber(int number) { this.number = number; }
        public GitHubHead getHead() { return head; }
        public void setHead(GitHubHead head) { this.head = head; }
        public List<GitHubFile> getChangedFiles() { return changedFiles; }
        public void setChangedFiles(List<GitHubFile> changedFiles) { this.changedFiles = changedFiles; }
    }
    
    public static class GitHubHead {
        private String ref;
        public String getRef() { return ref; }
        public void setRef(String ref) { this.ref = ref; }
    }
    
    public static class GitHubFile {
        private String filename;
        public String getFilename() { return filename; }
        public void setFilename(String filename) { this.filename = filename; }
    }
    
    public static class GitHubRepository {
        private String fullName;
        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }
    }
    
    // Similar classes for GitLab and Bitbucket...
    public static class GitLabWebhookPayload {
        private GitLabMergeRequest mergeRequest;
        private GitLabProject project;
        
        public GitLabMergeRequest getMergeRequest() { return mergeRequest; }
        public void setMergeRequest(GitLabMergeRequest mergeRequest) { this.mergeRequest = mergeRequest; }
        public GitLabProject getProject() { return project; }
        public void setProject(GitLabProject project) { this.project = project; }
    }
    
    public static class GitLabMergeRequest {
        private int iid;
        private String sourceBranch;
        private String state;
        private List<GitLabFile> changedFiles;
        
        public int getIid() { return iid; }
        public void setIid(int iid) { this.iid = iid; }
        public String getSourceBranch() { return sourceBranch; }
        public void setSourceBranch(String sourceBranch) { this.sourceBranch = sourceBranch; }
        public String getState() { return state; }
        public void setState(String state) { this.state = state; }
        public List<GitLabFile> getChangedFiles() { return changedFiles; }
        public void setChangedFiles(List<GitLabFile> changedFiles) { this.changedFiles = changedFiles; }
    }
    
    public static class GitLabFile {
        private String newPath;
        public String getNewPath() { return newPath; }
        public void setNewPath(String newPath) { this.newPath = newPath; }
    }
    
    public static class GitLabProject {
        private String id;
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
    }
    
    public static class BitbucketWebhookPayload {
        private BitbucketPullRequest pullRequest;
        private BitbucketRepository repository;
        
        public BitbucketPullRequest getPullRequest() { return pullRequest; }
        public void setPullRequest(BitbucketPullRequest pullRequest) { this.pullRequest = pullRequest; }
        public BitbucketRepository getRepository() { return repository; }
        public void setRepository(BitbucketRepository repository) { this.repository = repository; }
    }
    
    public static class BitbucketPullRequest {
        private String id;
        private String state;
        private BitbucketSource source;
        private List<BitbucketFile> changedFiles;
        
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getState() { return state; }
        public void setState(String state) { this.state = state; }
        public BitbucketSource getSource() { return source; }
        public void setSource(BitbucketSource source) { this.source = source; }
        public List<BitbucketFile> getChangedFiles() { return changedFiles; }
        public void setChangedFiles(List<BitbucketFile> changedFiles) { this.changedFiles = changedFiles; }
    }
    
    public static class BitbucketSource {
        private BitbucketBranch branch;
        public BitbucketBranch getBranch() { return branch; }
        public void setBranch(BitbucketBranch branch) { this.branch = branch; }
    }
    
    public static class BitbucketBranch {
        private String name;
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }
    
    public static class BitbucketFile {
        private String path;
        public String getPath() { return path; }
        public void setPath(String path) { this.path = path; }
    }
    
    public static class BitbucketRepository {
        private String fullName;
        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }
    }
}
