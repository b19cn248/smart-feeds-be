package com.olh.feeds.dao.repository;

import com.olh.feeds.dao.entity.SourceCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SourceCategoryRepository extends JpaRepository<SourceCategory, Long> {

    @Query("""
        SELECT CASE WHEN COUNT(sc) > 0 THEN true ELSE false END FROM SourceCategory sc 
        WHERE sc.sourceId = :sourceId 
          AND sc.categoryId = :categoryId 
          AND sc.isDeleted = false
        """)
    boolean existsBySourceIdAndCategoryId(
            @Param("sourceId") Long sourceId,
            @Param("categoryId") Long categoryId
    );
}