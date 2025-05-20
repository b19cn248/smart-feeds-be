package com.olh.feeds.dao.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "source_categories")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SourceCategory extends BaseEntity {

    @Column(name = "source_id", nullable = false)
    private Long sourceId;

    @Column(name = "category_id", nullable = false)
    private Long categoryId;
}