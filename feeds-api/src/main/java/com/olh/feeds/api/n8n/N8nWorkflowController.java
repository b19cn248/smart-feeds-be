package com.olh.feeds.api.n8n;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v1/n8n")
@RequiredArgsConstructor
@Slf4j
public class N8nWorkflowController {

    private final N8nWorkflowService workflowService;

    @PostMapping("/workflows/{workflowId}/rss-nodes")
    public ResponseEntity<AddRssNodeResponse> addRssNode(
            @PathVariable String workflowId,
            @Valid @RequestBody AddRssNodeRequest request) {

        log.info("Adding RSS node '{}' with feed URL '{}' to workflow '{}'",
                request.getNodeName(), request.getFeedUrl(), workflowId);

        try {
            AddRssNodeResponse response = workflowService.addRssNodeToWorkflow(workflowId, request);

            if (response.isSuccess()) {
                log.info("Successfully added RSS node to workflow {}", workflowId);
                return ResponseEntity.ok(response);
            } else {
                log.warn("Failed to add RSS node to workflow {}: {}", workflowId, response.getMessage());
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception error) {
            log.error("Error processing request for workflow {}: {}", workflowId, error.getMessage());
            AddRssNodeResponse errorResponse = new AddRssNodeResponse(false,
                    "Internal server error: " + error.getMessage(), null, workflowId);
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
}
