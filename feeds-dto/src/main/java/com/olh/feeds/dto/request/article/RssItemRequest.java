package com.olh.feeds.dto.request.article;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Builder
public class RssItemRequest {

    private String creator;

    @NotBlank(message = "{article.title.required}")
    private String title;

    @NotBlank(message = "{article.link.required}")
    private String link;

    private String enclosureUrl;

    private String pubDate;

    private String contentEncoded;

    private String contentEncodedSnippet;

    private EnclosureRequest enclosure;

    private String dcCreator;

    private String content;

    private String contentSnippet;

    private String guid;

    private String isoDate;

    private ItunesRequest itunes;

    private List<String> hashtag;

    private String sourceName;
    private String sourceLink;
    private List<String> category;
}