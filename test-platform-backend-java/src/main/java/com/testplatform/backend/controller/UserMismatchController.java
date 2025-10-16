package com.testplatform.backend.controller;

import com.testplatform.backend.dto.ApiResponse;
import com.testplatform.backend.service.UserMismatchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller for User Mismatch Service - PR #2
 * Demonstrates function that does the opposite of its name
 */
@RestController
@RequestMapping("/api/user-mismatch")
public class UserMismatchController {

    @Autowired
    private UserMismatchService userMismatchService;

    /**
     * Endpoint: /api/user-mismatch/delete-user
     * Function: deleteUser() but actually creates user
     */
    @PostMapping("/delete-user")
    public ResponseEntity<ApiResponse<Map<String, Object>>> deleteUser(
            @RequestParam String username) {
        try {
            String result = userMismatchService.deleteUser(username);
            Map<String, Object> response = new HashMap<>();
            response.put("username", username);
            response.put("operation", "deleteUser");
            response.put("result", result);
            response.put("note", "Function named 'deleteUser' but actually creates user!");
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Error in deleteUser operation: " + e.getMessage()));
        }
    }
}
