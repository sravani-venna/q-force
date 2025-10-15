package com.testplatform.backend.service;

import com.testplatform.backend.enums.TestType;
import com.testplatform.backend.model.TestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Service
public class TestOrchestrationService {
    
    private static final Logger logger = LoggerFactory.getLogger(TestOrchestrationService.class);
    
    @Autowired
    private TestExecutionEngine testExecutionEngine;
    
    @Autowired
    private PathFlowAnalysisService pathFlowAnalysisService;
    
    @Autowired
    private LlmService llmService;
    
    private final ExecutorService executorService = Executors.newFixedThreadPool(10);
    
    /**
     * Orchestrate comprehensive test execution across all services
     */
    public CompletableFuture<TestOrchestrationResult> orchestrateTests(
            String pathFlow, List<String> services, Map<String, String> serviceConfigs) {
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                logger.info("🎯 Starting test orchestration for path flow: {}", pathFlow);
                
                // Analyze path flow
                PathFlowAnalysisService.PathFlowAnalysisResult analysis = 
                    pathFlowAnalysisService.analyzePathFlow("orchestration", pathFlow, services, serviceConfigs);
                
                // Execute tests in parallel across all services
                List<CompletableFuture<ServiceTestResult>> serviceFutures = new ArrayList<>();
                
                for (PathFlowAnalysisService.ServiceAnalysis serviceAnalysis : analysis.getServiceAnalyses()) {
                    CompletableFuture<ServiceTestResult> future = executeServiceTests(serviceAnalysis);
                    serviceFutures.add(future);
                }
                
                // Execute integration tests
                CompletableFuture<List<TestCase>> integrationFuture = executeIntegrationTests(analysis.getIntegrationTests());
                
                // Execute end-to-end tests
                CompletableFuture<List<TestCase>> e2eFuture = executeE2eTests(analysis.getE2eTests());
                
                // Wait for all tests to complete
                List<ServiceTestResult> serviceResults = serviceFutures.stream()
                    .map(CompletableFuture::join)
                    .collect(Collectors.toList());
                
                List<TestCase> integrationResults = integrationFuture.join();
                List<TestCase> e2eResults = e2eFuture.join();
                
                // Generate comprehensive report
                TestOrchestrationResult result = generateOrchestrationReport(
                    analysis, serviceResults, integrationResults, e2eResults);
                
                logger.info("✅ Test orchestration completed: {} services, {} total tests", 
                    serviceResults.size(), result.getTotalTests());
                
                return result;
                
            } catch (Exception e) {
                logger.error("❌ Error in test orchestration: {}", e.getMessage(), e);
                throw new RuntimeException("Test orchestration failed", e);
            }
        }, executorService);
    }
    
    /**
     * Execute tests for a specific service
     */
    private CompletableFuture<ServiceTestResult> executeServiceTests(
            PathFlowAnalysisService.ServiceAnalysis serviceAnalysis) {
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                logger.info("🧪 Executing tests for service: {}", serviceAnalysis.getServiceName());
                
                // Execute unit tests
                List<TestCase> unitTestResults = new ArrayList<>();
                for (TestCase test : serviceAnalysis.getUnitTests()) {
                    TestExecutionEngine.TestExecutionResult executionResult = 
                        testExecutionEngine.executeTests(
                            serviceAnalysis.getServiceName(),
                            serviceAnalysis.getLanguage(),
                            serviceAnalysis.getFramework(),
                            Arrays.asList(test)
                        ).get();
                    
                    test.setStatus(executionResult.isSuccess() ? 
                        com.testplatform.backend.enums.TestStatus.PASSED : 
                        com.testplatform.backend.enums.TestStatus.FAILED);
                    unitTestResults.add(test);
                }
                
                // Execute integration tests
                List<TestCase> integrationTestResults = new ArrayList<>();
                for (TestCase test : serviceAnalysis.getIntegrationTests()) {
                    TestExecutionEngine.TestExecutionResult executionResult = 
                        testExecutionEngine.executeTests(
                            serviceAnalysis.getServiceName(),
                            serviceAnalysis.getLanguage(),
                            serviceAnalysis.getFramework(),
                            Arrays.asList(test)
                        ).get();
                    
                    test.setStatus(executionResult.isSuccess() ? 
                        com.testplatform.backend.enums.TestStatus.PASSED : 
                        com.testplatform.backend.enums.TestStatus.FAILED);
                    integrationTestResults.add(test);
                }
                
                // Calculate service metrics
                int totalTests = unitTestResults.size() + integrationTestResults.size();
                int passedTests = (int) unitTestResults.stream()
                    .filter(t -> t.getStatus() == com.testplatform.backend.enums.TestStatus.PASSED)
                    .count() + (int) integrationTestResults.stream()
                    .filter(t -> t.getStatus() == com.testplatform.backend.enums.TestStatus.PASSED)
                    .count();
                
                double successRate = totalTests > 0 ? (double) passedTests / totalTests : 0.0;
                
                return new ServiceTestResult(
                    serviceAnalysis.getServiceName(),
                    serviceAnalysis.getLanguage(),
                    serviceAnalysis.getFramework(),
                    unitTestResults,
                    integrationTestResults,
                    totalTests,
                    passedTests,
                    successRate
                );
                
            } catch (Exception e) {
                logger.error("❌ Error executing tests for service {}: {}", 
                    serviceAnalysis.getServiceName(), e.getMessage(), e);
                
                return new ServiceTestResult(
                    serviceAnalysis.getServiceName(),
                    serviceAnalysis.getLanguage(),
                    serviceAnalysis.getFramework(),
                    new ArrayList<>(),
                    new ArrayList<>(),
                    0,
                    0,
                    0.0
                );
            }
        }, executorService);
    }
    
    /**
     * Execute integration tests
     */
    private CompletableFuture<List<TestCase>> executeIntegrationTests(List<TestCase> integrationTests) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                logger.info("🔗 Executing {} integration tests", integrationTests.size());
                
                List<TestCase> results = new ArrayList<>();
                for (TestCase test : integrationTests) {
                    TestExecutionEngine.TestExecutionResult executionResult = 
                        testExecutionEngine.executeTests(
                            "integration",
                            "java",
                            "spring-boot",
                            Arrays.asList(test)
                        ).get();
                    
                    test.setStatus(executionResult.isSuccess() ? 
                        com.testplatform.backend.enums.TestStatus.PASSED : 
                        com.testplatform.backend.enums.TestStatus.FAILED);
                    results.add(test);
                }
                
                return results;
                
            } catch (Exception e) {
                logger.error("❌ Error executing integration tests: {}", e.getMessage(), e);
                return new ArrayList<>();
            }
        }, executorService);
    }
    
    /**
     * Execute end-to-end tests
     */
    private CompletableFuture<List<TestCase>> executeE2eTests(List<TestCase> e2eTests) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                logger.info("🎯 Executing {} end-to-end tests", e2eTests.size());
                
                List<TestCase> results = new ArrayList<>();
                for (TestCase test : e2eTests) {
                    TestExecutionEngine.TestExecutionResult executionResult = 
                        testExecutionEngine.executeTests(
                            "e2e",
                            "java",
                            "spring-boot",
                            Arrays.asList(test)
                        ).get();
                    
                    test.setStatus(executionResult.isSuccess() ? 
                        com.testplatform.backend.enums.TestStatus.PASSED : 
                        com.testplatform.backend.enums.TestStatus.FAILED);
                    results.add(test);
                }
                
                return results;
                
            } catch (Exception e) {
                logger.error("❌ Error executing end-to-end tests: {}", e.getMessage(), e);
                return new ArrayList<>();
            }
        }, executorService);
    }
    
    /**
     * Generate comprehensive orchestration report
     */
    private TestOrchestrationResult generateOrchestrationReport(
            PathFlowAnalysisService.PathFlowAnalysisResult analysis,
            List<ServiceTestResult> serviceResults,
            List<TestCase> integrationResults,
            List<TestCase> e2eResults) {
        
        // Calculate overall metrics
        int totalTests = serviceResults.stream().mapToInt(ServiceTestResult::getTotalTests).sum() +
                        integrationResults.size() + e2eResults.size();
        
        int passedTests = serviceResults.stream().mapToInt(ServiceTestResult::getPassedTests).sum() +
                         (int) integrationResults.stream()
                             .filter(t -> t.getStatus() == com.testplatform.backend.enums.TestStatus.PASSED)
                             .count() +
                         (int) e2eResults.stream()
                             .filter(t -> t.getStatus() == com.testplatform.backend.enums.TestStatus.PASSED)
                             .count();
        
        double overallSuccessRate = totalTests > 0 ? (double) passedTests / totalTests : 0.0;
        
        // Calculate coverage metrics
        double serviceCoverage = analysis.getMetrics().getServiceCoverage();
        double testCoverage = analysis.getMetrics().getTestCoverage();
        
        // Generate recommendations
        List<String> recommendations = generateRecommendations(serviceResults, overallSuccessRate, testCoverage);
        
        // Generate quality gates
        List<QualityGate> qualityGates = generateQualityGates(serviceResults, overallSuccessRate, testCoverage);
        
        return new TestOrchestrationResult(
            analysis.getComponents(),
            serviceResults,
            integrationResults,
            e2eResults,
            totalTests,
            passedTests,
            overallSuccessRate,
            serviceCoverage,
            testCoverage,
            recommendations,
            qualityGates
        );
    }
    
    /**
     * Generate recommendations based on test results
     */
    private List<String> generateRecommendations(List<ServiceTestResult> serviceResults, 
                                                double overallSuccessRate, double testCoverage) {
        List<String> recommendations = new ArrayList<>();
        
        if (overallSuccessRate < 0.8) {
            recommendations.add("Overall test success rate is below 80%. Review failing tests and improve test quality.");
        }
        
        if (testCoverage < 0.7) {
            recommendations.add("Test coverage is below 70%. Add more unit tests for better coverage.");
        }
        
        for (ServiceTestResult serviceResult : serviceResults) {
            if (serviceResult.getSuccessRate() < 0.8) {
                recommendations.add(String.format("Service %s has low success rate (%.1f%%). Review and fix failing tests.", 
                    serviceResult.getServiceName(), serviceResult.getSuccessRate() * 100));
            }
        }
        
        if (recommendations.isEmpty()) {
            recommendations.add("All tests are passing with good coverage. Great job!");
        }
        
        return recommendations;
    }
    
    /**
     * Generate quality gates for CI/CD pipeline
     */
    private List<QualityGate> generateQualityGates(List<ServiceTestResult> serviceResults, 
                                                  double overallSuccessRate, double testCoverage) {
        List<QualityGate> qualityGates = new ArrayList<>();
        
        // Overall success rate gate
        qualityGates.add(new QualityGate(
            "Overall Success Rate",
            overallSuccessRate >= 0.8,
            String.format("Success rate: %.1f%% (required: 80%%)", overallSuccessRate * 100)
        ));
        
        // Test coverage gate
        qualityGates.add(new QualityGate(
            "Test Coverage",
            testCoverage >= 0.7,
            String.format("Coverage: %.1f%% (required: 70%%)", testCoverage * 100)
        ));
        
        // Service-specific gates
        for (ServiceTestResult serviceResult : serviceResults) {
            qualityGates.add(new QualityGate(
                serviceResult.getServiceName() + " Success Rate",
                serviceResult.getSuccessRate() >= 0.8,
                String.format("%s success rate: %.1f%% (required: 80%%)", 
                    serviceResult.getServiceName(), serviceResult.getSuccessRate() * 100)
            ));
        }
        
        return qualityGates;
    }
    
    /**
     * Execute tests for CI/CD pipeline
     */
    public CompletableFuture<CICDTestResult> executeCICDTests(String pipelineId, String branch, 
                                                              String pathFlow, List<String> services) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                logger.info("🚀 Executing CI/CD tests for pipeline: {}, branch: {}", pipelineId, branch);
                
                // Configure services for CI/CD environment
                Map<String, String> serviceConfigs = configureServicesForCICD(services);
                
                // Orchestrate tests
                TestOrchestrationResult orchestrationResult = orchestrateTests(pathFlow, services, serviceConfigs).get();
                
                // Generate CI/CD specific report
                CICDTestResult result = new CICDTestResult(
                    pipelineId,
                    branch,
                    orchestrationResult,
                    generateCICDReport(orchestrationResult)
                );
                
                logger.info("✅ CI/CD tests completed for pipeline: {}", pipelineId);
                return result;
                
            } catch (Exception e) {
                logger.error("❌ Error executing CI/CD tests: {}", e.getMessage(), e);
                throw new RuntimeException("CI/CD test execution failed", e);
            }
        }, executorService);
    }
    
    /**
     * Configure services for CI/CD environment
     */
    private Map<String, String> configureServicesForCICD(List<String> services) {
        Map<String, String> configs = new HashMap<>();
        
        for (String service : services) {
            // Configure service for CI/CD environment
            String config = String.format(
                "{\n" +
                "    \"environment\": \"ci\",\n" +
                "    \"database\": \"test_db\",\n" +
                "    \"timeout\": 30000,\n" +
                "    \"retries\": 3,\n" +
                "    \"parallel\": true\n" +
                "}"
            );
            configs.put(service, config);
        }
        
        return configs;
    }
    
    /**
     * Generate CI/CD specific report
     */
    private String generateCICDReport(TestOrchestrationResult orchestrationResult) {
        StringBuilder report = new StringBuilder();
        
        report.append("# CI/CD Test Report\n\n");
        report.append("## Summary\n");
        report.append(String.format("- Total Tests: %d\n", orchestrationResult.getTotalTests()));
        report.append(String.format("- Passed Tests: %d\n", orchestrationResult.getPassedTests()));
        report.append(String.format("- Success Rate: %.1f%%\n", orchestrationResult.getOverallSuccessRate() * 100));
        report.append(String.format("- Test Coverage: %.1f%%\n", orchestrationResult.getTestCoverage() * 100));
        
        report.append("\n## Quality Gates\n");
        for (QualityGate gate : orchestrationResult.getQualityGates()) {
            report.append(String.format("- %s: %s - %s\n", 
                gate.getName(), 
                gate.isPassed() ? "✅ PASS" : "❌ FAIL",
                gate.getDescription()));
        }
        
        report.append("\n## Recommendations\n");
        for (String recommendation : orchestrationResult.getRecommendations()) {
            report.append(String.format("- %s\n", recommendation));
        }
        
        return report.toString();
    }
    
    // Data classes
    public static class ServiceTestResult {
        private final String serviceName;
        private final String language;
        private final String framework;
        private final List<TestCase> unitTestResults;
        private final List<TestCase> integrationTestResults;
        private final int totalTests;
        private final int passedTests;
        private final double successRate;
        
        public ServiceTestResult(String serviceName, String language, String framework,
                               List<TestCase> unitTestResults, List<TestCase> integrationTestResults,
                               int totalTests, int passedTests, double successRate) {
            this.serviceName = serviceName;
            this.language = language;
            this.framework = framework;
            this.unitTestResults = unitTestResults;
            this.integrationTestResults = integrationTestResults;
            this.totalTests = totalTests;
            this.passedTests = passedTests;
            this.successRate = successRate;
        }
        
        // Getters
        public String getServiceName() { return serviceName; }
        public String getLanguage() { return language; }
        public String getFramework() { return framework; }
        public List<TestCase> getUnitTestResults() { return unitTestResults; }
        public List<TestCase> getIntegrationTestResults() { return integrationTestResults; }
        public int getTotalTests() { return totalTests; }
        public int getPassedTests() { return passedTests; }
        public double getSuccessRate() { return successRate; }
    }
    
    public static class QualityGate {
        private final String name;
        private final boolean passed;
        private final String description;
        
        public QualityGate(String name, boolean passed, String description) {
            this.name = name;
            this.passed = passed;
            this.description = description;
        }
        
        // Getters
        public String getName() { return name; }
        public boolean isPassed() { return passed; }
        public String getDescription() { return description; }
    }
    
    public static class TestOrchestrationResult {
        private final PathFlowAnalysisService.PathFlowComponents components;
        private final List<ServiceTestResult> serviceResults;
        private final List<TestCase> integrationResults;
        private final List<TestCase> e2eResults;
        private final int totalTests;
        private final int passedTests;
        private final double overallSuccessRate;
        private final double serviceCoverage;
        private final double testCoverage;
        private final List<String> recommendations;
        private final List<QualityGate> qualityGates;
        
        public TestOrchestrationResult(PathFlowAnalysisService.PathFlowComponents components,
                                     List<ServiceTestResult> serviceResults,
                                     List<TestCase> integrationResults,
                                     List<TestCase> e2eResults,
                                     int totalTests, int passedTests, double overallSuccessRate,
                                     double serviceCoverage, double testCoverage,
                                     List<String> recommendations, List<QualityGate> qualityGates) {
            this.components = components;
            this.serviceResults = serviceResults;
            this.integrationResults = integrationResults;
            this.e2eResults = e2eResults;
            this.totalTests = totalTests;
            this.passedTests = passedTests;
            this.overallSuccessRate = overallSuccessRate;
            this.serviceCoverage = serviceCoverage;
            this.testCoverage = testCoverage;
            this.recommendations = recommendations;
            this.qualityGates = qualityGates;
        }
        
        // Getters
        public PathFlowAnalysisService.PathFlowComponents getComponents() { return components; }
        public List<ServiceTestResult> getServiceResults() { return serviceResults; }
        public List<TestCase> getIntegrationResults() { return integrationResults; }
        public List<TestCase> getE2eResults() { return e2eResults; }
        public int getTotalTests() { return totalTests; }
        public int getPassedTests() { return passedTests; }
        public double getOverallSuccessRate() { return overallSuccessRate; }
        public double getServiceCoverage() { return serviceCoverage; }
        public double getTestCoverage() { return testCoverage; }
        public List<String> getRecommendations() { return recommendations; }
        public List<QualityGate> getQualityGates() { return qualityGates; }
    }
    
    public static class CICDTestResult {
        private final String pipelineId;
        private final String branch;
        private final TestOrchestrationResult orchestrationResult;
        private final String report;
        
        public CICDTestResult(String pipelineId, String branch, 
                            TestOrchestrationResult orchestrationResult, String report) {
            this.pipelineId = pipelineId;
            this.branch = branch;
            this.orchestrationResult = orchestrationResult;
            this.report = report;
        }
        
        // Getters
        public String getPipelineId() { return pipelineId; }
        public String getBranch() { return branch; }
        public TestOrchestrationResult getOrchestrationResult() { return orchestrationResult; }
        public String getReport() { return report; }
    }
}
