package com.testplatform.backend.model;

import com.testplatform.backend.enums.PRStatus;
import com.testplatform.backend.enums.PRPriority;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;

public class PullRequest {
    private Long id;
    
    @NotNull(message = "PR number is required")
    private Integer number;
    
    @NotNull(message = "Title is required")
    @Size(min = 5, max = 200, message = "Title must be between 5 and 200 characters")
    private String title;
    
    @NotNull(message = "Branch is required")
    private String branch;
    
    @NotNull(message = "Author is required")
    private String author;
    
    private PRStatus status = PRStatus.OPEN;
    private PRPriority priority = PRPriority.MEDIUM;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime mergedAt;
    
    private Integer testsGenerated = 0;
    private Integer testsPassed = 0;
    private Integer testsFailed = 0;
    private Double coverage = 0.0;
    
    private List<ChangedFile> changedFiles;
    
    // Constructors
    public PullRequest() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public PullRequest(Integer number, String title, String branch, String author) {
        this();
        this.number = number;
        this.title = title;
        this.branch = branch;
        this.author = author;
    }
    
    // Inner class for changed files
    public static class ChangedFile {
        private String filename;
        private Integer additions;
        private Integer deletions;
        
        public ChangedFile() {}
        
        public ChangedFile(String filename, Integer additions, Integer deletions) {
            this.filename = filename;
            this.additions = additions;
            this.deletions = deletions;
        }
        
        // Getters and Setters
        public String getFilename() { return filename; }
        public void setFilename(String filename) { this.filename = filename; }
        
        public Integer getAdditions() { return additions; }
        public void setAdditions(Integer additions) { this.additions = additions; }
        
        public Integer getDeletions() { return deletions; }
        public void setDeletions(Integer deletions) { this.deletions = deletions; }
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Integer getNumber() { return number; }
    public void setNumber(Integer number) { this.number = number; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getBranch() { return branch; }
    public void setBranch(String branch) { this.branch = branch; }
    
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    
    public PRStatus getStatus() { return status; }
    public void setStatus(PRStatus status) { this.status = status; }
    
    public PRPriority getPriority() { return priority; }
    public void setPriority(PRPriority priority) { this.priority = priority; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public LocalDateTime getMergedAt() { return mergedAt; }
    public void setMergedAt(LocalDateTime mergedAt) { this.mergedAt = mergedAt; }
    
    public Integer getTestsGenerated() { return testsGenerated; }
    public void setTestsGenerated(Integer testsGenerated) { this.testsGenerated = testsGenerated; }
    
    public Integer getTestsPassed() { return testsPassed; }
    public void setTestsPassed(Integer testsPassed) { this.testsPassed = testsPassed; }
    
    public Integer getTestsFailed() { return testsFailed; }
    public void setTestsFailed(Integer testsFailed) { this.testsFailed = testsFailed; }
    
    public Double getCoverage() { return coverage; }
    public void setCoverage(Double coverage) { this.coverage = coverage; }
    
    public List<ChangedFile> getChangedFiles() { return changedFiles; }
    public void setChangedFiles(List<ChangedFile> changedFiles) { this.changedFiles = changedFiles; }
}
