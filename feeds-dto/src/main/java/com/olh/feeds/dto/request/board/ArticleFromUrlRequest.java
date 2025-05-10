// feeds-dto/src/main/java/com/olh/feeds/dto/request/board/ArticleFromUrlRequest.java
package com.olh.feeds.dto.request.board;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Builder
public class ArticleFromUrlRequest {

    @NotBlank(message = "{article.url.required}")
    private String url;

    private String title;

    private String content;

    private String note;
}