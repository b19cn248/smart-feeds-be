package com.olh.feeds.api.controller;

import com.olh.feeds.core.exception.response.ResponseGeneral;
import com.olh.feeds.dto.request.article.RssFeedRequest;
import com.olh.feeds.dto.request.article.RssItemRequest;
import com.olh.feeds.dto.response.PageResponse;
import com.olh.feeds.dto.response.article.ArticleResponse;
import com.olh.feeds.service.ArticleService;
import com.olh.feeds.service.RssFeedService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/api/v1/articles")
@RestController
@Slf4j
@RequiredArgsConstructor
public class ArticleController {

    private final ArticleService articleService;
    private final RssFeedService rssFeedService;

    @GetMapping
    public ResponseGeneral<PageResponse<ArticleResponse>> getAllArticles(
            @PageableDefault Pageable pageable
    ) {

        return ResponseGeneral.of(
                HttpStatus.OK.value(),
                "Success",
                articleService.getAllArticles(pageable)
        );
    }

    @PostMapping("/rss-feed")
    public ResponseGeneral<List<ArticleResponse>> processRssFeed(
            @Validated @RequestBody List<RssItemRequest> items
    ) {
        log.info("Received RSS feed request with {} items", items.size());

        List<ArticleResponse> savedArticles = rssFeedService.processRssFeed(
                RssFeedRequest.builder()
                        .items(items)
                        .build()
        );

        return ResponseGeneral.of(
                HttpStatus.CREATED.value(),
                "rss.feed.processing.success",
                savedArticles
        );
    }
}
