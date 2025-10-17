package com.testplatform.backend.controller;

import com.testplatform.backend.dto.ApiResponse;
import com.testplatform.backend.service.EmailMismatchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller for Email Mismatch Service - PR #5
 * Demonstrates function that does the opposite of its name
 */
@RestController
@RequestMapping("/api/email-mismatch")
public class EmailMismatchController {

    @Autowired
    private EmailMismatchService emailMismatchService;

    /**
     * Endpoint: /api/email-mismatch/validate-email
     * Function: validateEmail() but actually returns random validation
     */
    @GetMapping("/validate-email")
    public ResponseEntity<ApiResponse<Map<String, Object>>> validateEmail(
            @RequestParam String email) {
        try {
            boolean result = emailMismatchService.validateEmail(email);
            Map<String, Object> response = new HashMap<>();
            response.put("email", email);
            response.put("operation", "validateEmail");
            response.put("result", result);
            response.put("note", "Function named 'validateEmail' but actually returns random validation based on email length!");
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Error in validateEmail operation: " + e.getMessage()));
        }
    }
}
