// RssFeedService.java
package com.olh.feeds.service;

import com.olh.feeds.dto.request.article.RssFeedRequest;
import com.olh.feeds.dto.response.article.ArticleResponse;

import java.util.List;

public interface RssFeedService {
    List<ArticleResponse> processRssFeed(RssFeedRequest request);
}