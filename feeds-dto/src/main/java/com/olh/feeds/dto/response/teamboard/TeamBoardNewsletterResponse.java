// feeds-dto/src/main/java/com/olh/feeds/dto/response/teamboard/TeamBoardNewsletterResponse.java
package com.olh.feeds.dto.response.teamboard;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
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
public class TeamBoardNewsletterResponse {
    private Long id;
    private Long teamBoardId;
    private String title;
    private List<String> recipients;
    private List<Long> articleIds;
    private String scheduleType;
    private LocalDateTime nextRunTime;
    private LocalDateTime lastRunTime;
    private Boolean isActive;
    private LocalDateTime createdAt;
}