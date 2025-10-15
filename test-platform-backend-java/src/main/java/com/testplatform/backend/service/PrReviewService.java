package com.testplatform.backend.service;

import com.testplatform.backend.config.AppProperties;
import com.testplatform.backend.dto.DiffComment;
import com.testplatform.backend.dto.PrReviewRequest;
import com.testplatform.backend.dto.PrReviewResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Service for PR review functionality
 */
@Service
public class PrReviewService {
    
    private static final Logger logger = LoggerFactory.getLogger(PrReviewService.class);
    
    @Autowired
    private GitHubApiClient githubApiClient;
    
    @Autowired
    private LocalLlmClient localLlmClient;
    
    @Autowired
    private AppProperties appProperties;
    
    /**
     * Review a pull request
     */
    public PrReviewResponse reviewPullRequest(PrReviewRequest request) {
        try {
            logger.info("🔍 Starting PR review for {}/{} PR #{}", 
                request.getRepoOwner(), request.getRepoName(), request.getPrNumber());
            
            // Fetch PR diff files from GitHub
            List<GitHubApiClient.PrDiffFile> diffFiles = githubApiClient.fetchPrDiffFiles(
                request.getRepoOwner(), request.getRepoName(), request.getPrNumber()
            );
            
            if (diffFiles.isEmpty()) {
                return new PrReviewResponse(
                    "no_suggestions",
                    request.getPrNumber(),
                    "No files changed in this PR",
                    new ArrayList<>(),
                    null
                );
            }
            
            // Review each diff file
            List<DiffComment> allComments = new ArrayList<>();
            StringBuilder reviewSummary = new StringBuilder();
            
            for (GitHubApiClient.PrDiffFile diffFile : diffFiles) {
                // Skip deleted files
                if ("deleted".equals(diffFile.getStatus())) {
                    continue;
                }
                
                // Only review Java files for now
                if (!diffFile.getFileName().endsWith(".java")) {
                    continue;
                }
                
                logger.info("🤖 Reviewing file: {}", diffFile.getFileName());
                
                PrReviewResponse fileReview = localLlmClient.reviewPrDiff(
                    diffFile.getFileName(), 
                    diffFile.getPatch(), 
                    request.getPrNumber()
                );
                
                if ("success".equals(fileReview.getStatus()) && !fileReview.getComments().isEmpty()) {
                    allComments.addAll(fileReview.getComments());
                }
                
                if (fileReview.getReviewSummary() != null) {
                    reviewSummary.append("**").append(diffFile.getFileName()).append("**: ")
                        .append(fileReview.getReviewSummary()).append("\n");
                }
            }
            
            // Determine overall status
            String overallStatus = allComments.isEmpty() ? "no_suggestions" : "success";
            String finalSummary = reviewSummary.length() > 0 ? reviewSummary.toString() : 
                (allComments.isEmpty() ? "No issues found in this PR" : 
                "Found " + allComments.size() + " suggestions across " + 
                diffFiles.stream().filter(f -> f.getFileName().endsWith(".java")).count() + " Java files");
            
            PrReviewResponse response = new PrReviewResponse(
                overallStatus,
                request.getPrNumber(),
                finalSummary,
                allComments,
                null
            );
            
            // Optionally post comments to GitHub
            if (appProperties.getReviewer().isAutoInlineComment() && !allComments.isEmpty()) {
                postCommentsToGitHub(request, allComments);
            }
            
            logger.info("✅ PR review completed: {} comments found", allComments.size());
            return response;
            
        } catch (Exception e) {
            logger.error("❌ Error reviewing PR: {}", e.getMessage(), e);
            return new PrReviewResponse(
                "error",
                request.getPrNumber(),
                null,
                new ArrayList<>(),
                "Error reviewing PR: " + e.getMessage()
            );
        }
    }
    
    /**
     * Post comments to GitHub PR
     */
    private void postCommentsToGitHub(PrReviewRequest request, List<DiffComment> comments) {
        try {
            // Get PR head commit SHA
            String commitSha = githubApiClient.getPrHeadCommitSha(
                request.getRepoOwner(), request.getRepoName(), request.getPrNumber()
            );
            
            if (commitSha == null) {
                logger.warn("⚠️ Could not get commit SHA for PR #{}", request.getPrNumber());
                return;
            }
            
            // Post each comment
            for (DiffComment comment : comments) {
                githubApiClient.postInlineComment(
                    request.getRepoOwner(),
                    request.getRepoName(),
                    request.getPrNumber(),
                    comment.getFileName(),
                    comment.getDiffLine(),
                    comment.getComment(),
                    commitSha
                );
                
                // Add small delay to avoid rate limiting
                Thread.sleep(1000);
            }
            
            logger.info("✅ Posted {} comments to GitHub PR #{}", comments.size(), request.getPrNumber());
            
        } catch (Exception e) {
            logger.error("❌ Error posting comments to GitHub: {}", e.getMessage(), e);
        }
    }
}
