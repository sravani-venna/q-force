package com.testplatform.backend.dto;

import com.testplatform.backend.enums.TestStatus;
import com.testplatform.backend.enums.TestType;
import com.testplatform.backend.enums.TestPriority;

import java.time.LocalDateTime;

public class DetailedTestCaseDTO {
    private String id;
    private String name;
    private TestType type;
    private TestStatus status;
    private TestPriority priority;
    private String description;
    private String filePath;
    private String language;
    private String code;
    private LocalDateTime executedAt;
    private Long executionTime;
    private String errorMessage;
    private String suiteName;
    private String suiteId;

    // Constructors
    public DetailedTestCaseDTO() {}

    public DetailedTestCaseDTO(String id, String name, TestType type, TestStatus status, 
                              String description, String suiteName, String suiteId) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.status = status;
        this.description = description;
        this.suiteName = suiteName;
        this.suiteId = suiteId;
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

    public LocalDateTime getExecutedAt() { return executedAt; }
    public void setExecutedAt(LocalDateTime executedAt) { this.executedAt = executedAt; }

    public Long getExecutionTime() { return executionTime; }
    public void setExecutionTime(Long executionTime) { this.executionTime = executionTime; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public String getSuiteName() { return suiteName; }
    public void setSuiteName(String suiteName) { this.suiteName = suiteName; }

    public String getSuiteId() { return suiteId; }
    public void setSuiteId(String suiteId) { this.suiteId = suiteId; }
}
