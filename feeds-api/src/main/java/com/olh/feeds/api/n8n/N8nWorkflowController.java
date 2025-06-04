package com.olh.feeds.api.n8n;

import com.olh.feeds.service.n8n.AddRssNodeRequest;
import com.olh.feeds.service.n8n.AddRssNodeResponse;
import com.olh.feeds.service.n8n.EnhancedN8nWorkflowService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;


@RestController
@RequestMapping("/api/v1/n8n")
@RequiredArgsConstructor
@Slf4j
public class N8nWorkflowController {

    private final EnhancedN8nWorkflowService workflowService;

    @PostMapping("/workflows/{workflowId}/rss-nodes")
    public ResponseEntity<AddRssNodeResponse> addRssNode(
            @PathVariable String workflowId,
            @Valid @RequestBody AddRssNodeRequest request) {

        log.info("=== RSS Node Addition Request ===");
        log.info("Workflow ID: {}", workflowId);
        log.info("RSS Node Name: '{}'", request.getNodeName());
        log.info("Feed URL: {}", request.getFeedUrl());
        log.info("Selection Strategy: {}", request.getSelectionStrategy());

        if (request.getTargetCodeNodeName() != null) {
            log.info("Target Code Node Name: '{}'", request.getTargetCodeNodeName());
        }
        if (request.getTargetCodeNodeId() != null) {
            log.info("Target Code Node ID: '{}'", request.getTargetCodeNodeId());
        }

        try {
            AddRssNodeResponse response = workflowService.addRssNodeToWorkflow(workflowId, request);

            if (response.isSuccess()) {
                log.info("=== SUCCESS ===");
                log.info("RSS Node ID: {}", response.getNodeId());
                log.info("Connected to Code Node: '{}'", response.getConnectedToCodeNode());
                log.info("Strategy Used: {}", response.getSelectedStrategy());
                log.info("Message: {}", response.getMessage());
                return ResponseEntity.ok(response);
            } else {
                log.warn("=== FAILED ===");
                log.warn("Reason: {}", response.getMessage());
                log.warn("Strategy Used: {}", response.getSelectedStrategy());
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception error) {
            log.error("=== ERROR ===");
            log.error("Error processing request for workflow {}: {}", workflowId, error.getMessage(), error);
            AddRssNodeResponse errorResponse = new AddRssNodeResponse(false,
                    "Internal server error: " + error.getMessage(),
                    null, workflowId, null, request.getSelectionStrategy().toString());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    // Endpoint bổ sung để liệt kê các Code nodes có sẵn
    @GetMapping("/workflows/{workflowId}/code-nodes")
    public ResponseEntity<Map<String, Object>> listCodeNodes(@PathVariable String workflowId) {
        log.info("Listing Code nodes for workflow: {}", workflowId);

        try {
            // Gọi service để lấy thông tin workflow và extract Code nodes
            // (Implementation sẽ cần thêm method trong service)
            Map<String, Object> result = new HashMap<>();
            result.put("message", "Feature coming soon - please check logs for now");
            result.put("workflowId", workflowId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error listing Code nodes for workflow {}: {}", workflowId, e.getMessage());
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("error", "Failed to list Code nodes: " + e.getMessage());
            errorResult.put("workflowId", workflowId);
            return ResponseEntity.internalServerError().body(errorResult);
        }
    }
}