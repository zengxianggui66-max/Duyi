package com.k12.resource.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.k12.common.BusinessException;
import com.k12.common.Result;
import com.k12.common.annotation.AdminLog;
import com.k12.common.annotation.RequiresPermission;
import com.k12.common.dto.AdminResourceBatchAuditDTO;
import com.k12.common.dto.AdminResourceBatchDTO;
import com.k12.common.dto.AdminResourceBatchResultVO;
import com.k12.common.dto.AdminResourceAuditInsightsVO;
import com.k12.common.dto.AdminResourceUpdateDTO;
import com.k12.common.dto.ResourceMainDetailVO;
import com.k12.common.dto.ResourceMainQueryDTO;
import com.k12.common.dto.ResourceMainVO;
import com.k12.common.entity.ResourceMain;
import com.k12.resource.adapter.ResourceSourceAdapter;
import com.k12.resource.adapter.ResourceSourceAdapterRegistry;
import com.k12.resource.mapper.ResourceMainMapper;
import com.k12.resource.service.AdminResourceMainService;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Phase 7：统一资源主域 API（管理端资源列表 / 审核中心共用）
 * 基于 v_admin_resource_main 统一视图，支持跨学段、跨学科、跨源类型
 *
 * 与现有 AdminResourcesController（/api/admin/resources）并存，
 * 新学科接入后无需改动审核系统。
 */
@RestController
@RequestMapping("/api/admin/resource-main")
public class AdminResourceMainController {

    private final AdminResourceMainService adminResourceMainService;
    private final ResourceMainMapper resourceMainMapper;
    private final ResourceSourceAdapterRegistry adapterRegistry;

    public AdminResourceMainController(AdminResourceMainService adminResourceMainService,
                                       ResourceMainMapper resourceMainMapper,
                                       ResourceSourceAdapterRegistry adapterRegistry) {
        this.adminResourceMainService = adminResourceMainService;
        this.resourceMainMapper = resourceMainMapper;
        this.adapterRegistry = adapterRegistry;
    }


    /**
     * 统一资源列表（支持 source_type 筛选多学科）
     * GET /api/admin/resource-main?sourceType=primary_chinese&stage=primary&auditStatus=1
     */
    @GetMapping
    public Result<Page<ResourceMainVO>> list(
            ResourceMainQueryDTO query,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        return Result.success(adminResourceMainService.listPage(query, userId));
    }

    /**
     * 统一待审队列（跨源类型聚合）
     * GET /api/admin/resource-main/pending?sourceType=primary_chinese&keyword=教案
     */
    @GetMapping("/pending")
    public Result<Page<ResourceMainVO>> pending(
            ResourceMainQueryDTO query,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        return Result.success(adminResourceMainService.listPending(query, userId));
    }

    /**
     * 统一资源详情（按 global_id）
     * GET /api/admin/resource-main/{globalId}
     */
    @GetMapping("/{globalId}")
    public Result<ResourceMainDetailVO> detail(
            @PathVariable Long globalId,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        return Result.success(adminResourceMainService.getDetail(globalId, userId));
    }

    /**
     * 统一资源统计（按 source_type 分组）
     * GET /api/admin/resource-main/stats
     */
    @GetMapping("/stats")
    public Result<Map<String, Object>> stats(
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        return Result.success(adminResourceMainService.getStats(userId));
    }

    @PutMapping("/{globalId}")
    @RequiresPermission("admin:resource:edit")
    @AdminLog(module = "resource", action = "update", permission = "admin:resource:edit")
    public Result<Void> update(
            @PathVariable Long globalId,
            @RequestBody AdminResourceUpdateDTO dto,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        ResourceMain row = requireSourceResource(globalId);
        ResourceSourceAdapter adapter = adapterRegistry.require(row.getSourceType());
        adapter.update(row.getSourceId(), dto, userId);
        return Result.success();
    }

    @PostMapping("/{globalId}/audit")
    @RequiresPermission("admin:audit:view")
    @AdminLog(module = "audit", action = "audit", permission = "admin:audit:approve")
    public Result<Void> audit(
            @PathVariable Long globalId,
            @RequestParam Integer status,
            @RequestParam(required = false) String reason,
            @RequestHeader(value = "X-User-Id", required = false) Long auditorId,
            @RequestHeader(value = "X-User-Name", required = false) String auditorName) {
        ResourceMain row = requireSourceResource(globalId);
        ResourceSourceAdapter adapter = adapterRegistry.require(row.getSourceType());
        adapter.audit(row.getSourceId(), status, reason, auditorId, auditorName);
        return Result.success();
    }

    @PostMapping("/{globalId}/publish")
    @RequiresPermission("admin:resource:edit")
    @AdminLog(module = "resource", action = "publish", permission = "admin:resource:edit")
    public Result<Void> publish(
            @PathVariable Long globalId,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        ResourceMain row = requireSourceResource(globalId);
        adapterRegistry.require(row.getSourceType()).publish(row.getSourceId(), userId);
        return Result.success();
    }

    @PostMapping("/{globalId}/offline")
    @RequiresPermission("admin:resource:edit")
    @AdminLog(module = "resource", action = "offline", permission = "admin:resource:edit")
    public Result<Void> offline(
            @PathVariable Long globalId,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        ResourceMain row = requireSourceResource(globalId);
        adapterRegistry.require(row.getSourceType()).offline(row.getSourceId(), userId);
        return Result.success();
    }

    @DeleteMapping("/{globalId}")
    @RequiresPermission("admin:resource:delete")
    @AdminLog(module = "resource", action = "delete", permission = "admin:resource:delete")
    public Result<Void> recycle(
            @PathVariable Long globalId,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        ResourceMain row = requireSourceResource(globalId);
        adapterRegistry.require(row.getSourceType()).recycle(row.getSourceId(), userId);
        return Result.success();
    }

    @PostMapping("/{globalId}/restore")
    @RequiresPermission("admin:resource:edit")
    @AdminLog(module = "resource", action = "restore", permission = "admin:resource:edit")
    public Result<Void> restore(
            @PathVariable Long globalId,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        ResourceMain row = requireSourceResource(globalId);
        adapterRegistry.require(row.getSourceType()).restore(row.getSourceId(), userId);
        return Result.success();
    }

    @PostMapping("/{globalId}/recommend")
    @RequiresPermission("admin:resource:recommend")
    @AdminLog(module = "resource", action = "recommend", permission = "admin:resource:recommend")
    public Result<Void> recommend(
            @PathVariable Long globalId,
            @RequestParam(defaultValue = "true") boolean enabled,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        ResourceMain row = requireSourceResource(globalId);
        adapterRegistry.require(row.getSourceType()).setRecommend(row.getSourceId(), enabled, userId);
        return Result.success();
    }

    @PostMapping("/{globalId}/top")
    @RequiresPermission("admin:resource:top")
    @AdminLog(module = "resource", action = "top", permission = "admin:resource:top")
    public Result<Void> top(
            @PathVariable Long globalId,
            @RequestParam(defaultValue = "true") boolean enabled,
            @RequestParam(required = false) Integer topSort,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        ResourceMain row = requireSourceResource(globalId);
        ResourceSourceAdapter adapter = adapterRegistry.require(row.getSourceType());
        if (!adapter.supportsTop()) {
            throw new BusinessException(400, "当前资源类型不支持置顶");
        }
        adapter.setTop(row.getSourceId(), enabled, topSort, userId);
        return Result.success();
    }

    @GetMapping("/{globalId}/audit-insights")
    @RequiresPermission("admin:audit:view")
    public Result<AdminResourceAuditInsightsVO> auditInsights(
            @PathVariable Long globalId,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        ResourceMain row = requireSourceResource(globalId);
        ResourceSourceAdapter adapter = adapterRegistry.require(row.getSourceType());
        AdminResourceAuditInsightsVO vo = adapter.getAuditInsights(row.getSourceId(), userId);
        vo.setResourceId(globalId);
        return Result.success(vo);
    }

    @PostMapping("/{globalId}/placement")
    @RequiresPermission("admin:resource:edit")
    @AdminLog(module = "resource", action = "placement", permission = "admin:resource:edit")
    public Result<Void> placement(
            @PathVariable Long globalId,
            @RequestBody AdminResourceUpdateDTO dto,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        ResourceMain row = requireSourceResource(globalId);
        ResourceSourceAdapter adapter = adapterRegistry.require(row.getSourceType());
        if (!adapter.supportsPlacement()) {
            throw new BusinessException(400, "当前资源类型暂不支持目录挂载");
        }
        AdminResourceUpdateDTO update = new AdminResourceUpdateDTO();
        update.setCatalogNodeId(dto.getCatalogNodeId());
        update.setCatalogPath(dto.getCatalogPath());
        adapter.update(row.getSourceId(), update, userId);
        return Result.success();
    }

    @PostMapping("/batch")
    @RequiresPermission("admin:resource:batch")
    @AdminLog(module = "resource", action = "batch", permission = "admin:resource:batch")
    public Result<AdminResourceBatchResultVO> batch(
            @RequestBody AdminResourceBatchDTO body,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        if (body == null || body.getIds() == null || body.getIds().isEmpty()) {
            throw new BusinessException(400, "请选择资源");
        }
        int success = 0;
        List<String> reasons = new ArrayList<>();
        for (Long globalId : body.getIds()) {
            try {
                ResourceMain row = requireSourceResource(globalId);
                applyBatchAction(row, body.getAction(), userId);
                success++;
            } catch (Exception ex) {
                reasons.add("globalId=" + globalId + "，原因：" + ex.getMessage());
            }
        }
        return Result.success(AdminResourceBatchResultVO.of(
                success,
                body.getIds().size() - success,
                reasons,
                "批量操作完成"));
    }

    @PostMapping("/batch-audit")
    @RequiresPermission("admin:audit:view")
    @AdminLog(module = "audit", action = "batch_audit", permission = "admin:audit:approve")
    public Result<AdminResourceBatchResultVO> batchAudit(
            @RequestBody AdminResourceBatchAuditDTO body,
            @RequestHeader(value = "X-User-Id", required = false) Long auditorId,
            @RequestHeader(value = "X-User-Name", required = false) String auditorName) {
        if (body == null || body.getIds() == null || body.getIds().isEmpty()) {
            throw new BusinessException(400, "请选择资源");
        }
        int success = 0;
        List<String> reasons = new ArrayList<>();
        Integer status = "approve".equalsIgnoreCase(body.getAction()) ? 1 : 2;
        for (Long globalId : body.getIds()) {
            try {
                ResourceMain row = requireSourceResource(globalId);
                adapterRegistry.require(row.getSourceType())
                        .audit(row.getSourceId(), status, body.getReason(), auditorId, auditorName);
                success++;
            } catch (Exception ex) {
                reasons.add("globalId=" + globalId + "，原因：" + ex.getMessage());
            }
        }
        return Result.success(AdminResourceBatchResultVO.of(
                success,
                body.getIds().size() - success,
                reasons,
                "批量审核完成"));
    }

    private ResourceMain requireSourceResource(Long globalId) {
        ResourceMain row = resourceMainMapper.selectById(globalId);
        if (row == null || row.getIsDeleted() != null && row.getIsDeleted() == 1) {
            throw new BusinessException(404, "资源不存在");
        }
        if (row.getSourceId() == null) {
            throw new BusinessException(400, "资源来源数据异常");
        }
        return row;
    }

    private void applyBatchAction(ResourceMain row, String action, Long userId) {
        if (row == null) {
            throw new BusinessException(404, "资源不存在");
        }
        if (!org.springframework.util.StringUtils.hasText(action)) {
            throw new BusinessException(400, "批量动作不能为空");
        }
        ResourceSourceAdapter adapter = adapterRegistry.require(row.getSourceType());
        String normalized = action.trim().toLowerCase();
        switch (normalized) {
            case "publish" -> adapter.publish(row.getSourceId(), userId);
            case "offline" -> adapter.offline(row.getSourceId(), userId);
            case "recommend" -> adapter.setRecommend(row.getSourceId(), true, userId);
            case "unrecommend" -> adapter.setRecommend(row.getSourceId(), false, userId);
            case "top" -> {
                if (!adapter.supportsTop()) {
                    throw new BusinessException(400, "当前资源类型不支持置顶");
                }
                adapter.setTop(row.getSourceId(), true, null, userId);
            }
            case "untop" -> {
                if (!adapter.supportsTop()) {
                    throw new BusinessException(400, "当前资源类型不支持取消置顶");
                }
                adapter.setTop(row.getSourceId(), false, null, userId);
            }
            case "recycle" -> adapter.recycle(row.getSourceId(), userId);
            case "restore" -> adapter.restore(row.getSourceId(), userId);
            default -> throw new BusinessException(400, "不支持的批量动作: " + action);
        }
    }
}
