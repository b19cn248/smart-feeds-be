package com.olh.feeds.dto.response.explore;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Builder
public class TrendingTopicResponse {
    private Long id;
    private String topicName;
    private Long categoryId;
    private String categoryName;
    private Float score;
    private LocalDate startDate;
    private LocalDate endDate;
}