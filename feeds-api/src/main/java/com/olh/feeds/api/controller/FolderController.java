package com.olh.feeds.api.controller;

import com.olh.feeds.core.exception.response.ResponseGeneral;
import com.olh.feeds.dto.request.folder.FolderRequest;
import com.olh.feeds.dto.request.folder.FolderSourceRequest;
import com.olh.feeds.dto.response.PageResponse;
import com.olh.feeds.dto.response.folder.FolderDetailResponse;
import com.olh.feeds.dto.response.folder.FolderResponse;
import com.olh.feeds.dto.response.folder.FolderWithArticlesResponse;
import com.olh.feeds.service.FolderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/folders")
@Slf4j
@RequiredArgsConstructor
public class FolderController {

    private final FolderService folderService;

    /**
     * Lấy danh sách folders của người dùng hiện tại
     * @param pageable Thông tin phân trang
     * @return Danh sách folders
     */
    @GetMapping
    public ResponseGeneral<PageResponse<FolderResponse>> getCurrentUserFolders(
            @PageableDefault Pageable pageable
    ) {
        log.info("REST request to get folders for current user");

        PageResponse<FolderResponse> folders = folderService.getFoldersByCurrentUser(pageable);
        return ResponseGeneral.of(
                HttpStatus.OK.value(),
                "folder.list.success",
                folders
        );
    }

    /**
     * API cũ - Lấy tất cả folders với filter userId (giữ lại để tương thích)
     * @param userId Optional user ID filter
     * @param pageable Thông tin phân trang
     * @return Danh sách folders
     */
    @GetMapping("/all")
    public ResponseGeneral<PageResponse<FolderResponse>> getAllFolders(
            @RequestParam(name = "userId", required = false) Long userId,
            @PageableDefault Pageable pageable
    ) {
        log.info("REST request to get all folders for user ID: {}", userId);

        PageResponse<FolderResponse> folders = folderService.getAllFolders(userId, pageable);
        return ResponseGeneral.of(
                HttpStatus.OK.value(),
                "folder.list.success",
                folders
        );
    }

    /**
     * Lấy chi tiết folder bao gồm danh sách sources
     * @param id Folder ID
     * @return Folder với danh sách sources
     */
    @GetMapping("/{id}")
    public ResponseGeneral<FolderDetailResponse> getFolderDetail(
            @PathVariable("id") Long id
    ) {
        log.info("REST request to get folder details for ID: {}", id);

        FolderDetailResponse folder = folderService.getFolderDetail(id);
        return ResponseGeneral.of(
                HttpStatus.OK.value(),
                "folder.detail.success",
                folder
        );
    }

    /**
     * Tạo folder mới
     * @param request Folder creation request
     * @return Created folder
     */
    @PostMapping
    public ResponseGeneral<FolderResponse> createFolder(
            @Valid @RequestBody FolderRequest request
    ) {
        log.info("REST request to create folder: {}", request.getName());

        FolderResponse folder = folderService.createFolder(request);
        return ResponseGeneral.of(
                HttpStatus.CREATED.value(),
                "folder.create.success",
                folder
        );
    }


    /**
     * Chỉnh sửa folder
     * @param request Folder creation request
     * @return Created folder
     */
    @PutMapping("/{id}")
    public ResponseGeneral<FolderResponse> updateFolder(
          @PathVariable("id") Long id,
          @Valid @RequestBody FolderRequest request
    ) {
        log.info("REST request to update folder: {}", request.getName());

        FolderResponse folder = folderService.updateFolder(id, request);
        return ResponseGeneral.of(
              HttpStatus.CREATED.value(),
              "folder.create.success",
              folder
        );
    }

    /**
     * Thêm nhiều source vào folder
     * @param id Folder ID
     * @param request Danh sách source IDs cần thêm
     * @return Thông tin folder đã cập nhật
     */
    @PostMapping("/{id}/sources")
    public ResponseGeneral<FolderDetailResponse> addSourceToFolder(
            @PathVariable("id") Long id,
            @Valid @RequestBody FolderSourceRequest request
    ) {
        log.info("REST request to add {} sources to folder ID: {}", request.getSourceIds().size(), id);

        FolderDetailResponse folder = folderService.addSourceToFolder(id, request);
        return ResponseGeneral.of(
                HttpStatus.OK.value(),
                "folder.source.add.success",
                folder
        );
    }

    /**
     * Lấy danh sách folders kèm articles cho mỗi folder
     *
     * @param pageable Thông tin phân trang cho folders
     * @param articleSize Số lượng articles cho mỗi folder
     * @return Danh sách folders với articles
     */
    @GetMapping("/with-articles")
    public ResponseGeneral<PageResponse<FolderWithArticlesResponse>> getFoldersWithArticles(
          @PageableDefault Pageable pageable,
          @RequestParam(name = "article_size", defaultValue = "5") int articleSize,
          @RequestParam(name = "keyword", required = false) String keyword
    ) {
        log.info("REST request to get folders with articles for current user, keyword: {}", keyword);
        return ResponseGeneral.of(
              HttpStatus.OK.value(),
              "folder.with.articles.success",
              folderService.getFoldersWithArticles(pageable, articleSize, keyword)
        );
    }

    /**
     * Lấy chi tiết folder kèm danh sách articles có phân trang
     *
     * @param id ID của folder
     * @param articlesPageable Thông tin phân trang cho articles
     * @return Thông tin chi tiết folder với articles
     */
    @GetMapping("/{id}/articles")
    public ResponseGeneral<FolderDetailResponse> getFolderWithArticles(
            @PathVariable("id") Long id,
            @PageableDefault Pageable articlesPageable
    ) {
        log.info("REST request to get folder with articles for ID: {}", id);
        return ResponseGeneral.of(
                HttpStatus.OK.value(),
                "folder.articles.success",
                folderService.getFolderArticles(id, articlesPageable)
        );
    }

    /**
     * Xóa source khỏi folder
     * @param id Folder ID
     * @param sourceId Source ID cần xóa
     * @return Thông tin folder sau khi xóa source
     */
    @DeleteMapping("/{id}/sources/{sourceId}")
    public ResponseGeneral<FolderDetailResponse> removeSourceFromFolder(
            @PathVariable("id") Long id,
            @PathVariable("sourceId") Long sourceId
    ) {
        log.info("REST request to remove source ID: {} from folder ID: {}", sourceId, id);

        FolderDetailResponse folder = folderService.removeSourceFromFolder(id, sourceId);
        return ResponseGeneral.of(
                HttpStatus.OK.value(),
                "folder.source.remove.success",
                folder
        );
    }
}