// feeds-dao/src/main/java/com/olh/feeds/dao/entity/TeamBoardHighlight.java
package com.olh.feeds.dao.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "team_board_highlights")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamBoardHighlight extends BaseEntity {

    @Column(name = "team_board_id", nullable = false)
    private Long teamBoardId;

    @Column(name = "article_id", nullable = false)
    private Long articleId;

    @Column(name = "highlight_text", columnDefinition = "TEXT", nullable = false)
    private String highlightText;

    // Vị trí trong bài viết
    @Column(name = "position_info", columnDefinition = "TEXT")
    private String positionInfo;
}