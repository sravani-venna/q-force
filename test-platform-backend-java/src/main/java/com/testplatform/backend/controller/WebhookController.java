package com.testplatform.backend.controller;

import com.testplatform.backend.dto.ApiResponse;
import com.testplatform.backend.service.CiCdIntegrationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/webhooks")
public class WebhookController {
    
    private static final Logger logger = LoggerFactory.getLogger(WebhookController.class);
    
    @Autowired
    private CiCdIntegrationService ciCdIntegrationService;
    
    /**
     * POST /api/webhooks/github - Handle GitHub webhooks
     */
    @PostMapping("/github")
    public ResponseEntity<ApiResponse<String>> handleGitHubWebhook(
            @RequestBody Map<String, Object> payload,
            @RequestHeader Map<String, String> headers) {
        
        try {
            logger.info("🔄 Received GitHub webhook");
            
            // Validate GitHub webhook signature
            if (!validateGitHubSignature(payload, headers)) {
                logger.warn("❌ Invalid GitHub webhook signature");
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Invalid webhook signature"));
            }
            
            // Process webhook asynchronously
            CompletableFuture<CiCdIntegrationService.WebhookResult> future = 
                ciCdIntegrationService.handleGitHubWebhook(convertToGitHubPayload(payload));
            
            // Return immediate response
            return ResponseEntity.ok(ApiResponse.success("Webhook received and processing"));
            
        } catch (Exception e) {
            logger.error("❌ Error handling GitHub webhook: {}", e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Error processing webhook"));
        }
    }
    
    /**
     * POST /api/webhooks/gitlab - Handle GitLab webhooks
     */
    @PostMapping("/gitlab")
    public ResponseEntity<ApiResponse<String>> handleGitLabWebhook(
            @RequestBody Map<String, Object> payload,
            @RequestHeader Map<String, String> headers) {
        
        try {
            logger.info("🔄 Received GitLab webhook");
            
            // Validate GitLab webhook token
            if (!validateGitLabToken(headers)) {
                logger.warn("❌ Invalid GitLab webhook token");
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Invalid webhook token"));
            }
            
            // Process webhook asynchronously
            CompletableFuture<CiCdIntegrationService.WebhookResult> future = 
                ciCdIntegrationService.handleGitLabWebhook(convertToGitLabPayload(payload));
            
            // Return immediate response
            return ResponseEntity.ok(ApiResponse.success("Webhook received and processing"));
            
        } catch (Exception e) {
            logger.error("❌ Error handling GitLab webhook: {}", e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Error processing webhook"));
        }
    }
    
    /**
     * POST /api/webhooks/bitbucket - Handle Bitbucket webhooks
     */
    @PostMapping("/bitbucket")
    public ResponseEntity<ApiResponse<String>> handleBitbucketWebhook(
            @RequestBody Map<String, Object> payload,
            @RequestHeader Map<String, String> headers) {
        
        try {
            logger.info("🔄 Received Bitbucket webhook");
            
            // Validate Bitbucket webhook signature
            if (!validateBitbucketSignature(payload, headers)) {
                logger.warn("❌ Invalid Bitbucket webhook signature");
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Invalid webhook signature"));
            }
            
            // Process webhook asynchronously
            CompletableFuture<CiCdIntegrationService.WebhookResult> future = 
                ciCdIntegrationService.handleBitbucketWebhook(convertToBitbucketPayload(payload));
            
            // Return immediate response
            return ResponseEntity.ok(ApiResponse.success("Webhook received and processing"));
            
        } catch (Exception e) {
            logger.error("❌ Error handling Bitbucket webhook: {}", e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Error processing webhook"));
        }
    }
    
    /**
     * GET /api/webhooks/status - Get webhook processing status
     */
    @GetMapping("/status")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getWebhookStatus() {
        try {
            Map<String, Object> status = Map.of(
                "github", Map.of("enabled", true, "endpoint", "/api/webhooks/github"),
                "gitlab", Map.of("enabled", true, "endpoint", "/api/webhooks/gitlab"),
                "bitbucket", Map.of("enabled", true, "endpoint", "/api/webhooks/bitbucket"),
                "totalProcessed", 0, // This would be tracked in a real implementation
                "lastProcessed", null
            );
            
            return ResponseEntity.ok(ApiResponse.success(status, "Webhook status retrieved"));
            
        } catch (Exception e) {
            logger.error("❌ Error getting webhook status: {}", e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Error getting webhook status"));
        }
    }
    
    /**
     * Validate GitHub webhook signature
     */
    private boolean validateGitHubSignature(Map<String, Object> payload, Map<String, String> headers) {
        String signature = headers.get("x-hub-signature-256");
        if (signature == null) {
            return false;
        }
        
        // In a real implementation, you would validate the HMAC signature
        // For now, we'll just check if the header exists
        return signature.startsWith("sha256=");
    }
    
    /**
     * Validate GitLab webhook token
     */
    private boolean validateGitLabToken(Map<String, String> headers) {
        String token = headers.get("x-gitlab-token");
        if (token == null) {
            return false;
        }
        
        // In a real implementation, you would validate against configured token
        return token.equals(System.getenv("GITLAB_WEBHOOK_TOKEN"));
    }
    
    /**
     * Validate Bitbucket webhook signature
     */
    private boolean validateBitbucketSignature(Map<String, Object> payload, Map<String, String> headers) {
        String signature = headers.get("x-hub-signature");
        if (signature == null) {
            return false;
        }
        
        // In a real implementation, you would validate the HMAC signature
        return signature.startsWith("sha256=");
    }
    
    /**
     * Convert generic payload to GitHub payload
     */
    private CiCdIntegrationService.GitHubWebhookPayload convertToGitHubPayload(Map<String, Object> payload) {
        CiCdIntegrationService.GitHubWebhookPayload githubPayload = new CiCdIntegrationService.GitHubWebhookPayload();
        
        // Extract action
        githubPayload.setAction((String) payload.get("action"));
        
        // Extract pull request
        Map<String, Object> prData = (Map<String, Object>) payload.get("pull_request");
        if (prData != null) {
            CiCdIntegrationService.GitHubPullRequest pr = new CiCdIntegrationService.GitHubPullRequest();
            pr.setNumber((Integer) prData.get("number"));
            
            // Extract head
            Map<String, Object> headData = (Map<String, Object>) prData.get("head");
            if (headData != null) {
                CiCdIntegrationService.GitHubHead head = new CiCdIntegrationService.GitHubHead();
                head.setRef((String) headData.get("ref"));
                pr.setHead(head);
            }
            
            githubPayload.setPullRequest(pr);
        }
        
        // Extract repository
        Map<String, Object> repoData = (Map<String, Object>) payload.get("repository");
        if (repoData != null) {
            CiCdIntegrationService.GitHubRepository repo = new CiCdIntegrationService.GitHubRepository();
            repo.setFullName((String) repoData.get("full_name"));
            githubPayload.setRepository(repo);
        }
        
        return githubPayload;
    }
    
    /**
     * Convert generic payload to GitLab payload
     */
    private CiCdIntegrationService.GitLabWebhookPayload convertToGitLabPayload(Map<String, Object> payload) {
        CiCdIntegrationService.GitLabWebhookPayload gitlabPayload = new CiCdIntegrationService.GitLabWebhookPayload();
        
        // Extract merge request
        Map<String, Object> mrData = (Map<String, Object>) payload.get("merge_request");
        if (mrData != null) {
            CiCdIntegrationService.GitLabMergeRequest mr = new CiCdIntegrationService.GitLabMergeRequest();
            mr.setIid((Integer) mrData.get("iid"));
            mr.setSourceBranch((String) mrData.get("source_branch"));
            mr.setState((String) mrData.get("state"));
            gitlabPayload.setMergeRequest(mr);
        }
        
        // Extract project
        Map<String, Object> projectData = (Map<String, Object>) payload.get("project");
        if (projectData != null) {
            CiCdIntegrationService.GitLabProject project = new CiCdIntegrationService.GitLabProject();
            project.setId(String.valueOf(projectData.get("id")));
            gitlabPayload.setProject(project);
        }
        
        return gitlabPayload;
    }
    
    /**
     * Convert generic payload to Bitbucket payload
     */
    private CiCdIntegrationService.BitbucketWebhookPayload convertToBitbucketPayload(Map<String, Object> payload) {
        CiCdIntegrationService.BitbucketWebhookPayload bitbucketPayload = new CiCdIntegrationService.BitbucketWebhookPayload();
        
        // Extract pull request
        Map<String, Object> prData = (Map<String, Object>) payload.get("pullrequest");
        if (prData != null) {
            CiCdIntegrationService.BitbucketPullRequest pr = new CiCdIntegrationService.BitbucketPullRequest();
            pr.setId(String.valueOf(prData.get("id")));
            pr.setState((String) prData.get("state"));
            
            // Extract source
            Map<String, Object> sourceData = (Map<String, Object>) prData.get("source");
            if (sourceData != null) {
                CiCdIntegrationService.BitbucketSource source = new CiCdIntegrationService.BitbucketSource();
                
                Map<String, Object> branchData = (Map<String, Object>) sourceData.get("branch");
                if (branchData != null) {
                    CiCdIntegrationService.BitbucketBranch branch = new CiCdIntegrationService.BitbucketBranch();
                    branch.setName((String) branchData.get("name"));
                    source.setBranch(branch);
                }
                
                pr.setSource(source);
            }
            
            bitbucketPayload.setPullRequest(pr);
        }
        
        // Extract repository
        Map<String, Object> repoData = (Map<String, Object>) payload.get("repository");
        if (repoData != null) {
            CiCdIntegrationService.BitbucketRepository repo = new CiCdIntegrationService.BitbucketRepository();
            repo.setFullName((String) repoData.get("full_name"));
            bitbucketPayload.setRepository(repo);
        }
        
        return bitbucketPayload;
    }
}