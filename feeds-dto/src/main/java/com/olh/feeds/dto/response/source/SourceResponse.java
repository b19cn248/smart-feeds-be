package com.olh.feeds.dto.response.source;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Builder
public class SourceResponse {
    private Long id;
    private String url;
    private String language;
    private String type;
    private String accountId;
    private String hashtag;
    private String category;
    private Long userId;
    private Boolean active;
    private LocalDateTime createdAt;

    public SourceResponse(Long id, String url, String type, Boolean active) {
        this.id = id;
        this.url = url;
        this.type = type;
        this.active = active;
    }
}