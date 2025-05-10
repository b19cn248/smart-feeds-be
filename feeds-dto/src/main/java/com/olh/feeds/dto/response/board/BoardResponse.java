// feeds-dto/src/main/java/com/olh/feeds/dto/response/board/BoardResponse.java
package com.olh.feeds.dto.response.board;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Builder
public class BoardResponse {
    private Long id;
    private String name;
    private String description;
    private String color;
    private String icon;
    private Boolean isPublic;
    private LocalDateTime createdAt;
}