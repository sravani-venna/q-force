package com.testplatform.backend.controller;

import com.testplatform.backend.config.MultiRepositoryConfig;
import com.testplatform.backend.dto.ApiResponse;
import com.testplatform.backend.service.MultiRepositoryService;
import com.testplatform.backend.service.LanguageDetectionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/repositories")
public class MultiRepositoryController {
    
    private static final Logger logger = LoggerFactory.getLogger(MultiRepositoryController.class);
    
    @Autowired
    private MultiRepositoryService multiRepositoryService;
    
    @Autowired
    private LanguageDetectionService languageDetectionService;
    
    /**
     * GET /api/repositories - Get all repositories
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<MultiRepositoryConfig.RepositoryConfig>>> getAllRepositories() {
        try {
            List<MultiRepositoryConfig.RepositoryConfig> repositories = multiRepositoryService.getAllRepositories();
            return ResponseEntity.ok(ApiResponse.success(repositories, 
                String.format("Found %d repositories", repositories.size())));
        } catch (Exception e) {
            logger.error("Error getting repositories: {}", e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Failed to get repositories"));
        }
    }
    
    /**
     * GET /api/repositories/:id - Get repository by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MultiRepositoryConfig.RepositoryConfig>> getRepository(@PathVariable String id) {
        try {
            MultiRepositoryConfig.RepositoryConfig repository = multiRepositoryService.getRepository(id);
            if (repository == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(ApiResponse.success(repository));
        } catch (Exception e) {
            logger.error("Error getting repository {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Failed to get repository"));
        }
    }
    
    /**
     * GET /api/repositories/:id/stats - Get repository statistics
     */
    @GetMapping("/{id}/stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getRepositoryStats(@PathVariable String id) {
        try {
            Map<String, Object> stats = multiRepositoryService.getRepositoryStats(id);
            if (stats.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(ApiResponse.success(stats));
        } catch (Exception e) {
            logger.error("Error getting repository stats for {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Failed to get repository stats"));
        }
    }
    
    /**
     * GET /api/repositories/stats - Get all repository statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getAllRepositoryStats() {
        try {
            List<Map<String, Object>> stats = multiRepositoryService.getAllRepositoryStats();
            return ResponseEntity.ok(ApiResponse.success(stats, 
                String.format("Found %d repository statistics", stats.size())));
        } catch (Exception e) {
            logger.error("Error getting all repository stats: {}", e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Failed to get repository statistics"));
        }
    }
    
    /**
     * POST /api/repositories - Add new repository
     */
    @PostMapping
    public ResponseEntity<ApiResponse<MultiRepositoryConfig.RepositoryConfig>> addRepository(
            @RequestBody MultiRepositoryConfig.RepositoryConfig repository) {
        try {
            boolean added = multiRepositoryService.addRepository(repository);
            if (added) {
                return ResponseEntity.ok(ApiResponse.success(repository, "Repository added successfully"));
            } else {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Repository with this ID already exists"));
            }
        } catch (Exception e) {
            logger.error("Error adding repository: {}", e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Failed to add repository"));
        }
    }
    
    /**
     * PUT /api/repositories/:id - Update repository
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<MultiRepositoryConfig.RepositoryConfig>> updateRepository(
            @PathVariable String id, 
            @RequestBody MultiRepositoryConfig.RepositoryConfig repository) {
        try {
            boolean updated = multiRepositoryService.updateRepository(id, repository);
            if (updated) {
                return ResponseEntity.ok(ApiResponse.success(repository, "Repository updated successfully"));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("Error updating repository {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Failed to update repository"));
        }
    }
    
    /**
     * DELETE /api/repositories/:id - Remove repository
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> removeRepository(@PathVariable String id) {
        try {
            boolean removed = multiRepositoryService.removeRepository(id);
            if (removed) {
                return ResponseEntity.ok(ApiResponse.success("Repository removed successfully"));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("Error removing repository {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Failed to remove repository"));
        }
    }
    
    /**
     * PATCH /api/repositories/:id/toggle - Enable/disable repository
     */
    @PatchMapping("/{id}/toggle")
    public ResponseEntity<ApiResponse<String>> toggleRepository(@PathVariable String id, 
                                                               @RequestParam boolean enabled) {
        try {
            boolean toggled = multiRepositoryService.toggleRepository(id, enabled);
            if (toggled) {
                return ResponseEntity.ok(ApiResponse.success(
                    String.format("Repository %s %s", id, enabled ? "enabled" : "disabled")));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("Error toggling repository {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Failed to toggle repository"));
        }
    }
    
    /**
     * GET /api/repositories/language/:language - Get repositories by language
     */
    @GetMapping("/language/{language}")
    public ResponseEntity<ApiResponse<List<MultiRepositoryConfig.RepositoryConfig>>> getRepositoriesByLanguage(
            @PathVariable String language) {
        try {
            List<MultiRepositoryConfig.RepositoryConfig> repositories = 
                multiRepositoryService.getRepositoriesByLanguage(language);
            return ResponseEntity.ok(ApiResponse.success(repositories, 
                String.format("Found %d repositories for language: %s", repositories.size(), language)));
        } catch (Exception e) {
            logger.error("Error getting repositories by language {}: {}", language, e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Failed to get repositories by language"));
        }
    }
    
    /**
     * GET /api/repositories/framework/:framework - Get repositories by framework
     */
    @GetMapping("/framework/{framework}")
    public ResponseEntity<ApiResponse<List<MultiRepositoryConfig.RepositoryConfig>>> getRepositoriesByFramework(
            @PathVariable String framework) {
        try {
            List<MultiRepositoryConfig.RepositoryConfig> repositories = 
                multiRepositoryService.getRepositoriesByFramework(framework);
            return ResponseEntity.ok(ApiResponse.success(repositories, 
                String.format("Found %d repositories for framework: %s", repositories.size(), framework)));
        } catch (Exception e) {
            logger.error("Error getting repositories by framework {}: {}", framework, e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Failed to get repositories by framework"));
        }
    }
    
    /**
     * GET /api/repositories/enabled - Get enabled repositories
     */
    @GetMapping("/enabled")
    public ResponseEntity<ApiResponse<List<MultiRepositoryConfig.RepositoryConfig>>> getEnabledRepositories() {
        try {
            List<MultiRepositoryConfig.RepositoryConfig> repositories = 
                multiRepositoryService.getEnabledRepositories();
            return ResponseEntity.ok(ApiResponse.success(repositories, 
                String.format("Found %d enabled repositories", repositories.size())));
        } catch (Exception e) {
            logger.error("Error getting enabled repositories: {}", e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Failed to get enabled repositories"));
        }
    }
    
    /**
     * POST /api/repositories/:id/detect - Detect language and framework for repository
     */
    @PostMapping("/{id}/detect")
    public ResponseEntity<ApiResponse<Map<String, Object>>> detectLanguageAndFramework(
            @PathVariable String id, 
            @RequestParam(required = false) String filePath) {
        try {
            MultiRepositoryConfig.RepositoryConfig repository = multiRepositoryService.getRepository(id);
            if (repository == null) {
                return ResponseEntity.notFound().build();
            }
            
            // Get code content for analysis
            String content = multiRepositoryService.getCodeContent(id, filePath, repository.getBranch());
            if (content == null) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Could not read file content"));
            }
            
            // Detect language and framework
            LanguageDetectionService.LanguageDetectionResult languageResult = 
                languageDetectionService.detectLanguage(filePath, content);
            LanguageDetectionService.FrameworkDetectionResult frameworkResult = 
                languageDetectionService.detectFramework(languageResult.getLanguage(), content);
            
            Map<String, Object> result = Map.of(
                "language", Map.of(
                    "detected", languageResult.getLanguage(),
                    "confidence", languageResult.getConfidence(),
                    "extension", languageResult.getExtension()
                ),
                "framework", Map.of(
                    "detected", frameworkResult.getFramework(),
                    "confidence", frameworkResult.getConfidence()
                ),
                "testFramework", languageDetectionService.getTestFramework(
                    languageResult.getLanguage(), frameworkResult.getFramework())
            );
            
            return ResponseEntity.ok(ApiResponse.success(result, "Language and framework detected"));
        } catch (Exception e) {
            logger.error("Error detecting language and framework for repository {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Failed to detect language and framework"));
        }
    }
}
