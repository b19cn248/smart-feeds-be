package com.olh.feeds.service.impl;

import com.olh.feeds.core.exception.base.NotFoundException;
import com.olh.feeds.dao.entity.ArticleStat;
import com.olh.feeds.dao.entity.Category;
import com.olh.feeds.dao.entity.TrendingTopic;
import com.olh.feeds.dao.repository.ArticleRepository;
import com.olh.feeds.dao.repository.ArticleStatRepository;
import com.olh.feeds.dao.repository.CategoryRepository;
import com.olh.feeds.dao.repository.TrendingTopicRepository;
import com.olh.feeds.dto.mapper.PageMapper;
import com.olh.feeds.dto.response.PageResponse;
import com.olh.feeds.dto.response.article.ArticleResponse;
import com.olh.feeds.dto.response.explore.TrendingTopicResponse;
import com.olh.feeds.service.TopStoriesService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TopStoriesServiceImpl implements TopStoriesService {

    private final ArticleStatRepository articleStatRepository;
    private final ArticleRepository articleRepository;
    private final TrendingTopicRepository trendingTopicRepository;
    private final CategoryRepository categoryRepository;
    private final PageMapper pageMapper;

    // In-memory cache for tracking views to avoid too many DB updates
    private final Map<Long, Integer> viewCountCache = new ConcurrentHashMap<>();

    // Constants for score calculation
    private static final float VIEW_WEIGHT = 1.0f;
    private static final float SHARE_WEIGHT = 3.0f;
    private static final float SAVE_WEIGHT = 2.0f;
    private static final int TOP_STORIES_THRESHOLD = 50; // Minimum trending score to be a top story

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ArticleResponse> getTopStories(Pageable pageable) {
        log.info("Getting top stories");

        Page<ArticleResponse> storiesPage = articleStatRepository.findTopStories(pageable);

        // Populate hashtags for articles
        populateHashtags(storiesPage.getContent());

        return pageMapper.toPageDto(storiesPage);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ArticleResponse> getTrendingArticles(Pageable pageable) {
        log.info("Getting trending articles");

        Page<ArticleResponse> articlesPage = articleStatRepository.findTrendingArticles(pageable);

        // Populate hashtags for articles
        populateHashtags(articlesPage.getContent());

        return pageMapper.toPageDto(articlesPage);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ArticleResponse> getTrendingArticlesByCategory(Long categoryId, Pageable pageable) {
        log.info("Getting trending articles for category ID: {}", categoryId);

        // Check if category exists
        categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException(categoryId.toString(), "category"));

        Page<ArticleResponse> articlesPage = articleStatRepository.findTrendingArticlesByCategory(categoryId, pageable);

        // Populate hashtags for articles
        populateHashtags(articlesPage.getContent());

        return pageMapper.toPageDto(articlesPage);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ArticleResponse> getTrendingArticlesByTag(String tagName, Pageable pageable) {
        log.info("Getting trending articles for tag: {}", tagName);

        Page<ArticleResponse> articlesPage = articleStatRepository.findTrendingArticlesByTag(tagName, pageable);

        // Populate hashtags for articles
        populateHashtags(articlesPage.getContent());

        return pageMapper.toPageDto(articlesPage);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<TrendingTopicResponse> getTrendingTopics(Pageable pageable) {
        log.info("Getting trending topics");

        LocalDate today = LocalDate.now();
        Page<TrendingTopic> topicsPage = trendingTopicRepository.findActiveTopics(today, pageable);

        List<TrendingTopicResponse> responses = topicsPage.getContent().stream()
                .map(this::mapToTrendingTopicResponse)
                .collect(Collectors.toList());

        Page<TrendingTopicResponse> responsePage = new PageImpl<>(
                responses,
                pageable,
                topicsPage.getTotalElements()
        );

        return pageMapper.toPageDto(responsePage);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<TrendingTopicResponse> getTrendingTopicsByCategory(Long categoryId, Pageable pageable) {
        log.info("Getting trending topics for category ID: {}", categoryId);

        // Check if category exists
        categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException(categoryId.toString(), "category"));

        LocalDate today = LocalDate.now();
        Page<TrendingTopic> topicsPage = trendingTopicRepository.findActiveByCategoryId(
                categoryId, today, pageable);

        List<TrendingTopicResponse> responses = topicsPage.getContent().stream()
                .map(this::mapToTrendingTopicResponse)
                .collect(Collectors.toList());

        Page<TrendingTopicResponse> responsePage = new PageImpl<>(
                responses,
                pageable,
                topicsPage.getTotalElements()
        );

        return pageMapper.toPageDto(responsePage);
    }

    @Override
    @Transactional
    public void trackArticleView(Long articleId) {
        log.debug("Tracking view for article ID: {}", articleId);

        // Add to in-memory cache first
        viewCountCache.compute(articleId, (id, count) -> count == null ? 1 : count + 1);

        // If count reaches 10, update database and reset cache
        Integer currentCount = viewCountCache.get(articleId);
        if (currentCount != null && currentCount >= 10) {
            log.debug("Persisting {} views for article ID: {}", currentCount, articleId);

            // Find or create ArticleStat
            ArticleStat stat = articleStatRepository.findByArticleId(articleId)
                    .orElse(ArticleStat.builder()
                            .articleId(articleId)
                            .viewCount(0)
                            .shareCount(0)
                            .saveCount(0)
                            .engagementScore(0.0f)
                            .trendingScore(0.0f)
                            .isTopStory(false)
                            .build());

            // Update view count
            stat.setViewCount(stat.getViewCount() + currentCount);

            // Recalculate scores
            calculateScores(stat);

            // Save to database
            articleStatRepository.save(stat);

            // Reset cache
            viewCountCache.remove(articleId);
        }
    }

    @Override
    @Transactional
    @Scheduled(fixedRate = 3600000) // Run hourly
    public void updateArticleStats() {
        log.info("Updating article stats");

        // Flush any pending view counts to database
        if (!viewCountCache.isEmpty()) {
            log.info("Flushing {} article view counts to database", viewCountCache.size());

            for (Map.Entry<Long, Integer> entry : viewCountCache.entrySet()) {
                Long articleId = entry.getKey();
                Integer viewCount = entry.getValue();

                ArticleStat stat = articleStatRepository.findByArticleId(articleId)
                        .orElse(ArticleStat.builder()
                                .articleId(articleId)
                                .viewCount(0)
                                .shareCount(0)
                                .saveCount(0)
                                .engagementScore(0.0f)
                                .trendingScore(0.0f)
                                .isTopStory(false)
                                .build());

                stat.setViewCount(stat.getViewCount() + viewCount);
                calculateScores(stat);
                articleStatRepository.save(stat);
            }

            // Clear cache
            viewCountCache.clear();
        }

        // Find articles without stats and create initial records
        // Process in batches to avoid memory issues with large datasets
        Pageable batchPageable = PageRequest.of(0, 100);
        List<Long> articlesWithoutStats = articleStatRepository.findArticlesWithoutStats(batchPageable);

        while (!articlesWithoutStats.isEmpty()) {
            log.info("Creating initial stats for {} articles", articlesWithoutStats.size());

            List<ArticleStat> newStats = new ArrayList<>();
            for (Long articleId : articlesWithoutStats) {
                ArticleStat stat = ArticleStat.builder()
                        .articleId(articleId)
                        .viewCount(0)
                        .shareCount(0)
                        .saveCount(0)
                        .engagementScore(0.0f)
                        .trendingScore(0.0f)
                        .isTopStory(false)
                        .build();
                newStats.add(stat);
            }

            articleStatRepository.saveAll(newStats);

            // Get next batch
            articlesWithoutStats = articleStatRepository.findArticlesWithoutStats(batchPageable);
        }

        // Update top stories flag based on trending score
        // In a real implementation, this would be more complex and consider more factors
        log.info("Updating top stories flags");
        List<ArticleStat> allStats = articleStatRepository.findAll();

        List<ArticleStat> updatedStats = new ArrayList<>();
        for (ArticleStat stat : allStats) {
            boolean shouldBeTopStory = stat.getTrendingScore() >= TOP_STORIES_THRESHOLD;
            if (stat.getIsTopStory() != shouldBeTopStory) {
                stat.setIsTopStory(shouldBeTopStory);
                updatedStats.add(stat);
            }
        }

        if (!updatedStats.isEmpty()) {
            log.info("Updating top stories flag for {} articles", updatedStats.size());
            articleStatRepository.saveAll(updatedStats);
        }
    }

    /**
     * Calculate engagement and trending scores for an article
     *
     * @param stat ArticleStat to update
     */
    private void calculateScores(ArticleStat stat) {
        // Calculate engagement score
        float engagementScore = (stat.getViewCount() * VIEW_WEIGHT) +
                (stat.getShareCount() * SHARE_WEIGHT) +
                (stat.getSaveCount() * SAVE_WEIGHT);
        stat.setEngagementScore(engagementScore);

        // Calculate trending score (in a real implementation, this would consider recency)
        // For simplicity, we're using engagement score as trending score
        stat.setTrendingScore(engagementScore);

        // Update top story flag
        stat.setIsTopStory(engagementScore >= TOP_STORIES_THRESHOLD);
    }

    /**
     * Map TrendingTopic entity to TrendingTopicResponse DTO
     *
     * @param topic TrendingTopic entity
     * @return TrendingTopicResponse DTO
     */
    private TrendingTopicResponse mapToTrendingTopicResponse(TrendingTopic topic) {
        String categoryName = null;
        if (topic.getCategoryId() != null) {
            Optional<Category> category = categoryRepository.findById(topic.getCategoryId());
            categoryName = category.map(Category::getName).orElse(null);
        }

        return TrendingTopicResponse.builder()
                .id(topic.getId())
                .topicName(topic.getTopicName())
                .categoryId(topic.getCategoryId())
                .categoryName(categoryName)
                .score(topic.getScore())
                .startDate(topic.getStartDate())
                .endDate(topic.getEndDate())
                .build();
    }

    /**
     * Populate hashtags for a list of articles
     *
     * @param articles Articles to populate hashtags for
     */
    private void populateHashtags(List<ArticleResponse> articles) {
        if (articles == null || articles.isEmpty()) {
            return;
        }

        List<Long> articleIds = articles.stream()
                .map(ArticleResponse::getId)
                .toList();

        // Get all tags for these articles
        List<Object[]> tagResults = articleRepository.findTagNamesByArticleIds(articleIds);

        // Group tags by article ID
        for (ArticleResponse article : articles) {
            List<String> tags = new ArrayList<>();
            for (Object[] result : tagResults) {
                Long articleId = ((Number) result[0]).longValue();
                if (articleId.equals(article.getId())) {
                    tags.add((String) result[1]);
                }
            }
            article.setHashtag(tags);
        }
    }
}