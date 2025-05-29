package com.olh.feeds.dao.repository;

import com.olh.feeds.dao.entity.ExploreCuratedArticle;
import com.olh.feeds.dto.response.article.ArticleResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ExploreCuratedArticleRepository extends JpaRepository<ExploreCuratedArticle, Long> {

    @Query("""
        SELECT new com.olh.feeds.dto.response.article.ArticleResponse(
            a.id, a.title, a.content, a.contentEncoded, a.isoDate, a.summary,
            a.event, s.url, a.link, a.creator, a.enclosureUrl, a.contentSnippet, a.contentEncodedSnippet
        )
        FROM ExploreCuratedArticle eca
        JOIN Article a ON eca.articleId = a.id
        JOIN Source s ON a.sourceId = s.id
        WHERE eca.collectionId = :collectionId
        AND eca.isDeleted = false
        AND a.isDeleted = false
        ORDER BY eca.priority DESC
        """)
    Page<ArticleResponse> findArticlesByCollectionId(
            @Param("collectionId") Long collectionId,
            Pageable pageable);

    @Query("""
        SELECT eca FROM ExploreCuratedArticle eca
        WHERE eca.collectionId = :collectionId
        AND eca.articleId = :articleId
        AND eca.isDeleted = false
        """)
    Optional<ExploreCuratedArticle> findByCollectionIdAndArticleId(
            @Param("collectionId") Long collectionId,
            @Param("articleId") Long articleId);

    @Query("""
        SELECT eca FROM ExploreCuratedArticle eca
        WHERE eca.collectionId = :collectionId
        AND eca.isDeleted = false
        ORDER BY eca.priority DESC
        """)
    List<ExploreCuratedArticle> findAllByCollectionId(@Param("collectionId") Long collectionId);
}