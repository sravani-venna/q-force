package com.testplatform.backend.service;

import com.testplatform.backend.enums.TestType;
import com.testplatform.backend.model.TestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Service
public class TestExecutionEngine {
    
    private static final Logger logger = LoggerFactory.getLogger(TestExecutionEngine.class);
    
    @Autowired
    private LanguageDetectionService languageDetectionService;
    
    /**
     * Execute tests for a specific language and framework
     */
    public CompletableFuture<TestExecutionResult> executeTests(String repositoryId, String language, 
                                                              String framework, List<TestCase> testCases) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                logger.info("🚀 Executing {} tests for {} {} in repository {}", 
                    testCases.size(), language, framework, repositoryId);
                
                // Create test files
                List<String> testFiles = createTestFiles(language, framework, testCases);
                
                // Execute tests based on language and framework
                TestExecutionResult result = executeTestsByLanguage(language, framework, testFiles);
                
                logger.info("✅ Test execution completed: {} passed, {} failed", 
                    result.getPassedCount(), result.getFailedCount());
                
                return result;
                
            } catch (Exception e) {
                logger.error("❌ Error executing tests: {}", e.getMessage(), e);
                return new TestExecutionResult(0, testCases.size(), 0, 
                    Collections.singletonList("Test execution failed: " + e.getMessage()));
            }
        });
    }
    
    /**
     * Execute tests based on language and framework
     */
    private TestExecutionResult executeTestsByLanguage(String language, String framework, 
                                                       List<String> testFiles) throws Exception {
        switch (language.toLowerCase()) {
            case "java":
                return executeJavaTests(framework, testFiles);
            case "javascript":
            case "typescript":
                return executeJavaScriptTests(framework, testFiles);
            case "python":
                return executePythonTests(framework, testFiles);
            case "csharp":
                return executeCSharpTests(framework, testFiles);
            case "go":
                return executeGoTests(framework, testFiles);
            case "rust":
                return executeRustTests(framework, testFiles);
            default:
                throw new UnsupportedOperationException("Unsupported language: " + language);
        }
    }
    
    /**
     * Execute Java tests (JUnit)
     */
    private TestExecutionResult executeJavaTests(String framework, List<String> testFiles) throws Exception {
        logger.info("☕ Executing Java tests with framework: {}", framework);
        
        // Create Maven/Gradle test command based on framework
        String command = "mvn test";
        if ("spring-boot".equals(framework)) {
            command = "mvn test -Dspring.profiles.active=test";
        }
        
        return executeCommand(command, testFiles);
    }
    
    /**
     * Execute JavaScript/TypeScript tests
     */
    private TestExecutionResult executeJavaScriptTests(String framework, List<String> testFiles) throws Exception {
        logger.info("🟨 Executing JavaScript/TypeScript tests with framework: {}", framework);
        
        String command;
        switch (framework) {
            case "react":
                command = "npm test -- --coverage --watchAll=false";
                break;
            case "angular":
                command = "ng test --watch=false --browsers=ChromeHeadless";
                break;
            case "vue":
                command = "npm run test:unit";
                break;
            default:
                command = "npm test";
        }
        
        return executeCommand(command, testFiles);
    }
    
    /**
     * Execute Python tests
     */
    private TestExecutionResult executePythonTests(String framework, List<String> testFiles) throws Exception {
        logger.info("🐍 Executing Python tests with framework: {}", framework);
        
        String command;
        switch (framework) {
            case "django":
                command = "python manage.py test";
                break;
            case "flask":
                command = "pytest tests/ -v";
                break;
            case "fastapi":
                command = "pytest tests/ -v --cov=app";
                break;
            default:
                command = "pytest tests/ -v";
        }
        
        return executeCommand(command, testFiles);
    }
    
    /**
     * Execute C# tests
     */
    private TestExecutionResult executeCSharpTests(String framework, List<String> testFiles) throws Exception {
        logger.info("🔷 Executing C# tests with framework: {}", framework);
        
        String command;
        switch (framework) {
            case "dotnet":
                command = "dotnet test --logger trx --results-directory TestResults";
                break;
            default:
                command = "dotnet test";
        }
        
        return executeCommand(command, testFiles);
    }
    
    /**
     * Execute Go tests
     */
    private TestExecutionResult executeGoTests(String framework, List<String> testFiles) throws Exception {
        logger.info("🐹 Executing Go tests with framework: {}", framework);
        
        String command = "go test -v ./...";
        if ("gin".equals(framework) || "echo".equals(framework)) {
            command = "go test -v -cover ./...";
        }
        
        return executeCommand(command, testFiles);
    }
    
    /**
     * Execute Rust tests
     */
    private TestExecutionResult executeRustTests(String framework, List<String> testFiles) throws Exception {
        logger.info("🦀 Executing Rust tests with framework: {}", framework);
        
        String command = "cargo test";
        if ("actix".equals(framework) || "warp".equals(framework)) {
            command = "cargo test --features test";
        }
        
        return executeCommand(command, testFiles);
    }
    
    /**
     * Execute command and return results
     */
    private TestExecutionResult executeCommand(String command, List<String> testFiles) throws Exception {
        logger.info("🔧 Executing command: {}", command);
        
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command("sh", "-c", command);
        processBuilder.directory(new File("."));
        
        Process process = processBuilder.start();
        
        // Read output
        StringBuilder output = new StringBuilder();
        StringBuilder error = new StringBuilder();
        
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
             BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
            
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
            
            while ((line = errorReader.readLine()) != null) {
                error.append(line).append("\n");
            }
        }
        
        // Wait for completion
        boolean finished = process.waitFor(5, TimeUnit.MINUTES);
        if (!finished) {
            process.destroyForcibly();
            throw new RuntimeException("Test execution timed out");
        }
        
        int exitCode = process.exitValue();
        String outputStr = output.toString();
        String errorStr = error.toString();
        
        // Parse results
        return parseTestResults(outputStr, errorStr, exitCode, testFiles.size());
    }
    
    /**
     * Parse test execution results
     */
    private TestExecutionResult parseTestResults(String output, String error, int exitCode, int totalTests) {
        int passedCount = 0;
        int failedCount = 0;
        List<String> failures = new ArrayList<>();
        
        // Parse based on common test output patterns
        if (output.contains("Tests run:") || output.contains("test result:")) {
            // JUnit/Maven style
            passedCount = extractNumber(output, "Tests run:", "Failures:");
            failedCount = extractNumber(output, "Failures:", "Errors:");
        } else if (output.contains("PASS") || output.contains("FAIL")) {
            // Jest/Node.js style
            String[] lines = output.split("\n");
            for (String line : lines) {
                if (line.contains("PASS")) {
                    passedCount++;
                } else if (line.contains("FAIL")) {
                    failedCount++;
                }
            }
        } else if (output.contains("passed") || output.contains("failed")) {
            // pytest style
            passedCount = extractNumber(output, "passed", "failed");
            failedCount = extractNumber(output, "failed", "");
        }
        
        // If parsing failed, use exit code
        if (passedCount == 0 && failedCount == 0) {
            if (exitCode == 0) {
                passedCount = totalTests;
            } else {
                failedCount = totalTests;
                failures.add("Test execution failed with exit code: " + exitCode);
            }
        }
        
        // Add error output to failures
        if (!error.isEmpty()) {
            failures.add("Error output: " + error);
        }
        
        return new TestExecutionResult(passedCount, failedCount, totalTests, failures);
    }
    
    /**
     * Extract number from text
     */
    private int extractNumber(String text, String startPattern, String endPattern) {
        try {
            int startIndex = text.indexOf(startPattern);
            if (startIndex == -1) return 0;
            
            int endIndex = endPattern.isEmpty() ? text.length() : text.indexOf(endPattern, startIndex);
            if (endIndex == -1) endIndex = text.length();
            
            String numberStr = text.substring(startIndex + startPattern.length(), endIndex)
                .replaceAll("[^0-9]", "");
            
            return numberStr.isEmpty() ? 0 : Integer.parseInt(numberStr);
        } catch (Exception e) {
            return 0;
        }
    }
    
    /**
     * Create test files for execution
     */
    private List<String> createTestFiles(String language, String framework, List<TestCase> testCases) throws IOException {
        List<String> testFiles = new ArrayList<>();
        
        for (TestCase testCase : testCases) {
            String testFileName = createTestFile(language, framework, testCase);
            testFiles.add(testFileName);
        }
        
        return testFiles;
    }
    
    /**
     * Create individual test file
     */
    private String createTestFile(String language, String framework, TestCase testCase) throws IOException {
        String fileName = generateTestFileName(language, framework, testCase);
        String testContent = generateTestFileContent(language, framework, testCase);
        
        Path testPath = Paths.get("test-output", fileName);
        Files.createDirectories(testPath.getParent());
        Files.write(testPath, testContent.getBytes());
        
        return fileName;
    }
    
    /**
     * Generate test file name
     */
    private String generateTestFileName(String language, String framework, TestCase testCase) {
        String baseName = testCase.getName().replaceAll("[^a-zA-Z0-9]", "_");
        
        switch (language.toLowerCase()) {
            case "java":
                return baseName + "Test.java";
            case "javascript":
                return baseName + ".test.js";
            case "typescript":
                return baseName + ".test.ts";
            case "python":
                return "test_" + baseName + ".py";
            case "csharp":
                return baseName + "Tests.cs";
            case "go":
                return baseName + "_test.go";
            case "rust":
                return baseName + "_test.rs";
            default:
                return baseName + "_test.txt";
        }
    }
    
    /**
     * Generate test file content
     */
    private String generateTestFileContent(String language, String framework, TestCase testCase) {
        switch (language.toLowerCase()) {
            case "java":
                return generateJavaTestContent(framework, testCase);
            case "javascript":
            case "typescript":
                return generateJavaScriptTestContent(framework, testCase);
            case "python":
                return generatePythonTestContent(framework, testCase);
            case "csharp":
                return generateCSharpTestContent(framework, testCase);
            case "go":
                return generateGoTestContent(framework, testCase);
            case "rust":
                return generateRustTestContent(framework, testCase);
            default:
                return generateGenericTestContent(testCase);
        }
    }
    
    /**
     * Generate Java test content
     */
    private String generateJavaTestContent(String framework, TestCase testCase) {
        return String.format("""
            package com.testplatform.generated;
            
            import org.junit.jupiter.api.Test;
            import org.junit.jupiter.api.BeforeEach;
            import static org.junit.jupiter.api.Assertions.*;
            
            public class %sTest {
                
                @Test
                public void %s() {
                    // %s
                    %s
                }
            }
            """, 
            testCase.getName().replaceAll("[^a-zA-Z0-9]", ""),
            testCase.getName().replaceAll("[^a-zA-Z0-9]", ""),
            testCase.getDescription(),
            testCase.getCode()
        );
    }
    
    /**
     * Generate JavaScript test content
     */
    private String generateJavaScriptTestContent(String framework, TestCase testCase) {
        return String.format("""
            // %s
            test('%s', () => {
                %s
            });
            """, 
            testCase.getDescription(),
            testCase.getName(),
            testCase.getCode()
        );
    }
    
    /**
     * Generate Python test content
     */
    private String generatePythonTestContent(String framework, TestCase testCase) {
        return String.format("""
            import unittest
            
            class %sTest(unittest.TestCase):
                
                def test_%s(self):
                    \"\"\"%s\"\"\"
                    %s
            """, 
            testCase.getName().replaceAll("[^a-zA-Z0-9]", ""),
            testCase.getName().replaceAll("[^a-zA-Z0-9]", "_"),
            testCase.getDescription(),
            testCase.getCode()
        );
    }
    
    /**
     * Generate C# test content
     */
    private String generateCSharpTestContent(String framework, TestCase testCase) {
        return String.format("""
            using NUnit.Framework;
            
            [TestFixture]
            public class %sTest {
                
                [Test]
                public void %s() {
                    // %s
                    %s
                }
            }
            """, 
            testCase.getName().replaceAll("[^a-zA-Z0-9]", ""),
            testCase.getName().replaceAll("[^a-zA-Z0-9]", ""),
            testCase.getDescription(),
            testCase.getCode()
        );
    }
    
    /**
     * Generate Go test content
     */
    private String generateGoTestContent(String framework, TestCase testCase) {
        return String.format("""
            package main
            
            import "testing"
            
            func Test%s(t *testing.T) {
                // %s
                %s
            }
            """, 
            testCase.getName().replaceAll("[^a-zA-Z0-9]", ""),
            testCase.getDescription(),
            testCase.getCode()
        );
    }
    
    /**
     * Generate Rust test content
     */
    private String generateRustTestContent(String framework, TestCase testCase) {
        return String.format("""
            #[cfg(test)]
            mod tests {
                use super::*;
                
                #[test]
                fn test_%s() {
                    // %s
                    %s
                }
            }
            """, 
            testCase.getName().replaceAll("[^a-zA-Z0-9]", "_"),
            testCase.getDescription(),
            testCase.getCode()
        );
    }
    
    /**
     * Generate generic test content
     */
    private String generateGenericTestContent(TestCase testCase) {
        return String.format("""
            // %s
            // %s
            
            %s
            """, 
            testCase.getName(),
            testCase.getDescription(),
            testCase.getCode()
        );
    }
    
    /**
     * Test execution result
     */
    public static class TestExecutionResult {
        private final int passedCount;
        private final int failedCount;
        private final int totalCount;
        private final List<String> failures;
        private final long executionTime;
        
        public TestExecutionResult(int passedCount, int failedCount, int totalCount, List<String> failures) {
            this.passedCount = passedCount;
            this.failedCount = failedCount;
            this.totalCount = totalCount;
            this.failures = failures;
            this.executionTime = System.currentTimeMillis();
        }
        
        public int getPassedCount() { return passedCount; }
        public int getFailedCount() { return failedCount; }
        public int getTotalCount() { return totalCount; }
        public List<String> getFailures() { return failures; }
        public long getExecutionTime() { return executionTime; }
        
        public boolean isSuccess() { return failedCount == 0; }
        public double getSuccessRate() { 
            return totalCount > 0 ? (double) passedCount / totalCount : 0.0; 
        }
    }
}
