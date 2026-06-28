package com.k12.auth.admin.controller;

import com.k12.auth.admin.service.SystemConfigService;
import com.k12.common.Result;
import com.k12.common.annotation.AdminLog;
import com.k12.common.annotation.RequiresPermission;
import com.k12.common.dto.AdminSystemConfigGroupVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Phase 8-B：系统配置 GET/PUT
 */
@RestController
@RequestMapping("/api/admin/system")
public class AdminSystemConfigController {

    private final SystemConfigService systemConfigService;
    public AdminSystemConfigController(SystemConfigService systemConfigService) {
        this.systemConfigService = systemConfigService;
    }


    @GetMapping("/config")
    @RequiresPermission("admin:system:config_view")
    public Result<AdminSystemConfigGroupVO> getConfig(@RequestParam String group) {
        return Result.success(systemConfigService.getGroup(group));
    }

    @PutMapping("/config")
    @RequiresPermission("admin:system:config_edit")
    @AdminLog(module = "system", action = "update_config", permission = "admin:system:config_edit")
    public Result<AdminSystemConfigGroupVO> updateConfig(
            @RequestParam String group,
            @RequestBody Map<String, Object> values,
            @RequestHeader(value = "X-User-Id", required = false) Long operatorId) {
        return Result.success(systemConfigService.updateGroup(group, values, operatorId));
    }
}
