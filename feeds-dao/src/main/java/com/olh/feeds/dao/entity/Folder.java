// feeds-dao/src/main/java/com/olh/feeds/dao/entity/Folder.java
package com.olh.feeds.dao.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "folders")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Folder extends BaseEntity {

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "theme")
    private String theme;

    @Column(name = "user_id")
    private Long userId;
}