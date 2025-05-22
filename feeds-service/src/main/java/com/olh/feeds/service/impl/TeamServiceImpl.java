package com.olh.feeds.service.impl;

import com.olh.feeds.core.exception.base.BadRequestException;
import com.olh.feeds.core.exception.base.ConflictException;
import com.olh.feeds.core.exception.base.ForbiddenException;
import com.olh.feeds.core.exception.base.NotFoundException;
import com.olh.feeds.dao.entity.Team;
import com.olh.feeds.dao.entity.TeamBoard;
import com.olh.feeds.dao.entity.TeamBoardUser;
import com.olh.feeds.dao.entity.TeamUser;
import com.olh.feeds.dao.entity.User;
import com.olh.feeds.dao.repository.TeamBoardRepository;
import com.olh.feeds.dao.repository.TeamBoardUserRepository;
import com.olh.feeds.dao.repository.TeamRepository;
import com.olh.feeds.dao.repository.TeamUserRepository;
import com.olh.feeds.dao.repository.UserRepository;
import com.olh.feeds.dto.mapper.PageMapper;
import com.olh.feeds.dto.request.team.TeamMemberRequest;
import com.olh.feeds.dto.request.team.TeamRequest;
import com.olh.feeds.dto.response.PageResponse;
import com.olh.feeds.dto.response.team.TeamMemberResponse;
import com.olh.feeds.dto.response.team.TeamResponse;
import com.olh.feeds.service.TeamService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TeamServiceImpl implements TeamService {

    private final TeamRepository teamRepository;
    private final TeamUserRepository teamUserRepository;
    private final TeamBoardRepository teamBoardRepository;
    private final TeamBoardUserRepository teamBoardUserRepository;
    private final UserRepository userRepository;
    private final PageMapper pageMapper;
    private final AuditorAware<String> auditorAware;

    private static final String ROLE_ADMIN = "ADMIN";
    private static final String ROLE_MEMBER = "MEMBER";
    private static final List<String> VALID_ROLES = List.of(ROLE_ADMIN, ROLE_MEMBER);

    // Team Board permissions
    private static final String PERMISSION_VIEW = "VIEW";
    private static final String PERMISSION_EDIT = "EDIT";
    private static final String PERMISSION_ADMIN = "ADMIN";

    @Override
    @Transactional(readOnly = true)
    public PageResponse<TeamResponse> getTeamsForCurrentUser(Pageable pageable) {
        log.info("Getting teams for current user");

        // Get current user
        String username = auditorAware.getCurrentAuditor().orElse("anonymous");
        log.info("Current username: {}", username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException(username, "user"));

        // Get teams where user is a member
        Page<Team> teamsPage = teamRepository.findByUserId(user.getId(), pageable);

        // Convert to response DTOs
        List<TeamResponse> teamResponses = teamsPage.getContent().stream()
                .map(this::mapToTeamResponse)
                .collect(Collectors.toList());

        Page<TeamResponse> resultPage = new PageImpl<>(
                teamResponses,
                pageable,
                teamsPage.getTotalElements()
        );

        return pageMapper.toPageDto(resultPage);
    }

    @Override
    @Transactional
    public TeamResponse createTeam(TeamRequest request) {
        log.info("Creating new team: {}", request.getName());

        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new BadRequestException("team.name.required");
        }

        // Get current user
        String username = auditorAware.getCurrentAuditor().orElse("anonymous");
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException(username, "user"));

        // Create team
        Team team = Team.builder()
                .name(request.getName())
                .description(request.getDescription())
                .enterpriseId(request.getEnterpriseId())
                .build();

        team = teamRepository.save(team);
        log.info("Team created with ID: {}", team.getId());

        // Add creator as admin
        TeamUser teamUser = TeamUser.builder()
                .teamId(team.getId())
                .userId(user.getId())
                .role(ROLE_ADMIN)
                .build();

        teamUserRepository.save(teamUser);

        return mapToTeamResponse(team);
    }

    @Override
    @Transactional
    public TeamMemberResponse addTeamMember(Long teamId, TeamMemberRequest request) {
        log.info("Adding member with email: {} to team ID: {}", request.getEmail(), teamId);

        // Check if team exists
        Team team = teamRepository.findActiveById(teamId)
                .orElseThrow(() -> new NotFoundException(teamId.toString(), "team"));

        // Check if user has admin permission in team
        String username = auditorAware.getCurrentAuditor().orElse("anonymous");
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException(username, "user"));

        boolean isAdmin = teamUserRepository.existsByTeamIdAndUserIdAndRole(
                teamId,
                currentUser.getId(),
                ROLE_ADMIN
        );

        if (!isAdmin) {
            log.error("User {} does not have admin permission in team {}", username, teamId);
            throw new ForbiddenException("team.permission.denied");
        }

        // Check if requested role is valid
        if (!VALID_ROLES.contains(request.getRole())) {
            log.error("Invalid role: {}", request.getRole());
            throw new BadRequestException("team.member.role.invalid");
        }

        // Find user to add
        User userToAdd = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new NotFoundException(request.getEmail(), "user"));

        // Check if user is already a member
        Optional<TeamUser> existingMember = teamUserRepository.findByTeamIdAndUserEmail(teamId, request.getEmail());

        TeamUser savedMember;
        if (existingMember.isPresent()) {
            // Update role if different
            TeamUser member = existingMember.get();
            if (!member.getRole().equals(request.getRole())) {
                member.setRole(request.getRole());
                savedMember = teamUserRepository.save(member);
                log.info("Updated role for user {} in team {}", userToAdd.getEmail(), teamId);
            } else {
                throw new ConflictException("team.member.already.exists");
            }
        } else {
            // Add new member
            TeamUser newMember = TeamUser.builder()
                    .teamId(teamId)
                    .userId(userToAdd.getId())
                    .role(request.getRole())
                    .build();

            savedMember = teamUserRepository.save(newMember);
            log.info("Added user {} to team {} with role {}",
                    userToAdd.getEmail(), teamId, request.getRole());
        }

        // SYNC: Add member to all team boards of this team
        syncMemberToTeamBoards(teamId, userToAdd.getId(), request.getRole(), false);

        return mapToTeamMemberResponse(savedMember, userToAdd);
    }

    @Override
    @Transactional
    public void removeTeamMember(Long teamId, Long userId) {
        log.info("Removing member ID: {} from team ID: {}", userId, teamId);

        // Check if team exists
        Team team = teamRepository.findActiveById(teamId)
                .orElseThrow(() -> new NotFoundException(teamId.toString(), "team"));

        // Check if current user has admin permission in team
        String username = auditorAware.getCurrentAuditor().orElse("anonymous");
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException(username, "user"));

        boolean isAdmin = teamUserRepository.existsByTeamIdAndUserIdAndRole(
                teamId,
                currentUser.getId(),
                ROLE_ADMIN
        );

        if (!isAdmin) {
            log.error("User {} does not have admin permission in team {}", username, teamId);
            throw new ForbiddenException("team.permission.denied");
        }

        // Find team member to remove
        TeamUser teamMember = teamUserRepository.findByTeamIdAndUserId(teamId, userId)
                .orElseThrow(() -> new NotFoundException(userId.toString(), "team_member"));

        // Get user info for logging
        User userToRemove = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(userId.toString(), "user"));

        // Don't allow removing team creator (first admin)
        if (team.getCreatedBy().equals(userToRemove.getEmail())) {
            log.error("Cannot remove team creator from team");
            throw new ForbiddenException("team.creator.remove.denied");
        }

        // Soft delete team member
        teamMember.setIsDeleted(true);
        teamUserRepository.save(teamMember);

        log.info("Removed user {} from team {}", userToRemove.getEmail(), teamId);

        // SYNC: Remove member from all team boards of this team
        syncMemberToTeamBoards(teamId, userId, null, true);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TeamMemberResponse> getTeamMembers(Long teamId) {
        log.info("Getting members for team ID: {}", teamId);

        // Check if team exists
        if (!teamRepository.existsActiveById(teamId)) {
            throw new NotFoundException(teamId.toString(), "team");
        }

        // Check if current user has access to team
        String username = auditorAware.getCurrentAuditor().orElse("anonymous");
        if (!teamUserRepository.existsByTeamIdAndUserEmail(teamId, username)) {
            log.error("User {} does not have access to team {}", username, teamId);
            throw new ForbiddenException("team.access.denied");
        }

        return teamUserRepository.getMembersByTeamId(teamId);
    }

    /**
     * Sync team member addition/removal to all team boards of the team
     *
     * @param teamId Team ID
     * @param userId User ID to sync
     * @param teamRole User's role in team (used when adding)
     * @param isRemoval true for removal, false for addition
     */
    private void syncMemberToTeamBoards(Long teamId, Long userId, String teamRole, boolean isRemoval) {
        log.info("Syncing member ID: {} to team boards of team ID: {}, removal: {}",
                userId, teamId, isRemoval);

        // Get all team boards of this team
        List<TeamBoard> teamBoards = teamBoardRepository.findByTeamIdWithoutPagination(teamId);

        if (teamBoards.isEmpty()) {
            log.info("No team boards found for team ID: {}", teamId);
            return;
        }

        if (isRemoval) {
            // Remove user from all team boards
            removeUserFromTeamBoards(teamBoards, userId);
        } else {
            // Add user to all team boards
            addUserToTeamBoards(teamBoards, userId, teamRole);
        }

        log.info("Successfully synced member ID: {} to {} team boards",
                userId, teamBoards.size());
    }

    /**
     * Add user to multiple team boards with appropriate permission
     */
    private void addUserToTeamBoards(List<TeamBoard> teamBoards, Long userId, String teamRole) {
        List<TeamBoardUser> teamBoardUsersToAdd = new ArrayList<>();

        for (TeamBoard teamBoard : teamBoards) {
            // Check if user already exists in team board
            Optional<TeamBoardUser> existingBoardUser = teamBoardUserRepository
                    .findByTeamBoardIdAndUserId(teamBoard.getId(), userId);

            if (existingBoardUser.isEmpty()) {
                // Determine permission based on team role
                String permission = ROLE_ADMIN.equals(teamRole) ? PERMISSION_EDIT : PERMISSION_VIEW;

                TeamBoardUser teamBoardUser = TeamBoardUser.builder()
                        .teamBoardId(teamBoard.getId())
                        .userId(userId)
                        .permission(permission)
                        .build();

                teamBoardUsersToAdd.add(teamBoardUser);
            }
        }

        // Batch insert for better performance
        if (!teamBoardUsersToAdd.isEmpty()) {
            teamBoardUserRepository.saveAll(teamBoardUsersToAdd);
            log.info("Added user ID: {} to {} team boards", userId, teamBoardUsersToAdd.size());
        }
    }

    /**
     * Remove user from multiple team boards
     */
    private void removeUserFromTeamBoards(List<TeamBoard> teamBoards, Long userId) {
        List<TeamBoardUser> teamBoardUsersToUpdate = new ArrayList<>();

        for (TeamBoard teamBoard : teamBoards) {
            Optional<TeamBoardUser> boardUser = teamBoardUserRepository
                    .findByTeamBoardIdAndUserId(teamBoard.getId(), userId);

            if (boardUser.isPresent()) {
                TeamBoardUser teamBoardUser = boardUser.get();
                teamBoardUser.setIsDeleted(true);
                teamBoardUsersToUpdate.add(teamBoardUser);
            }
        }

        // Batch update for better performance
        if (!teamBoardUsersToUpdate.isEmpty()) {
            teamBoardUserRepository.saveAll(teamBoardUsersToUpdate);
            log.info("Removed user ID: {} from {} team boards", userId, teamBoardUsersToUpdate.size());
        }
    }

    /**
     * Helper method to map Team entity to TeamResponse DTO
     */
    private TeamResponse mapToTeamResponse(Team team) {
        return TeamResponse.builder()
                .id(team.getId())
                .name(team.getName())
                .description(team.getDescription())
                .enterpriseId(team.getEnterpriseId())
                .createdAt(team.getCreatedAt())
                .createdBy(team.getCreatedBy())
                .build();
    }

    /**
     * Helper method to map TeamUser entity to TeamMemberResponse DTO
     */
    private TeamMemberResponse mapToTeamMemberResponse(TeamUser teamUser, User user) {
        return TeamMemberResponse.builder()
                .id(teamUser.getId())
                .teamId(teamUser.getTeamId())
                .userId(teamUser.getUserId())
                .email(user.getEmail())
                .name(user.getName())
                .role(teamUser.getRole())
                .createdAt(teamUser.getCreatedAt())
                .build();
    }
}