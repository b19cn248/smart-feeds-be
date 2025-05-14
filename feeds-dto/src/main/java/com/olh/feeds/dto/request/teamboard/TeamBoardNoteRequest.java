// feeds-dto/src/main/java/com/olh/feeds/dto/request/teamboard/TeamBoardNoteRequest.java
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
public class TeamBoardNoteRequest {

    @NotNull(message = "{team.board.note.article_id.required}")
    private Long articleId;

    @NotBlank(message = "{team.board.note.content.required}")
    private String content;
}