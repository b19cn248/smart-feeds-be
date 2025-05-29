package com.olh.feeds.service.impl;

import com.olh.feeds.dao.repository.CategoryRepository;
import com.olh.feeds.dto.mapper.PageMapper;
import com.olh.feeds.dto.response.PageResponse;
import com.olh.feeds.dto.response.article.ArticleResponse;
import com.olh.feeds.service.CategoryService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Data
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final PageMapper pageMapper;

    @Override
    public PageResponse<ArticleResponse> getArticlesByCategory(Long categoryId, Pageable pageable) {
        return pageMapper.toPageDto(
                categoryRepository.getArticlesByCategoryId(categoryId, pageable)
        );
    }
}
