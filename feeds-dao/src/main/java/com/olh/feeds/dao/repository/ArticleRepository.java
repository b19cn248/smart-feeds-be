// ArticleRepository.java - thêm methods
package com.olh.feeds.dao.repository;

import com.olh.feeds.dao.entity.Article;
import com.olh.feeds.dto.response.article.ArticleResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ArticleRepository extends JpaRepository<Article, Long> {

    @Query("select new com.olh.feeds.dto.response.article.ArticleResponse" +
            "(a.id, a.title, a.content, a.isoDate, a.summary, a.event) from Article a")
    Page<ArticleResponse> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Optional<Article> findByGuid(String guid);

    @Query("SELECT a FROM Article a WHERE a.guid IN :guids")
    List<Article> findByGuidIn(@Param("guids") List<String> guids);

    boolean existsByGuid(String guid);
}