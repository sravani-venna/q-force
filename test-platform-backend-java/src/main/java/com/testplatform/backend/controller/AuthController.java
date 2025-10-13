package com.testplatform.backend.controller;

import com.testplatform.backend.dto.ApiResponse;
import com.testplatform.backend.enums.UserRole;
import com.testplatform.backend.exception.UnauthorizedException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    // Mock users database
    private final List<User> users = Arrays.asList(
        new User(1L, "admin@testplatform.com", "admin123", "Admin User", UserRole.ADMIN),
        new User(2L, "developer@testplatform.com", "dev123", "Developer User", UserRole.DEVELOPER),
        new User(3L, "tester@testplatform.com", "test123", "Test User", UserRole.TESTER)
    );
    
    /**
     * POST /api/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@RequestBody LoginRequest request) {
        try {
            Optional<User> userOpt = users.stream()
                    .filter(u -> u.getEmail().equals(request.getEmail()) && 
                               u.getPassword().equals(request.getPassword()))
                    .findFirst();
            
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                // Simple JWT-like token (not secure, just for demo)
                String token = Base64.getEncoder().encodeToString(
                    String.format("{\"userId\":%d,\"email\":\"%s\"}", user.getId(), user.getEmail()).getBytes()
                );
                
                UserInfo userInfo = new UserInfo(user.getId(), user.getName(), user.getEmail(), user.getRole());
                LoginResponse response = new LoginResponse(token, userInfo);
                
                return ResponseEntity.ok(ApiResponse.success(response));
            } else {
                throw new UnauthorizedException("Invalid credentials");
            }
        } catch (UnauthorizedException e) {
            throw e;
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Login failed"));
        }
    }
    
    /**
     * POST /api/auth/register
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserInfo>> register(@RequestBody RegisterRequest request) {
        try {
            boolean userExists = users.stream()
                    .anyMatch(u -> u.getEmail().equals(request.getEmail()));
            
            if (userExists) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("User already exists"));
            }
            
            UserInfo newUser = new UserInfo(
                (long) (users.size() + 1),
                request.getName(),
                request.getEmail(),
                UserRole.DEVELOPER
            );
            
            return ResponseEntity.status(201)
                    .body(ApiResponse.success(newUser, "User registered successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Registration failed"));
        }
    }
    
    // Inner classes for request/response
    public static class LoginRequest {
        private String email;
        private String password;
        
        // Getters and Setters
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }
    
    public static class RegisterRequest {
        private String email;
        private String password;
        private String name;
        
        // Getters and Setters
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }
    
    public static class LoginResponse {
        private String token;
        private UserInfo user;
        
        public LoginResponse(String token, UserInfo user) {
            this.token = token;
            this.user = user;
        }
        
        // Getters and Setters
        public String getToken() { return token; }
        public void setToken(String token) { this.token = token; }
        
        public UserInfo getUser() { return user; }
        public void setUser(UserInfo user) { this.user = user; }
    }
    
    public static class UserInfo {
        private Long id;
        private String name;
        private String email;
        private UserRole role;
        
        public UserInfo(Long id, String name, String email, UserRole role) {
            this.id = id;
            this.name = name;
            this.email = email;
            this.role = role;
        }
        
        // Getters and Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        
        public UserRole getRole() { return role; }
        public void setRole(UserRole role) { this.role = role; }
    }
    
    // Mock User class for internal use
    private static class User {
        private Long id;
        private String email;
        private String password;
        private String name;
        private UserRole role;
        
        public User(Long id, String email, String password, String name, UserRole role) {
            this.id = id;
            this.email = email;
            this.password = password;
            this.name = name;
            this.role = role;
        }
        
        // Getters
        public Long getId() { return id; }
        public String getEmail() { return email; }
        public String getPassword() { return password; }
        public String getName() { return name; }
        public UserRole getRole() { return role; }
    }
}
