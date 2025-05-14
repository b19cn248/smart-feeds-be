// feeds-dao/src/main/java/com/olh/feeds/dao/entity/TeamUser.java
package com.olh.feeds.dao.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "team_users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamUser extends BaseEntity {

    @Column(name = "team_id", nullable = false)
    private Long teamId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "role", nullable = false)
    private String role; // MEMBER, ADMIN
}