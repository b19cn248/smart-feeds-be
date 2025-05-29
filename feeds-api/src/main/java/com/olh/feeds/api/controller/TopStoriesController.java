package com.olh.feeds.api.controller;

import com.olh.feeds.core.exception.response.ResponseGeneral;
import com.olh.feeds.dto.response.PageResponse;
import com.olh.feeds.dto.response.article.ArticleResponse;
import com.olh.feeds.dto.response.explore.TrendingTopicResponse;
import com.olh.feeds.service.TopStoriesService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/top-stories")
@Slf4j
@RequiredArgsConstructor
public class TopStoriesController {

    private final TopStoriesService topStoriesService;

    /**
     * Get top stories
     *
     * @param pageable Pagination information
     * @return Top story articles
     */
    @GetMapping
    public ResponseGeneral<PageResponse<ArticleResponse>> getTopStories(
            @PageableDefault Pageable pageable
    ) {
        log.info("REST request to get top stories");
        return ResponseGeneral.of(
                HttpStatus.OK.value(),
                "top.stories.success",
                topStoriesService.getTopStories(pageable)
        );
    }

    /**
     * Get trending articles
     *
     * @param pageable Pagination information
     * @return Trending articles
     */
    @GetMapping("/trending")
    public ResponseGeneral<PageResponse<ArticleResponse>> getTrendingArticles(
            @PageableDefault Pageable pageable
    ) {
        log.info("REST request to get trending articles");
        return ResponseGeneral.of(
                HttpStatus.OK.value(),
                "top.stories.trending.success",
                topStoriesService.getTrendingArticles(pageable)
        );
    }

    /**
     * Get trending articles by category
     *
     * @param categoryId Category ID
     * @param pageable Pagination information
     * @return Trending articles in the category
     */
    @GetMapping("/trending/categories/{categoryId}")
    public ResponseGeneral<PageResponse<ArticleResponse>> getTrendingArticlesByCategory(
            @PathVariable Long categoryId,
            @PageableDefault Pageable pageable
    ) {
        log.info("REST request to get trending articles for category ID: {}", categoryId);
        return ResponseGeneral.of(
                HttpStatus.OK.value(),
                "top.stories.trending.category.success",
                topStoriesService.getTrendingArticlesByCategory(categoryId, pageable)
        );
    }

    /**
     * Get trending articles by tag
     *
     * @param tagName Tag name
     * @param pageable Pagination information
     * @return Trending articles with the tag
     */
    @GetMapping("/trending/tags/{tagName}")
    public ResponseGeneral<PageResponse<ArticleResponse>> getTrendingArticlesByTag(
            @PathVariable String tagName,
            @PageableDefault Pageable pageable
    ) {
        log.info("REST request to get trending articles for tag: {}", tagName);
        return ResponseGeneral.of(
                HttpStatus.OK.value(),
                "top.stories.trending.tag.success",
                topStoriesService.getTrendingArticlesByTag(tagName, pageable)
        );
    }

    /**
     * Get active trending topics
     *
     * @param pageable Pagination information
     * @return Active trending topics
     */
    @GetMapping("/trending-topics")
    public ResponseGeneral<PageResponse<TrendingTopicResponse>> getTrendingTopics(
            @PageableDefault Pageable pageable
    ) {
        log.info("REST request to get trending topics");
        return ResponseGeneral.of(
                HttpStatus.OK.value(),
                "top.stories.trending.topics.success",
                topStoriesService.getTrendingTopics(pageable)
        );
    }

    /**
     * Get active trending topics by category
     *
     * @param categoryId Category ID
     * @param pageable Pagination information
     * @return Active trending topics in the category
     */
    @GetMapping("/trending-topics/categories/{categoryId}")
    public ResponseGeneral<PageResponse<TrendingTopicResponse>> getTrendingTopicsByCategory(
            @PathVariable Long categoryId,
            @PageableDefault Pageable pageable
    ) {
        log.info("REST request to get trending topics for category ID: {}", categoryId);
        return ResponseGeneral.of(
                HttpStatus.OK.value(),
                "top.stories.trending.topics.category.success",
                topStoriesService.getTrendingTopicsByCategory(categoryId, pageable)
        );
    }

    /**
     * Track article view to update stats
     *
     * @param articleId Article ID
     * @return Success response
     */
    @PostMapping("/track/view/{articleId}")
    public ResponseGeneral<Void> trackArticleView(
            @PathVariable Long articleId
    ) {
        log.info("REST request to track view for article ID: {}", articleId);
        topStoriesService.trackArticleView(articleId);
        return ResponseGeneral.of(
                HttpStatus.OK.value(),
                "top.stories.track.view.success"
        );
    }
}