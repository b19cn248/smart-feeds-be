package com.olh.feeds.service.n8n;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddRssNodeResponse {
    private boolean success;
    private String message;
    private String nodeId;
    private String workflowId;
    private String connectedToCodeNode;
    private String selectedStrategy;

    public AddRssNodeResponse(boolean success, String message, String nodeId, String workflowId) {
        this.success = success;
        this.message = message;
        this.nodeId = nodeId;
        this.workflowId = workflowId;
    }
}
