// feeds-dto/src/main/java/com/olh/feeds/dto/request/board/BoardRequest.java
package com.olh.feeds.dto.request.board;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Builder
public class BoardRequest {

    @NotBlank(message = "{board.name.required}")
    private String name;

    private String description;

    private String color;

    private String icon;

    private Boolean isPublic;
}