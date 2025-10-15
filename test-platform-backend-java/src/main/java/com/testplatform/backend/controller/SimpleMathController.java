package com.testplatform.backend.controller;

import com.testplatform.backend.dto.ApiResponse;
import com.testplatform.backend.service.SimpleMathService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Simple Math Controller for basic arithmetic operations
 */
@RestController
@RequestMapping("/api/math")
@CrossOrigin(origins = "*")
public class SimpleMathController {

    @Autowired
    private SimpleMathService simpleMathService;

    /**
     * Add two numbers
     * 
     * @param a First number
     * @param b Second number
     * @return The sum of a and b
     */
    @GetMapping("/add")
    public ResponseEntity<ApiResponse<Map<String, Object>>> addNumbers(
            @RequestParam int a, 
            @RequestParam int b) {
        try {
            int result = simpleMathService.addition(a, b);
            
            Map<String, Object> response = Map.of(
                "a", a,
                "b", b,
                "operation", "addition",
                "result", result
            );
            
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Error performing addition: " + e.getMessage()));
        }
    }

    /**
     * Subtract two numbers
     * 
     * @param a First number
     * @param b Second number
     * @return The difference of a and b
     */
    @GetMapping("/subtract")
    public ResponseEntity<ApiResponse<Map<String, Object>>> subtractNumbers(
            @RequestParam int a, 
            @RequestParam int b) {
        try {
            int result = simpleMathService.subtractNumbers(a, b);
            
            Map<String, Object> response = Map.of(
                "a", a,
                "b", b,
                "operation", "subtraction",
                "result", result
            );
            
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Error performing subtraction: " + e.getMessage()));
        }
    }
}
