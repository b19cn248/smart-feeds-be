// RssFeedServiceImpl.java - updated
package com.olh.feeds.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.olh.feeds.core.exception.base.BadRequestException;
import com.olh.feeds.dao.entity.Article;
import com.olh.feeds.dao.entity.Source;
import com.olh.feeds.dao.repository.ArticleRepository;
import com.olh.feeds.dao.repository.SourceRepository;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class RssFeedServiceImpl implements RssFeedService {

    private final ArticleRepository articleRepository;
    private final SourceRepository sourceRepository;
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
                // Skip nếu đã tồn tại
                if (articleInDB.isPresent()) {
                    log.info("Article already exists, skipping");
                    continue;
                }

                // Tìm hoặc tạo source
                Source source = findOrCreateSource(item);

                // Tạo article entity
                Article article = createArticleFromRequest(item, source.getId());
                articlesToSave.add(article);

            } catch (Exception e) {
                log.error("Error processing RSS item: {}", item.getTitle(), e);
                throw new BadRequestException(e.getMessage());
            }
        }

        // Bulk save articles
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

        String enclosureUrl = this.extraImageFromContent(request.getContent());

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

        // Parse dates
        if (request.getPubDate() != null) {
            article.setPubDate(parseDateTime(request.getPubDate()));
        }
        if (request.getIsoDate() != null) {
            article.setIsoDate(parseIsoDateTime(request.getIsoDate()));
        }

        // Handle enclosure
        if (request.getEnclosure() != null) {
            article.setEnclosureUrl(request.getEnclosure().getUrl());
            article.setEnclosureLength(request.getEnclosure().getLength());
            article.setEnclosureType(request.getEnclosure().getType());
        }

        // Handle iTunes data
        if (request.getItunes() != null) {
            try {
                article.setItunesData(objectMapper.writeValueAsString(request.getItunes()));
            } catch (Exception e) {
                log.warn("Failed to serialize iTunes data", e);
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
        return ArticleResponse.builder()
                .id(article.getId())
                .title(article.getTitle())
                .content(article.getContent())
                .publishDate(article.getPubDate() != null ? article.getPubDate() : null)
                .summary(article.getSummary())
                .event(article.getEvent())
                .build();
    }

    private String extraImageFromContent(String content) {
        String imgPattern = "src=''(.*?)''";
        Pattern pattern = Pattern.compile(imgPattern);
        Matcher matcher = pattern.matcher(content);

        if (matcher.find()) {
            return matcher.group(1);
        }

        return "";
    }
}