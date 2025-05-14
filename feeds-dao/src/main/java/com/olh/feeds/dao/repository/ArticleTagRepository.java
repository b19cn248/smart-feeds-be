package com.olh.feeds.dao.repository;

import com.olh.feeds.dao.entity.ArticleTag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ArticleTagRepository extends JpaRepository<ArticleTag, Long> {
  List<ArticleTag> findByArticleId(Long articleId);
}