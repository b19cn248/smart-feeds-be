package com.olh.feeds.api.n8n;

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
}
