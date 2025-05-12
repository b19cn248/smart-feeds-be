// feeds-dto/src/main/java/com/olh/feeds/dto/request/teamboard/TeamBoardRequest.java
package com.olh.feeds.dto.request.teamboard;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Builder
public class TeamBoardRequest {

    @NotBlank(message = "{team.board.name.required}")
    private String name;

    private String description;

    @NotNull(message = "{team.board.team_id.required}")
    private Long teamId;
}