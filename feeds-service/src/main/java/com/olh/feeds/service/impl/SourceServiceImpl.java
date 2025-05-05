// feeds-service/src/main/java/com/olh/feeds/service/impl/SourceServiceImpl.java
package com.olh.feeds.service.impl;

import com.olh.feeds.dao.repository.SourceRepository;
import com.olh.feeds.dto.mapper.PageMapper;
import com.olh.feeds.dto.response.PageResponse;
import com.olh.feeds.dto.response.source.SourceResponse;
import com.olh.feeds.service.SourceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SourceServiceImpl implements SourceService {

    private final SourceRepository sourceRepository;
    private final PageMapper pageMapper;

    @Override
    public PageResponse<SourceResponse> getAllSources(Boolean active, Pageable pageable) {
        log.info("Getting all sources with active status: {}", active);

        Page<SourceResponse> sourcesPage = sourceRepository.findAllSources(active, pageable);

        log.info("Found {} sources", sourcesPage.getNumberOfElements());
        return pageMapper.toPageDto(sourcesPage);
    }
}