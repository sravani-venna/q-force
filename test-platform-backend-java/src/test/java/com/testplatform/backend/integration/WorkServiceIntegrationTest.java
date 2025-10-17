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
 * Integration tests for Work Service
 * Tests work item management, workflow, and dependencies
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class WorkServiceIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void testCreateWorkItemWorkflow() {
        // Given: A work item request
        String workRequest = """
            {
                "title": "Implement User Authentication",
                "description": "Add OAuth2 authentication",
                "projectId": "proj-work-1",
                "assigneeId": "user123",
                "priority": "HIGH",
                "estimatedHours": 16
            }
            """;

        // When: Creating work item
        ResponseEntity<String> response = restTemplate.postForEntity(
            "/api/work-items",
            workRequest,
            String.class
        );

        // Then: Work item is created
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).contains("Implement User Authentication");
        assertThat(response.getBody()).contains("HIGH");
    }

    @Test
    public void testWorkItemStatusTransition() {
        // Test work item status workflow: TODO -> IN_PROGRESS -> DONE
        String workItemId = "work-status-1";

        // Start work
        String startRequest = """
            {
                "status": "IN_PROGRESS",
                "startedAt": "2025-10-17T10:00:00Z"
            }
            """;

        ResponseEntity<String> startResponse = restTemplate.patchForObject(
            "/api/work-items/" + workItemId,
            startRequest,
            String.class
        );

        assertThat(startResponse).contains("IN_PROGRESS");

        // Complete work
        String completeRequest = """
            {
                "status": "DONE",
                "completedAt": "2025-10-17T18:00:00Z"
            }
            """;

        ResponseEntity<String> completeResponse = restTemplate.patchForObject(
            "/api/work-items/" + workItemId,
            completeRequest,
            String.class
        );

        assertThat(completeResponse).contains("DONE");
    }

    @Test
    public void testWorkItemDependencyManagement() {
        // Test work item dependencies
        String workItemId = "work-dep-1";
        String dependencyRequest = """
            {
                "dependsOn": ["work-dep-2", "work-dep-3"],
                "blockedBy": []
            }
            """;

        ResponseEntity<String> response = restTemplate.postForEntity(
            "/api/work-items/" + workItemId + "/dependencies",
            dependencyRequest,
            String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Verify dependencies are tracked
        ResponseEntity<String> depsResponse = restTemplate.getForEntity(
            "/api/work-items/" + workItemId + "/dependencies",
            String.class
        );

        assertThat(depsResponse.getBody()).contains("work-dep-2");
        assertThat(depsResponse.getBody()).contains("work-dep-3");
    }

    @Test
    public void testWorkItemTimeTracking() {
        // Test time tracking integration
        String workItemId = "work-time-1";
        String timeEntry = """
            {
                "userId": "user123",
                "hours": 4,
                "description": "Implemented authentication logic",
                "date": "2025-10-17"
            }
            """;

        ResponseEntity<String> response = restTemplate.postForEntity(
            "/api/work-items/" + workItemId + "/time-entries",
            timeEntry,
            String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        // Verify time is aggregated
        ResponseEntity<String> timeResponse = restTemplate.getForEntity(
            "/api/work-items/" + workItemId + "/time-summary",
            String.class
        );

        assertThat(timeResponse.getBody()).contains("totalHours");
        assertThat(timeResponse.getBody()).contains("4");
    }

    @Test
    public void testWorkItemComments() {
        // Test commenting on work items
        String workItemId = "work-comment-1";
        String commentRequest = """
            {
                "userId": "user456",
                "text": "We should consider using JWT tokens",
                "timestamp": "2025-10-17T14:30:00Z"
            }
            """;

        ResponseEntity<String> response = restTemplate.postForEntity(
            "/api/work-items/" + workItemId + "/comments",
            commentRequest,
            String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        // Verify comments are retrievable
        ResponseEntity<String> commentsResponse = restTemplate.getForEntity(
            "/api/work-items/" + workItemId + "/comments",
            String.class
        );

        assertThat(commentsResponse.getBody()).contains("JWT tokens");
    }

    @Test
    public void testWorkItemAssignmentChange() {
        // Test reassigning work items
        String workItemId = "work-assign-1";
        String reassignRequest = """
            {
                "newAssigneeId": "user789",
                "reason": "Original assignee on leave"
            }
            """;

        ResponseEntity<String> response = restTemplate.patchForObject(
            "/api/work-items/" + workItemId + "/assign",
            reassignRequest,
            String.class
        );

        // Verify assignment change is logged
        ResponseEntity<String> historyResponse = restTemplate.getForEntity(
            "/api/work-items/" + workItemId + "/history",
            String.class
        );

        assertThat(historyResponse.getBody()).contains("user789");
        assertThat(historyResponse.getBody()).contains("Original assignee on leave");
    }

    @Test
    public void testWorkItemAttachments() {
        // Test file attachments to work items
        String workItemId = "work-attach-1";

        ResponseEntity<String> uploadResponse = restTemplate.postForEntity(
            "/api/work-items/" + workItemId + "/attachments",
            "mock-file-data",
            String.class
        );

        assertThat(uploadResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Verify attachments are listed
        ResponseEntity<String> attachmentsResponse = restTemplate.getForEntity(
            "/api/work-items/" + workItemId + "/attachments",
            String.class
        );

        assertThat(attachmentsResponse.getBody()).isNotEmpty();
    }

    @Test
    public void testWorkItemLabelsAndTags() {
        // Test labeling/tagging work items
        String workItemId = "work-label-1";
        String labelRequest = """
            {
                "labels": ["bug", "critical", "security"],
                "tags": ["backend", "authentication"]
            }
            """;

        ResponseEntity<String> response = restTemplate.patchForObject(
            "/api/work-items/" + workItemId + "/labels",
            labelRequest,
            String.class
        );

        // Verify labels are searchable
        ResponseEntity<String> searchResponse = restTemplate.getForEntity(
            "/api/work-items/search?labels=security,critical",
            String.class
        );

        assertThat(searchResponse.getBody()).contains(workItemId);
    }

    @Test
    public void testWorkItemEstimationVsActual() {
        // Test comparing estimated vs actual time
        String workItemId = "work-estimate-1";

        ResponseEntity<String> metricsResponse = restTemplate.getForEntity(
            "/api/work-items/" + workItemId + "/metrics",
            String.class
        );

        assertThat(metricsResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(metricsResponse.getBody()).contains("estimatedHours");
        assertThat(metricsResponse.getBody()).contains("actualHours");
        assertThat(metricsResponse.getBody()).contains("variance");
    }

    @Test
    public void testWorkItemBulkOperations() {
        // Test bulk status updates
        String bulkRequest = """
            {
                "workItemIds": ["work-bulk-1", "work-bulk-2", "work-bulk-3"],
                "operation": "UPDATE_STATUS",
                "newStatus": "IN_REVIEW"
            }
            """;

        ResponseEntity<String> response = restTemplate.postForEntity(
            "/api/work-items/bulk-update",
            bulkRequest,
            String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("updated");
        assertThat(response.getBody()).contains("3");
    }

    @Test
    public void testWorkItemSubtasks() {
        // Test creating subtasks
        String parentWorkItemId = "work-parent-1";
        String subtaskRequest = """
            {
                "title": "Write unit tests",
                "parentId": "%s",
                "assigneeId": "user123"
            }
            """.formatted(parentWorkItemId);

        ResponseEntity<String> response = restTemplate.postForEntity(
            "/api/work-items",
            subtaskRequest,
            String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        // Verify subtask is linked to parent
        ResponseEntity<String> subtasksResponse = restTemplate.getForEntity(
            "/api/work-items/" + parentWorkItemId + "/subtasks",
            String.class
        );

        assertThat(subtasksResponse.getBody()).contains("Write unit tests");
    }

    @Test
    public void testWorkItemRecurringTasks() {
        // Test recurring work items (e.g., weekly code review)
        String recurringRequest = """
            {
                "title": "Weekly Code Review",
                "projectId": "proj-recurring-1",
                "recurring": true,
                "frequency": "WEEKLY",
                "dayOfWeek": "FRIDAY"
            }
            """;

        ResponseEntity<String> response = restTemplate.postForEntity(
            "/api/work-items/recurring",
            recurringRequest,
            String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).contains("WEEKLY");
    }
}

