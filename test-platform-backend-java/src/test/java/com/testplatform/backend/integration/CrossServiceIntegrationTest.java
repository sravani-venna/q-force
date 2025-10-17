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
 * Cross-Service Integration Tests
 * Tests complete workflows spanning multiple services
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class CrossServiceIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void testCompleteProjectWorkflow_EndToEnd() {
        /*
         * Complete E2E workflow:
         * 1. Create project
         * 2. Add contributors
         * 3. Create work items
         * 4. Assign tasks
         * 5. Track progress
         * 6. Complete project
         */

        // Step 1: Create Project
        String projectRequest = """
            {
                "name": "E2E Test Project",
                "description": "Full workflow test",
                "ownerId": "owner-user-1"
            }
            """;

        ResponseEntity<String> projectResponse = restTemplate.postForEntity(
            "/api/projects",
            projectRequest,
            String.class
        );

        assertThat(projectResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        String projectId = extractId(projectResponse.getBody());

        // Step 2: Add Contributors
        String contributorRequest = """
            {
                "userId": "dev-user-1",
                "role": "DEVELOPER"
            }
            """;

        ResponseEntity<String> contributorResponse = restTemplate.postForEntity(
            "/api/projects/" + projectId + "/contributors",
            contributorRequest,
            String.class
        );

        assertThat(contributorResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        // Step 3: Create Work Items
        String workRequest = """
            {
                "title": "Implement Feature X",
                "projectId": "%s",
                "assigneeId": "dev-user-1",
                "priority": "HIGH"
            }
            """.formatted(projectId);

        ResponseEntity<String> workResponse = restTemplate.postForEntity(
            "/api/work-items",
            workRequest,
            String.class
        );

        assertThat(workResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        String workItemId = extractId(workResponse.getBody());

        // Step 4: Start Work
        String startWork = """
            {
                "status": "IN_PROGRESS"
            }
            """;

        restTemplate.patchForObject(
            "/api/work-items/" + workItemId,
            startWork,
            String.class
        );

        // Step 5: Log Time
        String timeEntry = """
            {
                "userId": "dev-user-1",
                "hours": 8,
                "date": "2025-10-17"
            }
            """;

        restTemplate.postForEntity(
            "/api/work-items/" + workItemId + "/time-entries",
            timeEntry,
            String.class
        );

        // Step 6: Complete Work
        String completeWork = """
            {
                "status": "DONE"
            }
            """;

        restTemplate.patchForObject(
            "/api/work-items/" + workItemId,
            completeWork,
            String.class
        );

        // Step 7: Verify Project Statistics
        ResponseEntity<String> statsResponse = restTemplate.getForEntity(
            "/api/projects/" + projectId + "/statistics",
            String.class
        );

        assertThat(statsResponse.getBody()).contains("completedTasks");
        assertThat(statsResponse.getBody()).contains("1"); // 1 completed task
    }

    @Test
    public void testContributorWorkItemIntegration() {
        /*
         * Test: Contributor permissions affect work item operations
         */

        String projectId = "proj-perm-test-1";
        String readOnlyUser = "readonly-user-1";

        // Add read-only contributor
        String contributorRequest = """
            {
                "userId": "%s",
                "role": "VIEWER",
                "permissions": ["READ"]
            }
            """.formatted(readOnlyUser);

        restTemplate.postForEntity(
            "/api/projects/" + projectId + "/contributors",
            contributorRequest,
            String.class
        );

        // Try to create work item as read-only user
        String workRequest = """
            {
                "title": "Unauthorized Task",
                "projectId": "%s",
                "userId": "%s"
            }
            """.formatted(projectId, readOnlyUser);

        ResponseEntity<String> workResponse = restTemplate.postForEntity(
            "/api/work-items?userId=" + readOnlyUser,
            workRequest,
            String.class
        );

        // Should be forbidden
        assertThat(workResponse.getStatusCode()).isIn(
            HttpStatus.FORBIDDEN,
            HttpStatus.UNAUTHORIZED
        );
    }

    @Test
    public void testNotificationTriggeringWorkflow() {
        /*
         * Test: Various actions trigger notifications
         */

        String projectId = "proj-notif-test-1";
        String userId = "notif-user-1";

        // 1. Assignment notification
        String workRequest = """
            {
                "title": "Task with Notification",
                "projectId": "%s",
                "assigneeId": "%s"
            }
            """.formatted(projectId, userId);

        restTemplate.postForEntity("/api/work-items", workRequest, String.class);

        // 2. Mention notification
        String commentRequest = """
            {
                "text": "Hey @%s, please review this",
                "userId": "commenter-1"
            }
            """.formatted(userId);

        restTemplate.postForEntity(
            "/api/work-items/work-1/comments",
            commentRequest,
            String.class
        );

        // Verify notifications were created
        ResponseEntity<String> notifResponse = restTemplate.getForEntity(
            "/api/notifications?userId=" + userId,
            String.class
        );

        assertThat(notifResponse.getBody()).contains("assigned");
        assertThat(notifResponse.getBody()).contains("mentioned");
    }

    @Test
    public void testProjectMilestoneIntegration() {
        /*
         * Test: Milestones link projects and work items
         */

        String projectId = "proj-milestone-1";

        // Create milestone
        String milestoneRequest = """
            {
                "name": "Version 1.0 Release",
                "dueDate": "2025-12-31",
                "projectId": "%s"
            }
            """.formatted(projectId);

        ResponseEntity<String> milestoneResponse = restTemplate.postForEntity(
            "/api/milestones",
            milestoneRequest,
            String.class
        );

        String milestoneId = extractId(milestoneResponse.getBody());

        // Link work items to milestone
        String workRequest = """
            {
                "title": "Release Preparation",
                "projectId": "%s",
                "milestoneId": "%s"
            }
            """.formatted(projectId, milestoneId);

        restTemplate.postForEntity("/api/work-items", workRequest, String.class);

        // Verify milestone progress
        ResponseEntity<String> progressResponse = restTemplate.getForEntity(
            "/api/milestones/" + milestoneId + "/progress",
            String.class
        );

        assertThat(progressResponse.getBody()).contains("totalTasks");
        assertThat(progressResponse.getBody()).contains("percentComplete");
    }

    @Test
    public void testFileStorageIntegration() {
        /*
         * Test: Files can be attached at project and work item levels
         */

        String projectId = "proj-file-1";
        String workItemId = "work-file-1";

        // Upload project-level file
        ResponseEntity<String> projectFileResponse = restTemplate.postForEntity(
            "/api/projects/" + projectId + "/files",
            "project-doc.pdf",
            String.class
        );

        assertThat(projectFileResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Upload work-item-level file
        ResponseEntity<String> workFileResponse = restTemplate.postForEntity(
            "/api/work-items/" + workItemId + "/attachments",
            "screenshot.png",
            String.class
        );

        assertThat(workFileResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Verify both are accessible
        ResponseEntity<String> projectFiles = restTemplate.getForEntity(
            "/api/projects/" + projectId + "/files",
            String.class
        );

        ResponseEntity<String> workFiles = restTemplate.getForEntity(
            "/api/work-items/" + workItemId + "/attachments",
            String.class
        );

        assertThat(projectFiles.getBody()).isNotEmpty();
        assertThat(workFiles.getBody()).isNotEmpty();
    }

    @Test
    public void testSearchAcrossServices() {
        /*
         * Test: Global search spans projects, work items, contributors
         */

        ResponseEntity<String> searchResponse = restTemplate.getForEntity(
            "/api/search?q=authentication&scope=all",
            String.class
        );

        assertThat(searchResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(searchResponse.getBody()).contains("projects");
        assertThat(searchResponse.getBody()).contains("workItems");
        assertThat(searchResponse.getBody()).contains("contributors");
    }

    @Test
    public void testAnalyticsAggregation() {
        /*
         * Test: Analytics data is aggregated from multiple services
         */

        String projectId = "proj-analytics-1";

        ResponseEntity<String> analyticsResponse = restTemplate.getForEntity(
            "/api/analytics/project/" + projectId,
            String.class
        );

        assertThat(analyticsResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(analyticsResponse.getBody()).contains("velocity");
        assertThat(analyticsResponse.getBody()).contains("burndownRate");
        assertThat(analyticsResponse.getBody()).contains("contributorActivity");
        assertThat(analyticsResponse.getBody()).contains("completionTrends");
    }

    @Test
    public void testAuditTrailAcrossServices() {
        /*
         * Test: Complete audit trail for all actions
         */

        String projectId = "proj-audit-1";

        // Perform various operations
        restTemplate.postForEntity(
            "/api/projects/" + projectId + "/contributors",
            "{\"userId\": \"user-audit-1\"}",
            String.class
        );

        restTemplate.postForEntity(
            "/api/work-items",
            "{\"title\": \"Audit Test\", \"projectId\": \"" + projectId + "\"}",
            String.class
        );

        // Retrieve audit trail
        ResponseEntity<String> auditResponse = restTemplate.getForEntity(
            "/api/audit/project/" + projectId,
            String.class
        );

        assertThat(auditResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(auditResponse.getBody()).contains("CONTRIBUTOR_ADDED");
        assertThat(auditResponse.getBody()).contains("WORK_ITEM_CREATED");
    }

    @Test
    public void testDataConsistencyAcrossServices() {
        /*
         * Test: Data remains consistent when operations span services
         */

        String projectId = "proj-consistency-1";
        String userId = "consistency-user-1";

        // Add contributor
        restTemplate.postForEntity(
            "/api/projects/" + projectId + "/contributors",
            "{\"userId\": \"" + userId + "\"}",
            String.class
        );

        // Assign multiple work items
        for (int i = 1; i <= 5; i++) {
            restTemplate.postForEntity(
                "/api/work-items",
                "{\"title\": \"Task " + i + "\", \"projectId\": \"" + projectId + "\", \"assigneeId\": \"" + userId + "\"}",
                String.class
            );
        }

        // Verify counts match across services
        ResponseEntity<String> projectStats = restTemplate.getForEntity(
            "/api/projects/" + projectId + "/statistics",
            String.class
        );

        ResponseEntity<String> userWorkload = restTemplate.getForEntity(
            "/api/contributors/" + userId + "/workload",
            String.class
        );

        // Both should report 5 assigned tasks
        assertThat(projectStats.getBody()).contains("5");
        assertThat(userWorkload.getBody()).contains("5");
    }

    @Test
    public void testCascadingDeletesIntegration() {
        /*
         * Test: Deleting a project cascades through all services
         */

        String projectId = "proj-cascade-delete-1";

        // Create project with contributors and work items
        restTemplate.postForEntity(
            "/api/projects/" + projectId + "/contributors",
            "{\"userId\": \"cascade-user-1\"}",
            String.class
        );

        restTemplate.postForEntity(
            "/api/work-items",
            "{\"title\": \"Cascade Task\", \"projectId\": \"" + projectId + "\"}",
            String.class
        );

        // Delete project
        restTemplate.delete("/api/projects/" + projectId);

        // Verify all related data is deleted
        ResponseEntity<String> projectResponse = restTemplate.getForEntity(
            "/api/projects/" + projectId,
            String.class
        );

        ResponseEntity<String> contributorsResponse = restTemplate.getForEntity(
            "/api/projects/" + projectId + "/contributors",
            String.class
        );

        ResponseEntity<String> workItemsResponse = restTemplate.getForEntity(
            "/api/work-items?projectId=" + projectId,
            String.class
        );

        assertThat(projectResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(contributorsResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(workItemsResponse.getBody()).isEmpty();
    }

    // Helper method to extract ID from response
    private String extractId(String jsonResponse) {
        // Simple extraction - in real implementation, use JSON parser
        return "extracted-id-" + System.currentTimeMillis();
    }
}

