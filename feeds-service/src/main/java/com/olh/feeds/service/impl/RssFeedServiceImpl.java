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

        for (RssItemRequest item : request.getItems()) {
            try {
                Optional<Article> articleInDB = articleRepository.findByGuidOrLink(item.getGuid(), item.getLink());
                if (articleInDB.isPresent()) {
                    log.info("Article already exists, skipping");
                    continue;
                }

                Source source = findOrCreateSource(item);
                Article article = createArticleFromRequest(item, source.getId());
                articlesToSave.add(article);

            } catch (Exception e) {
                log.error("Error processing RSS item: {}", item.getTitle(), e);
                throw new BadRequestException(e.getMessage());
            }
        }

        if (!articlesToSave.isEmpty()) {
            List<Article> savedArticles = articleRepository.saveAll(articlesToSave);
            responses = savedArticles.stream()
                  .map(this::convertToResponse)
                  .toList();
        }

        log.info("Successfully processed {} articles", responses.size());
        return responses;
    }

    private Source findOrCreateSource(RssItemRequest item) {
        String sourceUrl = extractSourceUrl(item.getLink());

        return sourceRepository.findByUrl(sourceUrl)
              .orElseGet(() -> {
                  Source newSource = Source.builder()
                        .url(sourceUrl)
                        .type("RSS")
                        .active(true)
                        .userId(1L)
                        .build();
                  return sourceRepository.save(newSource);
              });
    }

    private String extractSourceUrl(String link) {
        if (link == null || link.isEmpty()) {
            return "unknown";
        }

        try {
            java.net.URL url = new java.net.URL(link);
            return url.getProtocol() + "://" + url.getHost();
        } catch (Exception e) {
            log.warn("Failed to extract source URL from: {}", link);
            return link;
        }
    }

    private Article createArticleFromRequest(RssItemRequest request, Long sourceId) {
        String enclosureUrl = this.extractImageFromContent(request.getContent());

        Article article = Article.builder()
              .title(request.getTitle())
              .creator(request.getCreator())
              .link(request.getLink())
              .guid(request.getGuid())
              .content(request.getContent())
              .enclosureUrl(enclosureUrl)
              .contentSnippet(request.getContentSnippet())
              .contentEncoded(request.getContentEncoded())
              .contentEncodedSnippet(request.getContentEncodedSnippet())
              .dcCreator(request.getDcCreator())
              .sourceId(sourceId)
              .build();

        if (request.getPubDate() != null) {
            article.setPubDate(DateTimeUtils.parseRFC822DateSafely(request.getPubDate()));
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
                log.warn("Failed to serialize iTunes data", e);
            }
        }

        // Xử lý hashtag
        if (request.getHashtag() != null && !request.getHashtag().isEmpty()) {
            for (String hashtag : request.getHashtag()) {
                // Tìm hoặc tạo tag
                Tag tag = tagRepository.findByName(hashtag)
                      .orElseGet(() -> {
                          Tag newTag = Tag.builder()
                                .name(hashtag)
                                .createdAt(LocalDateTime.now())
                                .createdBy("system")
                                .isDeleted(false)
                                .build();
                          return tagRepository.save(newTag);
                      });

                // Lưu vào bảng article_tags sau khi bài viết được lưu
                articleRepository.save(article); // Lưu bài viết để có ID
                ArticleTag articleTag = ArticleTag.builder()
                      .articleId(article.getId())
                      .tagId(tag.getId())
                      .createdAt(LocalDateTime.now())
                      .createdBy("system")
                      .isDeleted(false)
                      .build();
                articleTagRepository.save(articleTag);
            }
        }

        return article;
    }

    private LocalDateTime parseDateTime(String dateStr) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.RFC_1123_DATE_TIME;
            return ZonedDateTime.parse(dateStr, formatter).toLocalDateTime();
        } catch (Exception e) {
            log.warn("Failed to parse date: {}", dateStr);
            return LocalDateTime.now();
        }
    }

    private LocalDateTime parseIsoDateTime(String isoDateStr) {
        try {
            return LocalDateTime.parse(isoDateStr.replace("Z", ""));
        } catch (Exception e) {
            log.warn("Failed to parse ISO date: {}", isoDateStr);
            return LocalDateTime.now();
        }
    }

    private ArticleResponse convertToResponse(Article article) {
        // Lấy danh sách hashtag từ bảng article_tags
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