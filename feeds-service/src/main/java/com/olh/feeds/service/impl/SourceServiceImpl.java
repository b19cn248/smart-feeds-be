// feeds-service/src/main/java/com/olh/feeds/service/impl/SourceServiceImpl.java
package com.olh.feeds.service.impl;

import com.olh.feeds.core.exception.base.NotFoundException;
import com.olh.feeds.dao.repository.ArticleRepository;
import com.olh.feeds.dao.repository.SourceRepository;
import com.olh.feeds.dto.mapper.PageMapper;
import com.olh.feeds.dto.response.PageResponse;
import com.olh.feeds.dto.response.article.ArticleResponse;
import com.olh.feeds.dto.response.article.SourceArticlesResponse;
import com.olh.feeds.dto.response.source.SourceResponse;
import com.olh.feeds.service.SourceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SourceServiceImpl implements SourceService {

    private final SourceRepository sourceRepository;
    private final PageMapper pageMapper;
    private final ArticleRepository articleRepository;

    @Override
    public PageResponse<SourceResponse> getAllSources(Boolean active, Pageable pageable) {
        log.info("Getting all sources with active status: {}", active);

        Page<SourceResponse> sourcesPage = sourceRepository.findAllSources(active, pageable);

        log.info("Found {} sources", sourcesPage.getNumberOfElements());
        return pageMapper.toPageDto(sourcesPage);
    }

    @Override
    public SourceArticlesResponse getSourceWithArticles(Long sourceId, Pageable pageable) {
        log.info("Getting source with articles for source ID: {}", sourceId);

        // Lấy thông tin source
        SourceResponse sourceResponse = sourceRepository.findSourceById(sourceId);
        if (sourceResponse == null) {
            log.error("Source not found with ID: {}", sourceId);
            throw new NotFoundException(sourceId.toString(), "source");
        }

        // Lấy danh sách articles của source
        List<ArticleResponse> articles = articleRepository.findBySourceId(sourceId, pageable);
        log.info("Found {} articles for source ID: {}", articles.size(), sourceId);

        // Tạo và trả về SourceArticlesResponse
        return SourceArticlesResponse.builder()
                .source(sourceResponse)
                .articles(articles)
                .build();
    }
}