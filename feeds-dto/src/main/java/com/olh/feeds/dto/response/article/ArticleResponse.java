package com.olh.feeds.dto.response.article;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

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
    private String source; // ánh xạ tới s.url
    private String url;   // ánh xạ tới a.link
    private String author; // ánh xạ tới a.creator
    private String imageUrl; // ánh xạ tới a.enclosureUrl
    private String contentSnippet;
    private String contentEncodedSnippet;
    private List<String> hashtag;

    // Constructor cho truy vấn JPQL
    public ArticleResponse(Long id, String title, String content, String contentEncoded,
                           LocalDateTime isoDate, String summary, String event, String source,
                           String url, String author, String imageUrl, String contentSnippet,
                           String contentEncodedSnippet) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.contentEncoded = contentEncoded;
        this.publishDate = isoDate; // ánh xạ isoDate thành publishDate
        this.summary = summary;
        this.event = event;
        this.source = source;
        this.url = url;
        this.author = author;
        this.imageUrl = imageUrl;
        this.contentSnippet = contentSnippet;
        this.contentEncodedSnippet = contentEncodedSnippet;
        this.hashtag = List.of(); // Khởi tạo hashtag rỗng, sẽ được xử lý riêng
    }
}