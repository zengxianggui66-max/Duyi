package com.k12.article.controller;

import com.k12.common.PageResult;
import com.k12.common.Result;
import com.k12.common.annotation.AdminLog;
import com.k12.common.annotation.RequiresPermission;
import com.k12.common.dto.AdminArticleQueryDTO;
import com.k12.common.dto.ArticleCreateDTO;
import com.k12.common.entity.Article;
import com.k12.article.service.ArticleService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * Phase 3J-2：教育资讯内容中心 Admin
 */
@RestController
@RequestMapping("/api/admin/articles")
public class AdminArticleController {

    private final ArticleService articleService;

    public AdminArticleController(ArticleService articleService) {
        this.articleService = articleService;
    }

    @GetMapping
    @RequiresPermission("admin:content:view")
    public Result<PageResult<Article>> list(AdminArticleQueryDTO query) {
        return Result.success(articleService.listAdminArticles(query));
    }

    @GetMapping("/{id}")
    @RequiresPermission("admin:content:view")
    public Result<Article> detail(@PathVariable Long id) {
        return Result.success(articleService.getAdminArticle(id));
    }

    @PostMapping
    @RequiresPermission("admin:content:edit")
    @AdminLog(module = "content", action = "create_article", permission = "admin:content:edit")
    public Result<Long> create(
            @Valid @RequestBody ArticleCreateDTO dto,
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            @RequestHeader(value = "X-Username", required = false) String username) {
        return Result.success(articleService.createArticle(dto, userId, username));
    }

    @PutMapping("/{id}")
    @RequiresPermission("admin:content:edit")
    @AdminLog(module = "content", action = "update_article", permission = "admin:content:edit")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody ArticleCreateDTO dto) {
        articleService.updateArticle(id, dto);
        return Result.success(null);
    }

    @PutMapping("/{id}/status")
    @RequiresPermission("admin:content:edit")
    @AdminLog(module = "content", action = "update_article_status", permission = "admin:content:edit")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam int status) {
        articleService.updateArticleStatus(id, status);
        return Result.success(null);
    }

    @DeleteMapping("/{id}")
    @RequiresPermission("admin:content:edit")
    @AdminLog(module = "content", action = "delete_article", permission = "admin:content:edit")
    public Result<Void> delete(@PathVariable Long id) {
        articleService.deleteArticle(id);
        return Result.success(null);
    }
}
