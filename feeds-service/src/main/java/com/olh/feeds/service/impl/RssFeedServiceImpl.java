package com.olh.feeds.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.olh.feeds.core.exception.base.BadRequestException;
import com.olh.feeds.dao.entity.Article;
import com.olh.feeds.dao.entity.Source;
import com.olh.feeds.dao.entity.Tag;
import com.olh.feeds.dao.entity.ArticleTag;
import com.olh.feeds.dao.repository.ArticleRepository;
import com.olh.feeds.dao.repository.SourceRepository;
import com.olh.feeds.dao.repository.TagRepository;
import com.olh.feeds.dao.repository.ArticleTagRepository;
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
        log.info("Processing RSS feed with {} items", request.getItems().size());

        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new BadRequestException("com.olh.feeds.rss.items.empty");
        }

        List<Article> articlesToSave = new ArrayList<>();
        List<ArticleResponse> responses = new ArrayList<>();
        List<RssItemRequest> itemsToProcess = new ArrayList<>();

        for (RssItemRequest item : request.getItems()) {
            try {
                // Chuẩn hóa guid và link
                String normalizedGuid = normalizeGuid(item.getGuid());
                String normalizedLink = normalizeUrl(item.getLink());

                // Kiểm tra trùng lặp dựa trên guid hoặc link
                boolean articleExists = false;
                if (normalizedGuid != null && !normalizedGuid.isEmpty()) {
                    if (articleRepository.findByGuid(normalizedGuid).isPresent()) {
                        log.info("Article with guid {} already exists, skipping. Title: {}", normalizedGuid, item.getTitle());
                        articleExists = true;
                    }
                }
                if (!articleExists && normalizedLink != null && !normalizedLink.isEmpty()) {
                    if (articleRepository.findByLink(normalizedLink).isPresent()) {
                        log.info("Article with link {} already exists, skipping. Title: {}", normalizedLink, item.getTitle());
                        articleExists = true;
                    }
                }

                if (articleExists) {
                    continue;
                }

                Source source = findOrCreateSource(item);
                Article article = createArticleFromRequest(item, source.getId());
                article.setGuid(normalizedGuid);
                article.setLink(normalizedLink);
                articlesToSave.add(article);
                itemsToProcess.add(item);

            } catch (Exception e) {
                log.error("Error processing RSS item: {}. Guid: {}, Link: {}", item.getTitle(), item.getGuid(), item.getLink(), e);
                throw new BadRequestException("Failed to process RSS item: " + item.getTitle() + ". Error: " + e.getMessage());
            }
        }

        if (!articlesToSave.isEmpty()) {
            List<Article> savedArticles = articleRepository.saveAll(articlesToSave);
            log.info("Saved {} articles to database", savedArticles.size());

            // Xử lý hashtag sau khi lưu bài viết
            for (int i = 0; i < savedArticles.size(); i++) {
                Article article = savedArticles.get(i);
                RssItemRequest item = itemsToProcess.get(i);
                log.debug("Processing hashtags for article ID: {}, Title: {}", article.getId(), article.getTitle());

                if (item.getHashtag() != null && !item.getHashtag().isEmpty()) {
                    log.info("Found {} hashtags for article ID: {}", item.getHashtag().size(), article.getId());
                    for (String hashtag : item.getHashtag()) {
                        try {
                            // Chuẩn hóa hashtag
                            String normalizedHashtag = hashtag.trim().toLowerCase();
                            log.debug("Processing hashtag: {}", normalizedHashtag);

                            // Tìm hoặc tạo tag
                            Tag tag = tagRepository.findByName(normalizedHashtag)
                                    .orElseGet(() -> {
                                        Tag newTag = Tag.builder()
                                                .name(normalizedHashtag)
                                                .createdAt(LocalDateTime.now())
                                                .createdBy("system")
                                                .isDeleted(false)
                                                .build();
                                        Tag savedTag = tagRepository.save(newTag);
                                        log.info("Created new tag: {} with ID: {}", normalizedHashtag, savedTag.getId());
                                        return savedTag;
                                    });

                            // Kiểm tra xem ArticleTag đã tồn tại chưa
                            boolean articleTagExists = articleTagRepository.existsByArticleIdAndTagId(article.getId(), tag.getId());
                            if (!articleTagExists) {
                                // Tạo bản ghi trong article_tags
                                ArticleTag articleTag = ArticleTag.builder()
                                        .articleId(article.getId())
                                        .tagId(tag.getId())
                                        .createdAt(LocalDateTime.now())
                                        .createdBy("system")
                                        .isDeleted(false)
                                        .build();
                                articleTagRepository.save(articleTag);
                                log.info("Created ArticleTag for article ID: {} and tag ID: {}", article.getId(), tag.getId());
                            } else {
                                log.debug("ArticleTag already exists for article ID: {} and tag ID: {}", article.getId(), tag.getId());
                            }
                        } catch (Exception e) {
                            log.error("Failed to process hashtag '{}' for article ID: {}", hashtag, article.getId(), e);
                        }
                    }
                } else {
                    log.debug("No hashtags found for article ID: {}", article.getId());
                }
            }

            responses = savedArticles.stream()
                    .map(this::convertToResponse)
                    .toList();
        }

        log.info("Successfully processed {} articles", responses.size());
        return responses;
    }

    private Source findOrCreateSource(RssItemRequest item) {
        String sourceUrl = extractSourceUrl(item.getLink());
        log.debug("Finding or creating source for URL: {}", sourceUrl);

        // Tìm tất cả các nguồn với URL đã cho
        List<Source> sources = sourceRepository.findAllByUrl(sourceUrl); // Sửa lỗi từ articleRepository thành sourceRepository
        if (!sources.isEmpty()) {
            // Chọn nguồn gần nhất dựa trên created_at
            Source source = sources.stream()
                    .filter(s -> !s.getIsDeleted())
                    .sorted((s1, s2) -> s2.getCreatedAt().compareTo(s1.getCreatedAt()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("No non-deleted source found for URL: " + sourceUrl));
            log.info("Found existing source with URL: {} and ID: {}", sourceUrl, source.getId());
            return source;
        }

        // Tạo nguồn mới nếu không tìm thấy
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

        // Tạo đối tượng Article
        Article article = Article.builder()
                .title(request.getTitle())
                .creator(request.getCreator())
                .content(request.getContent())
                .enclosureUrl(enclosureUrl)
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