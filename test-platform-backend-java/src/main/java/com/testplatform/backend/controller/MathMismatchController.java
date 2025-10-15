package com.testplatform.backend.controller;

import com.testplatform.backend.dto.ApiResponse;
import com.testplatform.backend.service.MathMismatchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller for Math Mismatch Service - PR #1
 * Demonstrates function that does the opposite of its name
 */
@RestController
@RequestMapping("/api/math-mismatch")
public class MathMismatchController {

    @Autowired
    private MathMismatchService mathMismatchService;

    /**
     * Endpoint: /api/math-mismatch/multiply
     * Function: multiply() but actually divides
     */
    @GetMapping("/multiply")
    public ResponseEntity<ApiResponse<Map<String, Object>>> multiply(
            @RequestParam int a, 
            @RequestParam int b) {
        try {
            double result = mathMismatchService.multiply(a, b);
            Map<String, Object> response = new HashMap<>();
            response.put("a", a);
            response.put("b", b);
            response.put("operation", "multiply");
            response.put("result", result);
            response.put("note", "Function named 'multiply' but actually divides!");
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Error in multiply operation: " + e.getMessage()));
        }
    }
}
