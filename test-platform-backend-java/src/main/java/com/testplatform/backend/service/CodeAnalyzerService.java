package com.testplatform.backend.service;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class CodeAnalyzerService {
    
    private static final Logger logger = LoggerFactory.getLogger(CodeAnalyzerService.class);
    
    // Patterns for different code elements
    private static final Pattern CLASS_PATTERN = Pattern.compile("(?:public|private|protected)?\\s*class\\s+(\\w+)");
    private static final Pattern METHOD_PATTERN = Pattern.compile("(?:public|private|protected)\\s+(?:static\\s+)?(?:\\w+\\s+)*?(\\w+)\\s*\\([^)]*\\)\\s*(?:throws\\s+\\w+)?\\s*\\{");
    private static final Pattern ANNOTATION_PATTERN = Pattern.compile("@(\\w+)(?:\\([^)]*\\))?");
    private static final Pattern IMPORT_PATTERN = Pattern.compile("import\\s+([^;]+);");
    private static final Pattern EXCEPTION_PATTERN = Pattern.compile("throw\\s+new\\s+(\\w+)");
    
    /**
     * Analyze code and extract meaningful information for test generation
     */
    public CodeAnalysisResult analyzeCode(String code, String filePath, String language) {
        logger.info("🔍 Analyzing code for: {}", filePath);
        
        CodeAnalysisResult result = new CodeAnalysisResult();
        result.setFilePath(filePath);
        result.setLanguage(language);
        result.setOriginalCode(code);
        
        // Extract basic information
        result.setClasses(extractClasses(code));
        result.setMethods(extractMethods(code));
        result.setAnnotations(extractAnnotations(code));
        result.setImports(extractImports(code));
        result.setExceptions(extractExceptions(code));
        
        // Analyze code complexity and patterns
        result.setComplexity(analyzeComplexity(code));
        result.setTestableMethods(identifyTestableMethods(code));
        result.setDependencies(extractDependencies(code));
        result.setBusinessLogic(extractBusinessLogic(code));
        
        // Generate context for LLM
        result.setContext(generateContext(result));
        
        logger.info("✅ Code analysis complete: {} classes, {} methods, {} testable methods", 
                   result.getClasses().size(), result.getMethods().size(), result.getTestableMethods().size());
        
        return result;
    }
    
    /**
     * Extract class names from code
     */
    private List<String> extractClasses(String code) {
        List<String> classes = new ArrayList<>();
        Matcher matcher = CLASS_PATTERN.matcher(code);
        while (matcher.find()) {
            classes.add(matcher.group(1));
        }
        return classes;
    }
    
    /**
     * Extract method names from code
     */
    private List<String> extractMethods(String code) {
        List<String> methods = new ArrayList<>();
        Matcher matcher = METHOD_PATTERN.matcher(code);
        while (matcher.find()) {
            methods.add(matcher.group(1));
        }
        return methods;
    }
    
    /**
     * Extract annotations from code
     */
    private List<String> extractAnnotations(String code) {
        List<String> annotations = new ArrayList<>();
        Matcher matcher = ANNOTATION_PATTERN.matcher(code);
        while (matcher.find()) {
            annotations.add(matcher.group(1));
        }
        return annotations;
    }
    
    /**
     * Extract imports from code
     */
    private List<String> extractImports(String code) {
        List<String> imports = new ArrayList<>();
        Matcher matcher = IMPORT_PATTERN.matcher(code);
        while (matcher.find()) {
            imports.add(matcher.group(1));
        }
        return imports;
    }
    
    /**
     * Extract exceptions from code
     */
    private List<String> extractExceptions(String code) {
        List<String> exceptions = new ArrayList<>();
        Matcher matcher = EXCEPTION_PATTERN.matcher(code);
        while (matcher.find()) {
            exceptions.add(matcher.group(1));
        }
        return exceptions;
    }
    
    /**
     * Analyze code complexity
     */
    private int analyzeComplexity(String code) {
        int complexity = 0;
        
        // Count control structures
        complexity += StringUtils.countMatches(code, "if");
        complexity += StringUtils.countMatches(code, "for");
        complexity += StringUtils.countMatches(code, "while");
        complexity += StringUtils.countMatches(code, "switch");
        complexity += StringUtils.countMatches(code, "catch");
        
        // Count method calls
        complexity += StringUtils.countMatches(code, "(") - StringUtils.countMatches(code, ")");
        
        return complexity;
    }
    
    /**
     * Identify testable methods (public methods, not getters/setters)
     */
    private List<String> identifyTestableMethods(String code) {
        List<String> testableMethods = new ArrayList<>();
        
        // Look for public methods that are not simple getters/setters
        Pattern testableMethodPattern = Pattern.compile(
            "public\\s+(?!static\\s+)(?:\\w+\\s+)*?(\\w+)\\s*\\([^)]*\\)\\s*(?:throws\\s+\\w+)?\\s*\\{"
        );
        
        Matcher matcher = testableMethodPattern.matcher(code);
        while (matcher.find()) {
            String methodName = matcher.group(1);
            if (!isGetterOrSetter(methodName)) {
                testableMethods.add(methodName);
            }
        }
        
        return testableMethods;
    }
    
    /**
     * Check if method is a getter or setter
     */
    private boolean isGetterOrSetter(String methodName) {
        return methodName.startsWith("get") || methodName.startsWith("set") || 
               methodName.startsWith("is") || methodName.startsWith("has");
    }
    
    /**
     * Extract dependencies from code
     */
    private List<String> extractDependencies(String code) {
        List<String> dependencies = new ArrayList<>();
        
        // Look for @Autowired fields
        Pattern autowiredPattern = Pattern.compile("@Autowired\\s+private\\s+(\\w+)\\s+(\\w+)");
        Matcher matcher = autowiredPattern.matcher(code);
        while (matcher.find()) {
            dependencies.add(matcher.group(1));
        }
        
        // Look for constructor parameters
        Pattern constructorPattern = Pattern.compile("public\\s+\\w+\\s*\\(([^)]+)\\)");
        matcher = constructorPattern.matcher(code);
        while (matcher.find()) {
            String params = matcher.group(1);
            String[] paramTypes = params.split(",");
            for (String param : paramTypes) {
                String type = param.trim().split("\\s+")[0];
                if (!type.equals("String") && !type.equals("int") && !type.equals("boolean")) {
                    dependencies.add(type);
                }
            }
        }
        
        return dependencies;
    }
    
    /**
     * Extract business logic patterns
     */
    private List<String> extractBusinessLogic(String code) {
        List<String> businessLogic = new ArrayList<>();
        
        // Look for validation patterns
        if (code.contains("if") && (code.contains("null") || code.contains("empty"))) {
            businessLogic.add("input_validation");
        }
        
        // Look for exception handling
        if (code.contains("throw") || code.contains("catch")) {
            businessLogic.add("exception_handling");
        }
        
        // Look for data transformation
        if (code.contains("return") && code.contains("new")) {
            businessLogic.add("data_transformation");
        }
        
        // Look for external service calls
        if (code.contains("@Autowired") || code.contains("repository") || code.contains("service")) {
            businessLogic.add("external_dependencies");
        }
        
        return businessLogic;
    }
    
    /**
     * Generate context for LLM
     */
    private String generateContext(CodeAnalysisResult result) {
        StringBuilder context = new StringBuilder();
        
        context.append("Code Analysis Summary:\n");
        context.append("- File: ").append(result.getFilePath()).append("\n");
        context.append("- Language: ").append(result.getLanguage()).append("\n");
        context.append("- Classes: ").append(String.join(", ", result.getClasses())).append("\n");
        context.append("- Methods: ").append(String.join(", ", result.getMethods())).append("\n");
        context.append("- Testable Methods: ").append(String.join(", ", result.getTestableMethods())).append("\n");
        context.append("- Dependencies: ").append(String.join(", ", result.getDependencies())).append("\n");
        context.append("- Business Logic: ").append(String.join(", ", result.getBusinessLogic())).append("\n");
        context.append("- Complexity: ").append(result.getComplexity()).append("\n");
        
        return context.toString();
    }
    
    /**
     * Code analysis result class
     */
    public static class CodeAnalysisResult {
        private String filePath;
        private String language;
        private String originalCode;
        private List<String> classes;
        private List<String> methods;
        private List<String> annotations;
        private List<String> imports;
        private List<String> exceptions;
        private int complexity;
        private List<String> testableMethods;
        private List<String> dependencies;
        private List<String> businessLogic;
        private String context;
        
        // Getters and setters
        public String getFilePath() { return filePath; }
        public void setFilePath(String filePath) { this.filePath = filePath; }
        
        public String getLanguage() { return language; }
        public void setLanguage(String language) { this.language = language; }
        
        public String getOriginalCode() { return originalCode; }
        public void setOriginalCode(String originalCode) { this.originalCode = originalCode; }
        
        public List<String> getClasses() { return classes; }
        public void setClasses(List<String> classes) { this.classes = classes; }
        
        public List<String> getMethods() { return methods; }
        public void setMethods(List<String> methods) { this.methods = methods; }
        
        public List<String> getAnnotations() { return annotations; }
        public void setAnnotations(List<String> annotations) { this.annotations = annotations; }
        
        public List<String> getImports() { return imports; }
        public void setImports(List<String> imports) { this.imports = imports; }
        
        public List<String> getExceptions() { return exceptions; }
        public void setExceptions(List<String> exceptions) { this.exceptions = exceptions; }
        
        public int getComplexity() { return complexity; }
        public void setComplexity(int complexity) { this.complexity = complexity; }
        
        public List<String> getTestableMethods() { return testableMethods; }
        public void setTestableMethods(List<String> testableMethods) { this.testableMethods = testableMethods; }
        
        public List<String> getDependencies() { return dependencies; }
        public void setDependencies(List<String> dependencies) { this.dependencies = dependencies; }
        
        public List<String> getBusinessLogic() { return businessLogic; }
        public void setBusinessLogic(List<String> businessLogic) { this.businessLogic = businessLogic; }
        
        public String getContext() { return context; }
        public void setContext(String context) { this.context = context; }
    }
}
