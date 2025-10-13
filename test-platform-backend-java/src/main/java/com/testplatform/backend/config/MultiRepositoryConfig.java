package com.testplatform.backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "app.multi-repo")
public class MultiRepositoryConfig {
    
    private boolean enabled = true;
    private List<RepositoryConfig> repositories;
    private String defaultRepository;
    private Map<String, String> languageMappings;
    private Map<String, String> frameworkMappings;
    private boolean autoDetectLanguage = true;
    private boolean autoDetectFramework = true;
    
    public static class RepositoryConfig {
        private String id;
        private String name;
        private String path;
        private String remoteUrl;  // NEW: URL for remote Git repository
        private String branch;
        private String language;
        private String framework;
        private boolean enabled = true;
        private RepositoryCredentials credentials;  // NEW: Credentials for private repos
        private Map<String, String> customSettings;
        
        // Getters and Setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getPath() { return path; }
        public void setPath(String path) { this.path = path; }
        
        public String getRemoteUrl() { return remoteUrl; }
        public void setRemoteUrl(String remoteUrl) { this.remoteUrl = remoteUrl; }
        
        public String getBranch() { return branch; }
        public void setBranch(String branch) { this.branch = branch; }
        
        public String getLanguage() { return language; }
        public void setLanguage(String language) { this.language = language; }
        
        public String getFramework() { return framework; }
        public void setFramework(String framework) { this.framework = framework; }
        
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        
        public RepositoryCredentials getCredentials() { return credentials; }
        public void setCredentials(RepositoryCredentials credentials) { this.credentials = credentials; }
        
        public Map<String, String> getCustomSettings() { return customSettings; }
        public void setCustomSettings(Map<String, String> customSettings) { this.customSettings = customSettings; }
    }
    
    public static class RepositoryCredentials {
        private String username;
        private String token;
        
        // Getters and Setters
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        
        public String getToken() { return token; }
        public void setToken(String token) { this.token = token; }
    }
    
    // Getters and Setters
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    
    public List<RepositoryConfig> getRepositories() { return repositories; }
    public void setRepositories(List<RepositoryConfig> repositories) { this.repositories = repositories; }
    
    public String getDefaultRepository() { return defaultRepository; }
    public void setDefaultRepository(String defaultRepository) { this.defaultRepository = defaultRepository; }
    
    public Map<String, String> getLanguageMappings() { return languageMappings; }
    public void setLanguageMappings(Map<String, String> languageMappings) { this.languageMappings = languageMappings; }
    
    public Map<String, String> getFrameworkMappings() { return frameworkMappings; }
    public void setFrameworkMappings(Map<String, String> frameworkMappings) { this.frameworkMappings = frameworkMappings; }
    
    public boolean isAutoDetectLanguage() { return autoDetectLanguage; }
    public void setAutoDetectLanguage(boolean autoDetectLanguage) { this.autoDetectLanguage = autoDetectLanguage; }
    
    public boolean isAutoDetectFramework() { return autoDetectFramework; }
    public void setAutoDetectFramework(boolean autoDetectFramework) { this.autoDetectFramework = autoDetectFramework; }
}
