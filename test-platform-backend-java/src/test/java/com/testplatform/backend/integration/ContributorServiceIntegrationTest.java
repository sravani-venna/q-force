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
 * Integration tests for Contributor Service
 * Tests contributor management, permissions, and cross-service interactions
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class ContributorServiceIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void testAddContributorToProject() {
        // Given: A project and a user
        String projectId = "proj-contrib-1";
        String contributorRequest = """
            {
                "userId": "user123",
                "role": "DEVELOPER",
                "permissions": ["READ", "WRITE"]
            }
            """;

        // When: Adding contributor to project
        ResponseEntity<String> response = restTemplate.postForEntity(
            "/api/projects/" + projectId + "/contributors",
            contributorRequest,
            String.class
        );

        // Then: Contributor is added successfully
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).contains("user123");
        assertThat(response.getBody()).contains("DEVELOPER");
    }

    @Test
    public void testContributorRoleUpdate() {
        // Test updating contributor role
        String projectId = "proj-role-1";
        String userId = "user456";
        String updateRequest = """
            {
                "role": "ADMIN"
            }
            """;

        ResponseEntity<String> response = restTemplate.patchForObject(
            "/api/projects/" + projectId + "/contributors/" + userId,
            updateRequest,
            String.class
        );

        // Verify role is updated
        ResponseEntity<String> getResponse = restTemplate.getForEntity(
            "/api/projects/" + projectId + "/contributors/" + userId,
            String.class
        );
        
        assertThat(getResponse.getBody()).contains("ADMIN");
    }

    @Test
    public void testContributorPermissionValidation() {
        // Test that contributor permissions are enforced
        String projectId = "proj-perm-1";
        String userId = "readonly-user";

        // Try to perform write operation with read-only user
        ResponseEntity<String> response = restTemplate.postForEntity(
            "/api/projects/" + projectId + "/work-items?userId=" + userId,
            "{\"title\": \"New Task\"}",
            String.class
        );

        // Should be forbidden
        assertThat(response.getStatusCode()).isIn(
            HttpStatus.FORBIDDEN,
            HttpStatus.UNAUTHORIZED
        );
    }

    @Test
    public void testContributorActivityTracking() {
        // Test that contributor activities are tracked
        String projectId = "proj-activity-1";
        String userId = "user789";

        ResponseEntity<String> activityResponse = restTemplate.getForEntity(
            "/api/projects/" + projectId + "/contributors/" + userId + "/activity",
            String.class
        );

        assertThat(activityResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(activityResponse.getBody()).contains("lastActive");
        assertThat(activityResponse.getBody()).contains("contributions");
    }

    @Test
    public void testContributorInvitationWorkflow() {
        // Test complete invitation workflow
        String projectId = "proj-invite-1";
        String inviteRequest = """
            {
                "email": "newuser@example.com",
                "role": "DEVELOPER"
            }
            """;

        // Send invitation
        ResponseEntity<String> inviteResponse = restTemplate.postForEntity(
            "/api/projects/" + projectId + "/invite",
            inviteRequest,
            String.class
        );

        assertThat(inviteResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        
        // Verify invitation is pending
        ResponseEntity<String> pendingResponse = restTemplate.getForEntity(
            "/api/projects/" + projectId + "/invitations/pending",
            String.class
        );
        
        assertThat(pendingResponse.getBody()).contains("newuser@example.com");
    }

    @Test
    public void testRemoveContributorCascade() {
        // Test that removing contributor updates related entities
        String projectId = "proj-remove-1";
        String userId = "user-to-remove";

        // Remove contributor
        restTemplate.delete("/api/projects/" + projectId + "/contributors/" + userId);

        // Verify contributor is removed
        ResponseEntity<String> getResponse = restTemplate.getForEntity(
            "/api/projects/" + projectId + "/contributors/" + userId,
            String.class
        );
        
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        
        // Verify their assignments are unassigned
        ResponseEntity<String> workItemsResponse = restTemplate.getForEntity(
            "/api/work-items/assigned-to/" + userId + "?projectId=" + projectId,
            String.class
        );
        
        assertThat(workItemsResponse.getBody()).doesNotContain(projectId);
    }

    @Test
    public void testContributorGroupManagement() {
        // Test contributor groups/teams
        String projectId = "proj-group-1";
        String groupRequest = """
            {
                "name": "Backend Team",
                "members": ["user1", "user2", "user3"]
            }
            """;

        ResponseEntity<String> response = restTemplate.postForEntity(
            "/api/projects/" + projectId + "/contributor-groups",
            groupRequest,
            String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).contains("Backend Team");
    }

    @Test
    public void testContributorStatistics() {
        // Test contributor statistics aggregation
        String projectId = "proj-stats-contrib-1";

        ResponseEntity<String> statsResponse = restTemplate.getForEntity(
            "/api/projects/" + projectId + "/contributors/statistics",
            String.class
        );

        assertThat(statsResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(statsResponse.getBody()).contains("totalContributors");
        assertThat(statsResponse.getBody()).contains("activeContributors");
        assertThat(statsResponse.getBody()).contains("contributionsByRole");
    }

    @Test
    public void testContributorSearchAndFilter() {
        // Test searching contributors
        String projectId = "proj-search-1";

        ResponseEntity<String> searchResponse = restTemplate.getForEntity(
            "/api/projects/" + projectId + "/contributors/search?q=john&role=DEVELOPER",
            String.class
        );

        assertThat(searchResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(searchResponse.getBody()).contains("DEVELOPER");
    }

    @Test
    public void testContributorNotificationPreferences() {
        // Test contributor notification settings
        String projectId = "proj-notif-1";
        String userId = "user-notif-1";
        String preferencesRequest = """
            {
                "emailNotifications": true,
                "pushNotifications": false,
                "frequency": "DAILY"
            }
            """;

        ResponseEntity<String> response = restTemplate.putForObject(
            "/api/projects/" + projectId + "/contributors/" + userId + "/preferences",
            preferencesRequest,
            String.class
        );

        // Verify preferences are saved
        ResponseEntity<String> getPrefs = restTemplate.getForEntity(
            "/api/projects/" + projectId + "/contributors/" + userId + "/preferences",
            String.class
        );
        
        assertThat(getPrefs.getBody()).contains("DAILY");
    }
}

