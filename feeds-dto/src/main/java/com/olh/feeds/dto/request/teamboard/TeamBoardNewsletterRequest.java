// feeds-dto/src/main/java/com/olh/feeds/dto/request/teamboard/TeamBoardNewsletterRequest.java
package com.olh.feeds.dto.request.teamboard;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Builder
public class TeamBoardNewsletterRequest {

    @NotBlank(message = "{team.board.newsletter.title.required}")
    private String title;

    @NotEmpty(message = "{team.board.newsletter.recipients.required}")
    private List<String> recipients;

    private List<Long> articleIds;

    private String scheduleType; // IMMEDIATE, DAILY, WEEKLY, MONTHLY
}