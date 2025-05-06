// feeds-dto/src/main/java/com/olh/feeds/dto/response/folder/FolderResponse.java
package com.olh.feeds.dto.response.folder;

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
public class FolderResponse {
    private Long id;
    private String name;
    private String theme;
    private Long userId;
    private LocalDateTime createdAt;
}