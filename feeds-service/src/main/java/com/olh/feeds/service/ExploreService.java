package com.olh.feeds.service;

import com.olh.feeds.dto.response.PageResponse;
import com.olh.feeds.dto.response.article.ArticleResponse;
import com.olh.feeds.dto.response.explore.ExploreCollectionResponse;
import com.olh.feeds.dto.response.explore.ExplorePageResponse;
import org.springframework.data.domain.Pageable;

public interface ExploreService {

    /**
     * Get collections for explore page
     * @param pageable Pagination information
     * @return List of collections with articles
     */
    PageResponse<ExploreCollectionResponse> getExploreCollections(Pageable pageable);

    /**
     * Get articles from a specific collection
     * @param collectionId Collection ID
     * @param pageable Pagination information
     * @return Articles from the collection
     */
    PageResponse<ArticleResponse> getArticlesByCollection(Long collectionId, Pageable pageable);

    /**
     * Get articles by category
     * @param categoryId Category ID
     * @param pageable Pagination information
     * @return Articles from the category
     */
    PageResponse<ArticleResponse> getArticlesByCategory(Long categoryId, Pageable pageable);

    /**
     * Get recent articles for explore
     * @param pageable Pagination information
     * @return Recent articles
     */
    PageResponse<ArticleResponse> getRecentArticles(Pageable pageable);

    /**
     * Search articles with keywords
     * @param keyword Keyword to search
     * @param pageable Pagination information
     * @return Matching articles
     */
    PageResponse<ArticleResponse> searchArticles(String keyword, Pageable pageable);

    /**
     * Get the explore page with collections, top stories, and trending topics
     * @param collectionSize Number of collections to include
     * @param articlesPerCollection Number of articles per collection
     * @param topStoriesSize Number of top stories to include
     * @param trendingTopicsSize Number of trending topics to include
     * @return Explore page data
     */
    ExplorePageResponse getExplorePage(
            int collectionSize,
            int articlesPerCollection,
            int topStoriesSize,
            int trendingTopicsSize);
}