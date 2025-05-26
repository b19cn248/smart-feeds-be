// feeds-service/src/main/java/com/olh/feeds/service/TeamBoardService.java
package com.olh.feeds.service;

import com.olh.feeds.dto.request.teamboard.*;
import com.olh.feeds.dto.response.PageResponse;
import com.olh.feeds.dto.response.article.ArticleResponse;
import com.olh.feeds.dto.response.teamboard.*;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface TeamBoardService {
    /**
     * Create a new team board
     *
     * @param request Team board creation request
     * @return Created team board
     */
    TeamBoardResponse createTeamBoard(TeamBoardRequest request);

    /**
     * Update an existing team board
     *
     * @param id Team board ID
     * @param request Team board update request
     * @return Updated team board
     */
    TeamBoardResponse updateTeamBoard(Long id, TeamBoardRequest request);

    /**
     * Delete a team board
     *
     * @param id Team board ID
     */
    void deleteTeamBoard(Long id);

    /**
     * Get team board details including articles
     *
     * @param id Team board ID
     * @param pageable Pagination for articles
     * @return Team board details with articles
     */
    TeamBoardDetailResponse getTeamBoardDetail(Long id, Pageable pageable);

    /**
     * Get all team boards for current user (owned or shared)
     *
     * @param pageable Pagination
     * @return List of team boards
     */
    PageResponse<TeamBoardResponse> getTeamBoardsForCurrentUser(Pageable pageable);

    /**
     * Get team boards by team ID
     *
     * @param teamId Team ID
     * @param pageable Pagination
     * @return List of team boards
     */
    PageResponse<TeamBoardResponse> getTeamBoardsByTeamId(Long teamId, Pageable pageable);

    /**
     * Share team board with a user
     *
     * @param id Team board ID
     * @param request User and permission
     * @return Updated team board members
     */
    List<TeamBoardUserResponse> shareTeamBoard(Long id, TeamBoardUserRequest request);

    /**
     * Update team board member permissions
     *
     * @param id Team board ID
     * @param userId User ID
     * @param request Updated permission
     * @return Updated team board member
     */
    TeamBoardUserResponse updateTeamBoardMember(Long id, Long userId, TeamBoardUserRequest request);

    /**
     * Remove team board member
     *
     * @param id Team board ID
     * @param userId User ID to remove
     */
    void removeTeamBoardMember(Long id, Long userId);

    /**
     * Add article to team board
     *
     * @param id Team board ID
     * @param request Article to add
     * @return List of articles in board
     */
    PageResponse<ArticleResponse> addArticleToTeamBoard(Long id, TeamBoardArticleRequest request);

    /**
     * Remove article from team board
     *
     * @param id Team board ID
     * @param articleId Article ID
     */
    void removeArticleFromTeamBoard(Long id, Long articleId);

    /**
     * Add note to article in team board
     *
     * @param id Team board ID
     * @param request Note content and article ID
     * @return Created note
     */
    TeamBoardNoteResponse addNoteToArticle(Long id, TeamBoardNoteRequest request);

    /**
     * Get notes for article in team board
     *
     * @param id Team board ID
     * @param articleId Article ID
     * @return List of notes
     */
    List<TeamBoardNoteResponse> getArticleNotes(Long id, Long articleId);

    /**
     * Add highlight to article in team board
     *
     * @param id Team board ID
     * @param request Highlight text and article ID
     * @return Created highlight
     */
    TeamBoardHighlightResponse addHighlightToArticle(Long id, TeamBoardHighlightRequest request);

    /**
     * Get highlights for article in team board
     *
     * @param id Team board ID
     * @param articleId Article ID
     * @return List of highlights
     */
    List<TeamBoardHighlightResponse> getArticleHighlights(Long id, Long articleId);

    /**
     * Create newsletter for team board
     *
     * @param id Team board ID
     * @param request Newsletter details
     * @return Created newsletter
     */
    TeamBoardNewsletterResponse createNewsletter(Long id, TeamBoardNewsletterRequest request);

    /**
     * Get team board members
     *
     * @param id Team board ID
     * @return List of team board members
     */
    List<TeamBoardMemberSimpleResponse> getTeamBoardMembers(Long id);
}