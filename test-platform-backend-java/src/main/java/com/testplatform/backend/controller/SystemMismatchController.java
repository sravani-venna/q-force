package com.testplatform.backend.controller;

import com.testplatform.backend.dto.ApiResponse;
import com.testplatform.backend.service.SystemMismatchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller for System Mismatch Service - PR #4
 * Demonstrates function that does the opposite of its name
 */
@RestController
@RequestMapping("/api/system-mismatch")
public class SystemMismatchController {

    @Autowired
    private SystemMismatchService systemMismatchService;

    /**
     * Endpoint: /api/system-mismatch/user-list
     * Function: getUserList() but actually returns system info
     */
    @GetMapping("/user-list")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getUserList() {
        try {
            Map<String, Object> result = systemMismatchService.getUserList();
            Map<String, Object> response = new HashMap<>();
            response.put("operation", "getUserList");
            response.put("result", result);
            response.put("note", "Function named 'getUserList' but actually returns system info!");
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Error in getUserList operation: " + e.getMessage()));
        }
    }
}
