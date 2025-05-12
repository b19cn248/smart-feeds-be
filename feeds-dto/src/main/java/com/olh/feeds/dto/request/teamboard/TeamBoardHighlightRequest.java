// feeds-dto/src/main/java/com/olh/feeds/dto/request/teamboard/TeamBoardHighlightRequest.java
package com.olh.feeds.dto.request.teamboard;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Builder
public class TeamBoardHighlightRequest {

    @NotNull(message = "{team.board.highlight.article_id.required}")
    private Long articleId;

    @NotBlank(message = "{team.board.highlight.highlight_text.required}")
    private String highlightText;

    private String positionInfo;
}