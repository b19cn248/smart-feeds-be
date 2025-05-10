// feeds-dao/src/main/java/com/olh/feeds/dao/entity/BoardArticle.java
package com.olh.feeds.dao.entity;

import jakarta.persistence.*;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "board_articles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BoardArticle extends BaseEntity {

    @Column(name = "board_id", nullable = false)
    private Long boardId;

    @Column(name = "article_id", nullable = false)
    private Long articleId;

    @Column(name = "note")
    private String note;
}