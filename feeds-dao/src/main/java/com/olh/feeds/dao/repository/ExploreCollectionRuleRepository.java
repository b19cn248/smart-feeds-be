package com.olh.feeds.dao.repository;

import com.olh.feeds.dao.entity.ExploreCollectionRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ExploreCollectionRuleRepository extends JpaRepository<ExploreCollectionRule, Long> {

    @Query("""
        SELECT ecr FROM ExploreCollectionRule ecr
        WHERE ecr.collectionId = :collectionId
        AND ecr.isDeleted = false
        ORDER BY ecr.priority DESC
        """)
    List<ExploreCollectionRule> findByCollectionId(@Param("collectionId") Long collectionId);

    @Query("""
        SELECT ecr FROM ExploreCollectionRule ecr
        WHERE ecr.collectionId = :collectionId
        AND ecr.ruleType = :ruleType
        AND ecr.isDeleted = false
        ORDER BY ecr.priority DESC
        """)
    List<ExploreCollectionRule> findByCollectionIdAndRuleType(
            @Param("collectionId") Long collectionId,
            @Param("ruleType") String ruleType);
}