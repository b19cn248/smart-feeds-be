package com.olh.feeds.api.controller.gemini.response;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.olh.feeds.api.controller.gemini.request.ContentDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CandidateDto {
    private ContentDto content;
    private String finishReason;
    private CitationMetadataDto citationMetadata;
    private Double avgLogprobs;
}
