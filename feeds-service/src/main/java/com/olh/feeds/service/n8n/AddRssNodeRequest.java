package com.olh.feeds.service.n8n;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


// Request DTO
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddRssNodeRequest {
    @NotBlank(message = "Node name is required")
    private String nodeName;

    @NotBlank(message = "Feed URL is required")
    @Pattern(regexp = "^https?://.*", message = "Feed URL must be a valid HTTP/HTTPS URL")
    private String feedUrl;

    // === CÁC TÙYBE CHỌN ĐỂ CHỌN CODE NODE ===

    // Option 1: Chỉ định tên Code node cụ thể
    private String targetCodeNodeName;

    // Option 2: Chỉ định ID Code node cụ thể
    private String targetCodeNodeId;

    // Option 3: Strategy để tự động chọn Code node
    @JsonProperty("codeNodeSelectionStrategy")
    private CodeNodeSelectionStrategy selectionStrategy = CodeNodeSelectionStrategy.FIRST_AVAILABLE;
}