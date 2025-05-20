package com.olh.feeds.service;

import com.olh.feeds.dto.request.folder.FolderRequest;
import com.olh.feeds.dto.request.folder.FolderSourceRequest;
import com.olh.feeds.dto.response.PageResponse;
import com.olh.feeds.dto.response.folder.FolderDetailResponse;
import com.olh.feeds.dto.response.folder.FolderResponse;
import com.olh.feeds.dto.response.folder.FolderWithArticlesResponse;
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
     * Get folders by current authenticated user
     * @param pageable Pagination information
     * @return List of folders belonging to current user
     */
    PageResponse<FolderResponse> getFoldersByCurrentUser(Pageable pageable);

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
     * Create a new folder
     * @param request Folder creation request
     * @return Created folder
     */
    FolderResponse updateFolder(Long id, FolderRequest request);

    /**
     * Add a source to a folder
     * @param folderId Folder ID 
     * @param request Source to add
     * @return Updated folder details
     */
    FolderDetailResponse addSourceToFolder(Long folderId, FolderSourceRequest request);

    /**
     * Get folders with articles for current user
     * @param pageable Pagination for folders
     * @param articlePageSize Number of articles per folder
     * @return List of folders with articles
     */
    PageResponse<FolderWithArticlesResponse> getFoldersWithArticles(Pageable pageable, int articlePageSize, String keyword);

    /**
     * Get folder details with paginated articles
     * @param folderId Folder ID
     * @param articlesPageable Pagination for articles
     * @return Folder with paginated articles
     */
    FolderDetailResponse getFolderArticles(Long folderId, Pageable articlesPageable);
}