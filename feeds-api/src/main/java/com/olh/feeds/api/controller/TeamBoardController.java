// feeds-api/src/main/java/com/olh/feeds/api/controller/TeamBoardController.java
package com.olh.feeds.api.controller;

import com.olh.feeds.core.exception.response.ResponseGeneral;
import com.olh.feeds.dto.request.teamboard.*;
import com.olh.feeds.dto.response.PageResponse;
import com.olh.feeds.dto.response.article.ArticleResponse;
import com.olh.feeds.dto.response.teamboard.*;
import com.olh.feeds.service.TeamBoardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/team-boards")
@Slf4j
@RequiredArgsConstructor
public class TeamBoardController {

    private final TeamBoardService teamBoardService;

    /**
     * Get team boards for current user
     */
    @GetMapping
    public ResponseGeneral<PageResponse<TeamBoardResponse>> getTeamBoardsForCurrentUser(
            @PageableDefault Pageable pageable
    ) {
        log.info("REST request to get team boards for current user");
        return ResponseGeneral.of(
                HttpStatus.OK.value(),
                "team.board.list.success",
                teamBoardService.getTeamBoardsForCurrentUser(pageable)
        );
    }

    /**
     * Get team boards by team ID
     */
    @GetMapping("/by-team/{teamId}")
    public ResponseGeneral<PageResponse<TeamBoardResponse>> getTeamBoardsByTeamId(
            @PathVariable("teamId") Long teamId,
            @PageableDefault Pageable pageable
    ) {
        log.info("REST request to get team boards for team ID: {}", teamId);
        return ResponseGeneral.of(
                HttpStatus.OK.value(),
                "team.board.list.success",
                teamBoardService.getTeamBoardsByTeamId(teamId, pageable)
        );
    }

    /**
     * Get team board details
     */
    @GetMapping("/{id}")
    public ResponseGeneral<TeamBoardDetailResponse> getTeamBoardDetail(
            @PathVariable("id") Long id,
            @PageableDefault Pageable pageable
    ) {
        log.info("REST request to get team board details for ID: {}", id);
        return ResponseGeneral.of(
                HttpStatus.OK.value(),
                "team.board.detail.success",
                teamBoardService.getTeamBoardDetail(id, pageable)
        );
    }

    /**
     * Create team board
     */
    @PostMapping
    public ResponseGeneral<TeamBoardResponse> createTeamBoard(
            @Valid @RequestBody TeamBoardRequest request
    ) {
        log.info("REST request to create team board: {}", request.getName());
        return ResponseGeneral.of(
                HttpStatus.CREATED.value(),
                "team.board.create.success",
                teamBoardService.createTeamBoard(request)
        );
    }

    /**
     * Update team board
     */
    @PutMapping("/{id}")
    public ResponseGeneral<TeamBoardResponse> updateTeamBoard(
            @PathVariable("id") Long id,
            @Valid @RequestBody TeamBoardRequest request
    ) {
        log.info("REST request to update team board ID: {}", id);
        return ResponseGeneral.of(
                HttpStatus.OK.value(),
                "team.board.update.success",
                teamBoardService.updateTeamBoard(id, request)
        );
    }

    /**
     * Delete team board
     */
    @DeleteMapping("/{id}")
    public ResponseGeneral<Void> deleteTeamBoard(
            @PathVariable("id") Long id
    ) {
        log.info("REST request to delete team board ID: {}", id);
        teamBoardService.deleteTeamBoard(id);
        return ResponseGeneral.of(
                HttpStatus.OK.value(),
                "team.board.delete.success"
        );
    }

    /**
     * Share team board with user
     */
    @PostMapping("/{id}/share")
    public ResponseGeneral<List<TeamBoardUserResponse>> shareTeamBoard(
            @PathVariable("id") Long id,
            @Valid @RequestBody TeamBoardUserRequest request
    ) {
        log.info("REST request to share team board ID: {} with user: {}", id, request.getEmail());
        return ResponseGeneral.of(
                HttpStatus.OK.value(),
                "team.board.share.success",
                teamBoardService.shareTeamBoard(id, request)
        );
    }

    /**
     * Update team board member permission
     */
    @PutMapping("/{id}/members/{userId}")
    public ResponseGeneral<TeamBoardUserResponse> updateTeamBoardMember(
            @PathVariable("id") Long id,
            @PathVariable("userId") Long userId,
            @Valid @RequestBody TeamBoardUserRequest request
    ) {
        log.info("REST request to update team board ID: {} member ID: {}", id, userId);
        return ResponseGeneral.of(
                HttpStatus.OK.value(),
                "team.board.member.update.success",
                teamBoardService.updateTeamBoardMember(id, userId, request)
        );
    }

    /**
     * Remove team board member
     */
    @DeleteMapping("/{id}/members/{userId}")
    public ResponseGeneral<Void> removeTeamBoardMember(
            @PathVariable("id") Long id,
            @PathVariable("userId") Long userId
    ) {
        log.info("REST request to remove user ID: {} from team board ID: {}", userId, id);
        teamBoardService.removeTeamBoardMember(id, userId);
        return ResponseGeneral.of(
                HttpStatus.OK.value(),
                "team.board.member.remove.success"
        );
    }

    /**
     * Add article to team board
     */
    @PostMapping("/{id}/articles")
    public ResponseGeneral<PageResponse<ArticleResponse>> addArticleToTeamBoard(
            @PathVariable("id") Long id,
            @Valid @RequestBody TeamBoardArticleRequest request
    ) {
        log.info("REST request to add article ID: {} to team board ID: {}", request.getArticleId(), id);
        return ResponseGeneral.of(
                HttpStatus.OK.value(),
                "team.board.article.add.success",
                teamBoardService.addArticleToTeamBoard(id, request)
        );
    }

    /**
     * Remove article from team board
     */
    @DeleteMapping("/{id}/articles/{articleId}")
    public ResponseGeneral<Void> removeArticleFromTeamBoard(
            @PathVariable("id") Long id,
            @PathVariable("articleId") Long articleId
    ) {
        log.info("REST request to remove article ID: {} from team board ID: {}", articleId, id);
        teamBoardService.removeArticleFromTeamBoard(id, articleId);
        return ResponseGeneral.of(
                HttpStatus.OK.value(),
                "team.board.article.remove.success"
        );
    }

    /**
     * Add note to article in team board
     */
    @PostMapping("/{id}/notes")
    public ResponseGeneral<TeamBoardNoteResponse> addNoteToArticle(
            @PathVariable("id") Long id,
            @Valid @RequestBody TeamBoardNoteRequest request
    ) {
        log.info("REST request to add note to article ID: {} in team board ID: {}", request.getArticleId(), id);
        return ResponseGeneral.of(
                HttpStatus.CREATED.value(),
                "team.board.note.add.success",
                teamBoardService.addNoteToArticle(id, request)
        );
    }

    /**
     * Get notes for article in team board
     */
    @GetMapping("/{id}/articles/{articleId}/notes")
    public ResponseGeneral<List<TeamBoardNoteResponse>> getArticleNotes(
            @PathVariable("id") Long id,
            @PathVariable("articleId") Long articleId
    ) {
        log.info("REST request to get notes for article ID: {} in team board ID: {}", articleId, id);
        return ResponseGeneral.of(
                HttpStatus.OK.value(),
                "team.board.note.list.success",
                teamBoardService.getArticleNotes(id, articleId)
        );
    }

    /**
     * Add highlight to article in team board
     */
    @PostMapping("/{id}/highlights")
    public ResponseGeneral<TeamBoardHighlightResponse> addHighlightToArticle(
            @PathVariable("id") Long id,
            @Valid @RequestBody TeamBoardHighlightRequest request
    ) {
        log.info("REST request to add highlight to article ID: {} in team board ID: {}", request.getArticleId(), id);
        return ResponseGeneral.of(
                HttpStatus.CREATED.value(),
                "team.board.highlight.add.success",
                teamBoardService.addHighlightToArticle(id, request)
        );
    }

    /**
     * Get highlights for article in team board
     */
    @GetMapping("/{id}/articles/{articleId}/highlights")
    public ResponseGeneral<List<TeamBoardHighlightResponse>> getArticleHighlights(
            @PathVariable("id") Long id,
            @PathVariable("articleId") Long articleId
    ) {
        log.info("REST request to get highlights for article ID: {} in team board ID: {}", articleId, id);
        return ResponseGeneral.of(
                HttpStatus.OK.value(),
                "team.board.highlight.list.success",
                teamBoardService.getArticleHighlights(id, articleId)
        );
    }

    /**
     * Create newsletter for team board
     */
    @PostMapping("/{id}/newsletters")
    public ResponseGeneral<TeamBoardNewsletterResponse> createNewsletter(
            @PathVariable("id") Long id,
            @Valid @RequestBody TeamBoardNewsletterRequest request
    ) {
        log.info("REST request to create newsletter for team board ID: {}", id);
        return ResponseGeneral.of(
                HttpStatus.CREATED.value(),
                "team.board.newsletter.create.success",
                teamBoardService.createNewsletter(id, request)
        );
    }
}