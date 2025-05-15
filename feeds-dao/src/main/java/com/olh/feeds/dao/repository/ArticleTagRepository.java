package com.olh.feeds.dao.repository;

import com.olh.feeds.dao.entity.ArticleTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ArticleTagRepository extends JpaRepository<ArticleTag, Long> {

  @Query("SELECT at FROM ArticleTag at WHERE at.articleId = :articleId AND at.isDeleted = false")
  List<ArticleTag> findByArticleId(@Param("articleId") Long articleId);

  @Query("SELECT CASE WHEN COUNT(at) > 0 THEN true ELSE false END FROM ArticleTag at " +
          "WHERE at.articleId = :articleId AND at.tagId = :tagId AND at.isDeleted = false")
  boolean existsByArticleIdAndTagId(@Param("articleId") Long articleId, @Param("tagId") Long tagId);
}