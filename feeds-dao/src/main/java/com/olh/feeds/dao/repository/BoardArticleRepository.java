// feeds-dao/src/main/java/com/olh/feeds/dao/repository/BoardArticleRepository.java
package com.olh.feeds.dao.repository;

import com.olh.feeds.dao.entity.BoardArticle;
import com.olh.feeds.dto.response.article.ArticleResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface BoardArticleRepository extends JpaRepository<BoardArticle, Long> {

    @Query("""
        SELECT new com.olh.feeds.dto.response.article.ArticleResponse(
            a.id, a.title, a.content, a.contentEncoded, a.isoDate, a.summary,
            a.event, s.url, a.link, a.creator, a.enclosureUrl, a.contentSnippet, a.contentEncodedSnippet
        )
        FROM BoardArticle ba
        JOIN Article a ON ba.articleId = a.id
        LEFT JOIN Source s ON a.sourceId = s.id
        WHERE ba.boardId = :boardId
        AND ba.isDeleted = false
        AND a.isDeleted = false
        ORDER BY ba.createdAt DESC
        """)
    Page<ArticleResponse> findArticlesByBoardId(
            @Param("boardId") Long boardId,
            Pageable pageable
    );

    @Query("""
        SELECT ba
        FROM BoardArticle ba
        WHERE ba.boardId = :boardId
        AND ba.articleId = :articleId
        AND ba.isDeleted = false
        """)
    Optional<BoardArticle> findByBoardIdAndArticleId(
            @Param("boardId") Long boardId,
            @Param("articleId") Long articleId
    );

    boolean existsByBoardIdAndArticleId(Long boardId, Long articleId);
}