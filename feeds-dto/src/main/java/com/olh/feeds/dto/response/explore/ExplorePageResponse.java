package com.olh.feeds.dto.response.explore;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.olh.feeds.dto.response.article.ArticleResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Builder
public class ExplorePageResponse {
    private List<ExploreCollectionResponse> collections;
    private List<ArticleResponse> topStories;
    private List<TrendingTopicResponse> trendingTopics;
}