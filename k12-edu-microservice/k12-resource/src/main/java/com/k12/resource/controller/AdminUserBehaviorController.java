package com.k12.resource.controller;



import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import com.k12.common.Result;

import com.k12.common.annotation.RequiresPermission;

import com.k12.common.dto.AdminUserActionVO;

import com.k12.common.dto.AdminUserLoginLogVO;

import com.k12.common.dto.AdminUserStatsVO;

import com.k12.common.dto.AdminUserUploadVO;

import com.k12.common.dto.CollectItemVO;

import com.k12.resource.service.AdminUserBehaviorService;

import org.springframework.web.bind.annotation.*;



/**

 * Phase 6-A/C：用户行为只读（上传 / 收藏 / 浏览下载搜索流水 / 登录）

 */

@RestController

@RequestMapping("/api/admin/users")
public class AdminUserBehaviorController {



    private final AdminUserBehaviorService adminUserBehaviorService;
    public AdminUserBehaviorController(AdminUserBehaviorService adminUserBehaviorService) {
        this.adminUserBehaviorService = adminUserBehaviorService;
    }




    @GetMapping("/{id}/stats")

    @RequiresPermission("admin:user:view")

    public Result<AdminUserStatsVO> stats(@PathVariable Long id) {

        return Result.success(adminUserBehaviorService.getStats(id));

    }



    @GetMapping("/{id}/resources")

    @RequiresPermission("admin:user:view")

    public Result<Page<AdminUserUploadVO>> resources(

            @PathVariable Long id,

            @RequestParam(defaultValue = "1") int current,

            @RequestParam(defaultValue = "10") int size) {

        return Result.success(adminUserBehaviorService.listUploads(id, current, size));

    }



    @GetMapping("/{id}/collections")

    @RequiresPermission("admin:user:view")

    public Result<Page<CollectItemVO>> collections(

            @PathVariable Long id,

            @RequestParam(defaultValue = "1") int current,

            @RequestParam(defaultValue = "10") int size) {

        return Result.success(adminUserBehaviorService.listCollections(id, current, size));

    }



    @GetMapping("/{id}/actions")

    @RequiresPermission("admin:user:view_behavior")

    public Result<Page<AdminUserActionVO>> actions(

            @PathVariable Long id,

            @RequestParam(required = false) String actionType,

            @RequestParam(defaultValue = "1") int current,

            @RequestParam(defaultValue = "10") int size) {

        return Result.success(adminUserBehaviorService.listActions(id, actionType, current, size));

    }



    @GetMapping("/{id}/login-logs")

    @RequiresPermission("admin:user:view_behavior")

    public Result<Page<AdminUserLoginLogVO>> loginLogs(

            @PathVariable Long id,

            @RequestParam(defaultValue = "1") int current,

            @RequestParam(defaultValue = "10") int size) {

        return Result.success(adminUserBehaviorService.listLoginLogs(id, current, size));

    }

}


