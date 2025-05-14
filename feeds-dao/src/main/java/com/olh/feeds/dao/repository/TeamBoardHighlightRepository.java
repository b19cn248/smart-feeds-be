// feeds-dao/src/main/java/com/olh/feeds/dao/repository/TeamBoardHighlightRepository.java
package com.olh.feeds.dao.repository;

import com.olh.feeds.dao.entity.TeamBoardHighlight;
import com.olh.feeds.dto.response.teamboard.TeamBoardHighlightResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TeamBoardHighlightRepository extends JpaRepository<TeamBoardHighlight, Long> {

    @Query("""
        SELECT new com.olh.feeds.dto.response.teamboard.TeamBoardHighlightResponse(
            tbh.id, tbh.teamBoardId, tbh.articleId, tbh.highlightText, tbh.positionInfo,
            tbh.createdBy, u.name, tbh.createdAt
        )
        FROM TeamBoardHighlight tbh
        JOIN User u ON tbh.createdBy = u.email
        WHERE tbh.teamBoardId = :teamBoardId AND tbh.articleId = :articleId AND tbh.isDeleted = false
        ORDER BY tbh.createdAt DESC
        """)
    List<TeamBoardHighlightResponse> findByTeamBoardIdAndArticleId(
            @Param("teamBoardId") Long teamBoardId,
            @Param("articleId") Long articleId
    );

    Optional<TeamBoardHighlight> findByIdAndIsDeletedFalse(Long id);
}