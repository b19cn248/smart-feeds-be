package com.olh.feeds.api.controller;

import com.olh.feeds.core.exception.response.ResponseGeneral;
import com.olh.feeds.dto.response.PageResponse;
import com.olh.feeds.dto.response.source.SourceResponse;
import com.olh.feeds.service.SourceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
}