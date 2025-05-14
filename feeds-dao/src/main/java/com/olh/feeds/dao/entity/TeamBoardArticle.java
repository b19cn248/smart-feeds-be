// feeds-dao/src/main/java/com/olh/feeds/dao/entity/TeamBoardArticle.java
package com.olh.feeds.dao.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "team_board_articles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamBoardArticle extends BaseEntity {

    @Column(name = "team_board_id", nullable = false)
    private Long teamBoardId;

    @Column(name = "article_id", nullable = false)
    private Long articleId;

    @Column(name = "added_at")
    private java.time.LocalDateTime addedAt;
}