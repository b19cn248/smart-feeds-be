package com.olh.feeds.dao.repository;

import com.olh.feeds.dao.entity.Article;
import com.olh.feeds.dto.response.article.ArticleResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ArticleRepository extends JpaRepository<Article, Long> {

    @Query("select new com.olh.feeds.dto.response.article.ArticleResponse" +
            "(a.id, a.title, a.content, a.publishDate, a.summary, a.event) from Article a")
    Page<ArticleResponse> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
