package com.olh.feeds.dao.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "explore_collections")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExploreCollection extends BaseEntity {

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "type", nullable = false)
    private String type; // TRENDING, CATEGORY, CURATED

    @Column(name = "priority", nullable = false)
    private Integer priority = 0;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
}