package com.testplatform.backend.controller;

import com.testplatform.backend.dto.ApiResponse;
import com.testplatform.backend.dto.PrReviewRequest;
import com.testplatform.backend.dto.PrReviewResponse;
import com.testplatform.backend.service.PrReviewService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * Controller for PR review functionality
 */
@RestController
@RequestMapping("/api/review")
public class PrReviewController {
    
    private static final Logger logger = LoggerFactory.getLogger(PrReviewController.class);
    
    @Autowired
    private PrReviewService prReviewService;
    
    /**
     * Review a pull request
     */
    @PostMapping("/pr")
    public ResponseEntity<ApiResponse<PrReviewResponse>> reviewPullRequest(
            @Valid @RequestBody PrReviewRequest request) {
        
        try {
            logger.info("🔍 Received PR review request for {}/{} PR #{}", 
                request.getRepoOwner(), request.getRepoName(), request.getPrNumber());
            
            PrReviewResponse response = prReviewService.reviewPullRequest(request);
            
            return ResponseEntity.ok(ApiResponse.success(response, "PR review completed successfully"));
            
        } catch (Exception e) {
            logger.error("❌ Error in PR review endpoint: {}", e.getMessage(), e);
            
            PrReviewResponse errorResponse = new PrReviewResponse(
                "error",
                request.getPrNumber(),
                null,
                null,
                "Internal server error: " + e.getMessage()
            );
            
            return ResponseEntity.ok(ApiResponse.success(errorResponse, "PR review completed with errors"));
        }
    }
    
    /**
     * Health check for PR review service
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> healthCheck() {
        return ResponseEntity.ok(ApiResponse.success("PR Review service is healthy", "Service is running"));
    }
}
