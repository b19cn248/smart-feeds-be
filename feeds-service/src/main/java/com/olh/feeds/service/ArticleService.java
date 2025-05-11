package com.olh.feeds.service;

import com.olh.feeds.dto.response.PageResponse;
import com.olh.feeds.dto.response.article.ArticleResponse;
import com.olh.feeds.dto.response.article.SourceArticlesResponse;
import org.springframework.data.domain.Pageable;

public interface ArticleService {
    PageResponse<ArticleResponse> getAllArticles(Pageable pageable);

    PageResponse<SourceArticlesResponse> getArticlesByUserSources(Pageable pageable);

    boolean checkArticleExists(String guid, String link);

    ArticleResponse getArticleById(Long articleId);
}
