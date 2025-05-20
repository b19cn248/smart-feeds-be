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

import java.util.*;
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
    @Transactional(readOnly = true)
    public PageResponse<FolderWithArticlesResponse> getFoldersWithArticles(Pageable pageable, int articlePageSize, String keyword) {
        log.info("Getting folders with articles for current user, keyword: {}", keyword);

        String username = auditorAware.getCurrentAuditor().orElse("system");
        log.info("Current username: {}", username);

        Page<FolderResponse> foldersPage = folderRepository.findByCreatedBy(username, pageable);
        List<FolderWithArticlesResponse> folderWithArticles = new ArrayList<>();

        // Use keyword directly as a String (null if not provided or empty)
        String searchKeyword = keyword != null && !keyword.trim().isEmpty() ? keyword : null;

        for (FolderResponse folder : foldersPage.getContent()) {
            List<FolderSource> folderSources = folderSourceRepository.findByFolderId(folder.getId());

            if (folderSources.isEmpty()) {
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

            List<Long> sourceIds = folderSources.stream()
                  .map(FolderSource::getSourceId)
                  .collect(Collectors.toList());

            List<ArticleResponse> articles = articleRepository.findBySourceIdInAndKeywordOrderByPublishDateDesc(
                  sourceIds,
                  searchKeyword,
                  PageRequest.of(0, articlePageSize)
            );

            // Lấy hashtag cho các bài viết
            if (!articles.isEmpty()) {
                List<Long> articleIds = articles.stream()
                      .map(ArticleResponse::getId)
                      .collect(Collectors.toList());
                List<Object[]> tagResults = articleRepository.findTagNamesByArticleIds(articleIds);

                // Tạo map từ articleId sang danh sách hashtag
                Map<Long, List<String>> articleTagsMap = new HashMap<>();
                for (Object[] result : tagResults) {
                    Long articleId = ((Number) result[0]).longValue();
                    String tagName = (String) result[1];
                    articleTagsMap.computeIfAbsent(articleId, k -> new ArrayList<>()).add(tagName);
                }

                // Gán hashtag vào ArticleResponse
                articles.forEach(article -> article.setHashtag(
                      articleTagsMap.getOrDefault(article.getId(), Collections.emptyList())
                ));
            }

            folderWithArticles.add(FolderWithArticlesResponse.builder()
                  .id(folder.getId())
                  .name(folder.getName())
                  .theme(folder.getTheme())
                  .userId(folder.getUserId())
                  .createdAt(folder.getCreatedAt())
                  .articles(articles)
                  .build());
        }

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

        Folder folder = folderRepository.findFolderById(folderId);
        if (folder == null) {
            log.error("Folder not found with ID: {}", folderId);
            throw new NotFoundException(folderId.toString(), "folder");
        }

        String username = auditorAware.getCurrentAuditor().orElse("system");
        if (!folder.getCreatedBy().equals(username)) {
            log.error("User {} does not have permission to access folder {}", username, folderId);
            throw new BadRequestException("folder.access.denied");
        }

        List<FolderSource> folderSources = folderSourceRepository.findByFolderId(folderId);
        List<SourceResponse> sources = new ArrayList<>();

        if (!folderSources.isEmpty()) {
            List<Long> sourceIds = folderSources.stream()
                  .map(FolderSource::getSourceId)
                  .collect(Collectors.toList());

            List<Source> sourceList = sourceRepository.findAllById(sourceIds);
            sources = sourceList.stream()
                  .filter(Objects::nonNull)
                  .map(this::mapToSourceResponse)
                  .collect(Collectors.toList());

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

    private Page<ArticleResponse> getArticlesBySourceIds(List<Long> sourceIds, Pageable pageable) {
        int totalArticles = articleRepository.countBySourceIdIn(sourceIds);
        List<ArticleResponse> articles = articleRepository.findBySourceIdInAndKeywordOrderByPublishDateDesc(
              sourceIds,
              null,
              pageable
        );

        // Lấy hashtag cho các bài viết
        if (!articles.isEmpty()) {
            List<Long> articleIds = articles.stream()
                  .map(ArticleResponse::getId)
                  .collect(Collectors.toList());
            List<Object[]> tagResults = articleRepository.findTagNamesByArticleIds(articleIds);

            // Tạo map từ articleId sang danh sách hashtag
            Map<Long, List<String>> articleTagsMap = new HashMap<>();
            for (Object[] result : tagResults) {
                Long articleId = ((Number) result[0]).longValue();
                String tagName = (String) result[1];
                articleTagsMap.computeIfAbsent(articleId, k -> new ArrayList<>()).add(tagName);
            }

            // Gán hashtag vào ArticleResponse
            articles.forEach(article -> article.setHashtag(
                  articleTagsMap.getOrDefault(article.getId(), Collections.emptyList())
            ));
        }

        return new PageImpl<>(articles, pageable, totalArticles);
    }

    @Override
    public PageResponse<FolderResponse> getAllFolders(Long userId, Pageable pageable) {
        log.info("Getting all folders for user ID: {}", userId);
        Long effectiveUserId = userId;
        Page<FolderResponse> foldersPage = folderRepository.findAllFolders(effectiveUserId, pageable);
        return pageMapper.toPageDto(foldersPage);
    }

    @Override
    public PageResponse<FolderResponse> getFoldersByCurrentUser(Pageable pageable) {
        String username = auditorAware.getCurrentAuditor().get();
        log.info("Getting folders for current user: {}", username);
        Page<FolderResponse> foldersPage = folderRepository.findByCreatedBy(username, pageable);
        return pageMapper.toPageDto(foldersPage);
    }

    @Override
    public FolderDetailResponse getFolderDetail(Long folderId) {
        log.info("Getting folder details for ID: {}", folderId);
        Folder folder = folderRepository.findFolderById(folderId);
        if (folder == null) {
            log.error("Folder not found with ID: {}", folderId);
            throw new NotFoundException(folderId.toString(), "folder");
        }
        List<FolderSource> folderSources = folderSourceRepository.findByFolderId(folderId);
        List<Long> sourceIds = folderSources.stream()
              .map(FolderSource::getSourceId)
              .collect(Collectors.toList());
        List<SourceResponse> sources = new ArrayList<>();
        if (!sourceIds.isEmpty()) {
            List<Source> sourceList = sourceRepository.findAllById(sourceIds);
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
        String username = auditorAware.getCurrentAuditor().get();
        Folder folder = Folder.builder()
              .name(request.getName())
              .theme(request.getTheme())
              .userId(null)
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
    public FolderResponse updateFolder(Long id, FolderRequest request) {
        log.info("Updating folder with ID: {}", id);
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new BadRequestException("folder.name.required");
        }
        Folder folder = folderRepository.findFolderById(id);
        if (folder == null) {
            log.error("Folder not found with ID: {}", id);
            throw new NotFoundException(id.toString(), "folder");
        }
        String username = auditorAware.getCurrentAuditor().orElse("system");
        if (!folder.getCreatedBy().equals(username)) {
            log.error("User {} does not have permission to update folder {}", username, id);
            throw new BadRequestException("folder.access.denied");
        }
        folder.setName(request.getName());
        folder.setTheme(request.getTheme());
        folder = folderRepository.save(folder);
        log.info("Folder updated successfully with ID: {}", folder.getId());
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
        Folder folder = folderRepository.findFolderById(folderId);
        if (folder == null) {
            log.error("Folder not found with ID: {}", folderId);
            throw new NotFoundException(folderId.toString(), "folder");
        }
        String username = auditorAware.getCurrentAuditor().get();
        if (!folder.getCreatedBy().equals(username)) {
            log.error("User {} does not have permission to access folder {}", username, folderId);
            throw new BadRequestException("folder.access.denied");
        }
        Source source = sourceRepository.findById(request.getSourceId())
              .orElseThrow(() -> {
                  log.error("Source not found with ID: {}", request.getSourceId());
                  return new NotFoundException(request.getSourceId().toString(), "source");
              });
        boolean exists = folderSourceRepository.existsByFolderIdAndSourceId(folderId, request.getSourceId());
        if (exists) {
            log.error("Source already exists in folder");
            throw new ConflictException("folder.source.already.exists");
        }
        FolderSource folderSource = FolderSource.builder()
              .folderId(folderId)
              .sourceId(request.getSourceId())
              .build();
        folderSourceRepository.save(folderSource);
        log.info("Source added to folder successfully");
        return getFolderDetail(folderId);
    }

    @Override
    @Transactional
    public FolderDetailResponse removeSourceFromFolder(Long folderId, Long sourceId) {
        log.info("Removing source ID: {} from folder ID: {}", sourceId, folderId);

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

        // Kiểm tra source tồn tại trong folder
        boolean exists = folderSourceRepository.existsByFolderIdAndSourceId(folderId, sourceId);
        if (!exists) {
            log.error("Source ID: {} does not exist in folder ID: {}", sourceId, folderId);
            throw new NotFoundException("folder.source.not.found");
        }

        // Xóa source khỏi folder
        folderSourceRepository.deleteByFolderIdAndSourceId(folderId, sourceId);
        log.info("Successfully removed source ID: {} from folder ID: {}", sourceId, folderId);

        // Trả về thông tin folder sau khi xóa
        return getFolderDetail(folderId);
    }

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