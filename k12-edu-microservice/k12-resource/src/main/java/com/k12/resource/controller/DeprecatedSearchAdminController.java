package com.k12.resource.controller;

import com.k12.common.Result;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Phase 9-A：旧路径 /api/search/admin/** 已迁移至 /api/admin/search/**。
 * 直连 resource 服务时也返回 404，避免误用旧 URL 得到 500。
 */
@RestController
public class DeprecatedSearchAdminController {

    @RequestMapping("/api/search/admin/**")
    public ResponseEntity<Result<Void>> deprecated() {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(Result.fail(404, "搜索管理 API 已迁移至 /api/admin/search/*"));
    }
}
