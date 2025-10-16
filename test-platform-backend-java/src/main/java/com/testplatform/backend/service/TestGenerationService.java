package com.testplatform.backend.service;

import com.testplatform.backend.config.AppProperties;
import com.testplatform.backend.enums.TestType;
import com.testplatform.backend.enums.TestPriority;
import com.testplatform.backend.enums.TestStatus;
import com.testplatform.backend.model.TestCase;
import com.testplatform.backend.model.TestSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
public class TestGenerationService {
    
    private static final Logger logger = LoggerFactory.getLogger(TestGenerationService.class);
    
    @Autowired
    private AppProperties appProperties;
    
    @Autowired
    private LlmService llmService;
    
    @Autowired
    private KeplerTestDataService keplerTestDataService;
    
    // Real test storage - loaded from Kepler App
    private final List<TestSuite> generatedTests = new ArrayList<>();
    private int nextTestId = 1000;
    
    public TestGenerationService() {
        // Will be initialized after dependency injection
        logger.info("🚀 TestGenerationService initialized - will load real Kepler App test data");
    }
    
    @jakarta.annotation.PostConstruct
    public void init() {
        // Load real test cases from Kepler App after dependencies are injected
        loadKeplerAppTests();
    }
    
    /**
     * Load real test data from Kepler App
     */
    private void loadKeplerAppTests() {
        try {
            logger.info("📥 Loading real test data from Kepler App...");
            List<TestSuite> keplerTests = keplerTestDataService.generateAllTestSuites();
            generatedTests.clear();
            generatedTests.addAll(keplerTests);
            logger.info("✅ Loaded {} real test suites from Kepler App", generatedTests.size());
            
            // Log summary
            long unitTests = generatedTests.stream().filter(t -> t.getType() == TestType.UNIT).count();
            long integrationTests = generatedTests.stream().filter(t -> t.getType() == TestType.INTEGRATION).count();
            int totalTestCases = generatedTests.stream().mapToInt(t -> t.getTotalTests() != null ? t.getTotalTests() : 0).sum();
            
            logger.info("📊 Test Summary: {} Unit, {} Integration, {} Total Test Cases", 
                       unitTests, integrationTests, totalTestCases);
        } catch (Exception e) {
            logger.error("❌ Error loading Kepler App test data: {}", e.getMessage(), e);
        }
    }
    
    
    /**
     * Generate test cases using LLM based on code content and type
     */
    public CompletableFuture<List<TestCase>> generateTestCases(String code, TestType type, String language, String filePath) {
        return CompletableFuture.supplyAsync(() -> {
            logger.info("🤖 Generating {} tests using LLM for {}", type, filePath);
            
            try {
                // Use LLM service to generate intelligent test cases
                List<TestCase> llmGeneratedTests = llmService.generateTestCases(code, type, language, filePath);
                
                // Limit the number of tests based on configuration
                int maxTests = appProperties.getTestGeneration().getMaxTestsPerFile();
                if (llmGeneratedTests.size() > maxTests) {
                    llmGeneratedTests = llmGeneratedTests.subList(0, maxTests);
                }
                
                logger.info("✅ Generated {} intelligent test cases using LLM", llmGeneratedTests.size());
                return llmGeneratedTests;
                
            } catch (Exception e) {
                logger.error("❌ Error generating tests with LLM: {}", e.getMessage(), e);
                
                // Fallback to basic test cases if LLM fails
                logger.info("🔄 Falling back to template-based test generation");
                return createFallbackTestCases(type, language, filePath);
            }
        });
    }
    
    /**
     * Create fallback test cases when LLM is not available
     */
    private List<TestCase> createFallbackTestCases(TestType type, String language, String filePath) {
        List<TestCase> fallbackTests = new ArrayList<>();
        
        // Basic fallback test cases
        switch (type) {
            case UNIT:
                fallbackTests.addAll(Arrays.asList(
                    createTestCase("Test method functionality", type, TestPriority.HIGH, "Validates individual method behavior"),
                    createTestCase("Test edge cases", type, TestPriority.HIGH, "Tests boundary conditions"),
                    createTestCase("Test exception handling", type, TestPriority.MEDIUM, "Validates error scenarios")
                ));
                break;
            case INTEGRATION:
                fallbackTests.addAll(Arrays.asList(
                    createTestCase("Test component integration", type, TestPriority.HIGH, "Validates component interactions"),
                    createTestCase("Test API endpoints", type, TestPriority.HIGH, "Tests HTTP endpoints"),
                    createTestCase("Test database operations", type, TestPriority.HIGH, "Validates data persistence")
                ));
                break;
            case E2E:
                fallbackTests.addAll(Arrays.asList(
                    createTestCase("Test complete user workflow", type, TestPriority.HIGH, "Validates end-to-end scenarios"),
                    createTestCase("Test user interface interactions", type, TestPriority.HIGH, "Tests UI components")
                ));
                break;
            case PERFORMANCE:
                fallbackTests.addAll(Arrays.asList(
                    createTestCase("Test response time", type, TestPriority.HIGH, "Validates performance metrics"),
                    createTestCase("Test load handling", type, TestPriority.HIGH, "Tests system under load")
                ));
                break;
            case SECURITY:
                fallbackTests.addAll(Arrays.asList(
                    createTestCase("Test authentication", type, TestPriority.HIGH, "Validates authentication mechanisms"),
                    createTestCase("Test authorization", type, TestPriority.HIGH, "Tests access control")
                ));
                break;
        }
        
        return fallbackTests;
    }
    
    /**
     * Save generated test suite
     */
    public TestSuite saveTestSuite(TestSuite testSuite) {
        testSuite.setId(String.valueOf(nextTestId++));
        generatedTests.add(testSuite);
        logger.info("💾 Saved test suite: {} tests for {}", testSuite.getType(), testSuite.getFilePath());
        return testSuite;
    }
    
    /**
     * Get all test suites for a PR
     */
    public List<TestSuite> getTestsForPR(Integer prNumber) {
        return generatedTests.stream()
                .filter(test -> test.getPrNumber() != null && test.getPrNumber().equals(prNumber))
                .collect(Collectors.toList());
    }
    
    /**
     * Get all generated tests - Returns all real Kepler-app tests
     */
    public List<TestSuite> getAllTests() {
        logger.info("📊 Returning all Kepler-app tests: {} test suites", generatedTests.size());
        return new ArrayList<>(generatedTests);
    }
    
    /**
     * Delete tests for a PR
     */
    public void deleteTestsForPR(Integer prNumber) {
        int initialSize = generatedTests.size();
        generatedTests.removeIf(test -> test.getPrNumber() != null && test.getPrNumber().equals(prNumber));
        logger.info("🗑️ Deleted {} test suites for PR #{}", initialSize - generatedTests.size(), prNumber);
    }
    
    /**
     * Update test suite status
     */
    public void updateTestSuiteStatus(String suiteId, TestStatus status) {
        Optional<TestSuite> suiteOpt = generatedTests.stream()
                .filter(suite -> suite.getId().equals(suiteId))
                .findFirst();
        
        if (suiteOpt.isPresent()) {
            TestSuite suite = suiteOpt.get();
            suite.setStatus(status);
            suite.setUpdatedAt(LocalDateTime.now());
            if (status == TestStatus.COMPLETED) {
                suite.setLastRun(LocalDateTime.now());
            }
            logger.info("📝 Updated test suite {} status to {}", suiteId, status);
        }
    }
    
    /**
     * Get test generation statistics
     */
    public Map<String, Object> getTestStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalSuites", generatedTests.size());
        stats.put("totalTestCases", generatedTests.stream().mapToInt(suite -> suite.getTestCases() != null ? suite.getTestCases().size() : 0).sum());
        
        Map<String, Long> byType = new HashMap<>();
        for (String testType : appProperties.getTestGeneration().getTestTypes()) {
            byType.put(testType, generatedTests.stream()
                    .filter(test -> test.getType().name().equals(testType))
                    .count());
        }
        stats.put("byType", byType);
        
        Map<String, Long> byLanguage = new HashMap<>();
        for (String language : appProperties.getTestGeneration().getSupportedLanguages()) {
            byLanguage.put(language, generatedTests.stream()
                    .filter(test -> language.equals(test.getLanguage()))
                    .count());
        }
        stats.put("byLanguage", byLanguage);
        
        return stats;
    }
    
    /**
     * Get tests aggregated by service (Project, Contributor, Work)
     */
    public List<com.testplatform.backend.dto.ServiceTestSummaryDTO> getTestsByService() {
        Map<String, com.testplatform.backend.dto.ServiceTestSummaryDTO> serviceMap = new HashMap<>();
        
        for (TestSuite suite : generatedTests) {
            String serviceName = extractServiceName(suite.getFilePath());
            
            // Get or create service summary
            com.testplatform.backend.dto.ServiceTestSummaryDTO summary = serviceMap.computeIfAbsent(
                serviceName, 
                k -> createServiceSummary(k)
            );
            
            // Aggregate metrics
            summary.setTotalTestSuites(summary.getTotalTestSuites() + 1);
            summary.setTotalTestCases(summary.getTotalTestCases() + (suite.getTotalTests() != null ? suite.getTotalTests() : 0));
            summary.setPassedTests(summary.getPassedTests() + (suite.getPassedTests() != null ? suite.getPassedTests() : 0));
            summary.setFailedTests(summary.getFailedTests() + (suite.getFailedTests() != null ? suite.getFailedTests() : 0));
            summary.setTotalExecutionTime(summary.getTotalExecutionTime() + (suite.getExecutionTime() != null ? suite.getExecutionTime() : 0L));
            
            // Update last run
            if (suite.getLastRun() != null && 
                (summary.getLastRun() == null || suite.getLastRun().isAfter(summary.getLastRun()))) {
                summary.setLastRun(suite.getLastRun());
            }
            
            // Track tests by type
            String typeName = suite.getType().name();
            summary.getTestsByType().put(typeName, 
                summary.getTestsByType().getOrDefault(typeName, 0) + (suite.getTotalTests() != null ? suite.getTotalTests() : 0));
            summary.getSuitesByType().put(typeName,
                summary.getSuitesByType().getOrDefault(typeName, 0) + 1);
        }
        
        // Calculate derived metrics
        for (com.testplatform.backend.dto.ServiceTestSummaryDTO summary : serviceMap.values()) {
            int total = summary.getTotalTestCases();
            int passed = summary.getPassedTests();
            int failed = summary.getFailedTests();
            
            // Calculate pass rate
            if (total > 0) {
                summary.setPassRate((double) passed / total * 100.0);
            } else {
                summary.setPassRate(0.0);
            }
            
            // Set pending tests
            summary.setPendingTests(total - passed - failed);
            
            // Calculate average coverage
            double avgCoverage = generatedTests.stream()
                .filter(s -> extractServiceName(s.getFilePath()).equals(summary.getServiceName()))
                .filter(s -> s.getCoverage() != null)
                .mapToDouble(TestSuite::getCoverage)
                .average()
                .orElse(0.0);
            summary.setAvgCoverage(avgCoverage);
            
            // Set overall status
            if (failed > 0) {
                summary.setOverallStatus(TestStatus.FAILED);
            } else if (passed == total && total > 0) {
                summary.setOverallStatus(TestStatus.PASSED);
            } else {
                summary.setOverallStatus(TestStatus.PENDING);
            }
        }
        
        logger.info("📊 Aggregated tests into {} services", serviceMap.size());
        return new ArrayList<>(serviceMap.values());
    }
    
    /**
     * Extract service name from file path
     */
    private String extractServiceName(String filePath) {
        if (filePath == null) return "Unknown Service";
        
        if (filePath.contains("project-service")) {
            return "Project Service";
        } else if (filePath.contains("contributor-service")) {
            return "Contributor Service";
        } else if (filePath.contains("work-service")) {
            return "Work Service";
        } else if (filePath.contains("api-gateway")) {
            return "API Gateway";
        } else if (filePath.contains("batchjob-service")) {
            return "Batch Job Service";
        }
        
        return "Unknown Service";
    }
    
    /**
     * Create initial service summary
     */
    private com.testplatform.backend.dto.ServiceTestSummaryDTO createServiceSummary(String serviceName) {
        com.testplatform.backend.dto.ServiceTestSummaryDTO summary = 
            new com.testplatform.backend.dto.ServiceTestSummaryDTO();
        
        summary.setServiceName(serviceName);
        summary.setServiceId(serviceName.toLowerCase().replace(" ", "-"));
        summary.setTotalTestSuites(0);
        summary.setTotalTestCases(0);
        summary.setPassedTests(0);
        summary.setFailedTests(0);
        summary.setPendingTests(0);
        summary.setTotalExecutionTime(0L);
        summary.setTestsByType(new HashMap<>());
        summary.setSuitesByType(new HashMap<>());
        
        // Set descriptions
        switch (serviceName) {
            case "Project Service":
                summary.setDescription("Manages project lifecycle, datasets, units, and job statistics");
                summary.setRepository("kepler-app/project-service");
                break;
            case "Contributor Service":
                summary.setDescription("Handles contributor management, crowd groups, and sync settings");
                summary.setRepository("kepler-app/contributor-service");
                break;
            case "Work Service":
                summary.setDescription("Manages work items, jobs, prompts, and crowd operations");
                summary.setRepository("kepler-app/work-service");
                break;
            default:
                summary.setDescription("Kepler App Service");
                summary.setRepository("kepler-app");
        }
        
        return summary;
    }
    
    // Helper methods
    private TestSuite createTestSuite(Integer prNumber, String branch, String filePath, TestType type, String language) {
        TestSuite suite = new TestSuite();
        suite.setPrNumber(prNumber);
        suite.setBranch(branch);
        suite.setFilePath(filePath);
        suite.setType(type);
        suite.setLanguage(language);
        suite.setStatus(TestStatus.PENDING);
        return suite;
    }
    
    private TestCase createTestCase(String name, TestType type, TestPriority priority, String description) {
        TestCase testCase = new TestCase();
        testCase.setId(UUID.randomUUID().toString());
        testCase.setName(name);
        testCase.setType(type);
        testCase.setPriority(priority);
        testCase.setDescription(description);
        testCase.setFilePath("src/test/java/com/testplatform/backend/" + name.replaceAll("\\s+", "") + "Test.java");
        testCase.setLanguage("java");
        testCase.setCode("// Generated test code for " + name);
        
        // Set initial status as PENDING - will be updated after test execution
        testCase.setStatus(TestStatus.PENDING);
        
        return testCase;
    }
}
