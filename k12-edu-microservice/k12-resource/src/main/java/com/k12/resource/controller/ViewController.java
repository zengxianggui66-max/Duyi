package com.k12.resource.controller;

import com.k12.common.Result;
import com.k12.common.constant.ResourceTypeConstants;
import com.k12.common.dto.BehaviorQueryDTO;
import com.k12.common.dto.BehaviorStatsVO;
import com.k12.resource.security.UserIdResolver;
import com.k12.resource.service.ViewRecordService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 用户浏览记录（个人中心）
 */
@RestController
@RequestMapping("/api/resource/view")
public class ViewController {

    private final ViewRecordService viewRecordService;
    private final UserIdResolver userIdResolver;
    public ViewController(ViewRecordService viewRecordService, UserIdResolver userIdResolver) {
        this.viewRecordService = viewRecordService;
        this.userIdResolver = userIdResolver;
    }


    @GetMapping("/page")
    public Result<Map<String, Object>> page(BehaviorQueryDTO dto, HttpServletRequest request) {
        Long userId = userIdResolver.resolve(request);
        if (userId == null) {
            return Result.fail("请先登录");
        }
        return Result.success(viewRecordService.listByPage(userId, dto));
    }

    @GetMapping("/stats")
    public Result<BehaviorStatsVO> stats(HttpServletRequest request) {
        Long userId = userIdResolver.resolve(request);
        if (userId == null) {
            return Result.fail("请先登录");
        }
        return Result.success(viewRecordService.getStats(userId));
    }

    @PostMapping
    public Result<Void> upsert(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        Long userId = userIdResolver.resolve(request);
        if (userId == null) {
            return Result.success(null);
        }
        Long resourceId = toLong(params.get("resourceId"));
        if (resourceId == null) {
            return Result.fail("resourceId 不能为空");
        }
        String resourceType = params.get("resourceType") != null
                ? String.valueOf(params.get("resourceType"))
                : ResourceTypeConstants.PRIMARY_CHINESE;
        viewRecordService.upsertView(userId, resourceId, resourceType, params);
        return Result.success(null);
    }

    @DeleteMapping("/{id}")
    public Result<Void> remove(@PathVariable Long id, HttpServletRequest request) {
        Long userId = userIdResolver.resolve(request);
        if (userId == null) {
            return Result.fail("请先登录");
        }
        viewRecordService.remove(userId, id);
        return Result.success(null);
    }

    @DeleteMapping
    public Result<Void> clearAll(HttpServletRequest request) {
        Long userId = userIdResolver.resolve(request);
        if (userId == null) {
            return Result.fail("请先登录");
        }
        viewRecordService.clearAll(userId);
        return Result.success(null);
    }

    @DeleteMapping("/batch")
    public Result<Void> batchRemove(@RequestBody Map<String, List<Long>> body, HttpServletRequest request) {
        Long userId = userIdResolver.resolve(request);
        if (userId == null) {
            return Result.fail("请先登录");
        }
        viewRecordService.batchRemove(userId, body.get("ids"));
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
