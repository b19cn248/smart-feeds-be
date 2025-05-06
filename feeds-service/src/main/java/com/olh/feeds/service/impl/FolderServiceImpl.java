package com.olh.feeds.service.impl;

import com.olh.feeds.core.exception.base.BadRequestException;
import com.olh.feeds.core.exception.base.ConflictException;
import com.olh.feeds.core.exception.base.NotFoundException;
import com.olh.feeds.dao.entity.Folder;
import com.olh.feeds.dao.entity.FolderSource;
import com.olh.feeds.dao.entity.Source;
import com.olh.feeds.dao.repository.FolderRepository;
import com.olh.feeds.dao.repository.FolderSourceRepository;
import com.olh.feeds.dao.repository.SourceRepository;
import com.olh.feeds.dto.mapper.PageMapper;
import com.olh.feeds.dto.request.folder.FolderRequest;
import com.olh.feeds.dto.request.folder.FolderSourceRequest;
import com.olh.feeds.dto.response.PageResponse;
import com.olh.feeds.dto.response.folder.FolderDetailResponse;
import com.olh.feeds.dto.response.folder.FolderResponse;
import com.olh.feeds.dto.response.source.SourceResponse;
import com.olh.feeds.service.FolderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
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
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

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
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
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