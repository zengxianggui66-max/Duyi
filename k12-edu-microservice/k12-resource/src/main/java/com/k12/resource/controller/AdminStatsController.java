package com.k12.resource.controller;



import com.k12.common.Result;

import com.k12.resource.service.AdminPrimaryResourceService;

import com.k12.resource.service.AdminResourceService;

import org.springframework.web.bind.annotation.GetMapping;

import org.springframework.web.bind.annotation.RequestHeader;

import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;



import java.util.Map;



/**

 * 管理端 - 统计接口

 */

@RestController

@RequestMapping("/api/admin/stats")
public class AdminStatsController {



    private final AdminPrimaryResourceService adminPrimaryResourceService;

    private final AdminResourceService adminResourceService;
    public AdminStatsController(AdminPrimaryResourceService adminPrimaryResourceService, AdminResourceService adminResourceService) {
        this.adminPrimaryResourceService = adminPrimaryResourceService;
        this.adminResourceService = adminResourceService;
    }




    /**

     * 资源统计（主表 oss_primary_chinese_resource）

     */

    @GetMapping("/resources")

    @com.k12.common.annotation.RequiresPermission("admin:analytics:view")

    public Result<Map<String, Object>> resourceStats(

            @RequestHeader(value = "X-User-Id", required = false) Long userId) {

        return Result.success(adminPrimaryResourceService.getResourceStats(userId));

    }



    /** @deprecated 请使用 GET /api/admin/analytics/users */

    @GetMapping("/users")

    @com.k12.common.annotation.RequiresPermission("admin:analytics:view")

    public Result<Map<String, Object>> userStats() {

        return Result.success(adminResourceService.getUserStats());

    }

}

