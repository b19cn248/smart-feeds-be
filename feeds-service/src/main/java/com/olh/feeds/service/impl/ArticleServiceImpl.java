package com.olh.feeds.service.impl;

import com.olh.feeds.core.exception.base.NotFoundException;
import com.olh.feeds.dao.entity.Article;
import com.olh.feeds.dao.repository.ArticleRepository;
import com.olh.feeds.dao.repository.SourceRepository;
import com.olh.feeds.dto.mapper.PageMapper;
import com.olh.feeds.dto.response.PageResponse;
import com.olh.feeds.dto.response.article.ArticleResponse;
import com.olh.feeds.dto.response.article.SourceArticlesResponse;
import com.olh.feeds.dto.response.source.SourceResponse;
import com.olh.feeds.service.ArticleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ArticleServiceImpl implements ArticleService {

    private final ArticleRepository articleRepository;
    private final SourceRepository sourceRepository;
    private final PageMapper pageMapper;
    private final AuditorAware<String> auditorAware;

    @Override
    public PageResponse<ArticleResponse> getAllArticles(Pageable pageable) {
        log.info("getAllArticles");
        Page<ArticleResponse> articleResponsePage =
                articleRepository.findAllByOrderByCreatedAtDesc(pageable);
        return pageMapper.toPageDto(articleResponsePage);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<SourceArticlesResponse> getArticlesByUserSources(Pageable pageable) {
        log.info("getArticlesByUserSources");

        // Lấy username từ Security Context
        String username = auditorAware.getCurrentAuditor().get();
        log.info("Current username: {}", username);

        // Lấy danh sách source IDs mà user đang follow (qua folders)
        List<Long> sourceIds = articleRepository.findSourceIdsByUsername(username);
        log.info("Found {} sources followed by user", sourceIds.size());

        if (sourceIds.isEmpty()) {
            return PageResponse.<SourceArticlesResponse>builder()
                    .content(new ArrayList<>())
                    .totalElements(0)
                    .totalPages(0)
                    .last(true)
                    .first(true)
                    .size(pageable.getPageSize())
                    .number(pageable.getPageNumber())
                    .numberOfElements(0)
                    .empty(true)
                    .build();
        }

        // Tạo danh sách SourceArticlesResponse
        List<SourceArticlesResponse> sourceArticles = new ArrayList<>();

        // Cho mỗi source, lấy thông tin source và danh sách articles
        for (Long sourceId : sourceIds) {
            // Lấy thông tin source
            SourceResponse sourceResponse = sourceRepository.findSourceById(sourceId);

            if (sourceResponse != null) {
                // Lấy danh sách articles của source này
                List<ArticleResponse> articles = articleRepository.findBySourceId(sourceId, pageable);

                // Thêm vào kết quả
                sourceArticles.add(SourceArticlesResponse.builder()
                        .source(sourceResponse)
                        .articles(articles)
                        .build());
            }
        }

        // Tạo Page từ danh sách kết quả
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), sourceArticles.size());

        // Nếu start > size của danh sách, thì trả về trang rỗng
        if (start >= sourceArticles.size()) {
            return PageResponse.<SourceArticlesResponse>builder()
                    .content(new ArrayList<>())
                    .totalElements(sourceArticles.size())
                    .totalPages((int) Math.ceil((double) sourceArticles.size() / pageable.getPageSize()))
                    .last(true)
                    .first(pageable.getPageNumber() == 0)
                    .size(pageable.getPageSize())
                    .number(pageable.getPageNumber())
                    .numberOfElements(0)
                    .empty(true)
                    .build();
        }

        List<SourceArticlesResponse> pageContent = sourceArticles.subList(start, end);
        Page<SourceArticlesResponse> page = new PageImpl<>(pageContent, pageable, sourceArticles.size());

        return pageMapper.toPageDto(page);
    }

    @Override
    public boolean checkArticleExists(String guid, String link) {
        log.info("Checking if article exists with guid: {} and link: {}", guid, link);

        // Chuẩn hóa guid và link
        String normalizedGuid = normalizeGuid(guid);
        String normalizedLink = normalizeUrl(link);

        // Kiểm tra trùng lặp dựa trên guid hoặc link
        if (normalizedGuid != null && !normalizedGuid.isEmpty()) {
            if (articleRepository.findByGuid(normalizedGuid).isPresent()) {
                log.info("Article already exists with guid: {}", normalizedGuid);
                return true;
            }
        }
        if (normalizedLink != null && !normalizedLink.isEmpty()) {
            if (articleRepository.findByLink(normalizedLink).isPresent()) {
                log.info("Article already exists with link: {}", normalizedLink);
                return true;
            }
        }

        log.info("Article does not exist with guid: {} and link: {}", normalizedGuid, normalizedLink);
        return false;
    }

    @Override
    public ArticleResponse getArticleById(Long articleId) {
        log.info("getArticleById: {}", articleId);

        return articleRepository.findArticleById(articleId)
                .orElseThrow(
                        () -> new NotFoundException("Article not found with id: " + articleId)
                );
    }

    private String normalizeUrl(String url) {
        if (url == null || url.isEmpty()) {
            return url;
        }
        try {
            // Chuẩn hóa URL: loại bỏ khoảng trắng, chuyển thành https, loại bỏ trailing slash
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
        // Chuẩn hóa GUID: loại bỏ khoảng trắng
        return guid.trim();
    }
}