package com.k12.auth.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.k12.auth.admin.service.AdminOperationLogService;
import com.k12.common.Result;
import com.k12.common.annotation.RequiresPermission;
import com.k12.common.dto.AdminOperationLogVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/operation-logs")
public class AdminOperationLogController {

    private final AdminOperationLogService adminOperationLogService;
    public AdminOperationLogController(AdminOperationLogService adminOperationLogService) {
        this.adminOperationLogService = adminOperationLogService;
    }


    @GetMapping
    @RequiresPermission("admin:system:log_view")
    public Result<Page<AdminOperationLogVO>> list(
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
}
