package com.olh.feeds.api.controller;

import com.olh.feeds.core.exception.response.ResponseGeneral;
import com.olh.feeds.dto.response.PageResponse;
import com.olh.feeds.dto.response.article.SourceArticlesResponse;
import com.olh.feeds.dto.response.source.SourceResponse;
import com.olh.feeds.service.SourceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/sources")
@Slf4j
@RequiredArgsConstructor
public class SourceController {

    private final SourceService sourceService;

    /**
     * Get all sources with optional filtering by active status
     *
     * @param active   Filter by active status (optional)
     * @param pageable Pagination information
     * @return List of sources
     */
    @GetMapping
    public ResponseGeneral<PageResponse<SourceResponse>> getAllSources(
            @RequestParam(name = "active", required = false) Boolean active,
            @PageableDefault Pageable pageable
    ) {
        log.info("REST request to get all sources with active status: {}", active);

        try {
            PageResponse<SourceResponse> sources = sourceService.getAllSources(active, pageable);
            return ResponseGeneral.of(
                    HttpStatus.OK.value(),
                    "sources.list.success",
                    sources
            );
        } catch (Exception e) {
            log.error("Error retrieving sources", e);
            throw e;
        }
    }


    /**
     * Lấy chi tiết source và danh sách articles của source đó
     *
     * @param sourceId ID của source
     * @param pageable Thông tin phân trang cho articles
     * @return Thông tin chi tiết source và danh sách articles
     */
    @GetMapping("/{sourceId}/articles")
    public ResponseGeneral<SourceArticlesResponse> getSourceWithArticles(
            @PathVariable("sourceId") Long sourceId,
            @PageableDefault Pageable pageable
    ) {
        log.info("REST request to get source and its articles for ID: {}", sourceId);
        return ResponseGeneral.of(
                HttpStatus.OK.value(),
                "source.articles.success",
                sourceService.getSourceWithArticles(sourceId, pageable)
        );
    }
}