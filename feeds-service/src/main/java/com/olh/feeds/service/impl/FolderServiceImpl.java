package com.olh.feeds.service.impl;

import com.olh.feeds.core.exception.base.BadRequestException;
import com.olh.feeds.core.exception.base.ConflictException;
import com.olh.feeds.core.exception.base.NotFoundException;
import com.olh.feeds.dao.entity.Folder;
import com.olh.feeds.dao.entity.FolderSource;
import com.olh.feeds.dao.entity.Source;
import com.olh.feeds.dao.repository.ArticleRepository;
import com.olh.feeds.dao.repository.FolderRepository;
import com.olh.feeds.dao.repository.FolderSourceRepository;
import com.olh.feeds.dao.repository.SourceRepository;
import com.olh.feeds.dto.mapper.PageMapper;
import com.olh.feeds.dto.request.folder.FolderRequest;
import com.olh.feeds.dto.request.folder.FolderSourceRequest;
import com.olh.feeds.dto.response.PageResponse;
import com.olh.feeds.dto.response.article.ArticleResponse;
import com.olh.feeds.dto.response.folder.FolderDetailResponse;
import com.olh.feeds.dto.response.folder.FolderResponse;
import com.olh.feeds.dto.response.folder.FolderWithArticlesResponse;
import com.olh.feeds.dto.response.source.SourceResponse;
import com.olh.feeds.service.FolderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FolderServiceImpl implements FolderService {

    private final FolderRepository folderRepository;
    private final SourceRepository sourceRepository;
    private final FolderSourceRepository folderSourceRepository;
    private final PageMapper pageMapper;
    private final AuditorAware<String> auditorAware;
    private final ArticleRepository articleRepository;

    @Override
    public PageResponse<FolderResponse> getAllFolders(Long userId, Pageable pageable) {
        log.info("Getting all folders for user ID: {}", userId);

        // Sử dụng user ID được cung cấp hoặc mặc định nếu null
        Long effectiveUserId = userId;

        Page<FolderResponse> foldersPage = folderRepository.findAllFolders(effectiveUserId, pageable);

        log.info("Found {} folders", foldersPage.getNumberOfElements());
        return pageMapper.toPageDto(foldersPage);
    }

    @Override
    public PageResponse<FolderResponse> getFoldersByCurrentUser(Pageable pageable) {
        // Lấy username từ Security Context
        String username = auditorAware.getCurrentAuditor().get();
        log.info("Getting folders for current user: {}", username);

        Page<FolderResponse> foldersPage = folderRepository.findByCreatedBy(username, pageable);

        log.info("Found {} folders for user {}", foldersPage.getNumberOfElements(), username);
        return pageMapper.toPageDto(foldersPage);
    }

    @Override
    @Transactional(readOnly = true)
    public FolderDetailResponse getFolderDetail(Long folderId) {
        log.info("Getting folder details for ID: {}", folderId);

        Folder folder = folderRepository.findFolderById(folderId);
        if (folder == null) {
            log.error("Folder not found with ID: {}", folderId);
            throw new NotFoundException(folderId.toString(), "folder");
        }

        // Lấy danh sách FolderSource
        List<FolderSource> folderSources = folderSourceRepository.findByFolderId(folderId);

        // Lấy danh sách Source IDs
        List<Long> sourceIds = folderSources.stream()
                .map(FolderSource::getSourceId)
                .collect(Collectors.toList());

        List<SourceResponse> sources = new ArrayList<>();
        if (!sourceIds.isEmpty()) {
            // Lấy tất cả các sources từ ID list
            List<Source> sourceList = sourceRepository.findAllById(sourceIds);

            // Chuyển đổi sang DTO
            sources = sourceList.stream()
                    .filter(Objects::nonNull)
                    .map(this::mapToSourceResponse)
                    .toList();
        }

        return FolderDetailResponse.builder()
                .id(folder.getId())
                .name(folder.getName())
                .theme(folder.getTheme())
                .userId(folder.getUserId())
                .createdAt(folder.getCreatedAt())
                .sources(sources)
                .build();
    }

    @Override
    @Transactional
    public FolderResponse createFolder(FolderRequest request) {
        log.info("Creating new folder: {}", request.getName());

        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new BadRequestException("folder.name.required");
        }

        // Lấy username từ Security Context
        String username = auditorAware.getCurrentAuditor().get();

        Folder folder = Folder.builder()
                .name(request.getName())
                .theme(request.getTheme())
                .userId(null) // Không cần thiết lập userId vì sẽ dùng createdBy từ auditing
                .build();

        folder = folderRepository.save(folder);
        log.info("Folder created successfully with ID: {}", folder.getId());

        return FolderResponse.builder()
                .id(folder.getId())
                .name(folder.getName())
                .theme(folder.getTheme())
                .userId(folder.getUserId())
                .createdAt(folder.getCreatedAt())
                .build();
    }

    @Override
    @Transactional
    public FolderDetailResponse addSourceToFolder(Long folderId, FolderSourceRequest request) {
        log.info("Adding source ID: {} to folder ID: {}", request.getSourceId(), folderId);

        // Kiểm tra folder tồn tại
        Folder folder = folderRepository.findFolderById(folderId);
        if (folder == null) {
            log.error("Folder not found with ID: {}", folderId);
            throw new NotFoundException(folderId.toString(), "folder");
        }

        // Kiểm tra người dùng có quyền truy cập vào folder này không
        String username = auditorAware.getCurrentAuditor().get();
        if (!folder.getCreatedBy().equals(username)) {
            log.error("User {} does not have permission to access folder {}", username, folderId);
            throw new BadRequestException("folder.access.denied");
        }

        // Kiểm tra source tồn tại
        Source source = sourceRepository.findById(request.getSourceId())
                .orElseThrow(() -> {
                    log.error("Source not found with ID: {}", request.getSourceId());
                    return new NotFoundException(request.getSourceId().toString(), "source");
                });

        // Kiểm tra xem source đã được thêm vào folder chưa
        boolean exists = folderSourceRepository.existsByFolderIdAndSourceId(folderId, request.getSourceId());
        if (exists) {
            log.error("Source already exists in folder");
            throw new ConflictException("folder.source.already.exists");
        }

        // Thêm source vào folder
        FolderSource folderSource = FolderSource.builder()
                .folderId(folderId)
                .sourceId(request.getSourceId())
                .build();

        folderSourceRepository.save(folderSource);
        log.info("Source added to folder successfully");

        // Trả về folder details đã cập nhật
        return getFolderDetail(folderId);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<FolderWithArticlesResponse> getFoldersWithArticles(Pageable pageable, int articlePageSize) {
        log.info("Getting folders with articles for current user");

        // Lấy username từ Security Context
        String username = auditorAware.getCurrentAuditor().orElse("system");
        log.info("Current username: {}", username);

        // Lấy danh sách folders của người dùng hiện tại
        Page<FolderResponse> foldersPage = folderRepository.findByCreatedBy(username, pageable);

        // Chuyển đổi sang danh sách FolderWithArticlesResponse
        List<FolderWithArticlesResponse> folderWithArticles = new ArrayList<>();

        for (FolderResponse folder : foldersPage.getContent()) {
            // Lấy danh sách sources trong folder
            List<FolderSource> folderSources = folderSourceRepository.findByFolderId(folder.getId());

            if (folderSources.isEmpty()) {
                // Nếu không có source nào, tạo folder với danh sách articles rỗng
                folderWithArticles.add(FolderWithArticlesResponse.builder()
                        .id(folder.getId())
                        .name(folder.getName())
                        .theme(folder.getTheme())
                        .userId(folder.getUserId())
                        .createdAt(folder.getCreatedAt())
                        .articles(Collections.emptyList())
                        .build());
                continue;
            }

            // Lấy danh sách Source IDs
            List<Long> sourceIds = folderSources.stream()
                    .map(FolderSource::getSourceId)
                    .collect(Collectors.toList());

            // Lấy danh sách articles từ các sources
            List<ArticleResponse> articles = articleRepository.findBySourceIdInOrderByPublishDateDesc(
                    sourceIds,
                    PageRequest.of(0, articlePageSize)
            );

            // Tạo FolderWithArticlesResponse
            folderWithArticles.add(FolderWithArticlesResponse.builder()
                    .id(folder.getId())
                    .name(folder.getName())
                    .theme(folder.getTheme())
                    .userId(folder.getUserId())
                    .createdAt(folder.getCreatedAt())
                    .articles(articles)
                    .build());
        }

        // Tạo Page từ danh sách đã chuyển đổi
        Page<FolderWithArticlesResponse> resultPage = new PageImpl<>(
                folderWithArticles,
                pageable,
                foldersPage.getTotalElements()
        );

        return pageMapper.toPageDto(resultPage);
    }

    @Override
    @Transactional(readOnly = true)
    public FolderDetailResponse getFolderArticles(Long folderId, Pageable articlesPageable) {
        log.info("Getting folder details with articles for ID: {}", folderId);

        // Kiểm tra folder tồn tại
        Folder folder = folderRepository.findFolderById(folderId);
        if (folder == null) {
            log.error("Folder not found with ID: {}", folderId);
            throw new NotFoundException(folderId.toString(), "folder");
        }

        // Kiểm tra quyền truy cập
        String username = auditorAware.getCurrentAuditor().orElse("system");
        if (!folder.getCreatedBy().equals(username)) {
            log.error("User {} does not have permission to access folder {}", username, folderId);
            throw new BadRequestException("folder.access.denied");
        }

        // Lấy danh sách sources trong folder
        List<FolderSource> folderSources = folderSourceRepository.findByFolderId(folderId);
        List<SourceResponse> sources = new ArrayList<>();

        if (!folderSources.isEmpty()) {
            // Lấy danh sách Source IDs
            List<Long> sourceIds = folderSources.stream()
                    .map(FolderSource::getSourceId)
                    .collect(Collectors.toList());

            // Lấy tất cả các sources từ ID list
            List<Source> sourceList = sourceRepository.findAllById(sourceIds);

            // Chuyển đổi sang DTO
            sources = sourceList.stream()
                    .filter(Objects::nonNull)
                    .map(source -> SourceResponse.builder()
                            .id(source.getId())
                            .url(source.getUrl())
                            .language(source.getLanguage())
                            .type(source.getType())
                            .accountId(source.getAccountId())
                            .hashtag(source.getHashtag())
                            .category(source.getCategory())
                            .userId(source.getUserId())
                            .active(source.getActive())
                            .createdAt(source.getCreatedAt())
                            .build())
                    .collect(Collectors.toList());

            // Lấy danh sách articles phân trang
            Page<ArticleResponse> articlesPage = getArticlesBySourceIds(sourceIds, articlesPageable);

            return FolderDetailResponse.builder()
                    .id(folder.getId())
                    .name(folder.getName())
                    .theme(folder.getTheme())
                    .userId(folder.getUserId())
                    .createdAt(folder.getCreatedAt())
                    .sources(sources)
                    .articles(pageMapper.toPageDto(articlesPage))
                    .build();
        }

        // Nếu không có source nào
        return FolderDetailResponse.builder()
                .id(folder.getId())
                .name(folder.getName())
                .theme(folder.getTheme())
                .userId(folder.getUserId())
                .createdAt(folder.getCreatedAt())
                .sources(Collections.emptyList())
                .articles(new PageResponse<>())
                .build();
    }

    /**
     * Helper method để lấy articles từ danh sách source IDs với phân trang
     */
    private Page<ArticleResponse> getArticlesBySourceIds(List<Long> sourceIds, Pageable pageable) {
        // Tổng số articles
        int totalArticles = articleRepository.countBySourceIdIn(sourceIds);

        // Lấy danh sách articles theo phân trang
        List<ArticleResponse> articles = articleRepository.findBySourceIdInOrderByPublishDateDesc(
                sourceIds,
                pageable
        );

        return new PageImpl<>(articles, pageable, totalArticles);
    }

    /**
     * Map Source entity to SourceResponse DTO
     */
    private SourceResponse mapToSourceResponse(Source source) {
        return SourceResponse.builder()
                .id(source.getId())
                .url(source.getUrl())
                .language(source.getLanguage())
                .type(source.getType())
                .accountId(source.getAccountId())
                .hashtag(source.getHashtag())
                .category(source.getCategory())
                .userId(source.getUserId())
                .active(source.getActive())
                .createdAt(source.getCreatedAt())
                .build();
    }
}