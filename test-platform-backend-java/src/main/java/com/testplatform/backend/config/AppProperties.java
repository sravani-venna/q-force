package com.testplatform.backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "app")
public class AppProperties {
    private String apiPrefix = "/api";
    private List<String> allowedOrigins;
    private Jwt jwt = new Jwt();
    private RateLimit rateLimit = new RateLimit();
    private TestGeneration testGeneration = new TestGeneration();
    private Llm llm = new Llm();
    private MockData mockData = new MockData();
    
    // Inner classes for nested properties
    public static class Jwt {
        private String secret = "test-platform-default-secret-change-in-production";
        private String expiresIn = "24h";
        
        // Getters and Setters
        public String getSecret() { return secret; }
        public void setSecret(String secret) { this.secret = secret; }
        
        public String getExpiresIn() { return expiresIn; }
        public void setExpiresIn(String expiresIn) { this.expiresIn = expiresIn; }
    }
    
    public static class RateLimit {
        private Long windowMs = 900000L; // 15 minutes
        private Integer maxRequests = 100;
        
        // Getters and Setters
        public Long getWindowMs() { return windowMs; }
        public void setWindowMs(Long windowMs) { this.windowMs = windowMs; }
        
        public Integer getMaxRequests() { return maxRequests; }
        public void setMaxRequests(Integer maxRequests) { this.maxRequests = maxRequests; }
    }
    
    public static class TestGeneration {
        private Integer maxTestsPerFile = 10;
        private Long defaultTimeout = 30000L;
        private List<String> supportedLanguages = List.of("java", "javascript", "typescript", "python", "csharp");
        private List<String> testTypes = List.of("UNIT", "INTEGRATION", "E2E");
        
        // Getters and Setters
        public Integer getMaxTestsPerFile() { return maxTestsPerFile; }
        public void setMaxTestsPerFile(Integer maxTestsPerFile) { this.maxTestsPerFile = maxTestsPerFile; }
        
        public Long getDefaultTimeout() { return defaultTimeout; }
        public void setDefaultTimeout(Long defaultTimeout) { this.defaultTimeout = defaultTimeout; }
        
        public List<String> getSupportedLanguages() { return supportedLanguages; }
        public void setSupportedLanguages(List<String> supportedLanguages) { this.supportedLanguages = supportedLanguages; }
        
        public List<String> getTestTypes() { return testTypes; }
        public void setTestTypes(List<String> testTypes) { this.testTypes = testTypes; }
    }
    
    public static class Llm {
        private String provider = "openai";
        private String apiKey = "your-api-key-here";
        private String model = "gpt-4o-mini";
        private String baseUrl = "https://api.openai.com/v1";
        private Integer maxTokens = 4000;
        private Double temperature = 0.3;
        private Long timeout = 30000L;
        
        // Getters and Setters
        public String getProvider() { return provider; }
        public void setProvider(String provider) { this.provider = provider; }
        
        public String getApiKey() { return apiKey; }
        public void setApiKey(String apiKey) { this.apiKey = apiKey; }
        
        public String getModel() { return model; }
        public void setModel(String model) { this.model = model; }
        
        public String getBaseUrl() { return baseUrl; }
        public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
        
        public Integer getMaxTokens() { return maxTokens; }
        public void setMaxTokens(Integer maxTokens) { this.maxTokens = maxTokens; }
        
        public Double getTemperature() { return temperature; }
        public void setTemperature(Double temperature) { this.temperature = temperature; }
        
        public Long getTimeout() { return timeout; }
        public void setTimeout(Long timeout) { this.timeout = timeout; }
    }
    
    public static class MockData {
        private Boolean enabled = true;
        private Integer initialPrs = 3;
        private Integer initialTests = 156;
        
        // Getters and Setters
        public Boolean getEnabled() { return enabled; }
        public void setEnabled(Boolean enabled) { this.enabled = enabled; }
        
        public Integer getInitialPrs() { return initialPrs; }
        public void setInitialPrs(Integer initialPrs) { this.initialPrs = initialPrs; }
        
        public Integer getInitialTests() { return initialTests; }
        public void setInitialTests(Integer initialTests) { this.initialTests = initialTests; }
    }
    
    // Main getters and setters
    public String getApiPrefix() { return apiPrefix; }
    public void setApiPrefix(String apiPrefix) { this.apiPrefix = apiPrefix; }
    
    public List<String> getAllowedOrigins() { return allowedOrigins; }
    public void setAllowedOrigins(List<String> allowedOrigins) { this.allowedOrigins = allowedOrigins; }
    
    public Jwt getJwt() { return jwt; }
    public void setJwt(Jwt jwt) { this.jwt = jwt; }
    
    public RateLimit getRateLimit() { return rateLimit; }
    public void setRateLimit(RateLimit rateLimit) { this.rateLimit = rateLimit; }
    
    public TestGeneration getTestGeneration() { return testGeneration; }
    public void setTestGeneration(TestGeneration testGeneration) { this.testGeneration = testGeneration; }
    
    public Llm getLlm() { return llm; }
    public void setLlm(Llm llm) { this.llm = llm; }
    
    public MockData getMockData() { return mockData; }
    public void setMockData(MockData mockData) { this.mockData = mockData; }
}
