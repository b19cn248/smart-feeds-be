package com.olh.feeds.dao.repository;

import com.olh.feeds.dao.entity.ArticleStat;
import com.olh.feeds.dto.response.article.ArticleResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ArticleStatRepository extends JpaRepository<ArticleStat, Long> {

    Optional<ArticleStat> findByArticleId(Long articleId);

    @Query("""
        SELECT new com.olh.feeds.dto.response.article.ArticleResponse(
            a.id, a.title, a.content, a.contentEncoded, a.isoDate, a.summary,
            a.event, s.url, a.link, a.creator, a.enclosureUrl, a.contentSnippet, a.contentEncodedSnippet
        )
        FROM ArticleStat ast
        JOIN Article a ON ast.articleId = a.id
        JOIN Source s ON a.sourceId = s.id
        WHERE ast.isTopStory = true
        AND a.isDeleted = false
        AND s.isDeleted = false
        ORDER BY ast.trendingScore DESC
        """)
    Page<ArticleResponse> findTopStories(Pageable pageable);

    @Query("""
        SELECT new com.olh.feeds.dto.response.article.ArticleResponse(
            a.id, a.title, a.content, a.contentEncoded, a.isoDate, a.summary,
            a.event, s.url, a.link, a.creator, a.enclosureUrl, a.contentSnippet, a.contentEncodedSnippet
        )
        FROM ArticleStat ast
        JOIN Article a ON ast.articleId = a.id
        JOIN Source s ON a.sourceId = s.id
        WHERE a.isDeleted = false
        AND s.isDeleted = false
        ORDER BY ast.trendingScore DESC
        """)
    Page<ArticleResponse> findTrendingArticles(Pageable pageable);

    @Query("""
        SELECT new com.olh.feeds.dto.response.article.ArticleResponse(
            a.id, a.title, a.content, a.contentEncoded, a.isoDate, a.summary,
            a.event, s.url, a.link, a.creator, a.enclosureUrl, a.contentSnippet, a.contentEncodedSnippet
        )
        FROM ArticleStat ast
        JOIN Article a ON ast.articleId = a.id
        JOIN Source s ON a.sourceId = s.id
        JOIN SourceCategory sc ON s.id = sc.sourceId
        WHERE sc.categoryId = :categoryId
        AND a.isDeleted = false
        AND s.isDeleted = false
        AND sc.isDeleted = false
        ORDER BY ast.trendingScore DESC
        """)
    Page<ArticleResponse> findTrendingArticlesByCategory(@Param("categoryId") Long categoryId, Pageable pageable);

    @Query("""
        SELECT new com.olh.feeds.dto.response.article.ArticleResponse(
            a.id, a.title, a.content, a.contentEncoded, a.isoDate, a.summary,
            a.event, s.url, a.link, a.creator, a.enclosureUrl, a.contentSnippet, a.contentEncodedSnippet
        )
        FROM ArticleStat ast
        JOIN Article a ON ast.articleId = a.id
        JOIN Source s ON a.sourceId = s.id
        JOIN ArticleTag at ON a.id = at.articleId
        JOIN Tag t ON at.tagId = t.id
        WHERE t.name = :tagName
        AND a.isDeleted = false
        AND s.isDeleted = false
        AND at.isDeleted = false
        AND t.isDeleted = false
        ORDER BY ast.trendingScore DESC
        """)
    Page<ArticleResponse> findTrendingArticlesByTag(@Param("tagName") String tagName, Pageable pageable);

    @Query("""
        SELECT DISTINCT a.id
        FROM Article a
        WHERE a.isDeleted = false
        AND NOT EXISTS (
            SELECT 1 FROM ArticleStat ast WHERE ast.articleId = a.id
        )
        """)
    List<Long> findArticlesWithoutStats(Pageable pageable);
}