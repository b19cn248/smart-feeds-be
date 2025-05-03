package com.olh.feeds.service.impl;

import com.olh.feeds.dao.repository.ArticleRepository;
import com.olh.feeds.dto.mapper.PageMapper;
import com.olh.feeds.dto.response.PageResponse;
import com.olh.feeds.dto.response.article.ArticleResponse;
import com.olh.feeds.service.ArticleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ArticleServiceImpl implements ArticleService {

    private final ArticleRepository articleRepository;
    private final PageMapper pageMapper;

    @Override
    public PageResponse<ArticleResponse> getAllArticles(Pageable pageable) {

        log.info("getAllArticles");

        Page<ArticleResponse> articleResponsePage =
                articleRepository.findAllByOrderByCreatedAtDesc(pageable);

        return pageMapper.toPageDto(articleResponsePage);
    }
}
