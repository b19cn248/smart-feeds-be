package com.olh.feeds.api.controller.gemini.response;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class GeminiResponseDto {
    private List<CandidateDto> candidates;
    private UsageMetadataDto usageMetadata;
    private String modelVersion;
    private boolean overLimit;
}
