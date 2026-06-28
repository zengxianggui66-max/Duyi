package com.k12.auth.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.k12.auth.admin.service.AdminOperationLogService;
import com.k12.auth.admin.service.AdminSystemLoginLogService;
import com.k12.common.Result;
import com.k12.common.annotation.RequiresPermission;
import com.k12.common.dto.AdminOperationLogVO;
import com.k12.common.dto.AdminSystemLoginLogVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Phase 8-A：系统日志（操作日志 + 全站登录日志）
 */
@RestController
@RequestMapping("/api/admin/system")
public class AdminSystemLogController {

    private final AdminOperationLogService adminOperationLogService;
    private final AdminSystemLoginLogService adminSystemLoginLogService;
    public AdminSystemLogController(AdminOperationLogService adminOperationLogService, AdminSystemLoginLogService adminSystemLoginLogService) {
        this.adminOperationLogService = adminOperationLogService;
        this.adminSystemLoginLogService = adminSystemLoginLogService;
    }


    @GetMapping("/logs")
    @RequiresPermission("admin:system:log_view")
    public Result<Page<AdminOperationLogVO>> operationLogs(
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {
        return Result.success(adminOperationLogService.listLogs(
                current, size, module, username, action, status, startTime, endTime));
    }

    @GetMapping("/login-logs")
    @RequiresPermission("admin:system:log_view")
    public Result<Page<AdminSystemLoginLogVO>> loginLogs(
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String loginType,
            @RequestParam(required = false) Integer success,
            @RequestParam(required = false) Boolean staffOnly,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {
        return Result.success(adminSystemLoginLogService.listLogs(
                current, size, username, loginType, success, staffOnly, startTime, endTime));
    }
}
