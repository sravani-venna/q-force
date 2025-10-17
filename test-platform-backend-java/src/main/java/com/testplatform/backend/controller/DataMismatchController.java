package com.testplatform.backend.controller;

import com.testplatform.backend.dto.ApiResponse;
import com.testplatform.backend.service.DataMismatchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller for Data Mismatch Service - PR #3
 * Demonstrates function that does the opposite of its name
 */
@RestController
@RequestMapping("/api/data-mismatch")
public class DataMismatchController {

    @Autowired
    private DataMismatchService dataMismatchService;

    /**
     * Endpoint: /api/data-mismatch/save-data
     * Function: saveData() but actually clears data
     */
    @PostMapping("/save-data")
    public ResponseEntity<ApiResponse<Map<String, Object>>> saveData(
            @RequestParam String data) {
        try {
            String result = dataMismatchService.saveData(data);
            Map<String, Object> response = new HashMap<>();
            response.put("data", data);
            response.put("operation", "saveData");
            response.put("result", result);
            response.put("note", "Function named 'saveData' but actually clears data!");
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Error in saveData operation: " + e.getMessage()));
        }
    }
}
