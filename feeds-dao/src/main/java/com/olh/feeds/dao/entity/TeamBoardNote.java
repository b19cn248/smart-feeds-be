// feeds-dao/src/main/java/com/olh/feeds/dao/entity/TeamBoardNote.java
package com.olh.feeds.dao.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "team_board_notes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamBoardNote extends BaseEntity {

    @Column(name = "team_board_id", nullable = false)
    private Long teamBoardId;

    @Column(name = "article_id", nullable = false)
    private Long articleId;

    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "mentioned_users", columnDefinition = "TEXT")
    private String mentionedUsers;
}