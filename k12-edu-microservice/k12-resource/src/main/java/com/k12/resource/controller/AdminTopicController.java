package com.k12.resource.controller;

import com.k12.common.PageResult;
import com.k12.common.Result;
import com.k12.common.annotation.AdminLog;
import com.k12.common.annotation.RequiresPermission;
import com.k12.common.dto.AdminContentPackageWriteDTO;
import com.k12.common.dto.AdminContentQueryDTO;
import com.k12.common.entity.TopicAlbum;
import com.k12.resource.service.admin.AdminContentTopicService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * Phase 3J-2：专题内容中心 Admin（精品专辑 CRUD）
 */
@RestController
@RequestMapping("/api/admin/topic")
public class AdminTopicController {

    private final AdminContentTopicService adminContentTopicService;

    public AdminTopicController(AdminContentTopicService adminContentTopicService) {
        this.adminContentTopicService = adminContentTopicService;
    }

    @GetMapping("/albums")
    @RequiresPermission("admin:content:view")
    public Result<PageResult<TopicAlbum>> listAlbums(AdminContentQueryDTO query) {
        return Result.success(adminContentTopicService.listAlbums(query));
    }

    @GetMapping("/albums/{id}")
    @RequiresPermission("admin:content:view")
    public Result<TopicAlbum> getAlbum(@PathVariable Long id) {
        return Result.success(adminContentTopicService.getAlbum(id));
    }

    @PostMapping("/albums")
    @RequiresPermission("admin:content:edit")
    @AdminLog(module = "content", action = "create_topic_album", permission = "admin:content:edit")
    public Result<TopicAlbum> createAlbum(@Valid @RequestBody AdminContentPackageWriteDTO dto) {
        return Result.success(adminContentTopicService.createAlbum(dto));
    }

    @PutMapping("/albums/{id}")
    @RequiresPermission("admin:content:edit")
    @AdminLog(module = "content", action = "update_topic_album", permission = "admin:content:edit")
    public Result<TopicAlbum> updateAlbum(
            @PathVariable Long id,
            @Valid @RequestBody AdminContentPackageWriteDTO dto) {
        return Result.success(adminContentTopicService.updateAlbum(id, dto));
    }

    @PutMapping("/albums/{id}/status")
    @RequiresPermission("admin:content:edit")
    @AdminLog(module = "content", action = "update_topic_album_status", permission = "admin:content:edit")
    public Result<Void> updateAlbumStatus(@PathVariable Long id, @RequestParam int status) {
        adminContentTopicService.updateAlbumStatus(id, status);
        return Result.success(null);
    }

    @DeleteMapping("/albums/{id}")
    @RequiresPermission("admin:content:edit")
    @AdminLog(module = "content", action = "delete_topic_album", permission = "admin:content:edit")
    public Result<Void> deleteAlbum(@PathVariable Long id) {
        adminContentTopicService.deleteAlbum(id);
        return Result.success(null);
    }
}
