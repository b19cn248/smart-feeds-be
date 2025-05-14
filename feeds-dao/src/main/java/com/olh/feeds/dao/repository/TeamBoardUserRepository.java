// feeds-dao/src/main/java/com/olh/feeds/dao/repository/TeamBoardUserRepository.java
package com.olh.feeds.dao.repository;

import com.olh.feeds.dao.entity.TeamBoardUser;
import com.olh.feeds.dto.response.teamboard.TeamBoardUserResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TeamBoardUserRepository extends JpaRepository<TeamBoardUser, Long> {

    @Query("""
        SELECT new com.olh.feeds.dto.response.teamboard.TeamBoardUserResponse(
            tbu.id, tbu.teamBoardId, tbu.userId, u.email, u.name, tbu.permission, tbu.createdAt
        )
        FROM TeamBoardUser tbu
        JOIN User u ON tbu.userId = u.id
        WHERE tbu.teamBoardId = :teamBoardId AND tbu.isDeleted = false
        ORDER BY tbu.createdAt
        """)
    List<TeamBoardUserResponse> findByTeamBoardId(@Param("teamBoardId") Long teamBoardId);

    @Query("""
        SELECT tbu
        FROM TeamBoardUser tbu
        WHERE tbu.teamBoardId = :teamBoardId AND tbu.userId = :userId AND tbu.isDeleted = false
        """)
    Optional<TeamBoardUser> findByTeamBoardIdAndUserId(
            @Param("teamBoardId") Long teamBoardId,
            @Param("userId") Long userId
    );

    @Query("""
        SELECT COUNT(tbu) > 0
        FROM TeamBoardUser tbu
        WHERE tbu.teamBoardId = :teamBoardId 
          AND tbu.userId = :userId 
          AND tbu.permission IN (:permissions)
          AND tbu.isDeleted = false
        """)
    boolean existsByTeamBoardIdAndUserIdAndPermissionIn(
            @Param("teamBoardId") Long teamBoardId,
            @Param("userId") Long userId,
            @Param("permissions") List<String> permissions
    );

    @Query("""
        SELECT tbu.permission
        FROM TeamBoardUser tbu
        WHERE tbu.teamBoardId = :teamBoardId 
          AND tbu.userId = :userId 
          AND tbu.isDeleted = false
        """)
    Optional<String> findPermissionByTeamBoardIdAndUserId(
            @Param("teamBoardId") Long teamBoardId,
            @Param("userId") Long userId
    );
}