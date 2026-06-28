package com.k12.resource.controller;

import com.k12.common.Result;
import com.k12.common.constant.ResourceTypeConstants;
import com.k12.common.entity.ShareRecord;
import com.k12.resource.security.UserIdResolver;
import com.k12.resource.service.ShareRecordService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 分享接口
 */
@RestController
@RequestMapping("/api/resource/share")
public class ShareController {

    private final ShareRecordService shareRecordService;
    private final UserIdResolver userIdResolver;
    public ShareController(ShareRecordService shareRecordService, UserIdResolver userIdResolver) {
        this.shareRecordService = shareRecordService;
        this.userIdResolver = userIdResolver;
    }


    @PostMapping("/record")
    public Result<Map<String, Object>> recordShare(
            @RequestBody Map<String, Object> params,
            HttpServletRequest request) {
        Long resourceId = ((Number) params.get("resourceId")).longValue();
        String resourceType = params.get("resourceType") != null
                ? String.valueOf(params.get("resourceType"))
                : ResourceTypeConstants.RESOURCE;
        String shareType = params.get("shareType") != null ? String.valueOf(params.get("shareType")) : "link";
        String sharePlatform = params.get("sharePlatform") != null ? String.valueOf(params.get("sharePlatform")) : "copy";

        Long userId = userIdResolver.resolve(request);
        String ipAddress = getClientIp(request);
        String userAgent = request.getHeader("User-Agent");

        ShareRecord record = shareRecordService.recordShare(
                resourceId, resourceType, userId, shareType, sharePlatform, ipAddress, userAgent);

        Map<String, Object> result = new HashMap<>();
        result.put("shareId", record.getId());
        result.put("shareUrl", record.getShareUrl());
        return Result.success(result);
    }

    @GetMapping("/url/{resourceId}")
    public Result<Map<String, Object>> getShareUrl(
            @PathVariable Long resourceId,
            @RequestParam(required = false) String resourceType) {
        String type = resourceType != null ? resourceType : ResourceTypeConstants.RESOURCE;
        Map<String, Object> result = new HashMap<>();
        result.put("shareUrl", shareRecordService.buildShareUrl(resourceId, type));
        result.put("title", "新课堂教育");
        result.put("description", "发现优质教育资源");
        return Result.success(result);
    }

    @GetMapping("/count/{resourceId}")
    public Result<Map<String, Object>> getShareCount(
            @PathVariable Long resourceId,
            @RequestParam(required = false) String resourceType) {
        String type = resourceType != null ? resourceType : ResourceTypeConstants.RESOURCE;
        Long count = shareRecordService.getShareCount(resourceId, type);
        Map<String, Object> result = new HashMap<>();
        result.put("count", count);
        return Result.success(result);
    }

    @GetMapping("/my")
    public Result<List<ShareRecord>> myShares(HttpServletRequest request) {
        Long userId = userIdResolver.resolve(request);
        if (userId == null) {
            return Result.fail("请先登录");
        }
        return Result.success(shareRecordService.getUserShares(userId));
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
