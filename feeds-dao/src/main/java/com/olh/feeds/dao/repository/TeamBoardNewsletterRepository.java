// feeds-dao/src/main/java/com/olh/feeds/dao/repository/TeamBoardNewsletterRepository.java
package com.olh.feeds.dao.repository;

import com.olh.feeds.dao.entity.TeamBoardNewsletter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TeamBoardNewsletterRepository extends JpaRepository<TeamBoardNewsletter, Long> {

    @Query("""
        SELECT tbn
        FROM TeamBoardNewsletter tbn
        WHERE tbn.teamBoardId = :teamBoardId AND tbn.isDeleted = false
        ORDER BY tbn.createdAt DESC
        """)
    List<TeamBoardNewsletter> findByTeamBoardId(@Param("teamBoardId") Long teamBoardId);

    @Query("""
        SELECT tbn
        FROM TeamBoardNewsletter tbn
        WHERE tbn.nextRunTime <= :now AND tbn.isActive = true AND tbn.isDeleted = false
        ORDER BY tbn.nextRunTime
        """)
    List<TeamBoardNewsletter> findNewslettersDueToSend(@Param("now") LocalDateTime now);

    Optional<TeamBoardNewsletter> findByIdAndIsDeletedFalse(Long id);
}