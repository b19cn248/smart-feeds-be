package com.olh.feeds.dao.repository;

import com.olh.feeds.dao.entity.Article;
import com.olh.feeds.dao.entity.Source;
import com.olh.feeds.dto.response.article.ArticleResponse;
import com.olh.feeds.dto.response.source.SourceResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ArticleRepository extends JpaRepository<Article, Long> {

    @Query("select new com.olh.feeds.dto.response.article.ArticleResponse" +
            "(a.id, a.title, a.content, a.contentEncoded, a.isoDate, a.summary," +
            " a.event, s.url, a.link, a.creator, a.enclosureUrl, a.contentSnippet, a.contentEncodedSnippet) " +
            "from Article a join Source s on a.sourceId = s.id and a.isDeleted = false " )
    Page<ArticleResponse> findAllByOrderByCreatedAtDesc(Pageable pageable);

    @Query("select new com.olh.feeds.dto.response.article.ArticleResponse" +
            "(a.id, a.title, a.content, a.contentEncoded, a.isoDate, a.summary," +
            " a.event, s.url, a.link, a.creator, a.enclosureUrl, a.contentSnippet, a.contentEncodedSnippet) " +
            "from Article a join Source s on a.sourceId = s.id where a.id = :id and a.isDeleted = false")
    Optional<ArticleResponse> findArticleById(Long id);

    @Query("SELECT a FROM Article a WHERE a.guid = :guid AND a.isDeleted = false")
    Optional<Article> findByGuid(String guid);

    @Query("SELECT s FROM Source s WHERE s.url = :url and s.isDeleted = false")
    List<Source> findAllByUrl(@Param("url") String url);

    @Query("SELECT new com.olh.feeds.dto.response.source.SourceResponse(s.id, s.url, s.type, s.active) " +
            "FROM Source s WHERE s.id = :id AND s.isDeleted = false")
    SourceResponse findSourceById(@Param("id") Long id);

    @Query("SELECT a FROM Article a WHERE a.link = :link AND a.isDeleted = false")
    Optional<Article> findByLink(String link);

    @Query("select new com.olh.feeds.dto.response.article.ArticleResponse" +
            "(a.id, a.title, a.content, a.contentEncoded, a.isoDate, a.summary," +
            " a.event, s.url, a.link, a.creator, a.enclosureUrl, a.contentSnippet, a.contentEncodedSnippet) " +
            " from Article a join Source s on a.sourceId = s.id " +
            " where s.id = :sourceId and a.isDeleted = false" +
            " order by a.createdAt desc")
    List<ArticleResponse> findBySourceId(@Param("sourceId") Long sourceId, Pageable pageable);

    @Query("select distinct a.sourceId from Article a " +
            "join Source s on a.sourceId = s.id " +
            "join FolderSource fs on fs.sourceId = s.id " +
            "join Folder f on fs.folderId = f.id " +
            "where f.createdBy = :username " +
            "and f.isDeleted = false " +
            "and fs.isDeleted = false " +
            "and s.isDeleted = false " +
            "and s.active = true and a.isDeleted = false")
    List<Long> findSourceIdsByUsername(@Param("username") String username);

    @Query("""
            SELECT COUNT(a)
            FROM Article a
            WHERE a.sourceId IN :sourceIds
            AND a.isDeleted = false
            """)
    int countBySourceIdIn(@Param("sourceIds") List<Long> sourceIds);

    @Query("""
            SELECT new com.olh.feeds.dto.response.article.ArticleResponse(
                a.id, a.title, a.content, a.contentEncoded, a.isoDate, a.summary,
                a.event, s.url, a.link, a.creator, a.enclosureUrl, a.contentSnippet, a.contentEncodedSnippet
            )
            FROM Article a
            JOIN Source s ON a.sourceId = s.id
            WHERE a.sourceId IN :sourceIds
            AND a.isDeleted = false
            ORDER BY a.pubDate DESC NULLS LAST, a.createdAt DESC
            """)
    List<ArticleResponse> findBySourceIdInOrderByPublishDateDesc(
            @Param("sourceIds") List<Long> sourceIds,
            Pageable pageable);
}