// feeds-service/src/main/java/com/olh/feeds/service/BoardService.java
package com.olh.feeds.service;

import com.olh.feeds.dto.request.board.ArticleFromUrlRequest;
import com.olh.feeds.dto.request.board.BoardArticleRequest;
import com.olh.feeds.dto.request.board.BoardRequest;
import com.olh.feeds.dto.response.PageResponse;
import com.olh.feeds.dto.response.article.ArticleResponse;
import com.olh.feeds.dto.response.board.BoardDetailResponse;
import com.olh.feeds.dto.response.board.BoardResponse;
import org.springframework.data.domain.Pageable;

public interface BoardService {
    /**
     * Get all boards for the current user
     *
     * @param pageable Pagination information
     * @return List of boards
     */
    PageResponse<BoardResponse> getBoardsByCurrentUser(Pageable pageable);

    /**
     * Get board details including articles
     *
     * @param boardId Board ID
     * @param pageable Pagination information for articles
     * @return Board with articles
     */
    BoardDetailResponse getBoardDetail(Long boardId, Pageable pageable);

    /**
     * Create a new board
     *
     * @param request Board creation request
     * @return Created board
     */
    BoardResponse createBoard(BoardRequest request);

    /**
     * Update an existing board
     *
     * @param boardId Board ID to update
     * @param request Board update request
     * @return Updated board
     */
    BoardResponse updateBoard(Long boardId, BoardRequest request);

    /**
     * Delete a board
     *
     * @param boardId Board ID to delete
     */
    void deleteBoard(Long boardId);

    /**
     * Add an article to a board
     *
     * @param boardId Board ID
     * @param request Article to add
     * @return Updated board details
     */
    BoardDetailResponse addArticleToBoard(Long boardId, BoardArticleRequest request);

    /**
     * Add an article from URL to a board
     *
     * @param boardId Board ID
     * @param request Article URL and metadata
     * @return The added article
     */
    ArticleResponse addArticleFromUrlToBoard(Long boardId, ArticleFromUrlRequest request);

    /**
     * Remove an article from a board
     *
     * @param boardId Board ID
     * @param articleId Article ID to remove
     */
    void removeArticleFromBoard(Long boardId, Long articleId);
}