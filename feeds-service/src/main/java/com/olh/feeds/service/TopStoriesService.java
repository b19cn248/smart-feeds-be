package com.olh.feeds.service;

import com.olh.feeds.dto.response.PageResponse;
import com.olh.feeds.dto.response.article.ArticleResponse;
import com.olh.feeds.dto.response.explore.TrendingTopicResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface TopStoriesService {

    /**
     * Get top stories (articles marked as top stories)
     * @param pageable Pagination information
     * @return Top story articles
     */
    PageResponse<ArticleResponse> getTopStories(Pageable pageable);

    /**
     * Get trending articles
     * @param pageable Pagination information
     * @return Trending articles
     */
    PageResponse<ArticleResponse> getTrendingArticles(Pageable pageable);

    /**
     * Get trending articles by category
     * @param categoryId Category ID
     * @param pageable Pagination information
     * @return Trending articles in the category
     */
    PageResponse<ArticleResponse> getTrendingArticlesByCategory(Long categoryId, Pageable pageable);

    /**
     * Get trending articles by tag
     * @param tagName Tag name
     * @param pageable Pagination information
     * @return Trending articles with the tag
     */
    PageResponse<ArticleResponse> getTrendingArticlesByTag(String tagName, Pageable pageable);

    /**
     * Get active trending topics
     * @param pageable Pagination information
     * @return Active trending topics
     */
    PageResponse<TrendingTopicResponse> getTrendingTopics(Pageable pageable);

    /**
     * Get active trending topics by category
     * @param categoryId Category ID
     * @param pageable Pagination information
     * @return Active trending topics in the category
     */
    PageResponse<TrendingTopicResponse> getTrendingTopicsByCategory(Long categoryId, Pageable pageable);

    /**
     * Track article view to update stats
     * @param articleId Article ID
     */
    void trackArticleView(Long articleId);

    /**
     * Update article stats by calculating trending scores
     * This should be called periodically by a scheduled job
     */
    void updateArticleStats();
}