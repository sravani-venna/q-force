package com.testplatform.backend.dto;

import com.testplatform.backend.enums.PRStatus;
import com.testplatform.backend.model.PullRequest;

import java.time.LocalDateTime;
import java.util.List;

public class PullRequestDTO {
    private Long id;
    private Integer number;
    private String title;
    private String branch;
    private String author;
    private PRStatus status;
    private LocalDateTime createdAt;
    private Integer testsGenerated;
    private Integer testsPassed;
    private Integer testsFailed;
    private Double coverage;
    private List<PullRequest.ChangedFile> changedFiles;
    private List<TestSuiteDTO> generatedTests;
    
    // Constructors
    public PullRequestDTO() {}
    
    public PullRequestDTO(PullRequest pr) {
        this.id = pr.getId();
        this.number = pr.getNumber();
        this.title = pr.getTitle();
        this.branch = pr.getBranch();
        this.author = pr.getAuthor();
        this.status = pr.getStatus();
        this.createdAt = pr.getCreatedAt();
        this.testsGenerated = pr.getTestsGenerated();
        this.testsPassed = pr.getTestsPassed();
        this.testsFailed = pr.getTestsFailed();
        this.coverage = pr.getCoverage();
        this.changedFiles = pr.getChangedFiles();
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
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public Integer getTestsGenerated() { return testsGenerated; }
    public void setTestsGenerated(Integer testsGenerated) { this.testsGenerated = testsGenerated; }
    
    public Integer getTestsPassed() { return testsPassed; }
    public void setTestsPassed(Integer testsPassed) { this.testsPassed = testsPassed; }
    
    public Integer getTestsFailed() { return testsFailed; }
    public void setTestsFailed(Integer testsFailed) { this.testsFailed = testsFailed; }
    
    public Double getCoverage() { return coverage; }
    public void setCoverage(Double coverage) { this.coverage = coverage; }
    
    public List<PullRequest.ChangedFile> getChangedFiles() { return changedFiles; }
    public void setChangedFiles(List<PullRequest.ChangedFile> changedFiles) { this.changedFiles = changedFiles; }
    
    public List<TestSuiteDTO> getGeneratedTests() { return generatedTests; }
    public void setGeneratedTests(List<TestSuiteDTO> generatedTests) { this.generatedTests = generatedTests; }
}
