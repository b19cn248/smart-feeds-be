package com.olh.feeds.service;

import com.olh.feeds.dto.response.PageResponse;
import com.olh.feeds.dto.response.article.ArticleResponse;
import org.springframework.data.domain.Pageable;

public interface CategoryService {

    PageResponse<ArticleResponse> getArticlesByCategory(Long categoryId, Pageable pageable);
}
