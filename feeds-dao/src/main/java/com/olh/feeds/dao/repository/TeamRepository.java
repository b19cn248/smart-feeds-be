// feeds-dao/src/main/java/com/olh/feeds/dao/repository/TeamRepository.java
package com.olh.feeds.dao.repository;

import com.olh.feeds.dao.entity.Team;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TeamRepository extends JpaRepository<Team, Long> {

    @Query("""
        SELECT t
        FROM Team t
        WHERE t.id = :id AND t.isDeleted = false
        """)
    Optional<Team> findActiveById(@Param("id") Long id);

    @Query("""
        SELECT t
        FROM Team t
        WHERE t.enterpriseId = :enterpriseId AND t.isDeleted = false
        ORDER BY t.name
        """)
    List<Team> findByEnterpriseId(@Param("enterpriseId") Long enterpriseId);

    @Query("""
        SELECT t
        FROM Team t
        JOIN TeamUser tu ON t.id = tu.teamId
        WHERE tu.userId = :userId 
            AND t.isDeleted = false 
            AND tu.isDeleted = false
        ORDER BY t.name
        """)
    Page<Team> findByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("""
        SELECT COUNT(t) > 0
        FROM Team t
        WHERE t.id = :id
            AND t.isDeleted = false
        """)
    boolean existsActiveById(@Param("id") Long id);
}