package com.testplatform.backend.service;

import com.testplatform.backend.config.MultiRepositoryConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MultiRepositoryService {
    
    private static final Logger logger = LoggerFactory.getLogger(MultiRepositoryService.class);
    
    @Autowired
    private MultiRepositoryConfig multiRepositoryConfig;
    
    @Autowired
    private GitService gitService;
    
    @Autowired
    private LanguageDetectionService languageDetectionService;
    
    /**
     * Initialize repositories on application startup
     * Automatically clones remote repositories if configured
     */
    @PostConstruct
    public void initializeRepositories() {
        if (!multiRepositoryConfig.isEnabled()) {
            logger.info("Multi-repository support is disabled");
            return;
        }
        
        logger.info("🚀 Initializing repositories...");
        
        List<MultiRepositoryConfig.RepositoryConfig> repositories = multiRepositoryConfig.getRepositories();
        if (repositories == null || repositories.isEmpty()) {
            logger.warn("⚠️ No repositories configured");
            return;
        }
        
        int clonedCount = 0;
        int existingCount = 0;
        int failedCount = 0;
        
        for (MultiRepositoryConfig.RepositoryConfig repo : repositories) {
            if (!repo.isEnabled()) {
                logger.info("⏭️ Skipping disabled repository: {}", repo.getId());
                continue;
            }
            
            // Check if this is a remote repository
            if (repo.getRemoteUrl() != null && !repo.getRemoteUrl().isEmpty()) {
                logger.info("📥 Processing remote repository: {} ({})", repo.getName(), repo.getRemoteUrl());
                
                try {
                    // Extract credentials if available
                    String username = null;
                    String token = null;
                    if (repo.getCredentials() != null) {
                        username = repo.getCredentials().getUsername();
                        token = repo.getCredentials().getToken();
                    }
                    
                    // Check if already cloned
                    if (gitService.isRepositoryCloned(repo.getPath())) {
                        logger.info("✅ Repository already exists at: {}", repo.getPath());
                        
                        // Pull latest changes
                        if (gitService.pullLatestChanges(repo.getPath(), username, token)) {
                            logger.info("🔄 Updated repository with latest changes");
                            existingCount++;
                        } else {
                            logger.warn("⚠️ Failed to pull latest changes, using existing clone");
                            existingCount++;
                        }
                    } else {
                        // Clone the repository
                        if (gitService.cloneRepository(repo.getRemoteUrl(), repo.getPath(), username, token)) {
                            logger.info("✅ Successfully cloned repository to: {}", repo.getPath());
                            clonedCount++;
                        } else {
                            logger.error("❌ Failed to clone repository: {}", repo.getId());
                            failedCount++;
                        }
                    }
                    
                } catch (Exception e) {
                    logger.error("❌ Error initializing repository {}: {}", repo.getId(), e.getMessage());
                    failedCount++;
                }
            } else {
                // Local repository - just verify it exists
                if (gitService.isRepositoryCloned(repo.getPath())) {
                    logger.info("✅ Local repository ready: {} at {}", repo.getName(), repo.getPath());
                    existingCount++;
                } else {
                    logger.warn("⚠️ Local repository not found: {} at {}", repo.getName(), repo.getPath());
                    logger.warn("   Make sure the repository exists at the specified path");
                }
            }
        }
        
        logger.info("📊 Repository initialization complete:");
        logger.info("   ✅ Cloned: {}", clonedCount);
        logger.info("   📂 Existing: {}", existingCount);
        logger.info("   ❌ Failed: {}", failedCount);
    }
    
    /**
     * Get all configured repositories
     */
    public List<MultiRepositoryConfig.RepositoryConfig> getAllRepositories() {
        if (!multiRepositoryConfig.isEnabled()) {
            logger.warn("Multi-repository support is disabled");
            return Collections.emptyList();
        }
        
        return multiRepositoryConfig.getRepositories() != null 
            ? multiRepositoryConfig.getRepositories() 
            : Collections.emptyList();
    }
    
    /**
     * Get repository by ID
     */
    public MultiRepositoryConfig.RepositoryConfig getRepository(String repositoryId) {
        return getAllRepositories().stream()
            .filter(repo -> repo.getId().equals(repositoryId))
            .findFirst()
            .orElse(null);
    }
    
    /**
     * Get default repository
     */
    public MultiRepositoryConfig.RepositoryConfig getDefaultRepository() {
        String defaultRepoId = multiRepositoryConfig.getDefaultRepository();
        if (defaultRepoId != null) {
            return getRepository(defaultRepoId);
        }
        
        // Return first enabled repository
        return getAllRepositories().stream()
            .filter(MultiRepositoryConfig.RepositoryConfig::isEnabled)
            .findFirst()
            .orElse(null);
    }
    
    /**
     * Get code content from any repository
     */
    public String getCodeContent(String repositoryId, String filePath, String branch) {
        MultiRepositoryConfig.RepositoryConfig repo = getRepository(repositoryId);
        if (repo == null) {
            logger.error("Repository not found: {}", repositoryId);
            return null;
        }
        
        if (!repo.isEnabled()) {
            logger.warn("Repository is disabled: {}", repositoryId);
            return null;
        }
        
        // Set the repository path for GitService
        String originalPath = gitService.getRepositoryPath();
        try {
            // Temporarily set the repository path
            gitService.setRepositoryPath(repo.getPath());
            
            // Get code content
            String content = gitService.getCodeContentFromFile(filePath, branch != null ? branch : repo.getBranch());
            
            // Auto-detect language and framework if enabled
            if (multiRepositoryConfig.isAutoDetectLanguage() && content != null) {
                LanguageDetectionService.LanguageDetectionResult languageResult = 
                    languageDetectionService.detectLanguage(filePath, content);
                
                if (languageResult.getConfidence() > 0.7) {
                    logger.info("Auto-detected language: {} for file: {}", languageResult.getLanguage(), filePath);
                }
            }
            
            return content;
            
        } finally {
            // Restore original path
            gitService.setRepositoryPath(originalPath);
        }
    }
    
    /**
     * Get changed files from any repository
     */
    public List<String> getChangedFiles(String repositoryId, String baseBranch, String featureBranch) {
        MultiRepositoryConfig.RepositoryConfig repo = getRepository(repositoryId);
        if (repo == null) {
            logger.error("Repository not found: {}", repositoryId);
            return Collections.emptyList();
        }
        
        if (!repo.isEnabled()) {
            logger.warn("Repository is disabled: {}", repositoryId);
            return Collections.emptyList();
        }
        
        // Set the repository path for GitService
        String originalPath = gitService.getRepositoryPath();
        try {
            // Temporarily set the repository path
            gitService.setRepositoryPath(repo.getPath());
            
            // Get changed files
            return gitService.getChangedFiles(baseBranch, featureBranch);
            
        } finally {
            // Restore original path
            gitService.setRepositoryPath(originalPath);
        }
    }
    
    /**
     * Get repository statistics
     */
    public Map<String, Object> getRepositoryStats(String repositoryId) {
        MultiRepositoryConfig.RepositoryConfig repo = getRepository(repositoryId);
        if (repo == null) {
            return Collections.emptyMap();
        }
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("id", repo.getId());
        stats.put("name", repo.getName());
        stats.put("path", repo.getPath());
        stats.put("branch", repo.getBranch());
        stats.put("language", repo.getLanguage());
        stats.put("framework", repo.getFramework());
        stats.put("enabled", repo.isEnabled());
        
        // Get additional stats from GitService
        try {
            String originalPath = gitService.getRepositoryPath();
            gitService.setRepositoryPath(repo.getPath());
            
            // Add file count, last commit info, etc.
            stats.put("status", "active");
            
            gitService.setRepositoryPath(originalPath);
        } catch (Exception e) {
            logger.warn("Failed to get additional stats for repository {}: {}", repositoryId, e.getMessage());
            stats.put("status", "error");
        }
        
        return stats;
    }
    
    /**
     * Get all repository statistics
     */
    public List<Map<String, Object>> getAllRepositoryStats() {
        return getAllRepositories().stream()
            .map(repo -> getRepositoryStats(repo.getId()))
            .collect(Collectors.toList());
    }
    
    /**
     * Add new repository configuration
     */
    public boolean addRepository(MultiRepositoryConfig.RepositoryConfig newRepo) {
        if (!multiRepositoryConfig.isEnabled()) {
            logger.warn("Multi-repository support is disabled");
            return false;
        }
        
        List<MultiRepositoryConfig.RepositoryConfig> repos = multiRepositoryConfig.getRepositories();
        if (repos == null) {
            repos = new ArrayList<>();
            multiRepositoryConfig.setRepositories(repos);
        }
        
        // Check if repository with same ID already exists
        boolean exists = repos.stream()
            .anyMatch(repo -> repo.getId().equals(newRepo.getId()));
        
        if (exists) {
            logger.warn("Repository with ID {} already exists", newRepo.getId());
            return false;
        }
        
        repos.add(newRepo);
        logger.info("Added new repository: {}", newRepo.getId());
        return true;
    }
    
    /**
     * Update repository configuration
     */
    public boolean updateRepository(String repositoryId, MultiRepositoryConfig.RepositoryConfig updatedRepo) {
        List<MultiRepositoryConfig.RepositoryConfig> repos = multiRepositoryConfig.getRepositories();
        if (repos == null) {
            return false;
        }
        
        for (int i = 0; i < repos.size(); i++) {
            if (repos.get(i).getId().equals(repositoryId)) {
                repos.set(i, updatedRepo);
                logger.info("Updated repository: {}", repositoryId);
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Remove repository configuration
     */
    public boolean removeRepository(String repositoryId) {
        List<MultiRepositoryConfig.RepositoryConfig> repos = multiRepositoryConfig.getRepositories();
        if (repos == null) {
            return false;
        }
        
        boolean removed = repos.removeIf(repo -> repo.getId().equals(repositoryId));
        if (removed) {
            logger.info("Removed repository: {}", repositoryId);
        }
        
        return removed;
    }
    
    /**
     * Enable/disable repository
     */
    public boolean toggleRepository(String repositoryId, boolean enabled) {
        MultiRepositoryConfig.RepositoryConfig repo = getRepository(repositoryId);
        if (repo == null) {
            return false;
        }
        
        repo.setEnabled(enabled);
        logger.info("Repository {} {}", repositoryId, enabled ? "enabled" : "disabled");
        return true;
    }
    
    /**
     * Get repositories by language
     */
    public List<MultiRepositoryConfig.RepositoryConfig> getRepositoriesByLanguage(String language) {
        return getAllRepositories().stream()
            .filter(repo -> language.equals(repo.getLanguage()))
            .collect(Collectors.toList());
    }
    
    /**
     * Get repositories by framework
     */
    public List<MultiRepositoryConfig.RepositoryConfig> getRepositoriesByFramework(String framework) {
        return getAllRepositories().stream()
            .filter(repo -> framework.equals(repo.getFramework()))
            .collect(Collectors.toList());
    }
    
    /**
     * Get enabled repositories
     */
    public List<MultiRepositoryConfig.RepositoryConfig> getEnabledRepositories() {
        return getAllRepositories().stream()
            .filter(MultiRepositoryConfig.RepositoryConfig::isEnabled)
            .collect(Collectors.toList());
    }
}
