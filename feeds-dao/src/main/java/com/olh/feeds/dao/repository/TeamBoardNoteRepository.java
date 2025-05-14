// feeds-dao/src/main/java/com/olh/feeds/dao/repository/TeamBoardNoteRepository.java
package com.olh.feeds.dao.repository;

import com.olh.feeds.dao.entity.TeamBoardNote;
import com.olh.feeds.dto.response.teamboard.TeamBoardNoteResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TeamBoardNoteRepository extends JpaRepository<TeamBoardNote, Long> {

    @Query("""
        SELECT new com.olh.feeds.dto.response.teamboard.TeamBoardNoteResponse(
            tbn.id, tbn.teamBoardId, tbn.articleId, tbn.content, tbn.mentionedUsers,
            tbn.createdBy, u.name, tbn.createdAt
        )
        FROM TeamBoardNote tbn
        JOIN User u ON tbn.createdBy = u.email
        WHERE tbn.teamBoardId = :teamBoardId AND tbn.articleId = :articleId AND tbn.isDeleted = false
        ORDER BY tbn.createdAt DESC
        """)
    List<TeamBoardNoteResponse> findByTeamBoardIdAndArticleId(
            @Param("teamBoardId") Long teamBoardId,
            @Param("articleId") Long articleId
    );

    Optional<TeamBoardNote> findByIdAndIsDeletedFalse(Long id);
}