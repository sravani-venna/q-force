package com.testplatform.backend.service;

import com.testplatform.backend.enums.TestType;
import com.testplatform.backend.enums.TestStatus;
import com.testplatform.backend.enums.TestPriority;
import com.testplatform.backend.model.TestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class PathFlowAnalysisService {
    
    private static final Logger logger = LoggerFactory.getLogger(PathFlowAnalysisService.class);
    
    @Autowired
    private LlmService llmService;
    
    @Autowired
    private LanguageDetectionService languageDetectionService;
    
    @Autowired
    private RealCodeAnalysisService realCodeAnalysisService;
    
    /**
     * Analyze path flow and generate comprehensive test suites
     */
    public PathFlowAnalysisResult analyzePathFlow(String repositoryId, String pathFlow, 
                                                 List<String> services, Map<String, String> serviceConfigs) {
        try {
            logger.info("🔍 Analyzing path flow: {} across {} services", pathFlow, services.size());
            
            // Parse path flow to identify components
            PathFlowComponents components = parsePathFlow(pathFlow);
            
            // Analyze each service in the path flow
            List<ServiceAnalysis> serviceAnalyses = new ArrayList<>();
            for (String service : services) {
                ServiceAnalysis analysis = analyzeService(service, serviceConfigs.get(service), components);
                serviceAnalyses.add(analysis);
            }
            
            // Generate cross-service integration tests
            List<TestCase> integrationTests = generateCrossServiceTests(components, serviceAnalyses);
            
            // Generate end-to-end path flow tests
            List<TestCase> e2eTests = generateEndToEndTests(components, serviceAnalyses);
            
            // Calculate coverage metrics
            CoverageMetrics metrics = calculateCoverageMetrics(serviceAnalyses, integrationTests, e2eTests);
            
            return new PathFlowAnalysisResult(
                components,
                serviceAnalyses,
                integrationTests,
                e2eTests,
                metrics
            );
            
        } catch (Exception e) {
            logger.error("❌ Error analyzing path flow: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to analyze path flow", e);
        }
    }
    
    /**
     * Parse path flow to identify components and dependencies
     */
    private PathFlowComponents parsePathFlow(String pathFlow) {
        logger.info("📋 Parsing path flow: {}", pathFlow);
        
        // Extract user actions, API calls, database operations, etc.
        List<String> userActions = extractUserActions(pathFlow);
        List<String> apiEndpoints = extractApiEndpoints(pathFlow);
        List<String> databaseOperations = extractDatabaseOperations(pathFlow);
        List<String> externalServices = extractExternalServices(pathFlow);
        
        // Identify critical paths and edge cases
        List<String> criticalPaths = identifyCriticalPaths(pathFlow);
        List<String> edgeCases = identifyEdgeCases(pathFlow);
        
        return new PathFlowComponents(
            userActions,
            apiEndpoints,
            databaseOperations,
            externalServices,
            criticalPaths,
            edgeCases
        );
    }
    
    /**
     * Analyze individual service in the path flow
     */
    private ServiceAnalysis analyzeService(String serviceName, String serviceConfig, PathFlowComponents components) {
        logger.info("🔧 Analyzing service: {}", serviceName);
        
        // Use real code analysis for CDAC project
        RealCodeAnalysisService.CodeAnalysisResult realCodeResult = realCodeAnalysisService.analyzeRealCode(serviceName);
        
        String language = realCodeResult.getLanguage();
        String framework = realCodeResult.getFramework();
        
        // Identify service responsibilities based on real code
        List<String> responsibilities = identifyServiceResponsibilitiesFromRealCode(realCodeResult);
        
        // Generate service-specific tests based on real code
        List<TestCase> unitTests = generateServiceUnitTestsFromRealCode(serviceName, realCodeResult, responsibilities);
        List<TestCase> integrationTests = generateServiceIntegrationTestsFromRealCode(serviceName, realCodeResult, components);
        
        return new ServiceAnalysis(
            serviceName,
            language,
            framework,
            responsibilities,
            unitTests,
            integrationTests
        );
    }
    
    /**
     * Generate cross-service integration tests
     */
    private List<TestCase> generateCrossServiceTests(PathFlowComponents components, List<ServiceAnalysis> serviceAnalyses) {
        logger.info("🔗 Generating cross-service integration tests");
        
        List<TestCase> tests = new ArrayList<>();
        
        // Generate API integration tests
        for (String endpoint : components.getApiEndpoints()) {
            TestCase apiTest = createApiIntegrationTest(endpoint, components);
            tests.add(apiTest);
        }
        
        // Generate database integration tests
        for (String dbOp : components.getDatabaseOperations()) {
            TestCase dbTest = createDatabaseIntegrationTest(dbOp, components);
            tests.add(dbTest);
        }
        
        // Generate external service integration tests
        for (String externalService : components.getExternalServices()) {
            TestCase externalTest = createExternalServiceTest(externalService, components);
            tests.add(externalTest);
        }
        
        return tests;
    }
    
    /**
     * Generate end-to-end path flow tests
     */
    private List<TestCase> generateEndToEndTests(PathFlowComponents components, List<ServiceAnalysis> serviceAnalyses) {
        logger.info("🎯 Generating end-to-end path flow tests");
        
        List<TestCase> tests = new ArrayList<>();
        
        // Generate happy path tests
        TestCase happyPathTest = createHappyPathTest(components);
        tests.add(happyPathTest);
        
        // Generate error path tests
        for (String edgeCase : components.getEdgeCases()) {
            TestCase errorTest = createErrorPathTest(edgeCase, components);
            tests.add(errorTest);
        }
        
        // Generate performance tests
        TestCase performanceTest = createPerformanceTest(components);
        tests.add(performanceTest);
        
        return tests;
    }
    
    /**
     * Calculate coverage metrics
     */
    private CoverageMetrics calculateCoverageMetrics(List<ServiceAnalysis> serviceAnalyses, 
                                                    List<TestCase> integrationTests, 
                                                    List<TestCase> e2eTests) {
        int totalServices = serviceAnalyses.size();
        int coveredServices = (int) serviceAnalyses.stream()
            .filter(analysis -> !analysis.getUnitTests().isEmpty())
            .count();
        
        int totalUnitTests = serviceAnalyses.stream()
            .mapToInt(analysis -> analysis.getUnitTests().size())
            .sum();
        
        int totalIntegrationTests = integrationTests.size();
        int totalE2eTests = e2eTests.size();
        
        double serviceCoverage = totalServices > 0 ? (double) coveredServices / totalServices : 0.0;
        double testCoverage = calculateTestCoverage(serviceAnalyses);
        
        return new CoverageMetrics(
            totalServices,
            coveredServices,
            totalUnitTests,
            totalIntegrationTests,
            totalE2eTests,
            serviceCoverage,
            testCoverage
        );
    }
    
    // Helper methods for parsing and analysis
    
    private List<String> extractUserActions(String pathFlow) {
        List<String> actions = new ArrayList<>();
        Pattern pattern = Pattern.compile("user\\s+(?:clicks|enters|selects|navigates)\\s+([^\\n]+)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(pathFlow);
        while (matcher.find()) {
            actions.add(matcher.group(1).trim());
        }
        return actions;
    }
    
    private List<String> extractApiEndpoints(String pathFlow) {
        List<String> endpoints = new ArrayList<>();
        Pattern pattern = Pattern.compile("(?:GET|POST|PUT|DELETE)\\s+(/api/[^\\s]+)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(pathFlow);
        while (matcher.find()) {
            endpoints.add(matcher.group(1));
        }
        return endpoints;
    }
    
    private List<String> extractDatabaseOperations(String pathFlow) {
        List<String> operations = new ArrayList<>();
        Pattern pattern = Pattern.compile("(?:INSERT|UPDATE|DELETE|SELECT)\\s+([^\\n]+)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(pathFlow);
        while (matcher.find()) {
            operations.add(matcher.group(1).trim());
        }
        return operations;
    }
    
    private List<String> extractExternalServices(String pathFlow) {
        List<String> services = new ArrayList<>();
        Pattern pattern = Pattern.compile("(?:calls|integrates with)\\s+([A-Za-z]+Service)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(pathFlow);
        while (matcher.find()) {
            services.add(matcher.group(1));
        }
        return services;
    }
    
    private List<String> identifyCriticalPaths(String pathFlow) {
        // Identify critical business paths
        List<String> criticalPaths = new ArrayList<>();
        if (pathFlow.toLowerCase().contains("payment")) {
            criticalPaths.add("Payment Processing Flow");
        }
        if (pathFlow.toLowerCase().contains("authentication")) {
            criticalPaths.add("Authentication Flow");
        }
        if (pathFlow.toLowerCase().contains("registration")) {
            criticalPaths.add("User Registration Flow");
        }
        return criticalPaths;
    }
    
    private List<String> identifyEdgeCases(String pathFlow) {
        List<String> edgeCases = new ArrayList<>();
        edgeCases.add("Invalid input validation");
        edgeCases.add("Network timeout scenarios");
        edgeCases.add("Database connection failures");
        edgeCases.add("External service unavailability");
        return edgeCases;
    }
    
    private String detectServiceLanguage(String serviceName, String serviceConfig) {
        if (serviceName.toLowerCase().contains("ruby") || serviceConfig.contains("gemfile")) {
            return "ruby";
        } else if (serviceName.toLowerCase().contains("python") || serviceConfig.contains("requirements.txt")) {
            return "python";
        } else if (serviceName.toLowerCase().contains("java") || serviceConfig.contains("pom.xml")) {
            return "java";
        }
        return "java"; // default
    }
    
    private String detectServiceFramework(String serviceName, String serviceConfig) {
        if (serviceConfig.contains("rails") || serviceConfig.contains("sinatra")) {
            return "rails";
        } else if (serviceConfig.contains("django") || serviceConfig.contains("flask")) {
            return "django";
        } else if (serviceConfig.contains("spring")) {
            return "spring-boot";
        }
        return "spring-boot"; // default
    }
    
    private List<String> identifyServiceResponsibilities(String serviceName, PathFlowComponents components) {
        List<String> responsibilities = new ArrayList<>();
        
        if (serviceName.toLowerCase().contains("auth")) {
            responsibilities.add("User authentication");
            responsibilities.add("Token validation");
        }
        if (serviceName.toLowerCase().contains("payment")) {
            responsibilities.add("Payment processing");
            responsibilities.add("Transaction validation");
        }
        if (serviceName.toLowerCase().contains("user")) {
            responsibilities.add("User management");
            responsibilities.add("Profile operations");
        }
        
        return responsibilities;
    }
    
    private List<TestCase> generateServiceUnitTests(String serviceName, String language, String framework, List<String> responsibilities) {
        List<TestCase> tests = new ArrayList<>();
        
        for (String responsibility : responsibilities) {
            TestCase test = createUnitTest(serviceName, language, framework, responsibility);
            tests.add(test);
        }
        
        return tests;
    }
    
    private List<TestCase> generateServiceIntegrationTests(String serviceName, String language, String framework, PathFlowComponents components) {
        List<TestCase> tests = new ArrayList<>();
        
        // Generate integration tests based on service role in path flow
        TestCase integrationTest = createIntegrationTest(serviceName, language, framework, components);
        tests.add(integrationTest);
        
        return tests;
    }
    
    private TestCase createUnitTest(String serviceName, String language, String framework, String responsibility) {
        TestCase test = new TestCase();
        test.setName("test" + responsibility.replaceAll("\\s+", "") + "Unit");
        test.setType(TestType.UNIT);
        test.setDescription("Unit test for " + responsibility + " in " + serviceName);
        test.setLanguage(language);
        
        // Generate test code based on language and framework
        String testCode = generateTestCode(language, framework, responsibility);
        test.setCode(testCode);
        
        return test;
    }
    
    private TestCase createIntegrationTest(String serviceName, String language, String framework, PathFlowComponents components) {
        TestCase test = new TestCase();
        test.setName("test" + serviceName + "Integration");
        test.setType(TestType.INTEGRATION);
        test.setDescription("Integration test for " + serviceName + " in path flow");
        test.setLanguage(language);
        
        String testCode = generateIntegrationTestCode(language, framework, serviceName, components);
        test.setCode(testCode);
        
        return test;
    }
    
    private TestCase createApiIntegrationTest(String endpoint, PathFlowComponents components) {
        TestCase test = new TestCase();
        test.setName("testApi" + endpoint.replaceAll("[^a-zA-Z0-9]", ""));
        test.setType(TestType.INTEGRATION);
        test.setDescription("API integration test for " + endpoint);
        test.setLanguage("java");
        
        String testCode = generateApiTestCode(endpoint, components);
        test.setCode(testCode);
        
        return test;
    }
    
    private TestCase createDatabaseIntegrationTest(String dbOp, PathFlowComponents components) {
        TestCase test = new TestCase();
        test.setName("testDatabase" + dbOp.replaceAll("[^a-zA-Z0-9]", ""));
        test.setType(TestType.INTEGRATION);
        test.setDescription("Database integration test for " + dbOp);
        test.setLanguage("java");
        
        String testCode = generateDatabaseTestCode(dbOp, components);
        test.setCode(testCode);
        
        return test;
    }
    
    private TestCase createExternalServiceTest(String externalService, PathFlowComponents components) {
        TestCase test = new TestCase();
        test.setName("testExternal" + externalService);
        test.setType(TestType.INTEGRATION);
        test.setDescription("External service integration test for " + externalService);
        test.setLanguage("java");
        
        String testCode = generateExternalServiceTestCode(externalService, components);
        test.setCode(testCode);
        
        return test;
    }
    
    private TestCase createHappyPathTest(PathFlowComponents components) {
        TestCase test = new TestCase();
        test.setName("testHappyPathFlow");
        test.setType(TestType.E2E);
        test.setDescription("End-to-end happy path test");
        test.setLanguage("java");
        
        String testCode = generateHappyPathTestCode(components);
        test.setCode(testCode);
        
        return test;
    }
    
    private TestCase createErrorPathTest(String edgeCase, PathFlowComponents components) {
        TestCase test = new TestCase();
        test.setName("testErrorPath" + edgeCase.replaceAll("[^a-zA-Z0-9]", ""));
        test.setType(TestType.E2E);
        test.setDescription("Error path test for " + edgeCase);
        test.setLanguage("java");
        
        String testCode = generateErrorPathTestCode(edgeCase, components);
        test.setCode(testCode);
        
        return test;
    }
    
    private TestCase createPerformanceTest(PathFlowComponents components) {
        TestCase test = new TestCase();
        test.setName("testPathFlowPerformance");
        test.setType(TestType.E2E);
        test.setDescription("Performance test for path flow");
        test.setLanguage("java");
        
        String testCode = generatePerformanceTestCode(components);
        test.setCode(testCode);
        
        return test;
    }
    
    // Test code generation methods
    private String generateTestCode(String language, String framework, String responsibility) {
        switch (language.toLowerCase()) {
            case "java":
                return generateJavaTestCode(framework, responsibility);
            case "ruby":
                return generateRubyTestCode(framework, responsibility);
            case "python":
                return generatePythonTestCode(framework, responsibility);
            default:
                return generateJavaTestCode(framework, responsibility);
        }
    }
    
    private String generateJavaTestCode(String framework, String responsibility) {
        return String.format("""
            @Test
            public void test%s() {
                // Test %s functionality
                // Given
                %s
                
                // When
                %s
                
                // Then
                %s
            }
            """, 
            responsibility.replaceAll("\\s+", ""),
            responsibility,
            "// Setup test data",
            "// Execute the operation",
            "// Verify the result"
        );
    }
    
    private String generateRubyTestCode(String framework, String responsibility) {
        return String.format("""
            it 'should %s' do
              # Test %s functionality
              # Given
              %s
              
              # When
              %s
              
              # Then
              %s
            end
            """, 
            responsibility,
            responsibility,
            "# Setup test data",
            "# Execute the operation",
            "# Verify the result"
        );
    }
    
    private String generatePythonTestCode(String framework, String responsibility) {
        return String.format("""
            def test_%s(self):
                \"\"\"Test %s functionality\"\"\"
                # Given
                %s
                
                # When
                %s
                
                # Then
                %s
            """, 
            responsibility.replaceAll("\\s+", "_"),
            responsibility,
            "# Setup test data",
            "# Execute the operation",
            "# Verify the result"
        );
    }
    
    private String generateIntegrationTestCode(String language, String framework, String serviceName, PathFlowComponents components) {
        return String.format("""
            @Test
            public void test%sIntegration() {
                // Integration test for %s
                // Test cross-service communication
                // Verify data flow between services
            }
            """, 
            serviceName,
            serviceName
        );
    }
    
    private String generateApiTestCode(String endpoint, PathFlowComponents components) {
        return String.format("""
            @Test
            public void testApi%s() {
                // API integration test for %s
                // Test endpoint functionality
                // Verify response format and status codes
            }
            """, 
            endpoint.replaceAll("[^a-zA-Z0-9]", ""),
            endpoint
        );
    }
    
    private String generateDatabaseTestCode(String dbOp, PathFlowComponents components) {
        return String.format("""
            @Test
            public void testDatabase%s() {
                // Database integration test for %s
                // Test database operations
                // Verify data consistency
            }
            """, 
            dbOp.replaceAll("[^a-zA-Z0-9]", ""),
            dbOp
        );
    }
    
    private String generateExternalServiceTestCode(String externalService, PathFlowComponents components) {
        return String.format("""
            @Test
            public void testExternal%s() {
                // External service integration test for %s
                // Test service communication
                // Verify error handling and timeouts
            }
            """, 
            externalService,
            externalService
        );
    }
    
    private String generateHappyPathTestCode(PathFlowComponents components) {
        return """
            @Test
            public void testHappyPathFlow() {
                // End-to-end happy path test
                // Test complete user journey
                // Verify all services work together
            }
            """;
    }
    
    private String generateErrorPathTestCode(String edgeCase, PathFlowComponents components) {
        return String.format("""
            @Test
            public void testErrorPath%s() {
                // Error path test for %s
                // Test error handling and recovery
                // Verify appropriate error responses
            }
            """, 
            edgeCase.replaceAll("[^a-zA-Z0-9]", ""),
            edgeCase
        );
    }
    
    private String generatePerformanceTestCode(PathFlowComponents components) {
        return """
            @Test
            public void testPathFlowPerformance() {
                // Performance test for path flow
                // Test response times and throughput
                // Verify performance requirements
            }
            """;
    }
    
    private double calculateTestCoverage(List<ServiceAnalysis> serviceAnalyses) {
        // Calculate test coverage based on service analysis
        int totalMethods = serviceAnalyses.stream()
            .mapToInt(analysis -> analysis.getResponsibilities().size())
            .sum();
        
        int testedMethods = serviceAnalyses.stream()
            .mapToInt(analysis -> analysis.getUnitTests().size())
            .sum();
        
        return totalMethods > 0 ? (double) testedMethods / totalMethods : 0.0;
    }
    
    // Data classes
    public static class PathFlowComponents {
        private final List<String> userActions;
        private final List<String> apiEndpoints;
        private final List<String> databaseOperations;
        private final List<String> externalServices;
        private final List<String> criticalPaths;
        private final List<String> edgeCases;
        
        public PathFlowComponents(List<String> userActions, List<String> apiEndpoints, 
                                List<String> databaseOperations, List<String> externalServices,
                                List<String> criticalPaths, List<String> edgeCases) {
            this.userActions = userActions;
            this.apiEndpoints = apiEndpoints;
            this.databaseOperations = databaseOperations;
            this.externalServices = externalServices;
            this.criticalPaths = criticalPaths;
            this.edgeCases = edgeCases;
        }
        
        // Getters
        public List<String> getUserActions() { return userActions; }
        public List<String> getApiEndpoints() { return apiEndpoints; }
        public List<String> getDatabaseOperations() { return databaseOperations; }
        public List<String> getExternalServices() { return externalServices; }
        public List<String> getCriticalPaths() { return criticalPaths; }
        public List<String> getEdgeCases() { return edgeCases; }
    }
    
    public static class ServiceAnalysis {
        private final String serviceName;
        private final String language;
        private final String framework;
        private final List<String> responsibilities;
        private final List<TestCase> unitTests;
        private final List<TestCase> integrationTests;
        
        public ServiceAnalysis(String serviceName, String language, String framework,
                             List<String> responsibilities, List<TestCase> unitTests, 
                             List<TestCase> integrationTests) {
            this.serviceName = serviceName;
            this.language = language;
            this.framework = framework;
            this.responsibilities = responsibilities;
            this.unitTests = unitTests;
            this.integrationTests = integrationTests;
        }
        
        // Getters
        public String getServiceName() { return serviceName; }
        public String getLanguage() { return language; }
        public String getFramework() { return framework; }
        public List<String> getResponsibilities() { return responsibilities; }
        public List<TestCase> getUnitTests() { return unitTests; }
        public List<TestCase> getIntegrationTests() { return integrationTests; }
    }
    
    public static class CoverageMetrics {
        private final int totalServices;
        private final int coveredServices;
        private final int totalUnitTests;
        private final int totalIntegrationTests;
        private final int totalE2eTests;
        private final double serviceCoverage;
        private final double testCoverage;
        
        public CoverageMetrics(int totalServices, int coveredServices, int totalUnitTests,
                             int totalIntegrationTests, int totalE2eTests, 
                             double serviceCoverage, double testCoverage) {
            this.totalServices = totalServices;
            this.coveredServices = coveredServices;
            this.totalUnitTests = totalUnitTests;
            this.totalIntegrationTests = totalIntegrationTests;
            this.totalE2eTests = totalE2eTests;
            this.serviceCoverage = serviceCoverage;
            this.testCoverage = testCoverage;
        }
        
        // Getters
        public int getTotalServices() { return totalServices; }
        public int getCoveredServices() { return coveredServices; }
        public int getTotalUnitTests() { return totalUnitTests; }
        public int getTotalIntegrationTests() { return totalIntegrationTests; }
        public int getTotalE2eTests() { return totalE2eTests; }
        public double getServiceCoverage() { return serviceCoverage; }
        public double getTestCoverage() { return testCoverage; }
    }
    
    public static class PathFlowAnalysisResult {
        private final PathFlowComponents components;
        private final List<ServiceAnalysis> serviceAnalyses;
        private final List<TestCase> integrationTests;
        private final List<TestCase> e2eTests;
        private final CoverageMetrics metrics;
        
        public PathFlowAnalysisResult(PathFlowComponents components, List<ServiceAnalysis> serviceAnalyses,
                                    List<TestCase> integrationTests, List<TestCase> e2eTests,
                                    CoverageMetrics metrics) {
            this.components = components;
            this.serviceAnalyses = serviceAnalyses;
            this.integrationTests = integrationTests;
            this.e2eTests = e2eTests;
            this.metrics = metrics;
        }
        
        // Getters
        public PathFlowComponents getComponents() { return components; }
        public List<ServiceAnalysis> getServiceAnalyses() { return serviceAnalyses; }
        public List<TestCase> getIntegrationTests() { return integrationTests; }
        public List<TestCase> getE2eTests() { return e2eTests; }
        public CoverageMetrics getMetrics() { return metrics; }
    }
    
    /**
     * Identify service responsibilities from real code analysis
     */
    private List<String> identifyServiceResponsibilitiesFromRealCode(RealCodeAnalysisService.CodeAnalysisResult realCodeResult) {
        List<String> responsibilities = new ArrayList<>();
        
        // Add responsibilities based on real code components
        if (!realCodeResult.getControllers().isEmpty()) {
            responsibilities.add("API endpoints: " + String.join(", ", realCodeResult.getControllers()));
        }
        if (!realCodeResult.getServices().isEmpty()) {
            responsibilities.add("Business logic: " + String.join(", ", realCodeResult.getServices()));
        }
        if (!realCodeResult.getRepositories().isEmpty()) {
            responsibilities.add("Data access: " + String.join(", ", realCodeResult.getRepositories()));
        }
        if (!realCodeResult.getComponents().isEmpty()) {
            responsibilities.add("UI components: " + String.join(", ", realCodeResult.getComponents()));
        }
        if (!realCodeResult.getTables().isEmpty()) {
            responsibilities.add("Database tables: " + String.join(", ", realCodeResult.getTables()));
        }
        
        return responsibilities;
    }
    
    /**
     * Generate unit tests based on real code analysis
     */
    private List<TestCase> generateServiceUnitTestsFromRealCode(String serviceName, 
                                                               RealCodeAnalysisService.CodeAnalysisResult realCodeResult, 
                                                               List<String> responsibilities) {
        List<TestCase> tests = new ArrayList<>();
        
        // Generate tests for React components
        if ("javascript".equals(realCodeResult.getLanguage()) && "react".equals(realCodeResult.getFramework())) {
            for (String component : realCodeResult.getComponents()) {
                TestCase test = createReactComponentTest(component, serviceName);
                tests.add(test);
            }
            for (String function : realCodeResult.getFunctions()) {
                TestCase test = createReactFunctionTest(function, serviceName);
                tests.add(test);
            }
        }
        
        // Generate tests for Spring controllers
        if ("java".equals(realCodeResult.getLanguage()) && "spring-boot".equals(realCodeResult.getFramework())) {
            for (String controller : realCodeResult.getControllers()) {
                TestCase test = createSpringControllerTest(controller, serviceName);
                tests.add(test);
            }
            for (String service : realCodeResult.getServices()) {
                TestCase test = createSpringServiceTest(service, serviceName);
                tests.add(test);
            }
            for (String repository : realCodeResult.getRepositories()) {
                TestCase test = createSpringRepositoryTest(repository, serviceName);
                tests.add(test);
            }
        }
        
        // Generate tests for database
        if ("sql".equals(realCodeResult.getLanguage())) {
            for (String table : realCodeResult.getTables()) {
                TestCase test = createDatabaseTableTest(table, serviceName);
                tests.add(test);
            }
        }
        
        return tests;
    }
    
    /**
     * Generate integration tests based on real code analysis
     */
    private List<TestCase> generateServiceIntegrationTestsFromRealCode(String serviceName, 
                                                                      RealCodeAnalysisService.CodeAnalysisResult realCodeResult, 
                                                                      PathFlowComponents components) {
        List<TestCase> tests = new ArrayList<>();
        
        // Generate integration tests based on real code structure
        if (!realCodeResult.getControllers().isEmpty() && !realCodeResult.getServices().isEmpty()) {
            TestCase integrationTest = createControllerServiceIntegrationTest(realCodeResult.getControllers().get(0), 
                                                                             realCodeResult.getServices().get(0), 
                                                                             serviceName);
            tests.add(integrationTest);
        }
        
        if (!realCodeResult.getServices().isEmpty() && !realCodeResult.getRepositories().isEmpty()) {
            TestCase integrationTest = createServiceRepositoryIntegrationTest(realCodeResult.getServices().get(0), 
                                                                              realCodeResult.getRepositories().get(0), 
                                                                              serviceName);
            tests.add(integrationTest);
        }
        
        return tests;
    }
    
    // Helper methods for creating specific test types
    private TestCase createReactComponentTest(String componentName, String serviceName) {
        TestCase test = new TestCase();
        test.setName("test" + componentName + "Component");
        test.setType(TestType.UNIT);
        test.setStatus(TestStatus.PENDING);
        test.setPriority(TestPriority.MEDIUM);
        test.setDescription("Unit test for " + componentName + " component in " + serviceName);
        test.setLanguage("javascript");
        test.setCode(String.format("""
            import { render, screen } from '@testing-library/react';
            import %s from './%s';
            
            describe('%s Component', () => {
                test('renders without crashing', () => {
                    render(<%s />);
                    expect(screen.getByTestId('%s')).toBeInTheDocument();
                });
                
                test('handles user interactions', () => {
                    render(<%s />);
                    // Add interaction tests based on real component code
                });
            });
            """, componentName, componentName, componentName, componentName, componentName.toLowerCase(), componentName));
        return test;
    }
    
    private TestCase createReactFunctionTest(String functionName, String serviceName) {
        TestCase test = new TestCase();
        test.setName("test" + functionName + "Function");
        test.setType(TestType.UNIT);
        test.setStatus(TestStatus.PENDING);
        test.setPriority(TestPriority.MEDIUM);
        test.setDescription("Unit test for " + functionName + " function in " + serviceName);
        test.setLanguage("javascript");
        test.setCode(String.format("""
            import { %s } from './utils';
            
            describe('%s Function', () => {
                test('should work correctly', () => {
                    // Test based on real function implementation
                    const result = %s();
                    expect(result).toBeDefined();
                });
            });
            """, functionName, functionName, functionName));
        return test;
    }
    
    private TestCase createSpringControllerTest(String controllerName, String serviceName) {
        TestCase test = new TestCase();
        test.setName("test" + controllerName + "Controller");
        test.setType(TestType.UNIT);
        test.setStatus(TestStatus.PENDING);
        test.setPriority(TestPriority.MEDIUM);
        test.setDescription("Unit test for " + controllerName + " controller in " + serviceName);
        test.setLanguage("java");
        test.setCode(String.format("""
            @SpringBootTest
            @AutoConfigureTestDatabase
            class %sTest {
                
                @Autowired
                private TestRestTemplate restTemplate;
                
                @Test
                void testControllerEndpoints() {
                    // Test based on real controller implementation
                    ResponseEntity<String> response = restTemplate.getForEntity("/api/test", String.class);
                    assertEquals(HttpStatus.OK, response.getStatusCode());
                }
            }
            """, controllerName));
        return test;
    }
    
    private TestCase createSpringServiceTest(String serviceName, String serviceType) {
        TestCase test = new TestCase();
        test.setName("test" + serviceName + "Service");
        test.setType(TestType.UNIT);
        test.setStatus(TestStatus.PENDING);
        test.setPriority(TestPriority.MEDIUM);
        test.setDescription("Unit test for " + serviceName + " service in " + serviceType);
        test.setLanguage("java");
        test.setCode(String.format("""
            @SpringBootTest
            class %sTest {
                
                @Autowired
                private %s %s;
                
                @Test
                void testServiceMethod() {
                    // Test based on real service implementation
                    // Add specific test logic based on actual service code
                }
            }
            """, serviceName, serviceName, serviceName.toLowerCase()));
        return test;
    }
    
    private TestCase createSpringRepositoryTest(String repositoryName, String serviceName) {
        TestCase test = new TestCase();
        test.setName("test" + repositoryName + "Repository");
        test.setType(TestType.UNIT);
        test.setStatus(TestStatus.PENDING);
        test.setPriority(TestPriority.MEDIUM);
        test.setDescription("Unit test for " + repositoryName + " repository in " + serviceName);
        test.setLanguage("java");
        test.setCode(String.format("""
            @DataJpaTest
            class %sTest {
                
                @Autowired
                private TestEntityManager entityManager;
                
                @Autowired
                private %s %s;
                
                @Test
                void testRepositoryMethods() {
                    // Test based on real repository implementation
                    // Add specific test logic based on actual repository code
                }
            }
            """, repositoryName, repositoryName, repositoryName.toLowerCase()));
        return test;
    }
    
    private TestCase createDatabaseTableTest(String tableName, String serviceName) {
        TestCase test = new TestCase();
        test.setName("test" + tableName + "Table");
        test.setType(TestType.UNIT);
        test.setStatus(TestStatus.PENDING);
        test.setPriority(TestPriority.MEDIUM);
        test.setDescription("Unit test for " + tableName + " table in " + serviceName);
        test.setLanguage("sql");
        test.setCode(String.format("""
            -- Test for %s table
            SELECT COUNT(*) FROM %s;
            
            -- Test table constraints
            -- Add specific tests based on actual table structure
            """, tableName, tableName));
        return test;
    }
    
    private TestCase createControllerServiceIntegrationTest(String controllerName, String serviceName, String serviceType) {
        TestCase test = new TestCase();
        test.setName("test" + controllerName + serviceName + "Integration");
        test.setType(TestType.INTEGRATION);
        test.setStatus(TestStatus.PENDING);
        test.setPriority(TestPriority.MEDIUM);
        test.setDescription("Integration test for " + controllerName + " and " + serviceName + " in " + serviceType);
        test.setLanguage("java");
        test.setCode(String.format("""
            @SpringBootTest
            @AutoConfigureTestDatabase
            class %s%sIntegrationTest {
                
                @Autowired
                private TestRestTemplate restTemplate;
                
                @Test
                void testControllerServiceIntegration() {
                    // Integration test based on real code structure
                    // Test the actual interaction between controller and service
                }
            }
            """, controllerName, serviceName));
        return test;
    }
    
    private TestCase createServiceRepositoryIntegrationTest(String serviceName, String repositoryName, String serviceType) {
        TestCase test = new TestCase();
        test.setName("test" + serviceName + repositoryName + "Integration");
        test.setType(TestType.INTEGRATION);
        test.setStatus(TestStatus.PENDING);
        test.setPriority(TestPriority.MEDIUM);
        test.setDescription("Integration test for " + serviceName + " and " + repositoryName + " in " + serviceType);
        test.setLanguage("java");
        test.setCode(String.format("""
            @SpringBootTest
            @AutoConfigureTestDatabase
            class %s%sIntegrationTest {
                
                @Autowired
                private %s %s;
                
                @Test
                void testServiceRepositoryIntegration() {
                    // Integration test based on real code structure
                    // Test the actual interaction between service and repository
                }
            }
            """, serviceName, repositoryName, serviceName, serviceName.toLowerCase()));
        return test;
    }
}
