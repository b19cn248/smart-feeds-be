package com.olh.feeds.service;

import com.olh.feeds.dto.response.PageResponse;
import com.olh.feeds.dto.response.article.ArticleResponse;
import com.olh.feeds.dto.response.category.CategoryResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CategoryService {

    PageResponse<ArticleResponse> getArticlesByCategory(Long categoryId, Pageable pageable);

    List<CategoryResponse> getAllCategories();
}
