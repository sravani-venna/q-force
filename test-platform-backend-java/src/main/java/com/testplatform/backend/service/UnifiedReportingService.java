package com.testplatform.backend.service;

import com.testplatform.backend.model.TestCase;
import com.testplatform.backend.service.TestOrchestrationService.TestOrchestrationResult;
import com.testplatform.backend.service.TestOrchestrationService.ServiceTestResult;
import com.testplatform.backend.service.TestOrchestrationService.QualityGate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UnifiedReportingService {
    
    private static final Logger logger = LoggerFactory.getLogger(UnifiedReportingService.class);
    
    @Autowired
    private TestOrchestrationService testOrchestrationService;
    
    /**
     * Generate comprehensive test reports in multiple formats
     */
    public ReportGenerationResult generateReports(TestOrchestrationResult orchestrationResult, 
                                                 String reportId, String pathFlow) {
        try {
            logger.info("📊 Generating unified reports for path flow: {}", pathFlow);
            
            // Create reports directory
            Path reportsDir = createReportsDirectory(reportId);
            
            // Generate Allure report
            String allureReport = generateAllureReport(orchestrationResult, pathFlow);
            writeReportFile(reportsDir, "allure-results.json", allureReport);
            
            // Generate JSON report
            String jsonReport = generateJsonReport(orchestrationResult, pathFlow);
            writeReportFile(reportsDir, "test-results.json", jsonReport);
            
            // Generate HTML report
            String htmlReport = generateHtmlReport(orchestrationResult, pathFlow);
            writeReportFile(reportsDir, "test-report.html", htmlReport);
            
            // Generate metrics dashboard data
            String metricsData = generateMetricsData(orchestrationResult);
            writeReportFile(reportsDir, "metrics.json", metricsData);
            
            // Generate quality gates report
            String qualityGatesReport = generateQualityGatesReport(orchestrationResult);
            writeReportFile(reportsDir, "quality-gates.json", qualityGatesReport);
            
            logger.info("✅ Reports generated successfully in: {}", reportsDir);
            
            return new ReportGenerationResult(
                reportId,
                reportsDir.toString(),
                orchestrationResult.getTotalTests(),
                orchestrationResult.getPassedTests(),
                orchestrationResult.getOverallSuccessRate(),
                orchestrationResult.getTestCoverage(),
                orchestrationResult.getQualityGates().stream().allMatch(QualityGate::isPassed)
            );
            
        } catch (Exception e) {
            logger.error("❌ Error generating reports: {}", e.getMessage(), e);
            throw new RuntimeException("Report generation failed", e);
        }
    }
    
    /**
     * Generate Allure-compatible report
     */
    private String generateAllureReport(TestOrchestrationResult orchestrationResult, String pathFlow) {
        StringBuilder allureReport = new StringBuilder();
        
        allureReport.append("{\n");
        allureReport.append("  \"reportName\": \"Path Flow Test Report\",\n");
        allureReport.append("  \"pathFlow\": \"").append(pathFlow).append("\",\n");
        allureReport.append("  \"timestamp\": \"").append(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)).append("\",\n");
        allureReport.append("  \"summary\": {\n");
        allureReport.append("    \"totalTests\": ").append(orchestrationResult.getTotalTests()).append(",\n");
        allureReport.append("    \"passedTests\": ").append(orchestrationResult.getPassedTests()).append(",\n");
        allureReport.append("    \"failedTests\": ").append(orchestrationResult.getTotalTests() - orchestrationResult.getPassedTests()).append(",\n");
        allureReport.append("    \"successRate\": ").append(orchestrationResult.getOverallSuccessRate()).append(",\n");
        allureReport.append("    \"testCoverage\": ").append(orchestrationResult.getTestCoverage()).append("\n");
        allureReport.append("  },\n");
        
        // Service results
        allureReport.append("  \"services\": [\n");
        for (int i = 0; i < orchestrationResult.getServiceResults().size(); i++) {
            ServiceTestResult serviceResult = orchestrationResult.getServiceResults().get(i);
            allureReport.append("    {\n");
            allureReport.append("      \"name\": \"").append(serviceResult.getServiceName()).append("\",\n");
            allureReport.append("      \"language\": \"").append(serviceResult.getLanguage()).append("\",\n");
            allureReport.append("      \"framework\": \"").append(serviceResult.getFramework()).append("\",\n");
            allureReport.append("      \"totalTests\": ").append(serviceResult.getTotalTests()).append(",\n");
            allureReport.append("      \"passedTests\": ").append(serviceResult.getPassedTests()).append(",\n");
            allureReport.append("      \"successRate\": ").append(serviceResult.getSuccessRate()).append("\n");
            allureReport.append("    }");
            if (i < orchestrationResult.getServiceResults().size() - 1) {
                allureReport.append(",");
            }
            allureReport.append("\n");
        }
        allureReport.append("  ],\n");
        
        // Test cases
        allureReport.append("  \"testCases\": [\n");
        List<TestCase> allTests = new ArrayList<>();
        orchestrationResult.getServiceResults().forEach(service -> {
            allTests.addAll(service.getUnitTestResults());
            allTests.addAll(service.getIntegrationTestResults());
        });
        allTests.addAll(orchestrationResult.getIntegrationResults());
        allTests.addAll(orchestrationResult.getE2eResults());
        
        for (int i = 0; i < allTests.size(); i++) {
            TestCase test = allTests.get(i);
            allureReport.append("    {\n");
            allureReport.append("      \"name\": \"").append(test.getName()).append("\",\n");
            allureReport.append("      \"description\": \"").append(test.getDescription()).append("\",\n");
            allureReport.append("      \"type\": \"").append(test.getType()).append("\",\n");
            allureReport.append("      \"status\": \"").append(test.getStatus()).append("\",\n");
            allureReport.append("      \"priority\": \"").append(test.getPriority()).append("\",\n");
            allureReport.append("      \"executionTime\": ").append(test.getExecutionTime() != null ? test.getExecutionTime() : 0).append("\n");
            allureReport.append("    }");
            if (i < allTests.size() - 1) {
                allureReport.append(",");
            }
            allureReport.append("\n");
        }
        allureReport.append("  ]\n");
        allureReport.append("}\n");
        
        return allureReport.toString();
    }
    
    /**
     * Generate JSON report
     */
    private String generateJsonReport(TestOrchestrationResult orchestrationResult, String pathFlow) {
        Map<String, Object> report = new HashMap<>();
        
        report.put("reportId", UUID.randomUUID().toString());
        report.put("pathFlow", pathFlow);
        report.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        
        // Summary
        Map<String, Object> summary = new HashMap<>();
        summary.put("totalTests", orchestrationResult.getTotalTests());
        summary.put("passedTests", orchestrationResult.getPassedTests());
        summary.put("failedTests", orchestrationResult.getTotalTests() - orchestrationResult.getPassedTests());
        summary.put("successRate", orchestrationResult.getOverallSuccessRate());
        summary.put("testCoverage", orchestrationResult.getTestCoverage());
        summary.put("serviceCoverage", orchestrationResult.getServiceCoverage());
        report.put("summary", summary);
        
        // Service results
        List<Map<String, Object>> services = new ArrayList<>();
        for (ServiceTestResult serviceResult : orchestrationResult.getServiceResults()) {
            Map<String, Object> service = new HashMap<>();
            service.put("name", serviceResult.getServiceName());
            service.put("language", serviceResult.getLanguage());
            service.put("framework", serviceResult.getFramework());
            service.put("totalTests", serviceResult.getTotalTests());
            service.put("passedTests", serviceResult.getPassedTests());
            service.put("successRate", serviceResult.getSuccessRate());
            services.add(service);
        }
        report.put("services", services);
        
        // Quality gates
        List<Map<String, Object>> qualityGates = new ArrayList<>();
        for (QualityGate gate : orchestrationResult.getQualityGates()) {
            Map<String, Object> qualityGate = new HashMap<>();
            qualityGate.put("name", gate.getName());
            qualityGate.put("passed", gate.isPassed());
            qualityGate.put("description", gate.getDescription());
            qualityGates.add(qualityGate);
        }
        report.put("qualityGates", qualityGates);
        
        // Recommendations
        report.put("recommendations", orchestrationResult.getRecommendations());
        
        return convertToJson(report);
    }
    
    /**
     * Generate HTML report
     */
    private String generateHtmlReport(TestOrchestrationResult orchestrationResult, String pathFlow) {
        StringBuilder html = new StringBuilder();
        
        html.append("<!DOCTYPE html>\n");
        html.append("<html lang=\"en\">\n");
        html.append("<head>\n");
        html.append("    <meta charset=\"UTF-8\">\n");
        html.append("    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n");
        html.append("    <title>Path Flow Test Report</title>\n");
        html.append("    <style>\n");
        html.append(getHtmlStyles());
        html.append("    </style>\n");
        html.append("</head>\n");
        html.append("<body>\n");
        
        // Header
        html.append("    <header class=\"header\">\n");
        html.append("        <h1>🧪 Path Flow Test Report</h1>\n");
        html.append("        <p class=\"path-flow\">Path Flow: ").append(pathFlow).append("</p>\n");
        html.append("        <p class=\"timestamp\">Generated: ").append(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)).append("</p>\n");
        html.append("    </header>\n");
        
        // Summary
        html.append("    <section class=\"summary\">\n");
        html.append("        <h2>📊 Summary</h2>\n");
        html.append("        <div class=\"metrics\">\n");
        html.append("            <div class=\"metric\">\n");
        html.append("                <span class=\"label\">Total Tests:</span>\n");
        html.append("                <span class=\"value\">").append(orchestrationResult.getTotalTests()).append("</span>\n");
        html.append("            </div>\n");
        html.append("            <div class=\"metric\">\n");
        html.append("                <span class=\"label\">Passed Tests:</span>\n");
        html.append("                <span class=\"value passed\">").append(orchestrationResult.getPassedTests()).append("</span>\n");
        html.append("            </div>\n");
        html.append("            <div class=\"metric\">\n");
        html.append("                <span class=\"label\">Success Rate:</span>\n");
        html.append("                <span class=\"value\">").append(String.format("%.1f%%", orchestrationResult.getOverallSuccessRate() * 100)).append("</span>\n");
        html.append("            </div>\n");
        html.append("            <div class=\"metric\">\n");
        html.append("                <span class=\"label\">Test Coverage:</span>\n");
        html.append("                <span class=\"value\">").append(String.format("%.1f%%", orchestrationResult.getTestCoverage() * 100)).append("</span>\n");
        html.append("            </div>\n");
        html.append("        </div>\n");
        html.append("    </section>\n");
        
        // Service results
        html.append("    <section class=\"services\">\n");
        html.append("        <h2>🔧 Service Results</h2>\n");
        for (ServiceTestResult serviceResult : orchestrationResult.getServiceResults()) {
            html.append("        <div class=\"service\">\n");
            html.append("            <h3>").append(serviceResult.getServiceName()).append("</h3>\n");
            html.append("            <div class=\"service-info\">\n");
            html.append("                <span class=\"language\">").append(serviceResult.getLanguage()).append("</span>\n");
            html.append("                <span class=\"framework\">").append(serviceResult.getFramework()).append("</span>\n");
            html.append("            </div>\n");
            html.append("            <div class=\"service-metrics\">\n");
            html.append("                <span>Tests: ").append(serviceResult.getTotalTests()).append("</span>\n");
            html.append("                <span>Passed: ").append(serviceResult.getPassedTests()).append("</span>\n");
            html.append("                <span>Success Rate: ").append(String.format("%.1f%%", serviceResult.getSuccessRate() * 100)).append("</span>\n");
            html.append("            </div>\n");
            html.append("        </div>\n");
        }
        html.append("    </section>\n");
        
        // Quality gates
        html.append("    <section class=\"quality-gates\">\n");
        html.append("        <h2>🚪 Quality Gates</h2>\n");
        for (QualityGate gate : orchestrationResult.getQualityGates()) {
            html.append("        <div class=\"quality-gate ").append(gate.isPassed() ? "passed" : "failed").append("\">\n");
            html.append("            <span class=\"status\">").append(gate.isPassed() ? "✅" : "❌").append("</span>\n");
            html.append("            <span class=\"name\">").append(gate.getName()).append("</span>\n");
            html.append("            <span class=\"description\">").append(gate.getDescription()).append("</span>\n");
            html.append("        </div>\n");
        }
        html.append("    </section>\n");
        
        // Recommendations
        html.append("    <section class=\"recommendations\">\n");
        html.append("        <h2>💡 Recommendations</h2>\n");
        html.append("        <ul>\n");
        for (String recommendation : orchestrationResult.getRecommendations()) {
            html.append("            <li>").append(recommendation).append("</li>\n");
        }
        html.append("        </ul>\n");
        html.append("    </section>\n");
        
        html.append("</body>\n");
        html.append("</html>\n");
        
        return html.toString();
    }
    
    /**
     * Generate metrics dashboard data
     */
    private String generateMetricsData(TestOrchestrationResult orchestrationResult) {
        Map<String, Object> metrics = new HashMap<>();
        
        // Overall metrics
        Map<String, Object> overall = new HashMap<>();
        overall.put("totalTests", orchestrationResult.getTotalTests());
        overall.put("passedTests", orchestrationResult.getPassedTests());
        overall.put("successRate", orchestrationResult.getOverallSuccessRate());
        overall.put("testCoverage", orchestrationResult.getTestCoverage());
        overall.put("serviceCoverage", orchestrationResult.getServiceCoverage());
        metrics.put("overall", overall);
        
        // Service metrics
        List<Map<String, Object>> serviceMetrics = new ArrayList<>();
        for (ServiceTestResult serviceResult : orchestrationResult.getServiceResults()) {
            Map<String, Object> serviceMetric = new HashMap<>();
            serviceMetric.put("serviceName", serviceResult.getServiceName());
            serviceMetric.put("language", serviceResult.getLanguage());
            serviceMetric.put("framework", serviceResult.getFramework());
            serviceMetric.put("totalTests", serviceResult.getTotalTests());
            serviceMetric.put("passedTests", serviceResult.getPassedTests());
            serviceMetric.put("successRate", serviceResult.getSuccessRate());
            serviceMetrics.add(serviceMetric);
        }
        metrics.put("services", serviceMetrics);
        
        // Quality gate metrics
        Map<String, Object> qualityGateMetrics = new HashMap<>();
        qualityGateMetrics.put("totalGates", orchestrationResult.getQualityGates().size());
        qualityGateMetrics.put("passedGates", (int) orchestrationResult.getQualityGates().stream().filter(QualityGate::isPassed).count());
        qualityGateMetrics.put("allGatesPassed", orchestrationResult.getQualityGates().stream().allMatch(QualityGate::isPassed));
        metrics.put("qualityGates", qualityGateMetrics);
        
        return convertToJson(metrics);
    }
    
    /**
     * Generate quality gates report
     */
    private String generateQualityGatesReport(TestOrchestrationResult orchestrationResult) {
        Map<String, Object> report = new HashMap<>();
        
        report.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        report.put("allGatesPassed", orchestrationResult.getQualityGates().stream().allMatch(QualityGate::isPassed));
        
        List<Map<String, Object>> gates = new ArrayList<>();
        for (QualityGate gate : orchestrationResult.getQualityGates()) {
            Map<String, Object> gateData = new HashMap<>();
            gateData.put("name", gate.getName());
            gateData.put("passed", gate.isPassed());
            gateData.put("description", gate.getDescription());
            gates.add(gateData);
        }
        report.put("gates", gates);
        
        return convertToJson(report);
    }
    
    /**
     * Create reports directory
     */
    private Path createReportsDirectory(String reportId) throws IOException {
        Path reportsDir = Paths.get("reports", reportId);
        Files.createDirectories(reportsDir);
        return reportsDir;
    }
    
    /**
     * Write report file
     */
    private void writeReportFile(Path reportsDir, String filename, String content) throws IOException {
        Path filePath = reportsDir.resolve(filename);
        Files.write(filePath, content.getBytes());
        logger.info("📄 Report written: {}", filePath);
    }
    
    /**
     * Get HTML styles
     */
    private String getHtmlStyles() {
        return "            body {" +
    "                font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;" +
    "                margin: 0;" +
    "                padding: 20px;" +
    "                background-color: #f5f5f5;" +
    "            }" +
    "            .header {" +
    "                background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);" +
    "                color: white;" +
    "                padding: 30px;" +
    "                border-radius: 10px;" +
    "                margin-bottom: 30px;" +
    "            }" +
    "            .header h1 {" +
    "                margin: 0 0 10px 0;" +
    "                font-size: 2.5em;" +
    "            }" +
    "            .path-flow {" +
    "                font-size: 1.2em;" +
    "                margin: 10px 0;" +
    "            }" +
    "            .timestamp {" +
    "                opacity: 0.8;" +
    "                margin: 5px 0 0 0;" +
    "            }" +
    "            .summary {" +
    "                background: white;" +
    "                padding: 30px;" +
    "                border-radius: 10px;" +
    "                margin-bottom: 30px;" +
    "                box-shadow: 0 2px 10px rgba(0,0,0,0.1);" +
    "            }" +
    "            .metrics {" +
    "                display: grid;" +
    "                grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));" +
    "                gap: 20px;" +
    "                margin-top: 20px;" +
    "            }" +
    "            .metric {" +
    "                display: flex;" +
    "                flex-direction: column;" +
    "                align-items: center;" +
    "                padding: 20px;" +
    "                background: #f8f9fa;" +
    "                border-radius: 8px;" +
    "            }" +
    "            .metric .label {" +
    "                font-size: 0.9em;" +
    "                color: #666;" +
    "                margin-bottom: 5px;" +
    "            }" +
    "            .metric .value {" +
    "                font-size: 2em;" +
    "                font-weight: bold;" +
    "                color: #333;" +
    "            }" +
    "            .metric .value.passed {" +
    "                color: #28a745;" +
    "            }" +
    "            .services {" +
    "                background: white;" +
    "                padding: 30px;" +
    "                border-radius: 10px;" +
    "                margin-bottom: 30px;" +
    "                box-shadow: 0 2px 10px rgba(0,0,0,0.1);" +
    "            }" +
    "            .service {" +
    "                border: 1px solid #e9ecef;" +
    "                border-radius: 8px;" +
    "                padding: 20px;" +
    "                margin-bottom: 20px;" +
    "            }" +
    "            .service h3 {" +
    "                margin: 0 0 10px 0;" +
    "                color: #333;" +
    "            }" +
    "            .service-info {" +
    "                margin-bottom: 15px;" +
    "            }" +
    "            .service-info span {" +
    "                background: #e9ecef;" +
    "                padding: 4px 8px;" +
    "                border-radius: 4px;" +
    "                margin-right: 10px;" +
    "                font-size: 0.9em;" +
    "            }" +
    "            .service-metrics {" +
    "                display: flex;" +
    "                gap: 20px;" +
    "            }" +
    "            .service-metrics span {" +
    "                font-weight: 500;" +
    "            }" +
    "            .quality-gates {" +
    "                background: white;" +
    "                padding: 30px;" +
    "                border-radius: 10px;" +
    "                margin-bottom: 30px;" +
    "                box-shadow: 0 2px 10px rgba(0,0,0,0.1);" +
    "            }" +
    "            .quality-gate {" +
    "                display: flex;" +
    "                align-items: center;" +
    "                padding: 15px;" +
    "                border-radius: 8px;" +
    "                margin-bottom: 10px;" +
    "            }" +
    "            .quality-gate.passed {" +
    "                background: #d4edda;" +
    "                border-left: 4px solid #28a745;" +
    "            }" +
    "            .quality-gate.failed {" +
    "                background: #f8d7da;" +
    "                border-left: 4px solid #dc3545;" +
    "            }" +
    "            .quality-gate .status {" +
    "                font-size: 1.2em;" +
    "                margin-right: 15px;" +
    "            }" +
    "            .quality-gate .name {" +
    "                font-weight: bold;" +
    "                margin-right: 15px;" +
    "            }" +
    "            .recommendations {" +
    "                background: white;" +
    "                padding: 30px;" +
    "                border-radius: 10px;" +
    "                box-shadow: 0 2px 10px rgba(0,0,0,0.1);" +
    "            }" +
    "            .recommendations ul {" +
    "                list-style: none;" +
    "                padding: 0;" +
    "            }" +
    "            .recommendations li {" +
    "                padding: 10px 0;" +
    "                border-bottom: 1px solid #e9ecef;" +
    "            }" +
    "            .recommendations li:before {" +
            "                content: \"💡\";" +
    "                margin-right: 10px;" +
    "            }";
    }
    
    /**
     * Convert object to JSON string
     */
    private String convertToJson(Object obj) {
        // Simple JSON conversion - in production, use Jackson or Gson
        return obj.toString().replace("=", ": ").replace("{", "{\n  ").replace("}", "\n}");
    }
    
    /**
     * Report generation result
     */
    public static class ReportGenerationResult {
        private final String reportId;
        private final String reportPath;
        private final int totalTests;
        private final int passedTests;
        private final double successRate;
        private final double testCoverage;
        private final boolean allQualityGatesPassed;
        
        public ReportGenerationResult(String reportId, String reportPath, int totalTests, 
                                    int passedTests, double successRate, double testCoverage, 
                                    boolean allQualityGatesPassed) {
            this.reportId = reportId;
            this.reportPath = reportPath;
            this.totalTests = totalTests;
            this.passedTests = passedTests;
            this.successRate = successRate;
            this.testCoverage = testCoverage;
            this.allQualityGatesPassed = allQualityGatesPassed;
        }
        
        // Getters
        public String getReportId() { return reportId; }
        public String getReportPath() { return reportPath; }
        public int getTotalTests() { return totalTests; }
        public int getPassedTests() { return passedTests; }
        public double getSuccessRate() { return successRate; }
        public double getTestCoverage() { return testCoverage; }
        public boolean isAllQualityGatesPassed() { return allQualityGatesPassed; }
    }
}
