// feeds-dao/src/main/java/com/olh/feeds/dao/entity/TeamBoard.java
package com.olh.feeds.dao.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "team_boards")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamBoard extends BaseEntity {

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "team_id", nullable = false)
    private Long teamId;
}