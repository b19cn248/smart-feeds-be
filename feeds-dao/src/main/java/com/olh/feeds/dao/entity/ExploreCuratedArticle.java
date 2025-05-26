package com.olh.feeds.dao.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "explore_curated_articles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExploreCuratedArticle extends BaseEntity {

    @Column(name = "collection_id", nullable = false)
    private Long collectionId;

    @Column(name = "article_id", nullable = false)
    private Long articleId;

    @Column(name = "priority", nullable = false)
    private Integer priority = 0;
}