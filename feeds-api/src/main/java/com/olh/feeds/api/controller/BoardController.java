// feeds-api/src/main/java/com/olh/feeds/api/controller/BoardController.java
package com.olh.feeds.api.controller;

import com.olh.feeds.core.exception.response.ResponseGeneral;
import com.olh.feeds.dto.request.board.ArticleFromUrlRequest;
import com.olh.feeds.dto.request.board.BoardArticleRequest;
import com.olh.feeds.dto.request.board.BoardRequest;
import com.olh.feeds.dto.response.PageResponse;
import com.olh.feeds.dto.response.article.ArticleResponse;
import com.olh.feeds.dto.response.board.BoardDetailResponse;
import com.olh.feeds.dto.response.board.BoardResponse;
import com.olh.feeds.service.BoardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/boards")
@Slf4j
@RequiredArgsConstructor
public class BoardController {

    private final BoardService boardService;

    /**
     * Get boards for the current user
     *
     * @param pageable Pagination information
     * @return List of boards
     */
    @GetMapping
    public ResponseGeneral<PageResponse<BoardResponse>> getCurrentUserBoards(
            @PageableDefault Pageable pageable
    ) {
        log.info("REST request to get boards for current user");

        PageResponse<BoardResponse> boards = boardService.getBoardsByCurrentUser(pageable);
        return ResponseGeneral.of(
                HttpStatus.OK.value(),
                "board.list.success",
                boards
        );
    }

    /**
     * Get board details including articles
     *
     * @param id       Board ID
     * @param pageable Pagination for articles
     * @return Board with articles
     */
    @GetMapping("/{id}")
    public ResponseGeneral<BoardDetailResponse> getBoardDetail(
            @PathVariable("id") Long id,
            @PageableDefault Pageable pageable
    ) {
        log.info("REST request to get board details for ID: {}", id);

        BoardDetailResponse board = boardService.getBoardDetail(id, pageable);
        return ResponseGeneral.of(
                HttpStatus.OK.value(),
                "board.detail.success",
                board
        );
    }

    /**
     * Create a new board
     *
     * @param request Board creation request
     * @return Created board
     */
    @PostMapping
    public ResponseGeneral<BoardResponse> createBoard(
            @Valid @RequestBody BoardRequest request
    ) {
        log.info("REST request to create board: {}", request.getName());

        BoardResponse board = boardService.createBoard(request);
        return ResponseGeneral.of(
                HttpStatus.CREATED.value(),
                "board.create.success",
                board
        );
    }

    /**
     * Update an existing board
     *
     * @param id      Board ID
     * @param request Board update request
     * @return Updated board
     */
    @PutMapping("/{id}")
    public ResponseGeneral<BoardResponse> updateBoard(
            @PathVariable("id") Long id,
            @Valid @RequestBody BoardRequest request
    ) {
        log.info("REST request to update board ID: {}", id);

        BoardResponse board = boardService.updateBoard(id, request);
        return ResponseGeneral.of(
                HttpStatus.OK.value(),
                "board.update.success",
                board
        );
    }

    /**
     * Delete a board
     *
     * @param id Board ID
     * @return Success response
     */
    @DeleteMapping("/{id}")
    public ResponseGeneral<?> deleteBoard(
            @PathVariable("id") Long id
    ) {
        log.info("REST request to delete board ID: {}", id);

        boardService.deleteBoard(id);
        return ResponseGeneral.of(
                HttpStatus.OK.value(),
                "board.delete.success"
        );
    }

    /**
     * Add an article to a board
     *
     * @param id      Board ID
     * @param request Article to add
     * @return Updated board details
     */
    @PostMapping("/{id}/articles")
    public ResponseGeneral<BoardDetailResponse> addArticleToBoard(
            @PathVariable("id") Long id,
            @Valid @RequestBody BoardArticleRequest request
    ) {
        log.info("REST request to add article ID: {} to board ID: {}", request.getArticleId(), id);

        BoardDetailResponse board = boardService.addArticleToBoard(id, request);
        return ResponseGeneral.of(
                HttpStatus.OK.value(),
                "board.article.add.success",
                board
        );
    }

// Continuing the BoardController class...

    /**
     * Add an article from URL to a board
     *
     * @param id      Board ID
     * @param request Article URL request
     * @return Added article
     */
    @PostMapping("/{id}/articles/url")
    public ResponseGeneral<ArticleResponse> addArticleFromUrlToBoard(
            @PathVariable("id") Long id,
            @Valid @RequestBody ArticleFromUrlRequest request
    ) {
        log.info("REST request to add article from URL: {} to board ID: {}", request.getUrl(), id);

        ArticleResponse article = boardService.addArticleFromUrlToBoard(id, request);
        return ResponseGeneral.of(
                HttpStatus.CREATED.value(),
                "board.article.url.add.success",
                article
        );
    }

    /**
     * Remove an article from a board
     *
     * @param boardId   Board ID
     * @param articleId Article ID
     * @return Success response
     */
    @DeleteMapping("/{boardId}/articles/{articleId}")
    public ResponseGeneral<?> removeArticleFromBoard(
            @PathVariable("boardId") Long boardId,
            @PathVariable("articleId") Long articleId
    ) {
        log.info("REST request to remove article ID: {} from board ID: {}", articleId, boardId);

        boardService.removeArticleFromBoard(boardId, articleId);
        return ResponseGeneral.of(
                HttpStatus.OK.value(),
                "board.article.remove.success"
        );
    }
}