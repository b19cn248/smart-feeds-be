// feeds-dto/src/main/java/com/olh/feeds/dto/request/teamboard/TeamBoardUserRequest.java
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
public class TeamBoardUserRequest {

    @NotBlank(message = "{team.board.user.email.required}")
    private String email;

    @NotBlank(message = "{team.board.user.permission.required}")
    private String permission; // VIEW, EDIT, ADMIN
}