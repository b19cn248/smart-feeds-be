// feeds-service/src/main/java/com/olh/feeds/service/impl/BoardServiceImpl.java
package com.olh.feeds.service.impl;

import com.olh.feeds.core.exception.base.BadRequestException;
import com.olh.feeds.core.exception.base.ConflictException;
import com.olh.feeds.core.exception.base.NotFoundException;
import com.olh.feeds.dao.entity.Article;
import com.olh.feeds.dao.entity.Board;
import com.olh.feeds.dao.entity.BoardArticle;
import com.olh.feeds.dao.entity.Source;
import com.olh.feeds.dao.repository.ArticleRepository;
import com.olh.feeds.dao.repository.BoardArticleRepository;
import com.olh.feeds.dao.repository.BoardRepository;
import com.olh.feeds.dao.repository.SourceRepository;
import com.olh.feeds.dto.mapper.PageMapper;
import com.olh.feeds.dto.request.board.ArticleFromUrlRequest;
import com.olh.feeds.dto.request.board.BoardArticleRequest;
import com.olh.feeds.dto.request.board.BoardRequest;
import com.olh.feeds.dto.response.PageResponse;
import com.olh.feeds.dto.response.article.ArticleResponse;
import com.olh.feeds.dto.response.board.BoardDetailResponse;
import com.olh.feeds.dto.response.board.BoardResponse;
import com.olh.feeds.service.BoardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class BoardServiceImpl implements BoardService {

    private final BoardRepository boardRepository;
    private final BoardArticleRepository boardArticleRepository;
    private final ArticleRepository articleRepository;
    private final SourceRepository sourceRepository;
    private final PageMapper pageMapper;
    private final AuditorAware<String> auditorAware;

    @Override
    public PageResponse<BoardResponse> getBoardsByCurrentUser(Pageable pageable) {
        log.info("Getting boards for current user");

        // Get username from Security Context
        String username = auditorAware.getCurrentAuditor().orElse("anonymous");
        log.info("Current username: {}", username);

        Page<BoardResponse> boardsPage = boardRepository.findBoardsByUsername(username, pageable);

        log.info("Found {} boards for user {}", boardsPage.getNumberOfElements(), username);
        return pageMapper.toPageDto(boardsPage);
    }

    @Override
    @Transactional(readOnly = true)
    public BoardDetailResponse getBoardDetail(Long boardId, Pageable pageable) {
        log.info("Getting board details for ID: {}", boardId);

        // Get the board
        Board board = getBoardWithPermissionCheck(boardId);

        // Get articles for this board
        Page<ArticleResponse> articlesPage = boardArticleRepository.findArticlesByBoardId(boardId, pageable);

        return BoardDetailResponse.builder()
                .id(board.getId())
                .name(board.getName())
                .description(board.getDescription())
                .color(board.getColor())
                .icon(board.getIcon())
                .isPublic(board.getIsPublic())
                .createdAt(board.getCreatedAt())
                .articles(articlesPage.getContent())
                .build();
    }

    @Override
    @Transactional
    public BoardResponse createBoard(BoardRequest request) {
        log.info("Creating new board: {}", request.getName());

        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new BadRequestException("board.name.required");
        }

        // Get username from Security Context
        String username = auditorAware.getCurrentAuditor().orElse("anonymous");

        Board board = Board.builder()
                .name(request.getName())
                .description(request.getDescription())
                .color(request.getColor())
                .icon(request.getIcon())
                .isPublic(request.getIsPublic() != null ? request.getIsPublic() : false)
                .build();

        board = boardRepository.save(board);
        log.info("Board created successfully with ID: {}", board.getId());

        return mapToBoardResponse(board);
    }

    @Override
    @Transactional
    public BoardResponse updateBoard(Long boardId, BoardRequest request) {
        log.info("Updating board with ID: {}", boardId);

        // Get the board with permission check
        Board board = getBoardWithPermissionCheck(boardId);

        // Update the board fields
        board.setName(request.getName());
        board.setDescription(request.getDescription());
        board.setColor(request.getColor());
        board.setIcon(request.getIcon());

        if (request.getIsPublic() != null) {
            board.setIsPublic(request.getIsPublic());
        }

        board = boardRepository.save(board);
        log.info("Board updated successfully with ID: {}", board.getId());

        return mapToBoardResponse(board);
    }

    @Override
    @Transactional
    public void deleteBoard(Long boardId) {
        log.info("Deleting board with ID: {}", boardId);

        // Get the board with permission check
        Board board = getBoardWithPermissionCheck(boardId);

        // Soft delete
        board.setIsDeleted(true);
        boardRepository.save(board);

        log.info("Board deleted successfully with ID: {}", boardId);
    }

    @Override
    @Transactional
    public BoardDetailResponse addArticleToBoard(Long boardId, BoardArticleRequest request) {
        log.info("Adding article ID: {} to board ID: {}", request.getArticleId(), boardId);

        // Get the board with permission check
        Board board = getBoardWithPermissionCheck(boardId);

        // Check if the article exists
        Article article = articleRepository.findById(request.getArticleId())
                .orElseThrow(() -> {
                    log.error("Article not found with ID: {}", request.getArticleId());
                    return new NotFoundException(request.getArticleId().toString(), "article");
                });

        // Check if the article is already in the board
        boolean exists = boardArticleRepository.existsByBoardIdAndArticleId(boardId, request.getArticleId());
        if (exists) {
            log.error("Article already exists in board");
            throw new ConflictException("board.article.already.exists");
        }

        // Add the article to the board
        BoardArticle boardArticle = BoardArticle.builder()
                .boardId(boardId)
                .articleId(request.getArticleId())
                .note(request.getNote())
                .build();

        boardArticleRepository.save(boardArticle);
        log.info("Article added to board successfully");

        // Return updated board details
        return getBoardDetail(boardId, Pageable.unpaged());
    }

    @Override
    @Transactional
    public ArticleResponse addArticleFromUrlToBoard(Long boardId, ArticleFromUrlRequest request) {
        log.info("Adding article from URL: {} to board ID: {}", request.getUrl(), boardId);

        // Get the board with permission check
        Board board = getBoardWithPermissionCheck(boardId);

        // Extract domain from URL to create/find source
        String domain = extractDomainFromUrl(request.getUrl());

        // Find or create the source
        Source source = sourceRepository.findByUrl(domain)
                .orElseGet(() -> {
                    Source newSource = Source.builder()
                            .url(domain)
                            .type("URL")
                            .active(true)
                            .build();
                    return sourceRepository.save(newSource);
                });

        // Create the article
        Article article = Article.builder()
                .title(request.getTitle() != null ? request.getTitle() : "Article from " + domain)
                .link(request.getUrl())
                .content(request.getContent())
                .sourceId(source.getId())
                .guid(request.getUrl()) // Use URL as GUID
                .pubDate(LocalDateTime.now())
                .isoDate(LocalDateTime.now())
                .build();

        article = articleRepository.save(article);
        log.info("Article created successfully with ID: {}", article.getId());

        // Add the article to the board
        BoardArticle boardArticle = BoardArticle.builder()
                .boardId(boardId)
                .articleId(article.getId())
                .note(request.getNote())
                .build();

        boardArticleRepository.save(boardArticle);
        log.info("Article added to board successfully");

        // Return the article response
        return ArticleResponse.builder()
                .id(article.getId())
                .title(article.getTitle())
                .content(article.getContent())
                .url(article.getLink())
                .source(source.getUrl())
                .publishDate(article.getPubDate())
                .build();
    }

    @Override
    @Transactional
    public void removeArticleFromBoard(Long boardId, Long articleId) {
        log.info("Removing article ID: {} from board ID: {}", articleId, boardId);

        // Get the board with permission check
        Board board = getBoardWithPermissionCheck(boardId);

        // Find the board article entry
        Optional<BoardArticle> boardArticleOpt = boardArticleRepository.findByBoardIdAndArticleId(boardId, articleId);

        if (boardArticleOpt.isEmpty()) {
            log.error("Article not found in board");
            throw new NotFoundException(articleId.toString(), "board_article");
        }

        // Soft delete the board article entry
        BoardArticle boardArticle = boardArticleOpt.get();
        boardArticle.setIsDeleted(true);
        boardArticleRepository.save(boardArticle);

        log.info("Article removed from board successfully");
    }

    /**
     * Helper method to get a board with permission check
     */
    private Board getBoardWithPermissionCheck(Long boardId) {
        Board board = boardRepository.findBoardById(boardId);
        if (board == null) {
            log.error("Board not found with ID: {}", boardId);
            throw new NotFoundException(boardId.toString(), "board");
        }

        // Check if the current user is the owner of the board
        String username = auditorAware.getCurrentAuditor().orElse("anonymous");
        if (!board.getCreatedBy().equals(username) && !board.getIsPublic()) {
            log.error("User {} does not have permission to access board {}", username, boardId);
            throw new BadRequestException("board.access.denied");
        }

        return board;
    }

    /**
     * Helper method to map Board entity to BoardResponse DTO
     */
    private BoardResponse mapToBoardResponse(Board board) {
        return BoardResponse.builder()
                .id(board.getId())
                .name(board.getName())
                .description(board.getDescription())
                .color(board.getColor())
                .icon(board.getIcon())
                .isPublic(board.getIsPublic())
                .createdAt(board.getCreatedAt())
                .build();
    }

    /**
     * Helper method to extract domain from URL
     */
    private String extractDomainFromUrl(String urlString) {
        try {
            URL url = new URL(urlString);
            return url.getProtocol() + "://" + url.getHost();
        } catch (Exception e) {
            log.warn("Failed to extract domain from URL: {}", urlString, e);
            return urlString;
        }
    }
}