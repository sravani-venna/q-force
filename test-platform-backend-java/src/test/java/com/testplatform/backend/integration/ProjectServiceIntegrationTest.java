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
 * Integration tests for Project Service endpoints
 * Tests cross-service interactions and full request/response cycle
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class ProjectServiceIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void testCreateProjectWorkflow() {
        // Given: A new project request
        String projectRequest = """
            {
                "name": "Test Project",
                "description": "Integration test project",
                "ownerId": "user123"
            }
            """;

        // When: Creating a project
        ResponseEntity<String> response = restTemplate.postForEntity(
            "/api/projects",
            projectRequest,
            String.class
        );

        // Then: Project is created successfully
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).contains("Test Project");
    }

    @Test
    public void testProjectAndContributorIntegration() {
        // Test that creating a project automatically creates owner as contributor
        String projectId = "proj-123";
        
        // Verify project exists
        ResponseEntity<String> projectResponse = restTemplate.getForEntity(
            "/api/projects/" + projectId,
            String.class
        );
        
        assertThat(projectResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        
        // Verify owner is added as contributor
        ResponseEntity<String> contributorResponse = restTemplate.getForEntity(
            "/api/projects/" + projectId + "/contributors",
            String.class
        );
        
        assertThat(contributorResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(contributorResponse.getBody()).contains("owner");
    }

    @Test
    public void testProjectWorkServiceIntegration() {
        // Test that work items are properly linked to projects
        String projectId = "proj-456";
        String workRequest = """
            {
                "title": "Implementation Task",
                "description": "Implement feature X",
                "projectId": "%s",
                "assigneeId": "user456"
            }
            """.formatted(projectId);

        ResponseEntity<String> response = restTemplate.postForEntity(
            "/api/work-items",
            workRequest,
            String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        
        // Verify work item is linked to project
        ResponseEntity<String> projectWorkItems = restTemplate.getForEntity(
            "/api/projects/" + projectId + "/work-items",
            String.class
        );
        
        assertThat(projectWorkItems.getBody()).contains("Implementation Task");
    }

    @Test
    public void testProjectFileUploadIntegration() {
        // Test project file storage integration
        String projectId = "proj-789";
        
        // Simulate file upload
        ResponseEntity<String> uploadResponse = restTemplate.postForEntity(
            "/api/projects/" + projectId + "/files",
            "test-file-data",
            String.class
        );
        
        assertThat(uploadResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        
        // Verify file is retrievable
        ResponseEntity<String> filesResponse = restTemplate.getForEntity(
            "/api/projects/" + projectId + "/files",
            String.class
        );
        
        assertThat(filesResponse.getBody()).isNotEmpty();
    }

    @Test
    public void testProjectStatisticsIntegration() {
        // Test that project statistics are calculated correctly
        String projectId = "proj-stats-1";
        
        ResponseEntity<String> statsResponse = restTemplate.getForEntity(
            "/api/projects/" + projectId + "/statistics",
            String.class
        );
        
        assertThat(statsResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(statsResponse.getBody()).contains("totalTasks");
        assertThat(statsResponse.getBody()).contains("completedTasks");
        assertThat(statsResponse.getBody()).contains("contributors");
    }

    @Test
    public void testProjectDeletionCascade() {
        // Test that deleting a project cascades to related entities
        String projectId = "proj-delete-1";
        
        // Delete project
        restTemplate.delete("/api/projects/" + projectId);
        
        // Verify project is deleted
        ResponseEntity<String> projectResponse = restTemplate.getForEntity(
            "/api/projects/" + projectId,
            String.class
        );
        
        assertThat(projectResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        
        // Verify related work items are also deleted
        ResponseEntity<String> workItemsResponse = restTemplate.getForEntity(
            "/api/projects/" + projectId + "/work-items",
            String.class
        );
        
        assertThat(workItemsResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void testProjectAccessControlIntegration() {
        // Test that project access control works with contributor service
        String projectId = "proj-access-1";
        String userId = "unauthorized-user";
        
        ResponseEntity<String> accessResponse = restTemplate.getForEntity(
            "/api/projects/" + projectId + "/access?userId=" + userId,
            String.class
        );
        
        // Verify unauthorized access is denied
        assertThat(accessResponse.getStatusCode()).isIn(
            HttpStatus.FORBIDDEN, 
            HttpStatus.UNAUTHORIZED
        );
    }

    @Test
    public void testProjectSearchIntegration() {
        // Test project search across multiple fields
        ResponseEntity<String> searchResponse = restTemplate.getForEntity(
            "/api/projects/search?q=integration&status=ACTIVE",
            String.class
        );
        
        assertThat(searchResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(searchResponse.getBody()).contains("integration");
    }

    @Test
    public void testProjectNotificationIntegration() {
        // Test that project updates trigger notifications
        String projectId = "proj-notify-1";
        String updateRequest = """
            {
                "status": "COMPLETED"
            }
            """;
        
        restTemplate.put("/api/projects/" + projectId, updateRequest);
        
        // Verify notification was sent
        ResponseEntity<String> notificationsResponse = restTemplate.getForEntity(
            "/api/notifications?projectId=" + projectId,
            String.class
        );
        
        assertThat(notificationsResponse.getBody()).contains("COMPLETED");
    }

    @Test
    public void testProjectTimelineIntegration() {
        // Test project timeline/audit trail
        String projectId = "proj-timeline-1";
        
        ResponseEntity<String> timelineResponse = restTemplate.getForEntity(
            "/api/projects/" + projectId + "/timeline",
            String.class
        );
        
        assertThat(timelineResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(timelineResponse.getBody()).contains("events");
    }
}

