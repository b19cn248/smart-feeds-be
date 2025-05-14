// feeds-dao/src/main/java/com/olh/feeds/dao/repository/TeamUserRepository.java
package com.olh.feeds.dao.repository;

import com.olh.feeds.dao.entity.TeamUser;
import com.olh.feeds.dto.response.team.TeamMemberResponse;
import com.olh.feeds.dto.response.teamboard.TeamBoardUserResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TeamUserRepository extends JpaRepository<TeamUser, Long> {

    @Query("""
        SELECT tu
        FROM TeamUser tu
        WHERE tu.teamId = :teamId AND tu.userId = :userId AND tu.isDeleted = false
        """)
    Optional<TeamUser> findByTeamIdAndUserId(
            @Param("teamId") Long teamId,
            @Param("userId") Long userId
    );

    @Query("""
        SELECT tu
        FROM TeamUser tu
        JOIN User u ON tu.userId = u.id
        WHERE tu.teamId = :teamId AND u.email = :email AND tu.isDeleted = false
        """)
    Optional<TeamUser> findByTeamIdAndUserEmail(
            @Param("teamId") Long teamId,
            @Param("email") String email
    );

    @Query("""
        SELECT tu
        FROM TeamUser tu
        WHERE tu.teamId = :teamId AND tu.isDeleted = false
        """)
    List<TeamUser> findByTeamId(@Param("teamId") Long teamId);

    @Query("""
        SELECT tu
        FROM TeamUser tu
        WHERE tu.userId = :userId AND tu.isDeleted = false
        """)
    List<TeamUser> findByUserId(@Param("userId") Long userId);

    @Query("""
        SELECT COUNT(tu) > 0
        FROM TeamUser tu
        JOIN User u ON tu.userId = u.id
        WHERE tu.teamId = :teamId AND u.email = :email AND tu.isDeleted = false
        """)
    boolean existsByTeamIdAndUserEmail(
            @Param("teamId") Long teamId,
            @Param("email") String email
    );

    @Query("""
        SELECT COUNT(tu) > 0
        FROM TeamUser tu
        WHERE tu.teamId = :teamId AND tu.userId = :userId AND tu.role = :role AND tu.isDeleted = false
        """)
    boolean existsByTeamIdAndUserIdAndRole(
            @Param("teamId") Long teamId,
            @Param("userId") Long userId,
            @Param("role") String role
    );

    @Query("""
            SELECT new com.olh.feeds.dto.response.team.TeamMemberResponse(
                tu.id,
                tu.teamId,
                tu.userId,
                u.email,
                u.name,
                tu.role,
                tu.createdAt
            )
            FROM TeamUser tu join User u on tu.userId = u.id where tu.teamId = :teamId AND tu.isDeleted = false
            """)

    List<TeamMemberResponse> getMembersByTeamId(Long teamId);
}