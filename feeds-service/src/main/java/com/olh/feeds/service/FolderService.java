// feeds-service/src/main/java/com/olh/feeds/service/FolderService.java
package com.olh.feeds.service;

import com.olh.feeds.dto.request.folder.FolderRequest;
import com.olh.feeds.dto.request.folder.FolderSourceRequest;
import com.olh.feeds.dto.response.PageResponse;
import com.olh.feeds.dto.response.folder.FolderDetailResponse;
import com.olh.feeds.dto.response.folder.FolderResponse;
import org.springframework.data.domain.Pageable;

public interface FolderService {
    /**
     * Get all folders with pagination
     * @param userId Optional user ID filter
     * @param pageable Pagination information
     * @return List of folders
     */
    PageResponse<FolderResponse> getAllFolders(Long userId, Pageable pageable);

    /**
     * Get folder details including sources
     * @param folderId Folder ID
     * @return Folder with sources
     */
    FolderDetailResponse getFolderDetail(Long folderId);

    /**
     * Create a new folder
     * @param request Folder creation request
     * @return Created folder
     */
    FolderResponse createFolder(FolderRequest request);

    /**
     * Add a source to a folder
     * @param folderId Folder ID 
     * @param request Source to add
     * @return Updated folder details
     */
    FolderDetailResponse addSourceToFolder(Long folderId, FolderSourceRequest request);
}