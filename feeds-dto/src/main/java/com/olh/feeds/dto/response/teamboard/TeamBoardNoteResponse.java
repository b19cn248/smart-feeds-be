// feeds-dto/src/main/java/com/olh/feeds/dto/response/teamboard/TeamBoardNoteResponse.java
package com.olh.feeds.dto.response.teamboard;

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
public class TeamBoardNoteResponse {
    private Long id;
    private Long teamBoardId;
    private Long articleId;
    private String content;
    private String mentionedUsers;
    private String createdByEmail;
    private String createdByName;
    private LocalDateTime createdAt;
}