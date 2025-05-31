// feeds-dto/src/main/java/com/olh/feeds/dto/request/teamboard/TeamBoardNoteRequest.java
package com.olh.feeds.dto.request.teamboard;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamBoardNoteRequest {

    @NotNull(message = "Article ID is required")
    private Long articleId;

    @NotNull(message = "Content is required")
    private String content;

    private List<Long> mentionedUserIds;
}