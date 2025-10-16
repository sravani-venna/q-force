package com.testplatform.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.testplatform.backend.config.AppProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for interacting with GitHub API
 */
@Service
public class GitHubApiClient {
    
    private static final Logger logger = LoggerFactory.getLogger(GitHubApiClient.class);
    
    @Value("${app.github.token:}")
    private String githubToken;
    
    @Autowired
    private RestTemplate restTemplate;
    
    @Autowired
    private AppProperties appProperties;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    private static final String GITHUB_API_BASE = "https://api.github.com";
    
    @jakarta.annotation.PostConstruct
    public void init() {
        logger.info("=== GitHubApiClient Initialized ===");
        logger.info("Token from @Value: {}", githubToken != null && !githubToken.isEmpty() ? 
                    "Configured (" + githubToken.length() + " chars)" : "NOT CONFIGURED");
        if (githubToken != null && !githubToken.isEmpty()) {
            logger.info("Token prefix: {}...", githubToken.substring(0, Math.min(10, githubToken.length())));
        }
        logger.info("===================================");
    }
    
    /**
     * Fetch PR diff files from GitHub
     */
    public List<PrDiffFile> fetchPrDiffFiles(String repoOwner, String repoName, int prNumber) {
        try {
            String url = String.format("%s/repos/%s/%s/pulls/%d/files", 
                GITHUB_API_BASE, repoOwner, repoName, prNumber);
            
            HttpHeaders headers = createGitHubHeaders();
            logger.info("📤 Fetching PR files from: {}", url);
            logger.info("📤 Authorization header: {}", headers.getFirst("Authorization") != null ? 
                       "Present (length: " + headers.getFirst("Authorization").length() + ")" : "MISSING!");
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.GET, entity, String.class
            );
            
            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode filesNode = objectMapper.readTree(response.getBody());
                List<PrDiffFile> diffFiles = new ArrayList<>();
                
                for (JsonNode fileNode : filesNode) {
                    PrDiffFile diffFile = new PrDiffFile(
                        fileNode.get("filename").asText(),
                        fileNode.get("patch").asText(),
                        fileNode.get("status").asText(),
                        fileNode.get("additions").asInt(),
                        fileNode.get("deletions").asInt()
                    );
                    diffFiles.add(diffFile);
                }
                
                logger.info("✅ Fetched {} diff files for PR #{}", diffFiles.size(), prNumber);
                return diffFiles;
            } else {
                logger.error("❌ Failed to fetch PR diff files: {}", response.getStatusCode());
                throw new RuntimeException("Failed to fetch PR diff files: " + response.getStatusCode());
            }
            
        } catch (Exception e) {
            logger.error("❌ Error fetching PR diff files: {}", e.getMessage(), e);
            throw new RuntimeException("Error fetching PR diff files", e);
        }
    }
    
    /**
     * Post inline comment on GitHub PR
     */
    public void postInlineComment(String repoOwner, String repoName, int prNumber, 
                                 String fileName, int diffLine, String comment, String commitId) {
        try {
            String url = String.format("%s/repos/%s/%s/pulls/%d/comments", 
                GITHUB_API_BASE, repoOwner, repoName, prNumber);
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("body", comment);
            requestBody.put("commit_id", commitId);
            requestBody.put("path", fileName);
            requestBody.put("position", diffLine);
            
            HttpHeaders headers = createGitHubHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.POST, entity, String.class
            );
            
            if (response.getStatusCode() == HttpStatus.CREATED) {
                logger.info("✅ Posted inline comment on PR #{} at line {}", prNumber, diffLine);
            } else {
                logger.error("❌ Failed to post inline comment: {}", response.getStatusCode());
            }
            
        } catch (Exception e) {
            logger.error("❌ Error posting inline comment: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Get PR details including head commit SHA
     */
    public String getPrHeadCommitSha(String repoOwner, String repoName, int prNumber) {
        try {
            String url = String.format("%s/repos/%s/%s/pulls/%d", 
                GITHUB_API_BASE, repoOwner, repoName, prNumber);
            
            HttpHeaders headers = createGitHubHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.GET, entity, String.class
            );
            
            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode prNode = objectMapper.readTree(response.getBody());
                return prNode.get("head").get("sha").asText();
            } else {
                logger.error("❌ Failed to fetch PR details: {}", response.getStatusCode());
                return null;
            }
            
        } catch (Exception e) {
            logger.error("❌ Error fetching PR details: {}", e.getMessage(), e);
            return null;
        }
    }
    
    private HttpHeaders createGitHubHeaders() {
        HttpHeaders headers = new HttpHeaders();
        
        // Use the @Value injected token
        String token = githubToken;
        
        if (token != null && !token.isEmpty()) {
            logger.debug("✅ Using GitHub token (length: {})", token.length());
            headers.set("Authorization", "token " + token);
        } else {
            logger.error("❌ GitHub token is missing!");
        }
        
        headers.set("Accept", "application/vnd.github.v3+json");
        headers.set("User-Agent", "TestPlatform-Backend");
        return headers;
    }
    
    /**
     * Data class for PR diff file information
     */
    public static class PrDiffFile {
        private final String fileName;
        private final String patch;
        private final String status;
        private final int additions;
        private final int deletions;
        
        public PrDiffFile(String fileName, String patch, String status, int additions, int deletions) {
            this.fileName = fileName;
            this.patch = patch;
            this.status = status;
            this.additions = additions;
            this.deletions = deletions;
        }
        
        public String getFileName() { return fileName; }
        public String getPatch() { return patch; }
        public String getStatus() { return status; }
        public int getAdditions() { return additions; }
        public int getDeletions() { return deletions; }
    }
}
