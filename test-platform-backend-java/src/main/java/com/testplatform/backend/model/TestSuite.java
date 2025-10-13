package com.testplatform.backend.model;

import com.testplatform.backend.enums.TestType;
import com.testplatform.backend.enums.TestStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;

public class TestSuite {
    private String id;
    
    @NotNull(message = "Suite name is required")
    @Size(min = 5, max = 200, message = "Suite name must be between 5 and 200 characters")
    private String name;
    
    private Integer prNumber;
    private String branch;
    private String filePath;
    
    @NotNull(message = "Test type is required")
    private TestType type;
    
    @NotNull(message = "Language is required")
    private String language;
    
    private TestStatus status = TestStatus.PENDING;
    
    private List<TestCase> testCases;
    
    private LocalDateTime generatedAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastRun;
    
    // Constructors
    public TestSuite() {
        this.generatedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public TestSuite(String name, TestType type, String language) {
        this();
        this.name = name;
        this.type = type;
        this.language = language;
    }
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public Integer getPrNumber() { return prNumber; }
    public void setPrNumber(Integer prNumber) { this.prNumber = prNumber; }
    
    public String getBranch() { return branch; }
    public void setBranch(String branch) { this.branch = branch; }
    
    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }
    
    public TestType getType() { return type; }
    public void setType(TestType type) { this.type = type; }
    
    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }
    
    public TestStatus getStatus() { return status; }
    public void setStatus(TestStatus status) { this.status = status; }
    
    public List<TestCase> getTestCases() { return testCases; }
    public void setTestCases(List<TestCase> testCases) { this.testCases = testCases; }
    
    public LocalDateTime getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public LocalDateTime getLastRun() { return lastRun; }
    public void setLastRun(LocalDateTime lastRun) { this.lastRun = lastRun; }
}
