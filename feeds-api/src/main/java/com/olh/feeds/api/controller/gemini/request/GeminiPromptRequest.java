package com.olh.feeds.api.controller.gemini.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GeminiPromptRequest {
    @NotBlank
    private String prompt;
    private String apiKey;
}
