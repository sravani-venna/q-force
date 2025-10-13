package com.testplatform.backend.service;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Pattern;

@Service
public class LanguageDetectionService {
    
    private static final Logger logger = LoggerFactory.getLogger(LanguageDetectionService.class);
    
    // Language detection patterns
    private static final Map<String, List<String>> LANGUAGE_PATTERNS = new HashMap<>();
    
    static {
        LANGUAGE_PATTERNS.put("java", Arrays.asList("import java\\.", "public class", "@SpringBootApplication", "@RestController", "@Service", "@Repository"));
        LANGUAGE_PATTERNS.put("javascript", Arrays.asList("import.*from", "export.*", "function.*\\(", "const.*=", "let.*=", "var.*="));
        LANGUAGE_PATTERNS.put("typescript", Arrays.asList("interface.*\\{", "type.*=", "enum.*\\{", "import.*from", "export.*"));
        LANGUAGE_PATTERNS.put("python", Arrays.asList("import.*", "def.*\\(", "class.*:", "from.*import", "@app\\.route"));
        LANGUAGE_PATTERNS.put("csharp", Arrays.asList("using.*;", "public class", "namespace.*\\{", "\\[.*\\]"));
        LANGUAGE_PATTERNS.put("go", Arrays.asList("package.*", "import.*", "func.*\\(", "type.*struct"));
        LANGUAGE_PATTERNS.put("rust", Arrays.asList("use.*;", "fn.*\\(", "struct.*\\{", "impl.*\\{", "mod.*"));
    }
    
    // Framework detection patterns
    private static final Map<String, List<String>> FRAMEWORK_PATTERNS = new HashMap<>();
    
    static {
        FRAMEWORK_PATTERNS.put("spring-boot", Arrays.asList("@SpringBootApplication", "@RestController", "@Service", "@Repository", "spring-boot"));
        FRAMEWORK_PATTERNS.put("react", Arrays.asList("import React", "export default", "useState", "useEffect", "jsx", "tsx"));
        FRAMEWORK_PATTERNS.put("angular", Arrays.asList("@Component", "@Injectable", "@NgModule", "angular"));
        FRAMEWORK_PATTERNS.put("vue", Arrays.asList("Vue\\.component", "new Vue", "vue"));
        FRAMEWORK_PATTERNS.put("django", Arrays.asList("from django", "Django", "models\\.Model", "views\\.View"));
        FRAMEWORK_PATTERNS.put("flask", Arrays.asList("from flask", "Flask", "@app\\.route"));
        FRAMEWORK_PATTERNS.put("fastapi", Arrays.asList("from fastapi", "FastAPI", "@app\\.get", "@app\\.post"));
        FRAMEWORK_PATTERNS.put("dotnet", Arrays.asList("using Microsoft", "namespace.*", "\\[.*Controller\\]"));
        FRAMEWORK_PATTERNS.put("gin", Arrays.asList("gin\\.", "gin\\.Default", "c\\.JSON"));
        FRAMEWORK_PATTERNS.put("echo", Arrays.asList("echo\\.", "e\\.GET", "e\\.POST"));
        FRAMEWORK_PATTERNS.put("actix", Arrays.asList("actix", "Actor", "Handler"));
        FRAMEWORK_PATTERNS.put("warp", Arrays.asList("warp", "Filter", "and_then"));
    }
    
    // File extension to language mapping
    private static final Map<String, String> EXTENSION_MAPPING = new HashMap<>();
    
    static {
        EXTENSION_MAPPING.put(".java", "java");
        EXTENSION_MAPPING.put(".js", "javascript");
        EXTENSION_MAPPING.put(".jsx", "javascript");
        EXTENSION_MAPPING.put(".ts", "typescript");
        EXTENSION_MAPPING.put(".tsx", "typescript");
        EXTENSION_MAPPING.put(".py", "python");
        EXTENSION_MAPPING.put(".cs", "csharp");
        EXTENSION_MAPPING.put(".go", "go");
        EXTENSION_MAPPING.put(".rs", "rust");
    }
    
    /**
     * Detect programming language from file content and extension
     */
    public LanguageDetectionResult detectLanguage(String filePath, String content) {
        logger.info("🔍 Detecting language for: {}", filePath);
        
        String extension = getFileExtension(filePath);
        String detectedLanguage = null;
        double confidence = 0.0;
        
        // First try extension-based detection
        if (extension != null && EXTENSION_MAPPING.containsKey(extension)) {
            detectedLanguage = EXTENSION_MAPPING.get(extension);
            confidence = 0.8; // High confidence for extension-based detection
        }
        
        // Then try content-based detection
        if (content != null && !content.trim().isEmpty()) {
            Map<String, Double> languageScores = analyzeContent(content);
            String contentBasedLanguage = getBestMatch(languageScores);
            
            if (contentBasedLanguage != null) {
                double contentConfidence = languageScores.get(contentBasedLanguage);
                
                // If content-based detection has higher confidence, use it
                if (contentConfidence > confidence) {
                    detectedLanguage = contentBasedLanguage;
                    confidence = contentConfidence;
                }
            }
        }
        
        // Fallback to extension if no content-based detection
        if (detectedLanguage == null && extension != null) {
            detectedLanguage = EXTENSION_MAPPING.getOrDefault(extension, "unknown");
            confidence = 0.5;
        }
        
        logger.info("✅ Detected language: {} (confidence: {})", detectedLanguage, confidence);
        
        return new LanguageDetectionResult(detectedLanguage, confidence, extension);
    }
    
    /**
     * Detect framework from file content
     */
    public FrameworkDetectionResult detectFramework(String language, String content) {
        logger.info("🔍 Detecting framework for language: {}", language);
        
        if (content == null || content.trim().isEmpty()) {
            return new FrameworkDetectionResult(null, 0.0);
        }
        
        Map<String, Double> frameworkScores = new HashMap<>();
        
        // Analyze content for framework patterns
        for (Map.Entry<String, List<String>> entry : FRAMEWORK_PATTERNS.entrySet()) {
            String framework = entry.getKey();
            List<String> patterns = entry.getValue();
            
            double score = 0.0;
            int matches = 0;
            
            for (String pattern : patterns) {
                if (content.contains(pattern) || Pattern.compile(pattern).matcher(content).find()) {
                    matches++;
                }
            }
            
            if (matches > 0) {
                score = (double) matches / patterns.size();
                frameworkScores.put(framework, score);
            }
        }
        
        String detectedFramework = getBestMatch(frameworkScores);
        double confidence = frameworkScores.getOrDefault(detectedFramework, 0.0);
        
        logger.info("✅ Detected framework: {} (confidence: {})", detectedFramework, confidence);
        
        return new FrameworkDetectionResult(detectedFramework, confidence);
    }
    
    /**
     * Get test framework for a given language and framework
     */
    public String getTestFramework(String language, String framework) {
        Map<String, Map<String, String>> testFrameworkMapping = Map.of(
            "java", Map.of(
                "spring-boot", "junit",
                "default", "junit"
            ),
            "javascript", Map.of(
                "react", "jest",
                "angular", "jest",
                "vue", "jest",
                "default", "jest"
            ),
            "typescript", Map.of(
                "react", "jest",
                "angular", "jest",
                "vue", "jest",
                "default", "jest"
            ),
            "python", Map.of(
                "django", "pytest",
                "flask", "pytest",
                "fastapi", "pytest",
                "default", "pytest"
            ),
            "csharp", Map.of(
                "dotnet", "nunit",
                "default", "nunit"
            ),
            "go", Map.of(
                "gin", "testing",
                "echo", "testing",
                "default", "testing"
            ),
            "rust", Map.of(
                "actix", "cargo test",
                "warp", "cargo test",
                "default", "cargo test"
            )
        );
        
        return testFrameworkMapping
            .getOrDefault(language, Map.of("default", "unknown"))
            .getOrDefault(framework, "unknown");
    }
    
    /**
     * Analyze content for language patterns
     */
    private Map<String, Double> analyzeContent(String content) {
        Map<String, Double> scores = new HashMap<>();
        
        for (Map.Entry<String, List<String>> entry : LANGUAGE_PATTERNS.entrySet()) {
            String language = entry.getKey();
            List<String> patterns = entry.getValue();
            
            double score = 0.0;
            int matches = 0;
            
            for (String pattern : patterns) {
                if (content.contains(pattern) || Pattern.compile(pattern).matcher(content).find()) {
                    matches++;
                }
            }
            
            if (matches > 0) {
                score = (double) matches / patterns.size();
                scores.put(language, score);
            }
        }
        
        return scores;
    }
    
    /**
     * Get the best match from scores
     */
    private String getBestMatch(Map<String, Double> scores) {
        return scores.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse(null);
    }
    
    /**
     * Get file extension
     */
    private String getFileExtension(String filePath) {
        if (filePath == null || !filePath.contains(".")) {
            return null;
        }
        
        int lastDotIndex = filePath.lastIndexOf(".");
        if (lastDotIndex == -1) {
            return null;
        }
        
        return filePath.substring(lastDotIndex);
    }
    
    /**
     * Language detection result
     */
    public static class LanguageDetectionResult {
        private final String language;
        private final double confidence;
        private final String extension;
        
        public LanguageDetectionResult(String language, double confidence, String extension) {
            this.language = language;
            this.confidence = confidence;
            this.extension = extension;
        }
        
        public String getLanguage() { return language; }
        public double getConfidence() { return confidence; }
        public String getExtension() { return extension; }
    }
    
    /**
     * Framework detection result
     */
    public static class FrameworkDetectionResult {
        private final String framework;
        private final double confidence;
        
        public FrameworkDetectionResult(String framework, double confidence) {
            this.framework = framework;
            this.confidence = confidence;
        }
        
        public String getFramework() { return framework; }
        public double getConfidence() { return confidence; }
    }
}
