package com.olh.feeds.dao.repository;

import com.olh.feeds.dao.entity.Category;
import com.olh.feeds.dto.response.article.ArticleResponse;
import com.olh.feeds.dto.response.category.CategoryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    @Query("""
            SELECT c FROM Category c
            WHERE c.name = :name AND c.isDeleted = false
            """)
    Optional<Category> findByName(@Param("name") String name);

    @Query("""
            SELECT c FROM Category c
            WHERE c.name IN :names AND c.isDeleted = false
            """)
    List<Category> findAllByNameIn(@Param("names") List<String> names);

    @Query("""
            SELECT new com.olh.feeds.dto.response.article.ArticleResponse(
                a.id, a.title, a.content, a.contentEncoded, a.pubDate, a.summary,
                a.event, s.url, a.link, a.creator, a.enclosureUrl, a.contentSnippet, a.contentEncodedSnippet
            ) from Category c join SourceCategory  sc on c.id = sc.categoryId
            join Source s on sc.sourceId = s.id
            join Article a on a.sourceId = s.id
            where c.id = :categoryId and a.isDeleted = false
            order by a.pubDate
            """)
    Page<ArticleResponse> getArticlesByCategoryId(
            @Param("categoryId") Long categoryId,
            Pageable pageable
    );

    @Query("""
             SELECT new com.olh.feeds.dto.response.category.CategoryResponse(
                 c.id, c.name, c.description
             ) FROM Category c
             WHERE c.isDeleted = false
            """)
    List<CategoryResponse> getAllCategories();
}