package com.k12.resource.controller;

import com.k12.common.Result;
import com.k12.common.constant.ResourceTypeConstants;
import com.k12.common.dto.BehaviorQueryDTO;
import com.k12.common.dto.BehaviorStatsVO;
import com.k12.resource.security.UserIdResolver;
import com.k12.resource.service.DownloadRecordService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 用户下载记录（个人中心）
 */
@RestController
@RequestMapping("/api/resource/download")
public class DownloadController {

    private final DownloadRecordService downloadRecordService;
    private final UserIdResolver userIdResolver;
    public DownloadController(DownloadRecordService downloadRecordService, UserIdResolver userIdResolver) {
        this.downloadRecordService = downloadRecordService;
        this.userIdResolver = userIdResolver;
    }


    @GetMapping("/page")
    public Result<Map<String, Object>> page(BehaviorQueryDTO dto, HttpServletRequest request) {
        Long userId = userIdResolver.resolve(request);
        if (userId == null) {
            return Result.fail("请先登录");
        }
        return Result.success(downloadRecordService.listByPage(userId, dto));
    }

    @GetMapping("/stats")
    public Result<BehaviorStatsVO> stats(HttpServletRequest request) {
        Long userId = userIdResolver.resolve(request);
        if (userId == null) {
            return Result.fail("请先登录");
        }
        return Result.success(downloadRecordService.getStats(userId));
    }

    @PostMapping
    public Result<Void> add(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        Long userId = userIdResolver.resolve(request);
        if (userId == null) {
            return Result.fail("请先登录");
        }
        Long resourceId = toLong(params.get("resourceId"));
        if (resourceId == null) {
            return Result.fail("resourceId 不能为空");
        }
        String resourceTitle = params.get("resourceTitle") != null
                ? String.valueOf(params.get("resourceTitle"))
                : null;
        String resourceType = params.get("resourceType") != null
                ? String.valueOf(params.get("resourceType"))
                : ResourceTypeConstants.PRIMARY_CHINESE;
        downloadRecordService.addRecord(userId, resourceId, resourceTitle, resourceType);
        return Result.success(null);
    }

    @DeleteMapping("/{id}")
    public Result<Void> remove(@PathVariable Long id, HttpServletRequest request) {
        Long userId = userIdResolver.resolve(request);
        if (userId == null) {
            return Result.fail("请先登录");
        }
        downloadRecordService.remove(userId, id);
        return Result.success(null);
    }

    @DeleteMapping("/batch")
    public Result<Void> batchRemove(@RequestBody Map<String, List<Long>> body, HttpServletRequest request) {
        Long userId = userIdResolver.resolve(request);
        if (userId == null) {
            return Result.fail("请先登录");
        }
        downloadRecordService.batchRemove(userId, body.get("ids"));
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
