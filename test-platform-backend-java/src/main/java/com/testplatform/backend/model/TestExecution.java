package com.testplatform.backend.model;

import com.testplatform.backend.enums.ExecutionStatus;
import com.testplatform.backend.enums.ExecutionType;

import java.time.LocalDateTime;

public class TestExecution {
    private String id;
    private Integer prNumber;
    private String suiteId;
    private String branch;
    
    private ExecutionStatus status = ExecutionStatus.PENDING;
    private ExecutionType type = ExecutionType.SUITE;
    
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Long duration; // in milliseconds
    
    private TestResults results;
    private String errorMessage;
    
    // Constructors
    public TestExecution() {
        this.startTime = LocalDateTime.now();
    }
    
    public TestExecution(Integer prNumber, String branch) {
        this();
        this.prNumber = prNumber;
        this.branch = branch;
    }
    
    public TestExecution(String suiteId) {
        this();
        this.suiteId = suiteId;
    }
    
    // Inner class for test results
    public static class TestResults {
        private Integer total = 0;
        private Integer passed = 0;
        private Integer failed = 0;
        private Integer skipped = 0;
        
        public TestResults() {}
        
        public TestResults(Integer total, Integer passed, Integer failed, Integer skipped) {
            this.total = total;
            this.passed = passed;
            this.failed = failed;
            this.skipped = skipped;
        }
        
        // Getters and Setters
        public Integer getTotal() { return total; }
        public void setTotal(Integer total) { this.total = total; }
        
        public Integer getPassed() { return passed; }
        public void setPassed(Integer passed) { this.passed = passed; }
        
        public Integer getFailed() { return failed; }
        public void setFailed(Integer failed) { this.failed = failed; }
        
        public Integer getSkipped() { return skipped; }
        public void setSkipped(Integer skipped) { this.skipped = skipped; }
    }
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public Integer getPrNumber() { return prNumber; }
    public void setPrNumber(Integer prNumber) { this.prNumber = prNumber; }
    
    public String getSuiteId() { return suiteId; }
    public void setSuiteId(String suiteId) { this.suiteId = suiteId; }
    
    public String getBranch() { return branch; }
    public void setBranch(String branch) { this.branch = branch; }
    
    public ExecutionStatus getStatus() { return status; }
    public void setStatus(ExecutionStatus status) { this.status = status; }
    
    public ExecutionType getType() { return type; }
    public void setType(ExecutionType type) { this.type = type; }
    
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    
    public Long getDuration() { return duration; }
    public void setDuration(Long duration) { this.duration = duration; }
    
    public TestResults getResults() { return results; }
    public void setResults(TestResults results) { this.results = results; }
    
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
}
