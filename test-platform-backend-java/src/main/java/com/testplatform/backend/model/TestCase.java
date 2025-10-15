package com.testplatform.backend.model;

import com.testplatform.backend.enums.TestType;
import com.testplatform.backend.enums.TestStatus;
import com.testplatform.backend.enums.TestPriority;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import java.time.LocalDateTime;

public class TestCase {
    private String id;
    
    @NotNull(message = "Test name is required")
    @Size(min = 5, max = 200, message = "Test name must be between 5 and 200 characters")
    private String name;
    
    @NotNull(message = "Test type is required")
    private TestType type;
    
    private TestStatus status = TestStatus.PENDING;
    private TestPriority priority = TestPriority.MEDIUM;
    
    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;
    
    private String filePath;
    private String language;
    private String code;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime executedAt;
    
    private Long executionTime; // in milliseconds
    private String errorMessage;
    
    // Constructors
    public TestCase() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public TestCase(String name, TestType type, TestPriority priority, String description) {
        this();
        this.name = name;
        this.type = type;
        this.priority = priority;
        this.description = description;
    }
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public TestType getType() { return type; }
    public void setType(TestType type) { this.type = type; }
    
    public TestStatus getStatus() { return status; }
    public void setStatus(TestStatus status) { this.status = status; }
    
    public TestPriority getPriority() { return priority; }
    public void setPriority(TestPriority priority) { this.priority = priority; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }
    
    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }
    
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public LocalDateTime getExecutedAt() { return executedAt; }
    public void setExecutedAt(LocalDateTime executedAt) { this.executedAt = executedAt; }
    
    public Long getExecutionTime() { return executionTime; }
    public void setExecutionTime(Long executionTime) { this.executionTime = executionTime; }
    
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
}
