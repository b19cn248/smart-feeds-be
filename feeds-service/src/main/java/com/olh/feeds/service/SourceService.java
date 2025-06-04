// feeds-service/src/main/java/com/olh/feeds/service/SourceService.java
package com.olh.feeds.service;

import com.olh.feeds.dto.request.source.AddSourceRequest;
import com.olh.feeds.dto.response.PageResponse;
import com.olh.feeds.dto.response.article.SourceArticlesResponse;
import com.olh.feeds.dto.response.source.SourceResponse;
import org.springframework.data.domain.Pageable;

public interface SourceService {
    /**
     * Retrieve all sources with optional filtering by active status
     * @param active Filter by active status (null for all sources)
     * @param pageable Pagination information
     * @return Paginated list of sources
     */
    PageResponse<SourceResponse> getAllSources(Boolean active, Pageable pageable);

    SourceArticlesResponse getSourceWithArticles(Long sourceId, Pageable pageable);

    void addSource(AddSourceRequest addSourceRequest);
}