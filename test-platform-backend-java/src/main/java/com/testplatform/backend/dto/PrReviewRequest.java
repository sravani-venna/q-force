package com.testplatform.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Request DTO for PR review functionality
 */
public class PrReviewRequest {
    
    @NotBlank(message = "Repository owner is required")
    private String repoOwner;
    
    @NotBlank(message = "Repository name is required")
    private String repoName;
    
    @NotNull(message = "PR number is required")
    private Integer prNumber;
    
    // Default constructor
    public PrReviewRequest() {}
    
    // Constructor with parameters
    public PrReviewRequest(String repoOwner, String repoName, Integer prNumber) {
        this.repoOwner = repoOwner;
        this.repoName = repoName;
        this.prNumber = prNumber;
    }
    
    // Getters and Setters
    public String getRepoOwner() { return repoOwner; }
    public void setRepoOwner(String repoOwner) { this.repoOwner = repoOwner; }
    
    public String getRepoName() { return repoName; }
    public void setRepoName(String repoName) { this.repoName = repoName; }
    
    public Integer getPrNumber() { return prNumber; }
    public void setPrNumber(Integer prNumber) { this.prNumber = prNumber; }
}
