package com.olh.feeds.dao.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "article_stats")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ArticleStat extends BaseEntity {

    @Column(name = "article_id", nullable = false)
    private Long articleId;

    @Column(name = "view_count", nullable = false)
    private Integer viewCount = 0;

    @Column(name = "share_count", nullable = false)
    private Integer shareCount = 0;

    @Column(name = "save_count", nullable = false)
    private Integer saveCount = 0;

    @Column(name = "engagement_score", nullable = false)
    private Float engagementScore = 0.0f;

    @Column(name = "trending_score", nullable = false)
    private Float trendingScore = 0.0f;

    @Column(name = "is_top_story", nullable = false)
    private Boolean isTopStory = false;
}