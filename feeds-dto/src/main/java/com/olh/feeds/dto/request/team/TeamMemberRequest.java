package com.olh.feeds.dto.request.team;

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
public class TeamMemberRequest {

    @NotBlank(message = "{team.member.email.required}")
    private String email;

    @NotBlank(message = "{team.member.role.required}")
    private String role; // MEMBER, ADMIN
}