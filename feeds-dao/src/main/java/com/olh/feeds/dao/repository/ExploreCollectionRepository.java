package com.olh.feeds.dao.repository;

import com.olh.feeds.dao.entity.ExploreCollection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ExploreCollectionRepository extends JpaRepository<ExploreCollection, Long> {

    @Query("""
        SELECT ec FROM ExploreCollection ec
        WHERE ec.isActive = true
        AND ec.isDeleted = false
        ORDER BY ec.priority DESC
        """)
    List<ExploreCollection> findActiveCollections();

    @Query("""
        SELECT ec FROM ExploreCollection ec
        WHERE ec.type = :type
        AND ec.isActive = true
        AND ec.isDeleted = false
        ORDER BY ec.priority DESC
        """)
    List<ExploreCollection> findActiveCollectionsByType(String type);

    @Query("""
        SELECT ec FROM ExploreCollection ec
        WHERE ec.id = :id
        AND ec.isActive = true
        AND ec.isDeleted = false
        """)
    Optional<ExploreCollection> findActiveById(Long id);

    @Query("""
        SELECT ec FROM ExploreCollection ec
        WHERE ec.isActive = true
        AND ec.isDeleted = false
        ORDER BY ec.priority DESC
        """)
    Page<ExploreCollection> findAllActiveCollections(Pageable pageable);
}