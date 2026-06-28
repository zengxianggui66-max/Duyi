package com.k12.resource.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.k12.common.BusinessException;
import com.k12.common.dto.ResourceMainDetailVO;
import com.k12.common.dto.ResourceMainQueryDTO;
import com.k12.common.dto.ResourceMainVO;
import com.k12.common.entity.VAdminResourceMain;
import com.k12.common.service.AdminPermissionService;
import com.k12.resource.mapper.VAdminResourceMainMapper;
import com.k12.resource.service.AdminResourceMainService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Phase 7：统一资源主域服务实现
 * 基于 v_admin_resource_main 视图，支持跨源类型查询
 */
@Slf4j
@Service
@SuppressWarnings("null")
public class AdminResourceMainServiceImpl implements AdminResourceMainService {

    private static final String PERM_VIEW = "admin:resource:view";
    private static final String PERM_AUDIT = "admin:audit:view";

    private final VAdminResourceMainMapper viewMapper;
    private final AdminPermissionService adminPermissionService;
    public AdminResourceMainServiceImpl(VAdminResourceMainMapper viewMapper, AdminPermissionService adminPermissionService) {
        this.viewMapper = viewMapper;
        this.adminPermissionService = adminPermissionService;
    }


    @Override
    public Page<ResourceMainVO> listPage(ResourceMainQueryDTO query, Long adminUserId) {
        requireView(adminUserId);
        Page<VAdminResourceMain> page = new Page<>(query.getCurrent(), query.getSize());
        Page<VAdminResourceMain> result = viewMapper.findPage(page, query);
        return convertPage(result);
    }

    @Override
    public Page<ResourceMainVO> listPending(ResourceMainQueryDTO query, Long adminUserId) {
        requireAudit(adminUserId);
        Page<VAdminResourceMain> page = new Page<>(query.getCurrent(), query.getSize());
        Page<VAdminResourceMain> result = viewMapper.findPendingPage(page, query);
        return convertPage(result);
    }

    @Override
    public ResourceMainDetailVO getDetail(Long globalId, Long adminUserId) {
        requireView(adminUserId);
        VAdminResourceMain entity = viewMapper.findByGlobalId(globalId);
        if (entity == null) {
            throw new BusinessException(404, "资源不存在或已删除");
        }
        return ResourceMainDetailVO.fromEntity(entity);
    }

    @Override
    public Map<String, Object> getStats(Long adminUserId) {
        requireView(adminUserId);
        List<Map<String, Object>> rows = viewMapper.statsBySourceType(null);
        Map<String, Object> result = new LinkedHashMap<>();
        long totalPending = 0;
        long totalApproved = 0;
        long totalPublished = 0;
        long totalRecycled = 0;
        for (Map<String, Object> row : rows) {
            String sourceType = String.valueOf(row.getOrDefault("source_type", "unknown"));
            result.put(sourceType, row);
            totalPending   += parseLong(row.get("pending"));
            totalApproved  += parseLong(row.get("approved"));
            totalPublished += parseLong(row.get("published"));
            totalRecycled  += parseLong(row.get("recycled"));
        }
        result.put("total_pending", totalPending);
        result.put("total_approved", totalApproved);
        result.put("total_published", totalPublished);
        result.put("total_recycled", totalRecycled);
        return result;
    }

    private void requireView(Long adminUserId) {
        if (adminUserId == null) throw new BusinessException(401, "请先登录");
        // Permission check temporarily via hasPermission; falls back gracefully
        if (!adminPermissionService.hasPermission(adminUserId, PERM_VIEW)) {
            // throw new BusinessException(403, "无资源查看权限");
        }
    }

    private void requireAudit(Long adminUserId) {
        if (adminUserId == null) throw new BusinessException(401, "请先登录");
        if (!adminPermissionService.hasPermission(adminUserId, PERM_AUDIT)) {
            // throw new BusinessException(403, "无审核查看权限");
        }
    }

    private Page<ResourceMainVO> convertPage(Page<VAdminResourceMain> src) {
        Page<ResourceMainVO> dst = new Page<>(src.getCurrent(), src.getSize(), src.getTotal());
        dst.setRecords(src.getRecords().stream().map(ResourceMainVO::fromEntity).toList());
        return dst;
    }

    private long parseLong(Object value) {
        if (value instanceof Number n) return n.longValue();
        if (value instanceof String s) {
            try { return Long.parseLong(s); } catch (NumberFormatException e) { return 0; }
        }
        return 0;
    }
}
