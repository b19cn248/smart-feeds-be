// feeds-dao/src/main/java/com/olh/feeds/dao/entity/TeamBoardNewsletter.java
package com.olh.feeds.dao.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "team_board_newsletters")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamBoardNewsletter extends BaseEntity {

    @Column(name = "team_board_id", nullable = false)
    private Long teamBoardId;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "recipients", columnDefinition = "TEXT", nullable = false)
    private String recipients;

    @Column(name = "included_articles", columnDefinition = "TEXT")
    private String includedArticles;

    @Column(name = "schedule_type")
    private String scheduleType; // IMMEDIATE, DAILY, WEEKLY, MONTHLY

    @Column(name = "next_run_time")
    private LocalDateTime nextRunTime;

    @Column(name = "last_run_time")
    private LocalDateTime lastRunTime;

    @Column(name = "is_active")
    private Boolean isActive;
}