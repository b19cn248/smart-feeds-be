package com.olh.feeds.api.controller;

import com.olh.feeds.core.exception.response.ResponseGeneral;
import com.olh.feeds.dto.response.PageResponse;
import com.olh.feeds.dto.response.article.ArticleResponse;
import com.olh.feeds.dto.response.explore.ExploreCollectionResponse;
import com.olh.feeds.dto.response.explore.ExplorePageResponse;
import com.olh.feeds.service.ExploreService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/explore")
@Slf4j
@RequiredArgsConstructor
public class ExploreController {

    private final ExploreService exploreService;

    /**
     * Get collections for explore page
     *
     * @param pageable Pagination information
     * @return List of collections with articles
     */
    @GetMapping("/collections")
    public ResponseGeneral<PageResponse<ExploreCollectionResponse>> getExploreCollections(
            @PageableDefault Pageable pageable
    ) {
        log.info("REST request to get explore collections");
        return ResponseGeneral.of(
                HttpStatus.OK.value(),
                "explore.collections.success",
                exploreService.getExploreCollections(pageable)
        );
    }

    /**
     * Get articles from a specific collection
     *
     * @param collectionId Collection ID
     * @param pageable Pagination information
     * @return Articles from the collection
     */
    @GetMapping("/collections/{collectionId}/articles")
    public ResponseGeneral<PageResponse<ArticleResponse>> getCollectionArticles(
            @PathVariable Long collectionId,
            @PageableDefault Pageable pageable
    ) {
        log.info("REST request to get articles for collection ID: {}", collectionId);
        return ResponseGeneral.of(
                HttpStatus.OK.value(),
                "explore.collection.articles.success",
                exploreService.getArticlesByCollection(collectionId, pageable)
        );
    }

    /**
     * Get articles by category
     *
     * @param categoryId Category ID
     * @param pageable Pagination information
     * @return Articles from the category
     */
    @GetMapping("/categories/{categoryId}/articles")
    public ResponseGeneral<PageResponse<ArticleResponse>> getCategoryArticles(
            @PathVariable Long categoryId,
            @PageableDefault Pageable pageable
    ) {
        log.info("REST request to get articles for category ID: {}", categoryId);
        return ResponseGeneral.of(
                HttpStatus.OK.value(),
                "explore.category.articles.success",
                exploreService.getArticlesByCategory(categoryId, pageable)
        );
    }

    /**
     * Get recent articles for explore
     *
     * @param pageable Pagination information
     * @return Recent articles
     */
    @GetMapping("/recent")
    public ResponseGeneral<PageResponse<ArticleResponse>> getRecentArticles(
            @PageableDefault Pageable pageable
    ) {
        log.info("REST request to get recent articles");
        return ResponseGeneral.of(
                HttpStatus.OK.value(),
                "explore.recent.articles.success",
                exploreService.getRecentArticles(pageable)
        );
    }

    /**
     * Search articles with keywords
     *
     * @param keyword Keyword to search
     * @param pageable Pagination information
     * @return Matching articles
     */
    @GetMapping("/search")
    public ResponseGeneral<PageResponse<ArticleResponse>> searchArticles(
            @RequestParam String keyword,
            @PageableDefault Pageable pageable
    ) {
        log.info("REST request to search articles with keyword: {}", keyword);
        return ResponseGeneral.of(
                HttpStatus.OK.value(),
                "explore.search.articles.success",
                exploreService.searchArticles(keyword, pageable)
        );
    }

    /**
     * Get the explore page with collections, top stories, and trending topics
     *
     * @param collectionSize Number of collections to include (default: 5)
     * @param articlesPerCollection Number of articles per collection (default: 5)
     * @param topStoriesSize Number of top stories to include (default: 10)
     * @param trendingTopicsSize Number of trending topics to include (default: 10)
     * @return Explore page data
     */
    @GetMapping
    public ResponseGeneral<ExplorePageResponse> getExplorePage(
            @RequestParam(defaultValue = "5") int collectionSize,
            @RequestParam(defaultValue = "5") int articlesPerCollection,
            @RequestParam(defaultValue = "10") int topStoriesSize,
            @RequestParam(defaultValue = "10") int trendingTopicsSize
    ) {
        log.info("REST request to get explore page");
        return ResponseGeneral.of(
                HttpStatus.OK.value(),
                "explore.page.success",
                exploreService.getExplorePage(
                        collectionSize,
                        articlesPerCollection,
                        topStoriesSize,
                        trendingTopicsSize
                )
        );
    }
}