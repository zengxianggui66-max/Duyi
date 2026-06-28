package com.k12.resource.controller;

import com.k12.common.Result;
import com.k12.common.annotation.RequiresPermission;
import com.k12.common.entity.PreviewFailQueue;
import com.k12.resource.service.PreviewFailService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Phase 8：预览失败队列 Controller
 */
@RestController
@RequestMapping("/api/admin/quality")
public class AdminPreviewFailController {

    private final PreviewFailService previewFailService;
    public AdminPreviewFailController(PreviewFailService previewFailService) {
        this.previewFailService = previewFailService;
    }


    /**
     * 分页查询预览失败队列
     */
    @GetMapping("/preview-fails")
    @RequiresPermission("admin:quality:preview_fail")
    public Result<Page<PreviewFailQueue>> list(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String sourceType,
            @RequestParam(required = false) String keyword) {
        return Result.success(previewFailService.listPage(
                pageNum, pageSize, status, sourceType, keyword));
    }

    /**
     * 标记已处理
     */
    @PostMapping("/preview-fails/{id}/handle")
    @RequiresPermission("admin:quality:preview_fail")
    public Result<Void> markHandled(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", required = false) Long handlerId,
            @RequestHeader(value = "X-User-Name", required = false) String handlerName,
            @RequestBody(required = false) Map<String, String> body) {
        String note = body != null ? body.getOrDefault("note", "") : "";
        previewFailService.markHandled(id, handlerId, handlerName, note);
        return Result.success();
    }

    /**
     * 标记已忽略
     */
    @PostMapping("/preview-fails/{id}/ignore")
    @RequiresPermission("admin:quality:preview_fail")
    public Result<Void> markIgnored(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", required = false) Long handlerId,
            @RequestHeader(value = "X-User-Name", required = false) String handlerName,
            @RequestBody(required = false) Map<String, String> body) {
        String note = body != null ? body.getOrDefault("note", "") : "";
        previewFailService.markIgnored(id, handlerId, handlerName, note);
        return Result.success();
    }

    /**
     * 统计待处理数量
     */
    @GetMapping("/preview-fails/stats")
    @RequiresPermission("admin:quality:preview_fail")
    public Result<List<Map<String, Object>>> stats() {
        return Result.success(previewFailService.countByStatus());
    }
}
