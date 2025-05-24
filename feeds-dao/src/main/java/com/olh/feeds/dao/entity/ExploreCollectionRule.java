package com.olh.feeds.dao.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "explore_collection_rules")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExploreCollectionRule extends BaseEntity {

    @Column(name = "collection_id", nullable = false)
    private Long collectionId;

    @Column(name = "rule_type", nullable = false)
    private String ruleType; // CATEGORY, TAG, SOURCE, KEYWORD

    @Column(name = "rule_value", nullable = false)
    private String ruleValue;

    @Column(name = "priority", nullable = false)
    private Integer priority = 0;
}