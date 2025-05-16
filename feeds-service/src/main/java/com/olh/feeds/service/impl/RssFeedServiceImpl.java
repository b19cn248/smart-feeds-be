package com.olh.feeds.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.olh.feeds.core.exception.base.BadRequestException;
import com.olh.feeds.dao.entity.Article;
import com.olh.feeds.dao.entity.ArticleTag;
import com.olh.feeds.dao.entity.Source;
import com.olh.feeds.dao.entity.Tag;
import com.olh.feeds.dao.repository.ArticleRepository;
import com.olh.feeds.dao.repository.ArticleTagRepository;
import com.olh.feeds.dao.repository.SourceRepository;
import com.olh.feeds.dao.repository.TagRepository;
import com.olh.feeds.dto.request.article.RssFeedRequest;
import com.olh.feeds.dto.request.article.RssItemRequest;
import com.olh.feeds.dto.response.article.ArticleResponse;
import com.olh.feeds.service.RssFeedService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RssFeedServiceImpl implements RssFeedService {

    private final ArticleRepository articleRepository;
    private final SourceRepository sourceRepository;
    private final TagRepository tagRepository;
    private final ArticleTagRepository articleTagRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public List<ArticleResponse> processRssFeed(RssFeedRequest request) {
        log.info("Processing RSS feed with {} items", request.getItems() != null ? request.getItems().size() : 0);

        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new BadRequestException("com.olh.feeds.rss.items.empty");
        }

        List<Article> articlesToSave = new ArrayList<>();
        List<RssItemRequest> itemsToProcess = new ArrayList<>();

        // Process each RSS item
        for (RssItemRequest item : request.getItems()) {
            try {
                String normalizedGuid = normalizeGuid(item.getGuid());
                String normalizedLink = normalizeUrl(item.getLink());

                // Check for duplicates
                if (isArticleDuplicate(normalizedGuid, normalizedLink)) {
                    log.info("Article with guid {} or link {} already exists, skipping. Title: {}",
                            normalizedGuid, normalizedLink, item.getTitle());
                    continue;
                }

                Source source = findOrCreateSource(item);
                Article article = createArticleFromRequest(item, source.getId());
                article.setGuid(normalizedGuid);
                article.setLink(normalizedLink);
                articlesToSave.add(article);
                itemsToProcess.add(item);

            } catch (Exception e) {
                log.error("Error processing RSS item: {}. Guid: {}, Link: {}",
                        item.getTitle(), item.getGuid(), item.getLink(), e);
                // Continue processing other items
            }
        }

        List<ArticleResponse> responses = new ArrayList<>();
        if (!articlesToSave.isEmpty()) {
            // Save all articles
            List<Article> savedArticles = articleRepository.saveAll(articlesToSave);
            log.info("Saved {} articles to database", savedArticles.size());

            // Process hashtags for all saved articles
            processHashtags(savedArticles, itemsToProcess);

            // Convert to response
            responses = savedArticles.stream()
                    .map(this::convertToResponse)
                    .toList();
        }

        log.info("Successfully processed {} articles", responses.size());
        return responses;
    }

    private boolean isArticleDuplicate(String guid, String link) {
        if (guid != null && !guid.isEmpty() && articleRepository.findByGuid(guid).isPresent()) {
            return true;
        }
        return link != null && !link.isEmpty() && articleRepository.findByLink(link).isPresent();
    }

    private void processHashtags(List<Article> articles, List<RssItemRequest> items) {
        log.info("Starting hashtag processing for {} articles", articles.size());

        // Collect all unique hashtags
        Set<String> allHashtags = new HashSet<>();
        for (RssItemRequest item : items) {
            if (item.getHashtag() != null && !item.getHashtag().isEmpty()) {
                log.debug("Found {} hashtags for item: {}", item.getHashtag().size(), item.getTitle());
                item.getHashtag().stream()
                        .map(this::normalizeHashtag)
                        .filter(Objects::nonNull)
                        .forEach(allHashtags::add);
            } else {
                log.debug("No hashtags found for item: {}", item.getTitle());
            }
        }

        log.info("Collected {} unique hashtags: {}", allHashtags.size(), allHashtags);

        // Find or create tags
        Map<String, Tag> tagMap = findOrCreateTags(allHashtags);
        log.info("Tag map contains {} tags", tagMap.size());

        // Process article-tag relationships
        List<ArticleTag> articleTagsToSave = new ArrayList<>();
        for (int i = 0; i < articles.size(); i++) {
            Article article = articles.get(i);
            RssItemRequest item = items.get(i);

            if (item.getHashtag() == null || item.getHashtag().isEmpty()) {
                log.debug("No hashtags for article ID: {}", article.getId());
                continue;
            }

            log.debug("Processing hashtags for article ID: {}, Title: {}", article.getId(), article.getTitle());
            for (String hashtag : item.getHashtag()) {
                String normalizedHashtag = normalizeHashtag(hashtag);
                if (normalizedHashtag == null) {
                    log.warn("Invalid hashtag after normalization: {}", hashtag);
                    continue;
                }

                Tag tag = tagMap.get(normalizedHashtag);
                if (tag == null) {
                    log.error("Tag not found for normalized hashtag: {}", normalizedHashtag);
                    continue;
                }

                // Check if ArticleTag already exists
                if (!articleTagRepository.existsByArticleIdAndTagId(article.getId(), tag.getId())) {
                    ArticleTag articleTag = ArticleTag.builder()
                            .articleId(article.getId())
                            .tagId(tag.getId())
                            .createdAt(LocalDateTime.now())
                            .createdBy("system")
                            .isDeleted(false)
                            .build();
                    articleTagsToSave.add(articleTag);
                    log.debug("Added ArticleTag for article ID: {} and tag ID: {}", article.getId(), tag.getId());
                } else {
                    log.debug("ArticleTag already exists for article ID: {} and tag ID: {}", article.getId(), tag.getId());
                }
            }
        }

        // Save all article-tag relationships
        if (!articleTagsToSave.isEmpty()) {
            try {
                articleTagRepository.saveAll(articleTagsToSave);
                log.info("Successfully created {} article-tag relationships", articleTagsToSave.size());
            } catch (Exception e) {
                log.error("Failed to save article-tag relationships", e);
                throw new RuntimeException("Failed to save article-tag relationships", e);
            }
        } else {
            log.warn("No article-tag relationships to save");
        }
    }

    private Map<String, Tag> findOrCreateTags(Set<String> hashtags) {
        log.info("Finding or creating {} tags", hashtags.size());

        if (hashtags.isEmpty()) {
            log.debug("No hashtags to process");
            return new HashMap<>();
        }

        // Find existing tags
        List<Tag> existingTags = tagRepository.findAllByNameIn(hashtags);
        Map<String, Tag> tagMap = existingTags.stream()
                .collect(Collectors.toMap(Tag::getName, tag -> tag));

        log.debug("Found {} existing tags: {}", existingTags.size(), tagMap.keySet());

        // Create new tags
        List<Tag> newTags = new ArrayList<>();
        for (String hashtag : hashtags) {
            if (!tagMap.containsKey(hashtag)) {
                Tag newTag = Tag.builder()
                        .name(hashtag)
                        .createdAt(LocalDateTime.now())
                        .createdBy("system")
                        .isDeleted(false)
                        .build();
                newTags.add(newTag);
                log.debug("Created new tag: {}", hashtag);
            }
        }

        // Save new tags
        if (!newTags.isEmpty()) {
            try {
                List<Tag> savedTags = tagRepository.saveAll(newTags);
                savedTags.forEach(tag -> tagMap.put(tag.getName(), tag));
                log.info("Saved {} new tags", savedTags.size());
            } catch (Exception e) {
                log.error("Failed to save new tags", e);
                throw new RuntimeException("Failed to save new tags", e);
            }
        }

        return tagMap;
    }

    private String normalizeHashtag(String hashtag) {
        if (hashtag == null || hashtag.trim().isEmpty()) {
            log.debug("Hashtag is null or empty: {}", hashtag);
            return null;
        }

        // Remove leading #, trim, convert to lowercase, remove special characters
        String normalized = hashtag.trim().toLowerCase().replaceAll("^#+", "");
        // Remove non-alphanumeric except spaces and hyphens
        normalized = normalized.replaceAll("[^a-z0-9\\s-]", "");
        // Replace multiple spaces/hyphens with single hyphen
        normalized = normalized.replaceAll("[\\s-]+", "-");

        if (normalized.isEmpty()) {
            log.warn("Hashtag became empty after normalization: {}", hashtag);
            return null;
        }

        log.info("Normalized hashtag: {} -> {}", hashtag, normalized);
        return normalized;
    }

    private Source findOrCreateSource(RssItemRequest item) {
        String sourceUrl = extractSourceUrl(item.getLink());
        log.debug("Finding or creating source for URL: {}", sourceUrl);

        List<Source> sources = sourceRepository.findAllByUrl(sourceUrl);
        if (!sources.isEmpty()) {
            Source source = sources.stream()
                    .filter(s -> !s.getIsDeleted())
                    .sorted((s1, s2) -> s2.getCreatedAt().compareTo(s1.getCreatedAt()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("No non-deleted source found for URL: " + sourceUrl));
            log.info("Found existing source with URL: {} and ID: {}", sourceUrl, source.getId());
            return source;
        }

        log.info("No source found for URL: {}. Creating new source.", sourceUrl);
        Source newSource = Source.builder()
                .url(sourceUrl)
                .type("RSS")
                .active(true)
                .userId(1L)
                .build();
        return sourceRepository.save(newSource);
    }

    private String extractSourceUrl(String link) {
        if (link == null || link.isEmpty()) {
            log.warn("Link is null or empty, returning 'unknown' as source URL");
            return "unknown";
        }

        try {
            java.net.URL url = new java.net.URL(link);
            String sourceUrl = url.getProtocol() + "://" + url.getHost();
            return normalizeUrl(sourceUrl);
        } catch (Exception e) {
            log.warn("Failed to extract source URL from: {}", link, e);
            return normalizeUrl(link);
        }
    }

    private Article createArticleFromRequest(RssItemRequest request, Long sourceId) {
        String enclosureUrl = this.extractImageFromContent(request.getContent());

        if (enclosureUrl.isBlank()) {
            enclosureUrl = this.extractImageFromContent(request.getContentEncoded());
        }

        Article article = Article.builder()
                .title(request.getTitle())
                .creator(request.getCreator())
                .content(request.getContent())
                .enclosureUrl(enclosureUrl.isBlank() ? request.getEnclosureUrl() : enclosureUrl)
                .contentSnippet(request.getContentSnippet())
                .contentEncoded(request.getContentEncoded())
                .contentEncodedSnippet(request.getContentEncodedSnippet())
                .dcCreator(request.getDcCreator())
                .sourceId(sourceId)
                .build();

        if (request.getPubDate() != null) {
            article.setPubDate(parseDateTime(request.getPubDate()));
        }
        if (request.getIsoDate() != null) {
            article.setIsoDate(parseIsoDateTime(request.getIsoDate()));
        }

        if (request.getEnclosure() != null) {
            article.setEnclosureUrl(request.getEnclosure().getUrl());
            article.setEnclosureLength(request.getEnclosure().getLength());
            article.setEnclosureType(request.getEnclosure().getType());
        }

        if (request.getItunes() != null) {
            try {
                article.setItunesData(objectMapper.writeValueAsString(request.getItunes()));
            } catch (Exception e) {
                log.warn("Failed to serialize iTunes data for article: {}", request.getTitle(), e);
            }
        }

        return article;
    }

    private String normalizeUrl(String url) {
        if (url == null || url.isEmpty()) {
            return url;
        }
        try {
            url = url.trim();
            if (url.startsWith("http://")) {
                url = "https://" + url.substring(7);
            }
            if (url.endsWith("/")) {
                url = url.substring(0, url.length() - 1);
            }
            return url;
        } catch (Exception e) {
            log.warn("Failed to normalize URL: {}", url, e);
            return url;
        }
    }

    private String normalizeGuid(String guid) {
        if (guid == null || guid.isEmpty()) {
            return guid;
        }
        return guid.trim();
    }

    private LocalDateTime parseDateTime(String dateStr) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.RFC_1123_DATE_TIME;
            return ZonedDateTime.parse(dateStr, formatter).toLocalDateTime();
        } catch (Exception e) {
            log.warn("Failed to parse date: {}", dateStr, e);
            return LocalDateTime.now();
        }
    }

    private LocalDateTime parseIsoDateTime(String isoDateStr) {
        try {
            return LocalDateTime.parse(isoDateStr.replace("Z", ""));
        } catch (Exception e) {
            log.warn("Failed to parse ISO date: {}", isoDateStr, e);
            return LocalDateTime.now();
        }
    }

    private ArticleResponse convertToResponse(Article article) {
        List<ArticleTag> articleTags = articleTagRepository.findByArticleId(article.getId());
        List<String> hashtags = articleTags.stream()
                .map(articleTag -> tagRepository.findById(articleTag.getTagId()))
                .filter(Optional::isPresent)
                .map(opt -> opt.get().getName())
                .toList();

        return ArticleResponse.builder()
                .id(article.getId())
                .title(article.getTitle())
                .content(article.getContent())
                .contentEncoded(article.getContentEncoded())
                .publishDate(article.getPubDate() != null ? article.getPubDate() : null)
                .content(article.getContentSnippet())
                .url(article.getLink())
                .summary(article.getSummary())
                .event(article.getEvent())
                .imageUrl(article.getEnclosureUrl())
                .hashtag(hashtags)
                .build();
    }

    private String extractImageFromContent(String content) {
        if (content == null || content.isEmpty()) {
            return "";
        }

        List<String> patterns = Arrays.asList(
                "src=''(.*?)''",
                "src=\"(.*?)\"",
                "src = ''(.*?)''",
                "src = \"(.*?)\"",
                "src=\"(.*?)\"",
                "data-src=\"(.*?)\"",
                "data-src=''(.*?)''",
                "srcset=\"(.*?)(?:\\s|\")",
                "src=\"data:image.*?;base64,(.*?)\"",
                "src=(https?://[^\\s>]+)"
        );

        for (String patternStr : patterns) {
            Pattern pattern = Pattern.compile(patternStr);
            Matcher matcher = pattern.matcher(content);

            if (matcher.find()) {
                String url = matcher.group(1);
                url = url.trim();
                if (isValidImageUrl(url)) {
                    return url;
                }
            }
        }

        Pattern imgTagPattern = Pattern.compile("<img[^>]+>");
        Matcher imgTagMatcher = imgTagPattern.matcher(content);

        if (imgTagMatcher.find()) {
            String imgTag = imgTagMatcher.group(0);
            for (String patternStr : patterns) {
                Pattern pattern = Pattern.compile(patternStr);
                Matcher matcher = pattern.matcher(imgTag);

                if (matcher.find()) {
                    String url = matcher.group(1);
                    url = url.trim();
                    if (isValidImageUrl(url)) {
                        return url;
                    }
                }
            }
        }

        return "";
    }

    private boolean isValidImageUrl(String url) {
        if (url == null || url.isEmpty()) {
            return false;
        }

        if (!(url.startsWith("http://") || url.startsWith("https://") || url.startsWith("data:image"))) {
            return false;
        }

        if (url.startsWith("data:image")) {
            return true;
        }

        String[] imageExtensions = {".jpg", ".jpeg", ".png", ".gif", ".bmp", ".webp", ".svg"};
        String lowerUrl = url.toLowerCase();

        if (url.contains("?")) {
            return true;
        }

        for (String ext : imageExtensions) {
            if (lowerUrl.endsWith(ext)) {
                return true;
            }
        }

        return true;
    }
}