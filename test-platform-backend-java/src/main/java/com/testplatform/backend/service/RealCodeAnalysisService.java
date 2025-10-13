package com.testplatform.backend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class RealCodeAnalysisService {

    private static final Logger logger = LoggerFactory.getLogger(RealCodeAnalysisService.class);
    
    private static final String CDAC_PROJECT_PATH = "/Users/vnannuri/Desktop/test";
    
    /**
     * Analyze real CDAC project code and extract meaningful information
     */
    public CodeAnalysisResult analyzeRealCode(String serviceName) {
        logger.info("Analyzing real code for service: {}", serviceName);
        
        CodeAnalysisResult result = new CodeAnalysisResult();
        result.setServiceName(serviceName);
        
        try {
            switch (serviceName.toLowerCase()) {
                case "reactfrontend":
                    analyzeReactCode(result);
                    break;
                case "springbackend":
                    analyzeSpringCode(result);
                    break;
                case "database":
                    analyzeDatabaseCode(result);
                    break;
                default:
                    analyzeGenericCode(result, serviceName);
            }
        } catch (Exception e) {
            logger.error("Error analyzing real code for service {}: {}", serviceName, e.getMessage());
            result.setError("Failed to analyze code: " + e.getMessage());
        }
        
        return result;
    }
    
    private void analyzeReactCode(CodeAnalysisResult result) {
        result.setLanguage("javascript");
        result.setFramework("react");
        
        List<String> components = new ArrayList<>();
        List<String> functions = new ArrayList<>();
        List<String> dependencies = new ArrayList<>();
        
        try {
            // Analyze React components
            Path reactPath = Paths.get(CDAC_PROJECT_PATH, "React", "my-app", "src", "Components");
            if (Files.exists(reactPath)) {
                Files.walk(reactPath)
                    .filter(path -> path.toString().endsWith(".jsx") || path.toString().endsWith(".js"))
                    .forEach(path -> {
                        try {
                            String content = Files.readString(path);
                            String fileName = path.getFileName().toString();
                            
                            // Extract component names
                            Pattern componentPattern = Pattern.compile("(?:function|const)\\s+(\\w+)\\s*(?:=|\\()");
                            Matcher matcher = componentPattern.matcher(content);
                            while (matcher.find()) {
                                components.add(matcher.group(1));
                            }
                            
                            // Extract imports
                            Pattern importPattern = Pattern.compile("import\\s+.*?\\s+from\\s+['\"]([^'\"]+)['\"]");
                            Matcher importMatcher = importPattern.matcher(content);
                            while (importMatcher.find()) {
                                dependencies.add(importMatcher.group(1));
                            }
                            
                            // Extract function definitions
                            Pattern functionPattern = Pattern.compile("(?:function|const)\\s+(\\w+)\\s*=\\s*(?:async\\s+)?\\(");
                            Matcher functionMatcher = functionPattern.matcher(content);
                            while (functionMatcher.find()) {
                                functions.add(functionMatcher.group(1));
                            }
                            
                        } catch (IOException e) {
                            logger.warn("Could not read file {}: {}", path, e.getMessage());
                        }
                    });
            }
            
            result.setComponents(components);
            result.setFunctions(functions);
            result.setDependencies(dependencies);
            result.setCodeFiles(findCodeFiles("React"));
            
        } catch (Exception e) {
            logger.error("Error analyzing React code: {}", e.getMessage());
        }
    }
    
    private void analyzeSpringCode(CodeAnalysisResult result) {
        result.setLanguage("java");
        result.setFramework("spring-boot");
        
        List<String> controllers = new ArrayList<>();
        List<String> services = new ArrayList<>();
        List<String> repositories = new ArrayList<>();
        List<String> entities = new ArrayList<>();
        
        try {
            // Analyze Spring code
            Path springPath = Paths.get(CDAC_PROJECT_PATH, "Spring", "demo", "src", "main", "java");
            if (Files.exists(springPath)) {
                Files.walk(springPath)
                    .filter(path -> path.toString().endsWith(".java"))
                    .forEach(path -> {
                        try {
                            String content = Files.readString(path);
                            String fileName = path.getFileName().toString();
                            
                            // Extract controllers
                            if (content.contains("@RestController") || content.contains("@Controller")) {
                                Pattern classPattern = Pattern.compile("public\\s+class\\s+(\\w+)");
                                Matcher matcher = classPattern.matcher(content);
                                if (matcher.find()) {
                                    controllers.add(matcher.group(1));
                                }
                            }
                            
                            // Extract services
                            if (content.contains("@Service")) {
                                Pattern classPattern = Pattern.compile("public\\s+class\\s+(\\w+)");
                                Matcher matcher = classPattern.matcher(content);
                                if (matcher.find()) {
                                    services.add(matcher.group(1));
                                }
                            }
                            
                            // Extract repositories
                            if (content.contains("@Repository") || content.contains("extends JpaRepository")) {
                                Pattern classPattern = Pattern.compile("public\\s+(?:interface|class)\\s+(\\w+)");
                                Matcher matcher = classPattern.matcher(content);
                                if (matcher.find()) {
                                    repositories.add(matcher.group(1));
                                }
                            }
                            
                            // Extract entities
                            if (content.contains("@Entity")) {
                                Pattern classPattern = Pattern.compile("public\\s+class\\s+(\\w+)");
                                Matcher matcher = classPattern.matcher(content);
                                if (matcher.find()) {
                                    entities.add(matcher.group(1));
                                }
                            }
                            
                        } catch (IOException e) {
                            logger.warn("Could not read file {}: {}", path, e.getMessage());
                        }
                    });
            }
            
            result.setControllers(controllers);
            result.setServices(services);
            result.setRepositories(repositories);
            result.setEntities(entities);
            result.setCodeFiles(findCodeFiles("Spring"));
            
        } catch (Exception e) {
            logger.error("Error analyzing Spring code: {}", e.getMessage());
        }
    }
    
    private void analyzeDatabaseCode(CodeAnalysisResult result) {
        result.setLanguage("sql");
        result.setFramework("database");
        
        List<String> tables = new ArrayList<>();
        List<String> procedures = new ArrayList<>();
        
        try {
            // Analyze database schema
            Path dbPath = Paths.get(CDAC_PROJECT_PATH, "DB", "DB_Script.sql");
            if (Files.exists(dbPath)) {
                String content = Files.readString(dbPath);
                
                // Extract table names
                Pattern tablePattern = Pattern.compile("CREATE\\s+TABLE\\s+(?:IF\\s+NOT\\s+EXISTS\\s+)?(\\w+)", Pattern.CASE_INSENSITIVE);
                Matcher tableMatcher = tablePattern.matcher(content);
                while (tableMatcher.find()) {
                    tables.add(tableMatcher.group(1));
                }
                
                // Extract stored procedures
                Pattern procedurePattern = Pattern.compile("CREATE\\s+(?:OR\\s+REPLACE\\s+)?PROCEDURE\\s+(\\w+)", Pattern.CASE_INSENSITIVE);
                Matcher procedureMatcher = procedurePattern.matcher(content);
                while (procedureMatcher.find()) {
                    procedures.add(procedureMatcher.group(1));
                }
            }
            
            result.setTables(tables);
            result.setProcedures(procedures);
            result.setCodeFiles(List.of("DB_Script.sql"));
            
        } catch (Exception e) {
            logger.error("Error analyzing database code: {}", e.getMessage());
        }
    }
    
    private void analyzeGenericCode(CodeAnalysisResult result, String serviceName) {
        result.setLanguage("unknown");
        result.setFramework("unknown");
        result.setError("Service type not recognized: " + serviceName);
    }
    
    private List<String> findCodeFiles(String serviceType) {
        List<String> files = new ArrayList<>();
        try {
            Path servicePath = Paths.get(CDAC_PROJECT_PATH, serviceType);
            if (Files.exists(servicePath)) {
                Files.walk(servicePath)
                    .filter(path -> {
                        String fileName = path.toString().toLowerCase();
                        return fileName.endsWith(".java") || fileName.endsWith(".js") || 
                               fileName.endsWith(".jsx") || fileName.endsWith(".ts") || 
                               fileName.endsWith(".tsx") || fileName.endsWith(".sql");
                    })
                    .forEach(path -> files.add(path.toString()));
            }
        } catch (Exception e) {
            logger.error("Error finding code files for {}: {}", serviceType, e.getMessage());
        }
        return files;
    }
    
    // Inner class for result
    public static class CodeAnalysisResult {
        private String serviceName;
        private String language;
        private String framework;
        private List<String> components = new ArrayList<>();
        private List<String> functions = new ArrayList<>();
        private List<String> dependencies = new ArrayList<>();
        private List<String> controllers = new ArrayList<>();
        private List<String> services = new ArrayList<>();
        private List<String> repositories = new ArrayList<>();
        private List<String> entities = new ArrayList<>();
        private List<String> tables = new ArrayList<>();
        private List<String> procedures = new ArrayList<>();
        private List<String> codeFiles = new ArrayList<>();
        private String error;
        
        // Getters and setters
        public String getServiceName() { return serviceName; }
        public void setServiceName(String serviceName) { this.serviceName = serviceName; }
        
        public String getLanguage() { return language; }
        public void setLanguage(String language) { this.language = language; }
        
        public String getFramework() { return framework; }
        public void setFramework(String framework) { this.framework = framework; }
        
        public List<String> getComponents() { return components; }
        public void setComponents(List<String> components) { this.components = components; }
        
        public List<String> getFunctions() { return functions; }
        public void setFunctions(List<String> functions) { this.functions = functions; }
        
        public List<String> getDependencies() { return dependencies; }
        public void setDependencies(List<String> dependencies) { this.dependencies = dependencies; }
        
        public List<String> getControllers() { return controllers; }
        public void setControllers(List<String> controllers) { this.controllers = controllers; }
        
        public List<String> getServices() { return services; }
        public void setServices(List<String> services) { this.services = services; }
        
        public List<String> getRepositories() { return repositories; }
        public void setRepositories(List<String> repositories) { this.repositories = repositories; }
        
        public List<String> getEntities() { return entities; }
        public void setEntities(List<String> entities) { this.entities = entities; }
        
        public List<String> getTables() { return tables; }
        public void setTables(List<String> tables) { this.tables = tables; }
        
        public List<String> getProcedures() { return procedures; }
        public void setProcedures(List<String> procedures) { this.procedures = procedures; }
        
        public List<String> getCodeFiles() { return codeFiles; }
        public void setCodeFiles(List<String> codeFiles) { this.codeFiles = codeFiles; }
        
        public String getError() { return error; }
        public void setError(String error) { this.error = error; }
    }
}
