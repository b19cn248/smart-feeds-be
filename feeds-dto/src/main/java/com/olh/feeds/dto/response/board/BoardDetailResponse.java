// feeds-dto/src/main/java/com/olh/feeds/dto/response/board/BoardDetailResponse.java
package com.olh.feeds.dto.response.board;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.olh.feeds.dto.response.article.ArticleResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Builder
public class BoardDetailResponse {
    private Long id;
    private String name;
    private String description;
    private String color;
    private String icon;
    private Boolean isPublic;
    private LocalDateTime createdAt;
    private List<ArticleResponse> articles;
}