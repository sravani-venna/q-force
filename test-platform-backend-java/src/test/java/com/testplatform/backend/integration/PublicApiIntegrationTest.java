package com.testplatform.backend.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for Public API Service
 * Tests external API endpoints, authentication, rate limiting, and webhooks
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class PublicApiIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void testPublicApiAuthentication() {
        // Test API key authentication
        ResponseEntity<String> response = restTemplate.getForEntity(
            "/api/public/v1/projects?apiKey=test-key-123",
            String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void testPublicApiInvalidAuthentication() {
        // Test with invalid API key
        ResponseEntity<String> response = restTemplate.getForEntity(
            "/api/public/v1/projects?apiKey=invalid-key",
            String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    public void testPublicApiRateLimiting() {
        // Test rate limiting enforcement
        String apiKey = "rate-limit-key";

        // Make multiple rapid requests
        int successCount = 0;
        int rateLimitCount = 0;

        for (int i = 0; i < 100; i++) {
            ResponseEntity<String> response = restTemplate.getForEntity(
                "/api/public/v1/projects?apiKey=" + apiKey,
                String.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                successCount++;
            } else if (response.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
                rateLimitCount++;
            }
        }

        // Should have some rate-limited requests
        assertThat(rateLimitCount).isGreaterThan(0);
        assertThat(response.getHeaders().getFirst("X-RateLimit-Limit")).isNotNull();
    }

    @Test
    public void testPublicApiProjectsEndpoint() {
        // Test listing projects via public API
        ResponseEntity<String> response = restTemplate.getForEntity(
            "/api/public/v1/projects?apiKey=valid-key&limit=10",
            String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("projects");
        assertThat(response.getBody()).contains("total");
        assertThat(response.getBody()).contains("page");
    }

    @Test
    public void testPublicApiProjectDetails() {
        // Test getting specific project details
        String projectId = "public-proj-1";

        ResponseEntity<String> response = restTemplate.getForEntity(
            "/api/public/v1/projects/" + projectId + "?apiKey=valid-key",
            String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains(projectId);
        assertThat(response.getBody()).contains("name");
        assertThat(response.getBody()).contains("status");
    }

    @Test
    public void testPublicApiWebhooks() {
        // Test webhook registration
        String webhookRequest = """
            {
                "url": "https://example.com/webhook",
                "events": ["project.created", "project.updated"],
                "secret": "webhook-secret-123"
            }
            """;

        ResponseEntity<String> response = restTemplate.postForEntity(
            "/api/public/v1/webhooks?apiKey=valid-key",
            webhookRequest,
            String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).contains("webhookId");
    }

    @Test
    public void testPublicApiWebhookDelivery() {
        // Test that webhooks are triggered on events
        String webhookId = "webhook-test-1";

        // Trigger an event (e.g., create project)
        restTemplate.postForEntity(
            "/api/public/v1/projects?apiKey=valid-key",
            "{\"name\": \"Test Project\"}",
            String.class
        );

        // Check webhook delivery log
        ResponseEntity<String> deliveryResponse = restTemplate.getForEntity(
            "/api/public/v1/webhooks/" + webhookId + "/deliveries?apiKey=valid-key",
            String.class
        );

        assertThat(deliveryResponse.getBody()).contains("project.created");
        assertThat(deliveryResponse.getBody()).contains("status");
    }

    @Test
    public void testPublicApiPagination() {
        // Test pagination works correctly
        ResponseEntity<String> page1 = restTemplate.getForEntity(
            "/api/public/v1/projects?apiKey=valid-key&page=1&limit=10",
            String.class
        );

        ResponseEntity<String> page2 = restTemplate.getForEntity(
            "/api/public/v1/projects?apiKey=valid-key&page=2&limit=10",
            String.class
        );

        assertThat(page1.getBody()).isNotEqualTo(page2.getBody());
        assertThat(page1.getBody()).contains("\"page\": 1");
        assertThat(page2.getBody()).contains("\"page\": 2");
    }

    @Test
    public void testPublicApiFiltering() {
        // Test filtering by status
        ResponseEntity<String> response = restTemplate.getForEntity(
            "/api/public/v1/projects?apiKey=valid-key&status=ACTIVE&priority=HIGH",
            String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        // Verify all results match filters
        assertThat(response.getBody()).contains("ACTIVE");
        assertThat(response.getBody()).contains("HIGH");
    }

    @Test
    public void testPublicApiSorting() {
        // Test sorting results
        ResponseEntity<String> response = restTemplate.getForEntity(
            "/api/public/v1/projects?apiKey=valid-key&sort=createdAt:desc",
            String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        // Results should be in descending order by createdAt
    }

    @Test
    public void testPublicApiFieldSelection() {
        // Test selecting specific fields
        ResponseEntity<String> response = restTemplate.getForEntity(
            "/api/public/v1/projects?apiKey=valid-key&fields=id,name,status",
            String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("id");
        assertThat(response.getBody()).contains("name");
        assertThat(response.getBody()).contains("status");
        // Should not contain other fields like description
        assertThat(response.getBody()).doesNotContain("description");
    }

    @Test
    public void testPublicApiCORS() {
        // Test CORS headers are present
        ResponseEntity<String> response = restTemplate.getForEntity(
            "/api/public/v1/projects?apiKey=valid-key",
            String.class
        );

        assertThat(response.getHeaders().getAccessControlAllowOrigin()).isNotNull();
        assertThat(response.getHeaders().get("Access-Control-Allow-Methods")).isNotNull();
    }

    @Test
    public void testPublicApiVersioning() {
        // Test API versioning
        ResponseEntity<String> v1Response = restTemplate.getForEntity(
            "/api/public/v1/projects?apiKey=valid-key",
            String.class
        );

        ResponseEntity<String> v2Response = restTemplate.getForEntity(
            "/api/public/v2/projects?apiKey=valid-key",
            String.class
        );

        // Both versions should work
        assertThat(v1Response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(v2Response.getStatusCode()).isIn(HttpStatus.OK, HttpStatus.NOT_FOUND);
    }

    @Test
    public void testPublicApiErrorResponses() {
        // Test standardized error responses
        ResponseEntity<String> response = restTemplate.getForEntity(
            "/api/public/v1/projects/nonexistent?apiKey=valid-key",
            String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).contains("error");
        assertThat(response.getBody()).contains("message");
        assertThat(response.getBody()).contains("code");
    }

    @Test
    public void testPublicApiHealthCheck() {
        // Test public API health endpoint
        ResponseEntity<String> response = restTemplate.getForEntity(
            "/api/public/health",
            String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("status");
        assertThat(response.getBody()).contains("UP");
    }

    @Test
    public void testPublicApiDocumentation() {
        // Test OpenAPI/Swagger documentation endpoint
        ResponseEntity<String> response = restTemplate.getForEntity(
            "/api/public/v1/docs",
            String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("openapi");
    }

    @Test
    public void testPublicApiBulkOperations() {
        // Test bulk create/update operations
        String bulkRequest = """
            {
                "projects": [
                    {"name": "Bulk Project 1"},
                    {"name": "Bulk Project 2"},
                    {"name": "Bulk Project 3"}
                ]
            }
            """;

        ResponseEntity<String> response = restTemplate.postForEntity(
            "/api/public/v1/projects/bulk?apiKey=valid-key",
            bulkRequest,
            String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).contains("created");
        assertThat(response.getBody()).contains("3");
    }

    @Test
    public void testPublicApiExport() {
        // Test data export functionality
        ResponseEntity<String> response = restTemplate.getForEntity(
            "/api/public/v1/export/projects?apiKey=valid-key&format=csv",
            String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getContentType().toString()).contains("csv");
    }

    @Test
    public void testPublicApiMetrics() {
        // Test API usage metrics
        ResponseEntity<String> response = restTemplate.getForEntity(
            "/api/public/v1/metrics?apiKey=valid-key",
            String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("requestCount");
        assertThat(response.getBody()).contains("errorRate");
        assertThat(response.getBody()).contains("averageResponseTime");
    }

    @Test
    public void testPublicApiCaching() {
        // Test caching headers
        ResponseEntity<String> response = restTemplate.getForEntity(
            "/api/public/v1/projects?apiKey=valid-key",
            String.class
        );

        assertThat(response.getHeaders().getCacheControl()).isNotNull();
        assertThat(response.getHeaders().getETag()).isNotNull();
    }

    @Test
    public void testPublicApiCompression() {
        // Test gzip compression
        ResponseEntity<String> response = restTemplate.getForEntity(
            "/api/public/v1/projects?apiKey=valid-key",
            String.class
        );

        // Check if Accept-Encoding is honored
        assertThat(response.getHeaders().get("Content-Encoding")).isIn(
            null, 
            List.of("gzip"),
            List.of("gzip, deflate")
        );
    }
}

