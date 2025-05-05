// ArticleRepository.java - thêm methods
package com.olh.feeds.dao.repository;

import com.olh.feeds.dao.entity.Article;
import com.olh.feeds.dto.response.article.ArticleResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ArticleRepository extends JpaRepository<Article, Long> {

    @Query("select new com.olh.feeds.dto.response.article.ArticleResponse" +
            "(a.id, a.title, a.content, a.isoDate, a.summary, a.event, s.url, a.link, a.creator, a.enclosureUrl) " +
            "from Article a join Source s on a.sourceId = s.id ")
    Page<ArticleResponse> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Optional<Article> findByGuidOrLink(String guid, String link);
}