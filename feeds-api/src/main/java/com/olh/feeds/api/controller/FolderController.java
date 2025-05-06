// feeds-api/src/main/java/com/olh/feeds/api/controller/FolderController.java
package com.olh.feeds.api.controller;

import com.olh.feeds.core.exception.response.ResponseGeneral;
import com.olh.feeds.dto.request.folder.FolderRequest;
import com.olh.feeds.dto.request.folder.FolderSourceRequest;
import com.olh.feeds.dto.response.PageResponse;
import com.olh.feeds.dto.response.folder.FolderDetailResponse;
import com.olh.feeds.dto.response.folder.FolderResponse;
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
     * Get all folders with pagination
     * @param userId Optional user ID filter
     * @param pageable Pagination information
     * @return List of folders
     */
    @GetMapping
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
     * Get folder details including sources
     * @param id Folder ID
     * @return Folder with sources
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
     * Create a new folder
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
     * Add a source to a folder
     * @param id Folder ID
     * @param request Source to add
     * @return Updated folder details
     */
    @PostMapping("/{id}/sources")
    public ResponseGeneral<FolderDetailResponse> addSourceToFolder(
            @PathVariable("id") Long id,
            @Valid @RequestBody FolderSourceRequest request
    ) {
        log.info("REST request to add source ID: {} to folder ID: {}", request.getSourceId(), id);

        FolderDetailResponse folder = folderService.addSourceToFolder(id, request);
        return ResponseGeneral.of(
                HttpStatus.OK.value(),
                "folder.source.add.success",
                folder
        );
    }
}