package com.testplatform.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.testplatform.backend.config.AppProperties;
import com.testplatform.backend.dto.DiffComment;
import com.testplatform.backend.dto.PrReviewResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for interacting with local LLM (Ollama) for PR review
 */
@Service
public class LocalLlmClient {
    
    private static final Logger logger = LoggerFactory.getLogger(LocalLlmClient.class);
    
    @Autowired
    private RestTemplate restTemplate;
    
    @Autowired
    private AppProperties appProperties;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    /**
     * Review PR diff using local LLM
     */
    public PrReviewResponse reviewPrDiff(String fileName, String diffPatch, int prNumber) {
        try {
            String prompt = buildReviewPrompt(fileName, diffPatch, prNumber);
            String response = callLlmApi(prompt);
            return parseLlmResponse(response, prNumber);
            
        } catch (Exception e) {
            logger.error("❌ Error reviewing PR diff: {}", e.getMessage(), e);
            return new PrReviewResponse(
                "error",
                prNumber,
                null,
                new ArrayList<>(),
                "Error reviewing PR diff: " + e.getMessage()
            );
        }
    }
    
    /**
     * Build prompt for LLM review
     */
    private String buildReviewPrompt(String fileName, String diffPatch, int prNumber) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("You are a senior Java code reviewer. Review this PR diff and produce structured comments per diff line.\n\n");
        prompt.append("<DIFF>\n");
        prompt.append("File: ").append(fileName).append("\n");
        prompt.append(diffPatch);
        prompt.append("\n</DIFF>\n\n");
        
        prompt.append("Respond strictly in JSON format:\n");
        prompt.append("{\n");
        prompt.append("  \"status\": \"success\",\n");
        prompt.append("  \"prNumber\": ").append(prNumber).append(",\n");
        prompt.append("  \"reviewSummary\": \"Overall PR summary\",\n");
        prompt.append("  \"comments\": [\n");
        prompt.append("    {\n");
        prompt.append("      \"fileName\": \"").append(fileName).append("\",\n");
        prompt.append("      \"diffLine\": 4,\n");
        prompt.append("      \"comment\": \"Add null check before saving.\"\n");
        prompt.append("    }\n");
        prompt.append("  ],\n");
        prompt.append("  \"errorMessage\": null\n");
        prompt.append("}\n\n");
        
        prompt.append("Guidelines:\n");
        prompt.append("- Focus on code quality, security, performance, and best practices\n");
        prompt.append("- Provide specific, actionable feedback\n");
        prompt.append("- Map comments to specific diff lines\n");
        prompt.append("- If no issues found, return status \"no_suggestions\" with empty comments\n");
        prompt.append("- If error occurs, return status \"error\" with errorMessage\n");
        prompt.append("- CRITICAL: Return ONLY valid JSON, no markdown, no explanations\n");
        
        return prompt.toString();
    }
    
    /**
     * Call local LLM API (Ollama)
     */
    private String callLlmApi(String prompt) {
        try {
            String url = appProperties.getLlm().getBaseUrl() + "/api/generate";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", appProperties.getLlm().getModel());
            requestBody.put("prompt", prompt);
            requestBody.put("stream", false);
            requestBody.put("options", Map.of(
                "temperature", 0.1, // Lower temperature for more consistent JSON output
                "num_predict", 4000
            ));
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.POST, entity, String.class
            );
            
            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode responseJson = objectMapper.readTree(response.getBody());
                JsonNode responseNode = responseJson.get("response");
                if (responseNode != null) {
                    return responseNode.asText();
                }
                throw new RuntimeException("No valid response from local LLM API");
            } else {
                throw new RuntimeException("Local LLM API call failed with status: " + response.getStatusCode());
            }
            
        } catch (Exception e) {
            logger.error("❌ Error calling local LLM API: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to call local LLM API", e);
        }
    }
    
    /**
     * Parse LLM response into structured format
     */
    private PrReviewResponse parseLlmResponse(String response, int prNumber) {
        try {
            // Clean the response - remove any markdown formatting
            String cleanResponse = response.trim();
            if (cleanResponse.startsWith("```json")) {
                cleanResponse = cleanResponse.substring(7);
            }
            if (cleanResponse.endsWith("```")) {
                cleanResponse = cleanResponse.substring(0, cleanResponse.length() - 3);
            }
            cleanResponse = cleanResponse.trim();
            
            JsonNode jsonNode = objectMapper.readTree(cleanResponse);
            
            String status = jsonNode.get("status").asText();
            String reviewSummary = jsonNode.has("reviewSummary") && !jsonNode.get("reviewSummary").isNull() 
                ? jsonNode.get("reviewSummary").asText() : null;
            String errorMessage = jsonNode.has("errorMessage") && !jsonNode.get("errorMessage").isNull() 
                ? jsonNode.get("errorMessage").asText() : null;
            
            List<DiffComment> comments = new ArrayList<>();
            if (jsonNode.has("comments") && jsonNode.get("comments").isArray()) {
                for (JsonNode commentNode : jsonNode.get("comments")) {
                    DiffComment comment = new DiffComment(
                        commentNode.get("fileName").asText(),
                        commentNode.get("diffLine").asInt(),
                        commentNode.get("comment").asText()
                    );
                    comments.add(comment);
                }
            }
            
            return new PrReviewResponse(status, prNumber, reviewSummary, comments, errorMessage);
            
        } catch (Exception e) {
            logger.error("❌ Error parsing LLM response: {}", e.getMessage(), e);
            return new PrReviewResponse(
                "error",
                prNumber,
                null,
                new ArrayList<>(),
                "Error parsing LLM response: " + e.getMessage()
            );
        }
    }
}
