// feeds-service/src/main/java/com/olh/feeds/service/impl/TeamBoardServiceImpl.java
package com.olh.feeds.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.olh.feeds.core.email.service.MailService;
import com.olh.feeds.core.exception.base.BadRequestException;
import com.olh.feeds.core.exception.base.ConflictException;
import com.olh.feeds.core.exception.base.ForbiddenException;
import com.olh.feeds.core.exception.base.NotFoundException;
import com.olh.feeds.dao.entity.*;
import com.olh.feeds.dao.repository.*;
import com.olh.feeds.dto.mapper.PageMapper;
import com.olh.feeds.dto.request.teamboard.*;
import com.olh.feeds.dto.response.PageResponse;
import com.olh.feeds.dto.response.article.ArticleResponse;
import com.olh.feeds.dto.response.notification.NotificationDto;
import com.olh.feeds.dto.response.teamboard.*;
import com.olh.feeds.service.TeamBoardService;
import com.olh.feeds.service.WebSocketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TeamBoardServiceImpl implements TeamBoardService {

    private final TeamBoardRepository teamBoardRepository;
    private final TeamRepository teamRepository;
    private final TeamUserRepository teamUserRepository;
    private final TeamBoardUserRepository teamBoardUserRepository;
    private final TeamBoardArticleRepository teamBoardArticleRepository;
    private final TeamBoardNoteRepository teamBoardNoteRepository;
    private final TeamBoardHighlightRepository teamBoardHighlightRepository;
    private final TeamBoardNewsletterRepository teamBoardNewsletterRepository;
    private final ArticleRepository articleRepository;
    private final UserRepository userRepository;
    private final MailService mailService;
    private final PageMapper pageMapper;
    private final AuditorAware<String> auditorAware;
    private final ObjectMapper objectMapper;
    private final WebSocketService webSocketService;

    private static final String PERMISSION_VIEW = "VIEW";
    private static final String PERMISSION_EDIT = "EDIT";
    private static final String PERMISSION_ADMIN = "ADMIN";
    private static final List<String> ALL_PERMISSIONS = List.of(PERMISSION_VIEW, PERMISSION_EDIT, PERMISSION_ADMIN);
    private static final List<String> EDIT_PERMISSIONS = List.of(PERMISSION_EDIT, PERMISSION_ADMIN);

    @Override
    @Transactional
    public TeamBoardResponse createTeamBoard(TeamBoardRequest request) {
        log.info("Creating new team board: {}", request.getName());

        // Validate team exists
        Team team = teamRepository.findById(request.getTeamId())
                .orElseThrow(() -> new NotFoundException(request.getTeamId().toString(), "team"));

        // Check if user has permission in team
        String username = auditorAware.getCurrentAuditor().orElse("anonymous");
        if (!teamUserRepository.existsByTeamIdAndUserEmail(team.getId(), username)) {
            log.error("User {} is not a member of team {}", username, team.getId());
            throw new ForbiddenException("team.access.denied");
        }

        // Create team board
        TeamBoard teamBoard = TeamBoard.builder()
                .name(request.getName())
                .description(request.getDescription())
                .teamId(request.getTeamId())
                .build();

        teamBoard = teamBoardRepository.save(teamBoard);
        log.info("Team board created with ID: {}", teamBoard.getId());

        // Get current user info
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException(username, "user"));

        // REFACTOR: Add all team members to team board
        addAllTeamMembersToTeamBoard(teamBoard.getId(), team.getId(), currentUser.getId());

        return TeamBoardResponse.builder()
                .id(teamBoard.getId())
                .name(teamBoard.getName())
                .description(teamBoard.getDescription())
                .teamId(teamBoard.getTeamId())
                .teamName(team.getName())
                .createdAt(teamBoard.getCreatedAt())
                .createdBy(username)
                .build();
    }

    /**
     * Helper method to add all team members to team board with appropriate permissions
     * Creator gets ADMIN permission, other members get VIEW permission
     *
     * @param teamBoardId ID of the newly created team board
     * @param teamId ID of the team
     * @param creatorUserId ID of the user creating the team board
     */
    private void addAllTeamMembersToTeamBoard(Long teamBoardId, Long teamId, Long creatorUserId) {
        log.info("Adding all team members to team board ID: {} from team ID: {}", teamBoardId, teamId);

        // Get all team members - optimized single query
        List<TeamUser> teamMembers = teamUserRepository.findByTeamId(teamId);

        if (teamMembers.isEmpty()) {
            log.warn("No team members found for team ID: {}", teamId);
            return;
        }

        // Prepare team board users for batch insert
        List<TeamBoardUser> teamBoardUsers = new ArrayList<>();

        for (TeamUser teamMember : teamMembers) {
            // Determine permission: Creator gets ADMIN, others get VIEW
            String permission = teamMember.getUserId().equals(creatorUserId)
                    ? PERMISSION_ADMIN
                    : PERMISSION_VIEW;

            TeamBoardUser teamBoardUser = TeamBoardUser.builder()
                    .teamBoardId(teamBoardId)
                    .userId(teamMember.getUserId())
                    .permission(permission)
                    .build();

            teamBoardUsers.add(teamBoardUser);
        }

        // Batch insert for better performance
        teamBoardUserRepository.saveAll(teamBoardUsers);

        log.info("Successfully added {} team members to team board ID: {}",
                teamBoardUsers.size(), teamBoardId);
    }

    @Override
    @Transactional
    public TeamBoardResponse updateTeamBoard(Long id, TeamBoardRequest request) {
        log.info("Updating team board ID: {}", id);

        // Get team board with permission check
        TeamBoard teamBoard = getTeamBoardWithPermissionCheck(id, EDIT_PERMISSIONS);

        // Check if team exists
        Team team = teamRepository.findById(request.getTeamId())
                .orElseThrow(() -> new NotFoundException(request.getTeamId().toString(), "team"));

        // Update team board
        teamBoard.setName(request.getName());
        teamBoard.setDescription(request.getDescription());
        teamBoard.setTeamId(request.getTeamId());

        teamBoard = teamBoardRepository.save(teamBoard);
        log.info("Team board updated with ID: {}", teamBoard.getId());

        return TeamBoardResponse.builder()
                .id(teamBoard.getId())
                .name(teamBoard.getName())
                .description(teamBoard.getDescription())
                .teamId(teamBoard.getTeamId())
                .teamName(team.getName())
                .createdAt(teamBoard.getCreatedAt())
                .createdBy(teamBoard.getCreatedBy())
                .build();
    }

    @Override
    @Transactional
    public void deleteTeamBoard(Long id) {
        log.info("Deleting team board ID: {}", id);

        // Get team board
        TeamBoard teamBoard = teamBoardRepository.findActiveById(id)
                .orElseThrow(() -> new NotFoundException(id.toString(), "team_board"));

        // Check if user is the creator (only creator can delete)
        String username = auditorAware.getCurrentAuditor().orElse("anonymous");
        if (!teamBoard.getCreatedBy().equals(username)) {
            log.error("User {} is not the creator of team board {}", username, id);
            throw new ForbiddenException("team.board.delete.denied");
        }

        // Soft delete
        teamBoard.setIsDeleted(true);
        teamBoardRepository.save(teamBoard);

        log.info("Team board deleted with ID: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public TeamBoardDetailResponse getTeamBoardDetail(Long id, Pageable pageable) {
        log.info("Getting team board detail for ID: {}", id);

        // Get team board
        TeamBoardResponse teamBoard = teamBoardRepository.findTeamBoardById(id)
                .orElseThrow(() -> new NotFoundException(id.toString(), "team_board"));

        // Check user permission
        String username = auditorAware.getCurrentAuditor().orElse("anonymous");
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException(username, "user"));

        String permission = teamBoardUserRepository.findPermissionByTeamBoardIdAndUserId(id, user.getId())
                .orElse(null);

        if (permission == null) {
            log.error("User {} does not have access to team board {}", username, id);
            throw new ForbiddenException("team.board.access.denied");
        }

        // Get team board members
        List<TeamBoardUserResponse> members = teamBoardUserRepository.findByTeamBoardId(id);

        // Get articles
        Page<ArticleResponse> articlesPage = teamBoardArticleRepository.findArticlesByTeamBoardId(id, pageable);

        return TeamBoardDetailResponse.builder()
                .id(teamBoard.getId())
                .name(teamBoard.getName())
                .description(teamBoard.getDescription())
                .teamId(teamBoard.getTeamId())
                .teamName(teamBoard.getTeamName())
                .createdAt(teamBoard.getCreatedAt())
                .createdBy(teamBoard.getCreatedBy())
                .userPermission(permission)
                .members(members)
                .articles(pageMapper.toPageDto(articlesPage))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<TeamBoardResponse> getTeamBoardsForCurrentUser(Pageable pageable) {
        log.info("Getting team boards for current user");

        // Get user ID
        String username = auditorAware.getCurrentAuditor().orElse("anonymous");
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException(username, "user"));

        // Get boards where user is a member
        Page<TeamBoardResponse> boardsPage = teamBoardRepository.findByBoardUserId(user.getId(), pageable);

        return pageMapper.toPageDto(boardsPage);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<TeamBoardResponse> getTeamBoardsByTeamId(Long teamId, Pageable pageable) {
        log.info("Getting team boards for team ID: {}", teamId);

        // Check if team exists
        if (!teamRepository.existsById(teamId)) {
            throw new NotFoundException(teamId.toString(), "team");
        }

        // Check if user is a member of the team
        String username = auditorAware.getCurrentAuditor().orElse("anonymous");
        if (!teamUserRepository.existsByTeamIdAndUserEmail(teamId, username)) {
            log.error("User {} is not a member of team {}", username, teamId);
            throw new ForbiddenException("team.access.denied");
        }

        // Get team boards for team
        Page<TeamBoardResponse> boardsPage = teamBoardRepository.findByTeamId(teamId, pageable);

        return pageMapper.toPageDto(boardsPage);
    }

    @Override
    @Transactional
    public List<TeamBoardUserResponse> shareTeamBoard(Long id, TeamBoardUserRequest request) {
        log.info("Sharing team board ID: {} with user: {}", id, request.getEmail());

        // Check if team board exists and user has admin permission
        TeamBoard teamBoard = getTeamBoardWithPermissionCheck(id, List.of(PERMISSION_ADMIN));

        // Check if requested permission is valid
        if (!ALL_PERMISSIONS.contains(request.getPermission())) {
            log.error("Invalid permission: {}", request.getPermission());
            throw new BadRequestException("team.board.permission.invalid");
        }

        // Find user to share with
        User targetUser = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new NotFoundException(request.getEmail(), "user"));

        // Check if user is already a member
        Optional<TeamBoardUser> existingMember = teamBoardUserRepository.findByTeamBoardIdAndUserId(id, targetUser.getId());

        if (existingMember.isPresent()) {
            // Update permission if different
            TeamBoardUser member = existingMember.get();
            if (!member.getPermission().equals(request.getPermission())) {
                member.setPermission(request.getPermission());
                teamBoardUserRepository.save(member);
                log.info("Updated permission for user {} on team board {}", targetUser.getEmail(), id);
            }
        } else {
            // Add new member
            TeamBoardUser newMember = TeamBoardUser.builder()
                    .teamBoardId(id)
                    .userId(targetUser.getId())
                    .permission(request.getPermission())
                    .build();

            teamBoardUserRepository.save(newMember);
            log.info("Added user {} to team board {} with permission {}",
                    targetUser.getEmail(), id, request.getPermission());
        }

        // Return updated members list
        return teamBoardUserRepository.findByTeamBoardId(id);
    }

    @Override
    @Transactional
    public TeamBoardUserResponse updateTeamBoardMember(Long id, Long userId, TeamBoardUserRequest request) {
        log.info("Updating team board ID: {} member ID: {} permission", id, userId);

        // Check if team board exists and user has admin permission
        TeamBoard teamBoard = getTeamBoardWithPermissionCheck(id, List.of(PERMISSION_ADMIN));

        // Check if requested permission is valid
        if (!ALL_PERMISSIONS.contains(request.getPermission())) {
            log.error("Invalid permission: {}", request.getPermission());
            throw new BadRequestException("team.board.permission.invalid");
        }

        // Find team board user
        TeamBoardUser member = teamBoardUserRepository.findByTeamBoardIdAndUserId(id, userId)
                .orElseThrow(() -> new NotFoundException(userId.toString(), "team_board_user"));

        // Don't allow changing creator's permission
        if (teamBoard.getCreatedBy().equals(userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(userId.toString(), "user"))
                .getEmail())) {
            log.error("Cannot change creator's permission");
            throw new ForbiddenException("team.board.creator.permission.change.denied");
        }

        // Update permission
        member.setPermission(request.getPermission());
        teamBoardUserRepository.save(member);

        // Get updated member data
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(userId.toString(), "user"));

        return TeamBoardUserResponse.builder()
                .id(member.getId())
                .teamBoardId(member.getTeamBoardId())
                .userId(member.getUserId())
                .email(user.getEmail())
                .name(user.getName())
                .permission(member.getPermission())
                .createdAt(member.getCreatedAt())
                .build();
    }

    @Override
    @Transactional
    public void removeTeamBoardMember(Long id, Long userId) {
        log.info("Removing user ID: {} from team board ID: {}", userId, id);

        // Check if team board exists and user has admin permission
        TeamBoard teamBoard = getTeamBoardWithPermissionCheck(id, List.of(PERMISSION_ADMIN));

        // Find team board user
        TeamBoardUser member = teamBoardUserRepository.findByTeamBoardIdAndUserId(id, userId)
                .orElseThrow(() -> new NotFoundException(userId.toString(), "team_board_user"));

        // Don't allow removing creator
        if (teamBoard.getCreatedBy().equals(userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(userId.toString(), "user"))
                .getEmail())) {
            log.error("Cannot remove creator from team board");
            throw new ForbiddenException("team.board.creator.remove.denied");
        }

        // Soft delete
        member.setIsDeleted(true);
        teamBoardUserRepository.save(member);

        log.info("Removed user ID: {} from team board ID: {}", userId, id);
    }

    @Override
    @Transactional
    public PageResponse<ArticleResponse> addArticleToTeamBoard(Long id, TeamBoardArticleRequest request) {
        log.info("Adding article ID: {} to team board ID: {}", request.getArticleId(), id);

        // Check if team board exists and user has edit permission
        TeamBoard teamBoard = getTeamBoardWithPermissionCheck(id, EDIT_PERMISSIONS);

        // Check if article exists
        Article article = articleRepository.findById(request.getArticleId())
                .orElseThrow(() -> new NotFoundException(request.getArticleId().toString(), "article"));

        // Check if article is already in board
        boolean exists = teamBoardArticleRepository.existsByTeamBoardIdAndArticleIdAndIsDeletedFalse(
                id, request.getArticleId());

        if (exists) {
            log.error("Article already exists in team board");
            throw new ConflictException("team.board.article.already.exists");
        }

        // Add article to board
        TeamBoardArticle teamBoardArticle = TeamBoardArticle.builder()
                .teamBoardId(id)
                .articleId(request.getArticleId())
                .addedAt(LocalDateTime.now())
                .build();

        teamBoardArticleRepository.save(teamBoardArticle);
        log.info("Article added to team board successfully");

        // Return updated articles list
        Page<ArticleResponse> articlesPage = teamBoardArticleRepository.findArticlesByTeamBoardId(
                id, Pageable.unpaged());

        return pageMapper.toPageDto(articlesPage);
    }

    @Override
    @Transactional
    public void removeArticleFromTeamBoard(Long id, Long articleId) {
        log.info("Removing article ID: {} from team board ID: {}", articleId, id);

        // Check if team board exists and user has edit permission
        TeamBoard teamBoard = getTeamBoardWithPermissionCheck(id, EDIT_PERMISSIONS);

        // Find team board article
        TeamBoardArticle teamBoardArticle = teamBoardArticleRepository.findByTeamBoardIdAndArticleId(id, articleId)
                .orElseThrow(() -> new NotFoundException(articleId.toString(), "team_board_article"));

        // Soft delete
        teamBoardArticle.setIsDeleted(true);
        teamBoardArticleRepository.save(teamBoardArticle);

        log.info("Article removed from team board successfully");
    }

    @Override
    @Transactional
    public TeamBoardNoteResponse addNoteToArticle(Long id, TeamBoardNoteRequest request, Long recipientId) {
        log.info("Adding note to article ID: {} in team board ID: {}", request.getArticleId(), id);

        // Check if team board exists and user has edit permission
        TeamBoard teamBoard = getTeamBoardWithPermissionCheck(id, EDIT_PERMISSIONS);

        // Check if article exists in team board
        if (!teamBoardArticleRepository.existsByTeamBoardIdAndArticleIdAndIsDeletedFalse(
                id, request.getArticleId())) {
            log.error("Article not found in team board");
            throw new NotFoundException(request.getArticleId().toString(), "team_board_article");
        }

        // Get mentioned users
        Set<String> mentionedUsers = new HashSet<>();
        if (request.getMentionedUserIds() != null && !request.getMentionedUserIds().isEmpty()) {
            List<User> users = userRepository.findAllById(request.getMentionedUserIds());
            mentionedUsers = users.stream()
                    .map(User::getEmail)
                    .collect(Collectors.toSet());
        }

        // Save note
        final TeamBoardNote note = TeamBoardNote.builder()
                .teamBoardId(id)
                .articleId(request.getArticleId())
                .content(request.getContent())
                .mentionedUsers(mentionedUsers.isEmpty() ? null : String.join(",", mentionedUsers))
                .build();

        teamBoardNoteRepository.save(note);
        log.info("Note added to article successfully with ID: {}", note.getId());

        // Get current user info
        String username = note.getCreatedBy();
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException(username, "user"));

        // Get article details
        ArticleResponse article = articleRepository.findArticleById(note.getArticleId())
                .orElseThrow(() -> new NotFoundException(note.getArticleId().toString(), "article"));

        // Send notifications to mentioned users
        if (!mentionedUsers.isEmpty()) {
            for (String mentionedUser : mentionedUsers) {
                User user = userRepository.findByEmail(mentionedUser)
                        .orElse(null);
                if (user != null) {
                    NotificationDto notification = NotificationDto.builder()
                            .title("Bạn được nhắc đến trong một ghi chú")
                            .content(String.format("%s đã nhắc đến bạn trong một ghi chú về bài viết \"%s\" trong bảng \"%s\":\n\n%s",
                                    currentUser.getName(),
                                    article.getTitle(),
                                    teamBoard.getName(),
                                    note.getContent()))
                            .titleEn("You were mentioned in a note")
                            .contentEn(String.format("%s mentioned you in a note about article \"%s\" in board \"%s\":\n\n%s",
                                    currentUser.getName(),
                                    article.getTitle(),
                                    teamBoard.getName(),
                                    note.getContent()))
                            .url(String.format("/team-boards/%d/articles/%d", teamBoard.getId(), article.getId()))
                            .timestamp(LocalDateTime.now())
                            .notificationType("MENTION_IN_NOTE")
                            .senderId(currentUser.getId())
                            .recipientId(user.getId())
                            .build();

                    webSocketService.sendNotification(List.of(user.getEmail()), notification);
                }
            }
        }

        // Send notification to specific recipient if provided
        if (recipientId != null) {
            User recipient = userRepository.findById(recipientId)
                    .orElse(null);
            if (recipient != null) {
                NotificationDto notification = NotificationDto.builder()
                        .title("Bạn có một ghi chú mới")
                        .content(String.format("%s đã thêm một ghi chú về bài viết \"%s\" trong bảng \"%s\":\n\n%s",
                                currentUser.getName(),
                                article.getTitle(),
                                teamBoard.getName(),
                                note.getContent()))
                        .titleEn("You have a new note")
                        .contentEn(String.format("%s added a note about article \"%s\" in board \"%s\":\n\n%s",
                                currentUser.getName(),
                                article.getTitle(),
                                teamBoard.getName(),
                                note.getContent()))
                        .url(String.format("/team-boards/%d/articles/%d", teamBoard.getId(), article.getId()))
                        .timestamp(LocalDateTime.now())
                        .notificationType("NEW_NOTE")
                        .senderId(currentUser.getId())
                        .recipientId(recipient.getId())
                        .build();

                webSocketService.sendNotification(List.of(recipient.getEmail()), notification);
            }
        }

        return TeamBoardNoteResponse.builder()
                .id(note.getId())
                .teamBoardId(note.getTeamBoardId())
                .articleId(note.getArticleId())
                .content(note.getContent())
                .mentionedUsers(note.getMentionedUsers())
                .createdByEmail(username)
                .createdByName(currentUser.getName())
                .createdAt(note.getCreatedAt())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TeamBoardNoteResponse> getArticleNotes(Long id, Long articleId) {
        log.info("Getting notes for article ID: {} in team board ID: {}", articleId, id);

        // Check if team board exists and user has view permission
        TeamBoard teamBoard = getTeamBoardWithPermissionCheck(id, ALL_PERMISSIONS);

        // Check if article exists in team board
        if (!teamBoardArticleRepository.existsByTeamBoardIdAndArticleIdAndIsDeletedFalse(
                id, articleId)) {
            log.error("Article not found in team board");
            throw new NotFoundException(articleId.toString(), "team_board_article");
        }

        // Get notes
        return teamBoardNoteRepository.findByTeamBoardIdAndArticleId(id, articleId);
    }

    @Override
    @Transactional
    public TeamBoardHighlightResponse addHighlightToArticle(Long id, TeamBoardHighlightRequest request) {
        log.info("Adding highlight to article ID: {} in team board ID: {}", request.getArticleId(), id);

        // Check if team board exists and user has edit permission
        TeamBoard teamBoard = getTeamBoardWithPermissionCheck(id, EDIT_PERMISSIONS);

        // Check if article exists in team board
        if (!teamBoardArticleRepository.existsByTeamBoardIdAndArticleIdAndIsDeletedFalse(
                id, request.getArticleId())) {
            log.error("Article not found in team board");
            throw new NotFoundException(request.getArticleId().toString(), "team_board_article");
        }

        // Save highlight
        TeamBoardHighlight highlight = TeamBoardHighlight.builder()
                .teamBoardId(id)
                .articleId(request.getArticleId())
                .highlightText(request.getHighlightText())
                .positionInfo(request.getPositionInfo())
                .build();

        highlight = teamBoardHighlightRepository.save(highlight);
        log.info("Highlight added to article successfully with ID: {}", highlight.getId());

        // Get creator info
        String username = highlight.getCreatedBy();
        String creatorName = userRepository.findByUsername(username)
                .map(User::getName)
                .orElse(username);

        return TeamBoardHighlightResponse.builder()
                .id(highlight.getId())
                .teamBoardId(highlight.getTeamBoardId())
                .articleId(highlight.getArticleId())
                .highlightText(highlight.getHighlightText())
                .positionInfo(highlight.getPositionInfo())
                .createdByEmail(username)
                .createdByName(creatorName)
                .createdAt(highlight.getCreatedAt())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TeamBoardHighlightResponse> getArticleHighlights(Long id, Long articleId) {
        log.info("Getting highlights for article ID: {} in team board ID: {}", articleId, id);

        // Check if team board exists and user has view permission
        TeamBoard teamBoard = getTeamBoardWithPermissionCheck(id, ALL_PERMISSIONS);

        // Check if article exists in team board
        if (!teamBoardArticleRepository.existsByTeamBoardIdAndArticleIdAndIsDeletedFalse(
                id, articleId)) {
            log.error("Article not found in team board");
            throw new NotFoundException(articleId.toString(), "team_board_article");
        }

        // Get highlights
        return teamBoardHighlightRepository.findByTeamBoardIdAndArticleId(id, articleId);
    }

    @Override
    @Transactional
    public TeamBoardNewsletterResponse createNewsletter(Long id, TeamBoardNewsletterRequest request) {
        log.info("Creating newsletter for team board ID: {}", id);

        // Check if team board exists and user has edit permission
        TeamBoard teamBoard = getTeamBoardWithPermissionCheck(id, EDIT_PERMISSIONS);

        // Validate schedule type
        String scheduleType = request.getScheduleType();
        if (scheduleType != null && !List.of("IMMEDIATE", "DAILY", "WEEKLY", "MONTHLY").contains(scheduleType)) {
            log.error("Invalid schedule type: {}", scheduleType);
            throw new BadRequestException("team.board.newsletter.schedule_type.invalid");
        }

        // Calculate next run time
        LocalDateTime nextRunTime = null;
        if ("IMMEDIATE".equals(scheduleType) || scheduleType == null) {
            nextRunTime = LocalDateTime.now();
        } else {
            nextRunTime = calculateNextRunTime(scheduleType);
        }

        // Convert article IDs to JSON string
        String includedArticles = null;
        try {
            if (request.getArticleIds() != null && !request.getArticleIds().isEmpty()) {
                includedArticles = objectMapper.writeValueAsString(request.getArticleIds());
            }
        } catch (JsonProcessingException e) {
            log.error("Error serializing article IDs", e);
            throw new BadRequestException("team.board.newsletter.article_ids.invalid");
        }

        // Convert recipients to string
        String recipients = String.join(",", request.getRecipients());

        // Save newsletter
        TeamBoardNewsletter newsletter = TeamBoardNewsletter.builder()
                .teamBoardId(id)
                .title(request.getTitle())
                .recipients(recipients)
                .includedArticles(includedArticles)
                .scheduleType(scheduleType != null ? scheduleType : "IMMEDIATE")
                .nextRunTime(nextRunTime)
                .isActive(true)
                .build();

        newsletter = teamBoardNewsletterRepository.save(newsletter);
        log.info("Newsletter created successfully with ID: {}", newsletter.getId());

        // Send immediately if scheduled for now
        if ("IMMEDIATE".equals(newsletter.getScheduleType()) || newsletter.getScheduleType() == null) {
            sendNewsletter(newsletter);
        }

        // Convert JSON string back to list for response
        List<Long> articleIds = new ArrayList<>();
        try {
            if (newsletter.getIncludedArticles() != null) {
                articleIds = objectMapper.readValue(newsletter.getIncludedArticles(),
                        new TypeReference<List<Long>>() {
                        });
            }
        } catch (JsonProcessingException e) {
            log.error("Error deserializing article IDs", e);
        }

        return TeamBoardNewsletterResponse.builder()
                .id(newsletter.getId())
                .teamBoardId(newsletter.getTeamBoardId())
                .title(newsletter.getTitle())
                .recipients(Arrays.asList(newsletter.getRecipients().split(",")))
                .articleIds(articleIds)
                .scheduleType(newsletter.getScheduleType())
                .nextRunTime(newsletter.getNextRunTime())
                .lastRunTime(newsletter.getLastRunTime())
                .isActive(newsletter.getIsActive())
                .createdAt(newsletter.getCreatedAt())
                .build();
    }

    /**
     * Scheduled task to send newsletters
     */
    @Scheduled(fixedRate = 60000) // Run every minute
    @Transactional
    public void sendScheduledNewsletters() {
        log.debug("Checking for newsletters to send...");

        List<TeamBoardNewsletter> newsletters = teamBoardNewsletterRepository.findNewslettersDueToSend(LocalDateTime.now());

        for (TeamBoardNewsletter newsletter : newsletters) {
            sendNewsletter(newsletter);

            // Update next run time
            LocalDateTime nextRunTime = calculateNextRunTime(newsletter.getScheduleType());
            newsletter.setLastRunTime(LocalDateTime.now());
            newsletter.setNextRunTime(nextRunTime);
            teamBoardNewsletterRepository.save(newsletter);
        }
    }

    /**
     * Helper method to send a newsletter
     */
    private void sendNewsletter(TeamBoardNewsletter newsletter) {
        log.info("Sending newsletter ID: {}", newsletter.getId());

        try {
            // Get team board details
            TeamBoard teamBoard = teamBoardRepository.findActiveById(newsletter.getTeamBoardId())
                    .orElseThrow(() -> new NotFoundException(newsletter.getTeamBoardId().toString(), "team_board"));

            // Get articles
            List<Long> articleIds = new ArrayList<>();
            if (newsletter.getIncludedArticles() != null) {
                articleIds = objectMapper.readValue(newsletter.getIncludedArticles(),
                        new TypeReference<List<Long>>() {
                        });
            }

            List<ArticleResponse> articles;
            if (articleIds.isEmpty()) {
                // If no specific articles, get all from the board
                articles = teamBoardArticleRepository.findArticlesByTeamBoardId(
                        newsletter.getTeamBoardId(), Pageable.unpaged()).getContent();
            } else {
                // Get specific articles
                articles = articleIds.stream()
                        .map(id -> articleRepository.findArticleById(id).orElse(null))
                        .filter(Objects::nonNull)
                        .toList();
            }

            if (articles.isEmpty()) {
                log.warn("No articles found for newsletter ID: {}", newsletter.getId());
                return;
            }

            // Build content
            StringBuilder content = new StringBuilder();
            content.append("Articles from ")
                    .append(teamBoard.getName())
                    .append(":\n\n");

            for (ArticleResponse article : articles) {
                content.append("- ")
                        .append(article.getTitle())
                        .append("\n  ")
                        .append(article.getUrl())
                        .append("\n\n");
            }

            // Send emails
            String[] recipients = newsletter.getRecipients().split(",");
            for (String recipient : recipients) {
                mailService.sendThresholdEmail(
                        recipient.trim(),
                        newsletter.getTitle(),
                        content.toString(),
                        "");
                log.info("Newsletter sent to: {}", recipient);
            }

        } catch (Exception e) {
            log.error("Error sending newsletter ID: {}", newsletter.getId(), e);
        }
    }

    /**
     * Helper method to extract mentions from content
     */
    private Set<String> extractMentions(String content) {
        Set<String> mentions = new HashSet<>();
        if (content == null || content.isEmpty()) {
            return mentions;
        }

        // Pattern for @username
        Pattern pattern = Pattern.compile("@([\\w.]+)");
        Matcher matcher = pattern.matcher(content);

        while (matcher.find()) {
            String username = matcher.group(1);
            if (userRepository.existsByEmail(username)) {
                mentions.add(username);
            }
        }

        return mentions;
    }

    /**
     * Helper method to calculate next run time based on schedule type
     */
    private LocalDateTime calculateNextRunTime(String scheduleType) {
        LocalDateTime now = LocalDateTime.now();
        return switch (scheduleType) {
            case "DAILY" -> now.plusDays(1).withHour(9).withMinute(0).withSecond(0);
            case "WEEKLY" -> now.plusWeeks(1).withHour(9).withMinute(0).withSecond(0);
            case "MONTHLY" -> now.plusMonths(1).withHour(9).withMinute(0).withSecond(0);
            default -> now;
        };
    }

    /**
     * Helper method to get team board with permission check
     */
    private TeamBoard getTeamBoardWithPermissionCheck(Long id, List<String> requiredPermissions) {
        TeamBoard teamBoard = teamBoardRepository.findActiveById(id)
                .orElseThrow(() -> new NotFoundException(id.toString(), "team_board"));

        String username = auditorAware.getCurrentAuditor().orElse("anonymous");
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException(username, "user"));

        // Check if user has required permission
        boolean hasPermission = teamBoardUserRepository.existsByTeamBoardIdAndUserIdAndPermissionIn(
                id, user.getId(), requiredPermissions);

        if (!hasPermission) {
            log.error("User {} does not have required permission for team board {}", username, id);
            throw new ForbiddenException("team.board.permission.denied");
        }

        return teamBoard;
    }

    @Override
    @Transactional(readOnly = true)
    public List<TeamBoardMemberSimpleResponse> getTeamBoardMembers(Long id) {
        log.info("Getting members for team board ID: {}", id);

        // Check if team board exists and user has view permission
        TeamBoard teamBoard = getTeamBoardWithPermissionCheck(id, ALL_PERMISSIONS);

        // Get team board members with user info
        return teamBoardUserRepository.findMembersByTeamBoardId(id).stream()
                .map(member -> TeamBoardMemberSimpleResponse.builder()
                        .id(member.getUserId())
                        .name(member.getName())
                        .email(member.getEmail())
                        .build())
                .collect(Collectors.toList());
    }
}