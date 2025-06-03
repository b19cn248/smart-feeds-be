package com.olh.feeds.api.n8n;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


// Request DTO
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddRssNodeRequest {
    @NotBlank(message = "Node name is required")
    private String nodeName;

    @NotBlank(message = "Feed URL is required")
    @Pattern(regexp = "^https?://.*", message = "Feed URL must be a valid HTTP/HTTPS URL")
    private String feedUrl;
}