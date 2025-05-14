package com.olh.feeds.api.controller;

import com.olh.feeds.core.exception.response.ResponseGeneral;
import com.olh.feeds.dto.request.team.TeamMemberRequest;
import com.olh.feeds.dto.request.team.TeamRequest;
import com.olh.feeds.dto.response.PageResponse;
import com.olh.feeds.dto.response.team.TeamMemberResponse;
import com.olh.feeds.dto.response.team.TeamResponse;
import com.olh.feeds.service.TeamService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/teams")
@Slf4j
@RequiredArgsConstructor
public class TeamController {

    private final TeamService teamService;

    /**
     * Get teams for current user
     */
    @GetMapping
    public ResponseGeneral<PageResponse<TeamResponse>> getTeams(
            @PageableDefault Pageable pageable
    ) {
        log.info("REST request to get teams for current user");
        return ResponseGeneral.of(
                HttpStatus.OK.value(),
                "team.list.success",
                teamService.getTeamsForCurrentUser(pageable)
        );
    }

    /**
     * Get teams for current user
     */
    @GetMapping("/{id}/members")
    public ResponseGeneral<List<TeamMemberResponse>> getMembersOfTeam(
            @PathVariable Long id
    ) {
        log.info("REST request to get members of team");
        return ResponseGeneral.of(
                HttpStatus.OK.value(),
                "team.list.success",
                teamService.getTeamMembers(id)
        );
    }

    /**
     * Create new team
     */
    @PostMapping
    public ResponseGeneral<TeamResponse> createTeam(
            @Valid @RequestBody TeamRequest request
    ) {
        log.info("REST request to create team: {}", request.getName());
        return ResponseGeneral.of(
                HttpStatus.CREATED.value(),
                "team.create.success",
                teamService.createTeam(request)
        );
    }

    /**
     * Add member to team
     */
    @PostMapping("/{teamId}/members")
    public ResponseGeneral<TeamMemberResponse> addTeamMember(
            @PathVariable("teamId") Long teamId,
            @Valid @RequestBody TeamMemberRequest request
    ) {
        log.info("REST request to add member to team ID: {}", teamId);
        return ResponseGeneral.of(
                HttpStatus.OK.value(),
                "team.member.add.success",
                teamService.addTeamMember(teamId, request)
        );
    }
}