// feeds-dao/src/main/java/com/olh/feeds/dao/repository/TeamBoardRepository.java
package com.olh.feeds.dao.repository;

import com.olh.feeds.dao.entity.TeamBoard;
import com.olh.feeds.dto.response.teamboard.TeamBoardResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TeamBoardRepository extends JpaRepository<TeamBoard, Long> {

    @Query("""
        SELECT new com.olh.feeds.dto.response.teamboard.TeamBoardResponse(
            tb.id, tb.name, tb.description, tb.teamId, t.name, tb.createdAt, tb.createdBy
        )
        FROM TeamBoard tb
        JOIN Team t ON tb.teamId = t.id
        WHERE tb.id = :id AND tb.isDeleted = false
        """)
    Optional<TeamBoardResponse> findTeamBoardById(@Param("id") Long id);

    @Query("""
        SELECT new com.olh.feeds.dto.response.teamboard.TeamBoardResponse(
            tb.id, tb.name, tb.description, tb.teamId, t.name, tb.createdAt, tb.createdBy
        )
        FROM TeamBoard tb
        JOIN Team t ON tb.teamId = t.id
        WHERE tb.teamId = :teamId AND tb.isDeleted = false
        ORDER BY tb.createdAt DESC
        """)
    Page<TeamBoardResponse> findByTeamId(@Param("teamId") Long teamId, Pageable pageable);

    @Query("""
        SELECT new com.olh.feeds.dto.response.teamboard.TeamBoardResponse(
            tb.id, tb.name, tb.description, tb.teamId, t.name, tb.createdAt, tb.createdBy
        )
        FROM TeamBoard tb
        JOIN Team t ON tb.teamId = t.id
        JOIN TeamUser tu ON t.id = tu.teamId
        WHERE tu.userId = :userId AND tb.isDeleted = false AND tu.isDeleted = false
        ORDER BY tb.createdAt DESC
        """)
    Page<TeamBoardResponse> findByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("""
        SELECT new com.olh.feeds.dto.response.teamboard.TeamBoardResponse(
            tb.id, tb.name, tb.description, tb.teamId, t.name, tb.createdAt, tb.createdBy
        )
        FROM TeamBoard tb
        JOIN Team t ON tb.teamId = t.id
        JOIN TeamBoardUser tbu ON tb.id = tbu.teamBoardId
        WHERE tbu.userId = :userId AND tb.isDeleted = false AND tbu.isDeleted = false
        ORDER BY tb.createdAt DESC
        """)
    Page<TeamBoardResponse> findByBoardUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("""
        SELECT tb
        FROM TeamBoard tb
        WHERE tb.id = :id AND tb.isDeleted = false
        """)
    Optional<TeamBoard> findActiveById(@Param("id") Long id);

    /**
     * Find all team boards by team ID without pagination for internal sync operations
     * Performance optimized for bulk operations
     */
    @Query("""
        SELECT tb
        FROM TeamBoard tb
        WHERE tb.teamId = :teamId AND tb.isDeleted = false
        ORDER BY tb.id
        """)
    List<TeamBoard> findByTeamIdWithoutPagination(@Param("teamId") Long teamId);
}