package com.testplatform.backend.service;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.PullCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Service
public class GitService {
    
    private static final Logger logger = LoggerFactory.getLogger(GitService.class);
    
    @Value("${app.git.repository-path:./}")
    private String repositoryPath;
    
    /**
     * Set repository path dynamically
     */
    public void setRepositoryPath(String path) {
        this.repositoryPath = path;
    }
    
    /**
     * Get current repository path
     */
    public String getRepositoryPath() {
        return this.repositoryPath;
    }
    
    @Value("${app.git.enabled:true}")
    private boolean gitEnabled;
    
    /**
     * Get code content from a specific file in the repository
     */
    public String getCodeContentFromFile(String filePath, String branch) {
        if (!gitEnabled) {
            logger.warn("Git integration is disabled, using fallback code");
            return getFallbackCode(filePath);
        }
        
        try {
            // Try to get from Git repository first
            String gitContent = getCodeFromGit(filePath, branch);
            if (gitContent != null && !gitContent.trim().isEmpty()) {
                logger.info("✅ Retrieved code from Git for: {}", filePath);
                return gitContent;
            }
        } catch (Exception e) {
            logger.warn("Failed to get code from Git for {}: {}", filePath, e.getMessage());
        }
        
        // Fallback to file system
        try {
            String fsContent = getCodeFromFileSystem(filePath);
            if (fsContent != null && !fsContent.trim().isEmpty()) {
                logger.info("✅ Retrieved code from file system for: {}", filePath);
                return fsContent;
            }
        } catch (Exception e) {
            logger.warn("Failed to get code from file system for {}: {}", filePath, e.getMessage());
        }
        
        // Final fallback to mock code
        logger.warn("Using fallback mock code for: {}", filePath);
        return getFallbackCode(filePath);
    }
    
    /**
     * Get code content from Git repository
     */
    private String getCodeFromGit(String filePath, String branch) throws IOException, GitAPIException {
        try (Repository repository = Git.open(Paths.get(repositoryPath).toFile()).getRepository()) {
            try (Git git = new Git(repository)) {
                // Get the commit for the specified branch
                ObjectId branchId = repository.resolve(branch);
                if (branchId == null) {
                    logger.warn("Branch {} not found, using HEAD", branch);
                    branchId = repository.resolve("HEAD");
                }
                
                if (branchId == null) {
                    logger.warn("No HEAD commit found");
                    return null;
                }
                
                try (RevWalk revWalk = new RevWalk(repository)) {
                    RevCommit commit = revWalk.parseCommit(branchId);
                    RevTree tree = commit.getTree();
                    
                    try (TreeWalk treeWalk = TreeWalk.forPath(repository, filePath, tree)) {
                        if (treeWalk == null) {
                            logger.warn("File {} not found in branch {}", filePath, branch);
                            return null;
                        }
                        
                        byte[] content = repository.open(treeWalk.getObjectId(0)).getBytes();
                        return new String(content, StandardCharsets.UTF_8);
                    }
                }
            }
        }
    }
    
    /**
     * Get code content from file system
     */
    private String getCodeFromFileSystem(String filePath) throws IOException {
        Path fullPath = Paths.get(repositoryPath, filePath);
        
        if (!Files.exists(fullPath)) {
            logger.warn("File does not exist: {}", fullPath);
            return null;
        }
        
        if (!Files.isRegularFile(fullPath)) {
            logger.warn("Path is not a regular file: {}", fullPath);
            return null;
        }
        
        return Files.readString(fullPath, StandardCharsets.UTF_8);
    }
    
    /**
     * Get list of changed files in a pull request
     */
    public List<String> getChangedFiles(String baseBranch, String featureBranch) {
        if (!gitEnabled) {
            return getMockChangedFiles();
        }
        
        // For now, return mock changed files to avoid Git complexity
        // In production, implement proper Git diff functionality
        logger.info("Using mock changed files for branches {} and {}", baseBranch, featureBranch);
        return getMockChangedFiles();
    }
    
    /**
     * Get file content with language detection
     */
    public String getCodeContentWithLanguage(String filePath, String branch) {
        String content = getCodeContentFromFile(filePath, branch);
        String language = detectLanguage(filePath);
        
        // Add language-specific context to the content
        return String.format("// Language: %s\n// File: %s\n\n%s", language, filePath) + content;
    }
    
    /**
     * Detect programming language from file extension
     */
    private String detectLanguage(String filePath) {
        if (filePath.endsWith(".java")) {
            return "java";
        } else if (filePath.endsWith(".js") || filePath.endsWith(".jsx")) {
            return "javascript";
        } else if (filePath.endsWith(".ts") || filePath.endsWith(".tsx")) {
            return "typescript";
        } else if (filePath.endsWith(".py")) {
            return "python";
        } else if (filePath.endsWith(".cs")) {
            return "csharp";
        } else if (filePath.endsWith(".go")) {
            return "go";
        } else if (filePath.endsWith(".rs")) {
            return "rust";
        } else {
            return "unknown";
        }
    }
    
    /**
     * Get mock changed files for testing - now points to actual Spring Boot files
     */
    private List<String> getMockChangedFiles() {
        return List.of(
            "services/UsersService.java",
            "services/BookingService.java", 
            "Entity/Users.java",
            "repository/UsersRepository.java",
            "repository/BookingRepository.java",
            "repository/FunctionHallRepository.java"
        );
    }
    
    /**
     * Fallback code when Git and file system access fail
     * Now tries to read actual Spring Boot files from your repository
     */
    private String getFallbackCode(String filePath) {
        // Try to read actual Spring Boot files from your repository
        try {
            String springBootPath = repositoryPath + "/Spring/demo/src/main/java/com/example/demo/";
            String actualFilePath = springBootPath + filePath;
            
            if (Files.exists(Paths.get(actualFilePath))) {
                String content = Files.readString(Paths.get(actualFilePath), StandardCharsets.UTF_8);
                logger.info("✅ Successfully read actual Spring Boot file: {}", actualFilePath);
                return content;
            }
        } catch (Exception e) {
            logger.warn("Failed to read actual Spring Boot file: {}", e.getMessage());
        }
        
        // If actual file not found, return a message indicating the issue
        return """
            // ERROR: Could not read actual repository file
            // Expected file path: Spring/demo/src/main/java/com/example/demo/""" + filePath + """
            
            // Please ensure:
            // 1. Repository path is correctly configured: """ + repositoryPath + """
            // 2. Spring Boot files exist in the expected location
            // 3. File permissions allow reading the files
            
            // This is a placeholder - the system should read your actual Spring Boot code
            """;
    }
    
    // ==================== Remote Repository Management ====================
    
    /**
     * Clone a remote repository to local directory
     * @param remoteUrl The Git repository URL (https://github.com/user/repo.git)
     * @param localPath Local directory where to clone the repository
     * @param username Username for authentication (can be null for public repos)
     * @param token Personal access token or password (can be null for public repos)
     * @return true if cloning was successful, false otherwise
     */
    public boolean cloneRepository(String remoteUrl, String localPath, String username, String token) {
        try {
            File localDir = new File(localPath);
            
            // Check if repository already exists
            if (isRepositoryCloned(localPath)) {
                logger.info("📂 Repository already exists at {}, pulling latest changes", localPath);
                return pullLatestChanges(localPath, username, token);
            }
            
            // Create parent directories if they don't exist
            if (!localDir.getParentFile().exists()) {
                localDir.getParentFile().mkdirs();
            }
            
            logger.info("📥 Cloning repository from {} to {}", remoteUrl, localPath);
            
            // Build clone command
            CloneCommand cloneCommand = Git.cloneRepository()
                .setURI(remoteUrl)
                .setDirectory(localDir)
                .setCloneAllBranches(true);
            
            // Add credentials if provided
            if (username != null && token != null && !username.isEmpty() && !token.isEmpty()) {
                cloneCommand.setCredentialsProvider(
                    new UsernamePasswordCredentialsProvider(username, token)
                );
                logger.info("🔐 Using authenticated access for cloning");
            } else {
                logger.info("🌍 Using public access for cloning");
            }
            
            // Execute clone
            Git git = cloneCommand.call();
            git.close();
            
            logger.info("✅ Successfully cloned repository to {}", localPath);
            return true;
            
        } catch (GitAPIException e) {
            logger.error("❌ Failed to clone repository: {}", e.getMessage(), e);
            return false;
        } catch (Exception e) {
            logger.error("❌ Unexpected error during clone: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Pull latest changes from remote repository
     * @param localPath Local directory of the repository
     * @param username Username for authentication (can be null)
     * @param token Personal access token or password (can be null)
     * @return true if pull was successful, false otherwise
     */
    public boolean pullLatestChanges(String localPath, String username, String token) {
        try {
            File localDir = new File(localPath);
            
            if (!isRepositoryCloned(localPath)) {
                logger.warn("⚠️ Repository not found at {}, cannot pull", localPath);
                return false;
            }
            
            logger.info("🔄 Pulling latest changes for repository at {}", localPath);
            
            try (Git git = Git.open(localDir)) {
                PullCommand pullCommand = git.pull();
                
                // Add credentials if provided
                if (username != null && token != null && !username.isEmpty() && !token.isEmpty()) {
                    pullCommand.setCredentialsProvider(
                        new UsernamePasswordCredentialsProvider(username, token)
                    );
                }
                
                pullCommand.call();
                logger.info("✅ Successfully pulled latest changes");
                return true;
            }
            
        } catch (GitAPIException e) {
            logger.error("❌ Failed to pull changes: {}", e.getMessage(), e);
            return false;
        } catch (Exception e) {
            logger.error("❌ Unexpected error during pull: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Check if a repository is already cloned at the specified path
     * @param localPath Path to check
     * @return true if a valid Git repository exists at the path
     */
    public boolean isRepositoryCloned(String localPath) {
        File localDir = new File(localPath);
        File gitDir = new File(localDir, ".git");
        return gitDir.exists() && gitDir.isDirectory();
    }
    
    /**
     * Delete a cloned repository
     * @param localPath Path to the repository to delete
     * @return true if deletion was successful
     */
    public boolean deleteRepository(String localPath) {
        try {
            File localDir = new File(localPath);
            if (!localDir.exists()) {
                logger.warn("⚠️ Repository not found at {}", localPath);
                return false;
            }
            
            logger.info("🗑️ Deleting repository at {}", localPath);
            deleteDirectory(localDir);
            logger.info("✅ Successfully deleted repository");
            return true;
            
        } catch (Exception e) {
            logger.error("❌ Failed to delete repository: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Recursively delete a directory
     */
    private void deleteDirectory(File directory) throws IOException {
        if (directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    deleteDirectory(file);
                }
            }
        }
        if (!directory.delete()) {
            throw new IOException("Failed to delete: " + directory.getAbsolutePath());
        }
    }
    
    /**
     * Get repository information
     * @param localPath Path to the repository
     * @return Information about the repository
     */
    public String getRepositoryInfo(String localPath) {
        try {
            if (!isRepositoryCloned(localPath)) {
                return "Repository not found at " + localPath;
            }
            
            try (Git git = Git.open(new File(localPath))) {
                Repository repo = git.getRepository();
                String branch = repo.getBranch();
                String remoteUrl = repo.getConfig().getString("remote", "origin", "url");
                
                return String.format("Branch: %s, Remote: %s, Path: %s", 
                    branch, remoteUrl != null ? remoteUrl : "N/A", localPath);
            }
            
        } catch (Exception e) {
            logger.error("❌ Failed to get repository info: {}", e.getMessage());
            return "Error: " + e.getMessage();
        }
    }
}
