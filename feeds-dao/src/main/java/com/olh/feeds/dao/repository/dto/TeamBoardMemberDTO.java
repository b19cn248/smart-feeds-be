package com.olh.feeds.dao.repository.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TeamBoardMemberDTO {
    private final Long userId;
    private final String name;
    private final String email;
} 