// Article.java - updated
package com.olh.feeds.dao.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "articles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Article extends BaseEntity {

    @Column(name = "title", nullable = false, length = 512)
    private String title;

    @Column(name = "creator")
    private String creator;

    @Column(name = "link", length = 1024)
    private String link;

    @Column(name = "guid", length = 1024)
    private String guid;

    @Column(name = "pub_date")
    private LocalDateTime pubDate;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Column(name = "content_snippet", columnDefinition = "TEXT")
    private String contentSnippet;

    @Column(name = "content_encoded", columnDefinition = "TEXT")
    private String contentEncoded;

    @Column(name = "content_encoded_snippet", columnDefinition = "TEXT")
    private String contentEncodedSnippet;

    @Column(name = "enclosure_url", length = 1024)
    private String enclosureUrl;

    @Column(name = "enclosure_length")
    private String enclosureLength;

    @Column(name = "enclosure_type")
    private String enclosureType;

    @Column(name = "dc_creator")
    private String dcCreator;

    @Column(name = "iso_date")
    private LocalDateTime isoDate;

    @Column(name = "itunes_data", columnDefinition = "TEXT")
    private String itunesData;

    @Column(name = "event")
    private String event;

    @Column(name = "summary", columnDefinition = "TEXT")
    private String summary;

    @Column(name = "source_id")
    private Long sourceId;
}