package com.testplatform.backend.dto;

import com.testplatform.backend.enums.TestStatus;
import com.testplatform.backend.enums.TestType;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * DTO for service-level aggregated test results
 */
public class ServiceTestSummaryDTO {
    private String serviceName;
    private String serviceId;
    private Integer totalTestSuites;
    private Integer totalTestCases;
    private Integer passedTests;
    private Integer failedTests;
    private Integer pendingTests;
    private Double passRate;
    private Double avgCoverage;
    private Long totalExecutionTime;
    private LocalDateTime lastRun;
    private TestStatus overallStatus;
    
    // Breakdown by test type
    private Map<String, Integer> testsByType;
    private Map<String, Integer> suitesByType;
    
    // Service metadata
    private String description;
    private String repository;
    
    public ServiceTestSummaryDTO() {
        this.testsByType = new HashMap<>();
        this.suitesByType = new HashMap<>();
    }
    
    public ServiceTestSummaryDTO(String serviceName, String serviceId) {
        this();
        this.serviceName = serviceName;
        this.serviceId = serviceId;
    }
    
    // Getters and Setters
    public String getServiceName() { return serviceName; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }
    
    public String getServiceId() { return serviceId; }
    public void setServiceId(String serviceId) { this.serviceId = serviceId; }
    
    public Integer getTotalTestSuites() { return totalTestSuites; }
    public void setTotalTestSuites(Integer totalTestSuites) { this.totalTestSuites = totalTestSuites; }
    
    public Integer getTotalTestCases() { return totalTestCases; }
    public void setTotalTestCases(Integer totalTestCases) { this.totalTestCases = totalTestCases; }
    
    public Integer getPassedTests() { return passedTests; }
    public void setPassedTests(Integer passedTests) { this.passedTests = passedTests; }
    
    public Integer getFailedTests() { return failedTests; }
    public void setFailedTests(Integer failedTests) { this.failedTests = failedTests; }
    
    public Integer getPendingTests() { return pendingTests; }
    public void setPendingTests(Integer pendingTests) { this.pendingTests = pendingTests; }
    
    public Double getPassRate() { return passRate; }
    public void setPassRate(Double passRate) { this.passRate = passRate; }
    
    public Double getAvgCoverage() { return avgCoverage; }
    public void setAvgCoverage(Double avgCoverage) { this.avgCoverage = avgCoverage; }
    
    public Long getTotalExecutionTime() { return totalExecutionTime; }
    public void setTotalExecutionTime(Long totalExecutionTime) { this.totalExecutionTime = totalExecutionTime; }
    
    public LocalDateTime getLastRun() { return lastRun; }
    public void setLastRun(LocalDateTime lastRun) { this.lastRun = lastRun; }
    
    public TestStatus getOverallStatus() { return overallStatus; }
    public void setOverallStatus(TestStatus overallStatus) { this.overallStatus = overallStatus; }
    
    public Map<String, Integer> getTestsByType() { return testsByType; }
    public void setTestsByType(Map<String, Integer> testsByType) { this.testsByType = testsByType; }
    
    public Map<String, Integer> getSuitesByType() { return suitesByType; }
    public void setSuitesByType(Map<String, Integer> suitesByType) { this.suitesByType = suitesByType; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getRepository() { return repository; }
    public void setRepository(String repository) { this.repository = repository; }
}

