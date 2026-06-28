package com.k12.resource.controller;

import com.k12.common.Result;
import com.k12.common.annotation.RequiresPermission;
import com.k12.common.dto.SensitiveWordDTO;
import com.k12.common.entity.SysSensitiveWord;
import com.k12.resource.service.SensitiveWordService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Phase 8：敏感词管理 Controller
 */
@RestController
@RequestMapping("/api/admin/quality")
public class AdminSensitiveWordController {

    private final SensitiveWordService sensitiveWordService;
    public AdminSensitiveWordController(SensitiveWordService sensitiveWordService) {
        this.sensitiveWordService = sensitiveWordService;
    }


    /**
     * 分页查询敏感词
     */
    @GetMapping("/sensitive-words")
    @RequiresPermission("admin:quality:sensitive_view")
    public Result<Page<SysSensitiveWord>> list(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer category,
            @RequestParam(required = false) Integer level,
            @RequestParam(required = false) Integer status) {
        return Result.success(sensitiveWordService.listPage(
                pageNum, pageSize, keyword, category, level, status));
    }

    /**
     * 新增敏感词
     */
    @PostMapping("/sensitive-words")
    @RequiresPermission("admin:quality:sensitive_edit")
    public Result<SysSensitiveWord> create(@RequestBody SensitiveWordDTO dto) {
        return Result.success(sensitiveWordService.create(dto));
    }

    /**
     * 编辑敏感词
     */
    @PutMapping("/sensitive-words/{id}")
    @RequiresPermission("admin:quality:sensitive_edit")
    public Result<SysSensitiveWord> update(@PathVariable Long id, @RequestBody SensitiveWordDTO dto) {
        return Result.success(sensitiveWordService.update(id, dto));
    }

    /**
     * 删除敏感词
     */
    @DeleteMapping("/sensitive-words/{id}")
    @RequiresPermission("admin:quality:sensitive_edit")
    public Result<Void> delete(@PathVariable Long id) {
        sensitiveWordService.delete(id);
        return Result.success();
    }

    /**
     * 批量启用/禁用
     */
    @PutMapping("/sensitive-words/batch-status")
    @RequiresPermission("admin:quality:sensitive_edit")
    public Result<Void> batchUpdateStatus(@RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        List<Long> ids = (List<Long>) body.get("ids");
        Integer status = (Integer) body.get("status");
        sensitiveWordService.batchUpdateStatus(ids, status);
        return Result.success();
    }

    /**
     * 按分类统计
     */
    @GetMapping("/sensitive-words/stats")
    @RequiresPermission("admin:quality:sensitive_view")
    public Result<List<Map<String, Object>>> stats() {
        return Result.success(sensitiveWordService.countByCategory());
    }
}
