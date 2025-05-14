// feeds-dto/src/main/java/com/olh/feeds/dto/response/teamboard/TeamBoardDetailResponse.java
package com.olh.feeds.dto.response.teamboard;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.olh.feeds.dto.response.PageResponse;
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
public class TeamBoardDetailResponse {
    private Long id;
    private String name;
    private String description;
    private Long teamId;
    private String teamName;
    private LocalDateTime createdAt;
    private String createdBy;
    private String userPermission;
    private List<TeamBoardUserResponse> members;
    private PageResponse<ArticleResponse> articles;
}