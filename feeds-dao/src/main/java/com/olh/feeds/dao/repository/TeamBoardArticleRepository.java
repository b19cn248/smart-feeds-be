// feeds-dao/src/main/java/com/olh/feeds/dao/repository/TeamBoardArticleRepository.java
package com.olh.feeds.dao.repository;

import com.olh.feeds.dao.entity.TeamBoardArticle;
import com.olh.feeds.dto.response.article.ArticleResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TeamBoardArticleRepository extends JpaRepository<TeamBoardArticle, Long> {

    @Query("""
        SELECT new com.olh.feeds.dto.response.article.ArticleResponse(
            a.id, a.title, a.content, a.contentEncoded, a.isoDate, a.summary,
            a.event, s.url, a.link, a.creator, a.enclosureUrl, a.contentSnippet, a.contentEncodedSnippet
        )
        FROM TeamBoardArticle tba
        JOIN Article a ON tba.articleId = a.id
        LEFT JOIN Source s ON a.sourceId = s.id
        WHERE tba.teamBoardId = :teamBoardId
        AND tba.isDeleted = false
        AND a.isDeleted = false
        ORDER BY tba.createdAt DESC
        """)
    Page<ArticleResponse> findArticlesByTeamBoardId(
            @Param("teamBoardId") Long teamBoardId,
            Pageable pageable
    );

    @Query("""
        SELECT tba
        FROM TeamBoardArticle tba
        WHERE tba.teamBoardId = :teamBoardId AND tba.articleId = :articleId AND tba.isDeleted = false
        """)
    Optional<TeamBoardArticle> findByTeamBoardIdAndArticleId(
            @Param("teamBoardId") Long teamBoardId,
            @Param("articleId") Long articleId
    );

    boolean existsByTeamBoardIdAndArticleIdAndIsDeletedFalse(Long teamBoardId, Long articleId);
}