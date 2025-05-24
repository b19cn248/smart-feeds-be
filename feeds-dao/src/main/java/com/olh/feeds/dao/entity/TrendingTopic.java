package com.olh.feeds.dao.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

import java.time.LocalDate;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "trending_topics")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrendingTopic extends BaseEntity {

    @Column(name = "topic_name", nullable = false)
    private String topicName;

    @Column(name = "category_id")
    private Long categoryId;

    @Column(name = "score", nullable = false)
    private Float score = 0.0f;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;
}