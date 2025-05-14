package com.olh.feeds.service;

import com.olh.feeds.dto.request.team.TeamMemberRequest;
import com.olh.feeds.dto.request.team.TeamRequest;
import com.olh.feeds.dto.response.PageResponse;
import com.olh.feeds.dto.response.team.TeamMemberResponse;
import com.olh.feeds.dto.response.team.TeamResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface TeamService {
    /**
     * Get teams for current user
     *
     * @param pageable Pagination information
     * @return List of teams
     */
    PageResponse<TeamResponse> getTeamsForCurrentUser(Pageable pageable);

    /**
     * Create a new team
     *
     * @param request Team creation request
     * @return Created team
     */
    TeamResponse createTeam(TeamRequest request);

    /**
     * Add member to team
     *
     * @param teamId Team ID
     * @param request Member request with email and role
     * @return Added team member
     */
    TeamMemberResponse addTeamMember(Long teamId, TeamMemberRequest request);

    List<TeamMemberResponse> getTeamMembers(Long teamId);
}