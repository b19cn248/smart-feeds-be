// feeds-dao/src/main/java/com/olh/feeds/dao/entity/TeamBoardUser.java
package com.olh.feeds.dao.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "team_board_users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamBoardUser extends BaseEntity {

    @Column(name = "team_board_id", nullable = false)
    private Long teamBoardId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    // VIEW (chỉ xem), EDIT (thêm, sửa bài viết), ADMIN (quản lý board)
    @Column(name = "permission", nullable = false)
    private String permission;
}