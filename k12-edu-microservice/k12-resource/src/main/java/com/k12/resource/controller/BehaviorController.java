package com.k12.resource.controller;

import com.k12.common.Result;
import com.k12.common.constant.ResourceTypeConstants;
import com.k12.resource.security.UserIdResolver;
import com.k12.resource.service.AdminResourceService;
import com.k12.resource.service.DownloadRecordService;
import com.k12.resource.service.ViewRecordService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 用户行为记录接口（资源计数 + 个人记录）
 */
@RestController
@RequestMapping("/api/resource")
public class BehaviorController {

    private final AdminResourceService adminResourceService;
    private final DownloadRecordService downloadRecordService;
    private final ViewRecordService viewRecordService;
    private final UserIdResolver userIdResolver;
    public BehaviorController(AdminResourceService adminResourceService, DownloadRecordService downloadRecordService, ViewRecordService viewRecordService, UserIdResolver userIdResolver) {
        this.adminResourceService = adminResourceService;
        this.downloadRecordService = downloadRecordService;
        this.viewRecordService = viewRecordService;
        this.userIdResolver = userIdResolver;
    }


    /**
     * 记录下载
     */
    @PostMapping("/download/record")
    public Result<Void> recordDownload(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        Long resourceId = toLong(params.get("resourceId"));
        if (resourceId == null) {
            return Result.fail("resourceId 不能为空");
        }
        Long userId = userIdResolver.resolve(request);
        if (userId != null) {
            String resourceTitle = params.get("resourceTitle") != null
                    ? String.valueOf(params.get("resourceTitle"))
                    : null;
            String resourceType = params.get("resourceType") != null
                    ? String.valueOf(params.get("resourceType"))
                    : ResourceTypeConstants.PRIMARY_CHINESE;
            downloadRecordService.addRecord(userId, resourceId, resourceTitle, resourceType);
        } else {
            adminResourceService.recordDownload(resourceId);
        }
        return Result.success(null);
    }

    /**
     * 记录浏览
     */
    @PostMapping("/view/record")
    public Result<Void> recordView(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        Long resourceId = toLong(params.get("resourceId"));
        if (resourceId == null) {
            return Result.fail("resourceId 不能为空");
        }
        Long userId = userIdResolver.resolve(request);
        if (userId != null) {
            String resourceType = params.get("resourceType") != null
                    ? String.valueOf(params.get("resourceType"))
                    : ResourceTypeConstants.PRIMARY_CHINESE;
            viewRecordService.upsertView(userId, resourceId, resourceType, params);
        } else {
            adminResourceService.recordView(resourceId);
        }
        return Result.success(null);
    }

    private Long toLong(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number n) {
            return n.longValue();
        }
        try {
            return Long.parseLong(String.valueOf(value));
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
