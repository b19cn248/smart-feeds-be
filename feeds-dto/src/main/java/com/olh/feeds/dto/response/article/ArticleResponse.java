package com.olh.feeds.dto.response.article;

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
public class ArticleResponse {

    private Long id;
    private String title;
    private String content;
    private String contentEncoded;
    private LocalDateTime publishDate;
    private String summary;
    private String event;
    private String source;
    private String url;
    private String author;
    private String imageUrl;
    private String contentSnippet;
}
