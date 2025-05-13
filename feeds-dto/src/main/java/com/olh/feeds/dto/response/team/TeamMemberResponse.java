package com.olh.feeds.dto.response.team;

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
public class TeamMemberResponse {
    private Long id;
    private Long teamId;
    private Long userId;
    private String email;
    private String name;
    private String role;
    private LocalDateTime createdAt;
}