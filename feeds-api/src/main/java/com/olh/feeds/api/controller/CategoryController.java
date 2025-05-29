package com.olh.feeds.api.controller;


import com.olh.feeds.core.exception.response.ResponseGeneral;
import com.olh.feeds.dto.response.PageResponse;
import com.olh.feeds.dto.response.article.ArticleResponse;
import com.olh.feeds.dto.response.category.CategoryResponse;
import com.olh.feeds.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
@Slf4j
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping("/{categoryId}/articles")
    public ResponseGeneral<PageResponse<ArticleResponse>> getArticlesOfCategory(
            @PathVariable Long categoryId,
            @PageableDefault Pageable pageable
    ) {
        PageResponse<ArticleResponse> articleResponsePageResponse = categoryService.getArticlesByCategory(categoryId, pageable);
        return ResponseGeneral.of(
                HttpStatus.OK.value(),
                "folder.list.success",
                articleResponsePageResponse
        );
    }


    @GetMapping
    public ResponseGeneral<List<CategoryResponse>> getAllCategories() {
        List<CategoryResponse> categoryResponseList = categoryService.getAllCategories();
        return ResponseGeneral.of(
                HttpStatus.OK.value(),
                "folder.list.success",
                categoryResponseList
        );
    }


}
