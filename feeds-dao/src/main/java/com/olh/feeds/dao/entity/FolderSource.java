// feeds-dao/src/main/java/com/olh/feeds/dao/entity/FolderSource.java
package com.olh.feeds.dao.entity;

import jakarta.persistence.*;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "folder_sources")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FolderSource extends BaseEntity {

    @Column(name = "folder_id")
    private Long folderId;

    @Column(name = "source_id")
    private Long sourceId;
}