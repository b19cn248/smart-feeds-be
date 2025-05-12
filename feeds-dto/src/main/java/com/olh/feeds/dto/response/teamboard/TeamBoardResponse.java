// feeds-dto/src/main/java/com/olh/feeds/dto/response/teamboard/TeamBoardResponse.java
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
public class TeamBoardResponse {
    private Long id;
    private String name;
    private String description;
    private Long teamId;
    private String teamName;
    private LocalDateTime createdAt;
    private String createdBy;
}