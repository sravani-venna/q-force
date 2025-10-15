package com.testplatform.backend.dto;

import java.util.List;

/**
 * Response DTO for PR review functionality
 */
public class PrReviewResponse {
    
    private String status;
    private Integer prNumber;
    private String reviewSummary;
    private List<DiffComment> comments;
    private String errorMessage;
    
    // Default constructor
    public PrReviewResponse() {}
    
    // Constructor with parameters
    public PrReviewResponse(String status, Integer prNumber, String reviewSummary, 
                           List<DiffComment> comments, String errorMessage) {
        this.status = status;
        this.prNumber = prNumber;
        this.reviewSummary = reviewSummary;
        this.comments = comments;
        this.errorMessage = errorMessage;
    }
    
    // Getters and Setters
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public Integer getPrNumber() { return prNumber; }
    public void setPrNumber(Integer prNumber) { this.prNumber = prNumber; }
    
    public String getReviewSummary() { return reviewSummary; }
    public void setReviewSummary(String reviewSummary) { this.reviewSummary = reviewSummary; }
    
    public List<DiffComment> getComments() { return comments; }
    public void setComments(List<DiffComment> comments) { this.comments = comments; }
    
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
}
