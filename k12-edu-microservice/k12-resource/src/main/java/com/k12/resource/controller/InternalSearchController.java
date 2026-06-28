package com.k12.resource.controller;

import com.k12.common.Result;
import com.k12.resource.search.SearchDocumentSyncService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 微服务内部搜索索引同步（article 等服务直调，不经网关对外暴露）
 */
@RestController
@RequestMapping("/api/internal/search")
public class InternalSearchController {

    private final SearchDocumentSyncService syncService;

    public InternalSearchController(SearchDocumentSyncService syncService) {
        this.syncService = syncService;
    }

    @PostMapping("/articles/{id}")
    public Result<Void> syncArticle(@PathVariable Long id) {
        syncService.syncArticleByIdAsync(id);
        return Result.success();
    }
}
