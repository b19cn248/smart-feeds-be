// feeds-dto/src/main/java/com/olh/feeds/dto/request/board/BoardArticleRequest.java
package com.olh.feeds.dto.request.board;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
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
public class BoardArticleRequest {

    @NotNull(message = "{board.article.article_id.required}")
    private Long articleId;

    private String note;
}