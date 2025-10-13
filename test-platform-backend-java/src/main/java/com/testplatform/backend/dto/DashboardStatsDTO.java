package com.testplatform.backend.dto;

import java.util.List;
import java.util.Map;

public class DashboardStatsDTO {
    private Integer totalTests;
    private Integer passedTests;
    private Integer failedTests;
    private Double coverage;
    private Integer executionTime;
    private Integer activePRs;
    private Integer mergedPRs;
    private Integer generatedTestSuites;
    private Integer runningTests;
    private List<TrendData> trendsData;
    private List<RecentPR> recentPRs;
    
    // Inner classes
    public static class TrendData {
        private String date;
        private Integer passed;
        private Integer failed;
        private Double coverage;
        private Integer prs;
        
        public TrendData() {}
        
        public TrendData(String date, Integer passed, Integer failed, Double coverage, Integer prs) {
            this.date = date;
            this.passed = passed;
            this.failed = failed;
            this.coverage = coverage;
            this.prs = prs;
        }
        
        // Getters and Setters
        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }
        
        public Integer getPassed() { return passed; }
        public void setPassed(Integer passed) { this.passed = passed; }
        
        public Integer getFailed() { return failed; }
        public void setFailed(Integer failed) { this.failed = failed; }
        
        public Double getCoverage() { return coverage; }
        public void setCoverage(Double coverage) { this.coverage = coverage; }
        
        public Integer getPrs() { return prs; }
        public void setPrs(Integer prs) { this.prs = prs; }
    }
    
    public static class RecentPR {
        private Long id;
        private Integer number;
        private String title;
        private String status;
        private Integer testsGenerated;
        private String passRate;
        
        public RecentPR() {}
        
        public RecentPR(Long id, Integer number, String title, String status, Integer testsGenerated, String passRate) {
            this.id = id;
            this.number = number;
            this.title = title;
            this.status = status;
            this.testsGenerated = testsGenerated;
            this.passRate = passRate;
        }
        
        // Getters and Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        
        public Integer getNumber() { return number; }
        public void setNumber(Integer number) { this.number = number; }
        
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        
        public Integer getTestsGenerated() { return testsGenerated; }
        public void setTestsGenerated(Integer testsGenerated) { this.testsGenerated = testsGenerated; }
        
        public String getPassRate() { return passRate; }
        public void setPassRate(String passRate) { this.passRate = passRate; }
    }
    
    // Getters and Setters
    public Integer getTotalTests() { return totalTests; }
    public void setTotalTests(Integer totalTests) { this.totalTests = totalTests; }
    
    public Integer getPassedTests() { return passedTests; }
    public void setPassedTests(Integer passedTests) { this.passedTests = passedTests; }
    
    public Integer getFailedTests() { return failedTests; }
    public void setFailedTests(Integer failedTests) { this.failedTests = failedTests; }
    
    public Double getCoverage() { return coverage; }
    public void setCoverage(Double coverage) { this.coverage = coverage; }
    
    public Integer getExecutionTime() { return executionTime; }
    public void setExecutionTime(Integer executionTime) { this.executionTime = executionTime; }
    
    public Integer getActivePRs() { return activePRs; }
    public void setActivePRs(Integer activePRs) { this.activePRs = activePRs; }
    
    public Integer getMergedPRs() { return mergedPRs; }
    public void setMergedPRs(Integer mergedPRs) { this.mergedPRs = mergedPRs; }
    
    public Integer getGeneratedTestSuites() { return generatedTestSuites; }
    public void setGeneratedTestSuites(Integer generatedTestSuites) { this.generatedTestSuites = generatedTestSuites; }
    
    public Integer getRunningTests() { return runningTests; }
    public void setRunningTests(Integer runningTests) { this.runningTests = runningTests; }
    
    public List<TrendData> getTrendsData() { return trendsData; }
    public void setTrendsData(List<TrendData> trendsData) { this.trendsData = trendsData; }
    
    public List<RecentPR> getRecentPRs() { return recentPRs; }
    public void setRecentPRs(List<RecentPR> recentPRs) { this.recentPRs = recentPRs; }
}
