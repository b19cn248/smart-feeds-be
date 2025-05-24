package com.olh.feeds.service.impl;

import com.olh.feeds.core.exception.base.NotFoundException;
import com.olh.feeds.dao.entity.ExploreCollection;
import com.olh.feeds.dao.repository.ArticleRepository;
import com.olh.feeds.dao.repository.CategoryRepository;
import com.olh.feeds.dao.repository.ExploreCollectionRepository;
import com.olh.feeds.dao.repository.ExploreCuratedArticleRepository;
import com.olh.feeds.dto.mapper.PageMapper;
import com.olh.feeds.dto.response.PageResponse;
import com.olh.feeds.dto.response.article.ArticleResponse;
import com.olh.feeds.dto.response.explore.ExploreCollectionResponse;
import com.olh.feeds.dto.response.explore.ExplorePageResponse;
import com.olh.feeds.dto.response.explore.TrendingTopicResponse;
import com.olh.feeds.service.ExploreService;
import com.olh.feeds.service.TopStoriesService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExploreServiceImpl implements ExploreService {

    private final ExploreCollectionRepository exploreCollectionRepository;
    private final ExploreCuratedArticleRepository exploreCuratedArticleRepository;
    private final ArticleRepository articleRepository;
    private final CategoryRepository categoryRepository;
    private final PageMapper pageMapper;
    private final TopStoriesService topStoriesService;

    // Number of days to look back for recent articles
    private static final int RECENT_ARTICLES_DAYS = 7;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ExploreCollectionResponse> getExploreCollections(Pageable pageable) {
        log.info("Getting explore collections");

        Page<ExploreCollection> collectionsPage = exploreCollectionRepository.findAllActiveCollections(pageable);

        List<ExploreCollectionResponse> responses = new ArrayList<>();
        for (ExploreCollection collection : collectionsPage.getContent()) {
            // Get a preview of articles for each collection
            Pageable previewPageable = PageRequest.of(0, 5);
            PageResponse<ArticleResponse> articlesPage = getArticlesByCollectionType(collection, previewPageable);

            responses.add(ExploreCollectionResponse.builder()
                    .id(collection.getId())
                    .name(collection.getName())
                    .description(collection.getDescription())
                    .imageUrl(collection.getImageUrl())
                    .type(collection.getType())
                    .priority(collection.getPriority())
                    .isActive(collection.getIsActive())
                    .createdAt(collection.getCreatedAt())
                    .articles(articlesPage.getContent())
                    .build());
        }

        Page<ExploreCollectionResponse> responsePage = new PageImpl<>(
                responses,
                pageable,
                collectionsPage.getTotalElements()
        );

        return pageMapper.toPageDto(responsePage);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ArticleResponse> getArticlesByCollection(Long collectionId, Pageable pageable) {
        log.info("Getting articles for collection ID: {}", collectionId);

        ExploreCollection collection = exploreCollectionRepository.findActiveById(collectionId)
                .orElseThrow(() -> new NotFoundException(collectionId.toString(), "collection"));

        PageResponse<ArticleResponse> articlesPage = getArticlesByCollectionType(collection, pageable);

        // Populate hashtags for articles
        populateHashtags(articlesPage.getContent());

        return articlesPage;
    }

    @Override
    public PageResponse<ArticleResponse> getArticlesByCategory(Long categoryId, Pageable pageable) {
        log.info("Getting articles for category ID: {}", categoryId);

        // Check if category exists
        categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException(categoryId.toString(), "category"));

        Page<ArticleResponse> articlesPage = articleRepository.findArticlesByCategory(categoryId, pageable);

        // Populate hashtags for articles
        populateHashtags(articlesPage.getContent());

        return pageMapper.toPageDto(articlesPage);
    }

    @Override
    public PageResponse<ArticleResponse> getRecentArticles(Pageable pageable) {
        log.info("Getting recent articles");

        // Get articles from the last RECENT_ARTICLES_DAYS days
        LocalDateTime fromDate = LocalDateTime.now().minusDays(RECENT_ARTICLES_DAYS);
        Page<ArticleResponse> articlesPage = articleRepository.findRecentArticles(fromDate, pageable);

        // Populate hashtags for articles
        populateHashtags(articlesPage.getContent());

        return pageMapper.toPageDto(articlesPage);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ArticleResponse> searchArticles(String keyword, Pageable pageable) {
        log.info("Searching articles with keyword: {}", keyword);

        Page<ArticleResponse> articlesPage = articleRepository.searchArticles(keyword, pageable);

        // Populate hashtags for articles
        populateHashtags(articlesPage.getContent());

        return pageMapper.toPageDto(articlesPage);
    }

    @Override
    @Transactional(readOnly = true)
    public ExplorePageResponse getExplorePage(
            int collectionSize,
            int articlesPerCollection,
            int topStoriesSize,
            int trendingTopicsSize) {
        log.info("Getting explore page with {} collections, {} articles per collection, {} top stories, and {} trending topics",
                collectionSize, articlesPerCollection, topStoriesSize, trendingTopicsSize);

        // Get active collections
        List<ExploreCollection> collections = exploreCollectionRepository.findActiveCollections();

        // Limit to requested size
        if (collections.size() > collectionSize) {
            collections = collections.subList(0, collectionSize);
        }

        // Build collection responses with articles
        List<ExploreCollectionResponse> collectionResponses = new ArrayList<>();
        for (ExploreCollection collection : collections) {
            Pageable articlesPageable = PageRequest.of(0, articlesPerCollection);
            PageResponse<ArticleResponse> articlesPage = getArticlesByCollectionType(collection, articlesPageable);

            // Populate hashtags for articles
            populateHashtags(articlesPage.getContent());

            collectionResponses.add(ExploreCollectionResponse.builder()
                    .id(collection.getId())
                    .name(collection.getName())
                    .description(collection.getDescription())
                    .imageUrl(collection.getImageUrl())
                    .type(collection.getType())
                    .priority(collection.getPriority())
                    .isActive(collection.getIsActive())
                    .createdAt(collection.getCreatedAt())
                    .articles(articlesPage.getContent())
                    .build());
        }

        // Get top stories
        PageResponse<ArticleResponse> topStoriesPage = topStoriesService.getTopStories(
                PageRequest.of(0, topStoriesSize));

        // Get trending topics
        PageResponse<TrendingTopicResponse> trendingTopicsPage = topStoriesService.getTrendingTopics(
                PageRequest.of(0, trendingTopicsSize));

        return ExplorePageResponse.builder()
                .collections(collectionResponses)
                .topStories(topStoriesPage.getContent())
                .trendingTopics(trendingTopicsPage.getContent())
                .build();
    }

    /**
     * Get articles based on collection type
     *
     * @param collection Collection to get articles for
     * @param pageable   Pagination information
     * @return Page of articles
     */
    private PageResponse<ArticleResponse> getArticlesByCollectionType(ExploreCollection collection, Pageable pageable) {
        switch (collection.getType()) {
            case "CURATED":
                return pageMapper.toPageDto(exploreCuratedArticleRepository.findArticlesByCollectionId(collection.getId(), pageable));
            case "TRENDING":
                return topStoriesService.getTrendingArticles(pageable);
            case "CATEGORY":
                // Get category ID from collection rules
                Long categoryId = getCategoryIdFromCollection(collection);
                if (categoryId != null) {
                    return getArticlesByCategory(categoryId, pageable);
                }
                // Fallback to recent articles if no category found
                return getRecentArticles(pageable);
            default:
                // Default to recent
                return getRecentArticles(pageable);
        }
    }

    /**
     * Get category ID from collection rules
     *
     * @param collection Collection to get category ID for
     * @return Category ID or null if not found
     */
    private Long getCategoryIdFromCollection(ExploreCollection collection) {
        // In a real implementation, this would query ExploreCollectionRuleRepository
        // to find a rule of type "CATEGORY" for this collection
        // Simplified version for demonstration
        return switch (collection.getName().toLowerCase()) {
            case "technology" -> 1L;
            case "business" -> 2L;
            default -> null;
        };
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