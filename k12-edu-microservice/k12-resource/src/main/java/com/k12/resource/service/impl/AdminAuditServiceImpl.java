package com.k12.resource.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.k12.common.BusinessException;
import com.k12.common.dto.AuditRejectReasonWriteDTO;
import com.k12.common.dto.ResourceAuditLogQueryDTO;
import com.k12.common.dto.ResourceAuditLogVO;
import com.k12.common.entity.AuditRejectReason;
import com.k12.common.entity.ResourceAuditLog;
import com.k12.common.service.AdminPermissionService;
import com.k12.resource.mapper.AuditRejectReasonMapper;
import com.k12.resource.mapper.ResourceAuditLogMapper;
import com.k12.resource.service.AdminAuditService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@SuppressWarnings("null")
public class AdminAuditServiceImpl implements AdminAuditService {

    private final ResourceAuditLogMapper resourceAuditLogMapper;
    private final AuditRejectReasonMapper auditRejectReasonMapper;
    private final AdminPermissionService adminPermissionService;
    public AdminAuditServiceImpl(ResourceAuditLogMapper resourceAuditLogMapper, AuditRejectReasonMapper auditRejectReasonMapper, AdminPermissionService adminPermissionService) {
        this.resourceAuditLogMapper = resourceAuditLogMapper;
        this.auditRejectReasonMapper = auditRejectReasonMapper;
        this.adminPermissionService = adminPermissionService;
    }


    @Override
    public Page<ResourceAuditLogVO> listLogs(ResourceAuditLogQueryDTO query, Long adminUserId) {
        requirePermission(adminUserId, "admin:audit:records");
        int current = query.getCurrent() != null && query.getCurrent() > 0 ? query.getCurrent() : 1;
        int size = query.getSize() != null && query.getSize() > 0 ? Math.min(query.getSize(), 100) : 20;

        Page<Map<String, Object>> page = new Page<>(current, size);
        IPage<Map<String, Object>> result = resourceAuditLogMapper.selectLogPage(
                page,
                query.getResourceId(),
                query.getAuditorId(),
                query.getAction(),
                query.getKeyword(),
                query.getStartTime(),
                query.getEndTime());

        Page<ResourceAuditLogVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        List<ResourceAuditLogVO> records = new ArrayList<>();
        for (Map<String, Object> row : result.getRecords()) {
            ResourceAuditLog log = mapToLog(row);
            String title = row.get("resourceTitle") != null ? String.valueOf(row.get("resourceTitle")) : null;
            String stage = row.get("stage") != null ? String.valueOf(row.get("stage")) : null;
            String subject = row.get("subject") != null ? String.valueOf(row.get("subject")) : null;
            records.add(ResourceAuditLogVO.of(
                    log.getId(),
                    log.getResourceId(),
                    title,
                    stage,
                    subject,
                    log.getAuditorId(),
                    log.getAuditorName(),
                    log.getAction(),
                    log.getBeforeStatus(),
                    log.getAfterStatus(),
                    log.getReason(),
                    log.getCreatedAt()));
        }
        voPage.setRecords(records);
        return voPage;
    }

    @Override
    public List<AuditRejectReason> listRejectReasons(boolean includeDisabled) {
        LambdaQueryWrapper<AuditRejectReason> wrapper = new LambdaQueryWrapper<>();
        if (!includeDisabled) {
            wrapper.eq(AuditRejectReason::getStatus, 1);
        }
        wrapper.orderByAsc(AuditRejectReason::getSort).orderByAsc(AuditRejectReason::getId);
        return auditRejectReasonMapper.selectList(wrapper);
    }

    @Override
    public AuditRejectReason createRejectReason(AuditRejectReasonWriteDTO dto, Long adminUserId) {
        requirePermission(adminUserId, "admin:audit:reasons:edit");
        AuditRejectReason reason = new AuditRejectReason();
        reason.setContent(dto.getContent().trim());
        reason.setCategory(dto.getCategory() != null ? dto.getCategory() : 0);
        reason.setSort(dto.getSort() != null ? dto.getSort() : 0);
        reason.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);
        reason.setCreatedAt(LocalDateTime.now());
        reason.setUpdatedAt(LocalDateTime.now());
        auditRejectReasonMapper.insert(reason);
        return reason;
    }

    @Override
    public AuditRejectReason updateRejectReason(Long id, AuditRejectReasonWriteDTO dto, Long adminUserId) {
        requirePermission(adminUserId, "admin:audit:reasons:edit");
        AuditRejectReason reason = auditRejectReasonMapper.selectById(id);
        if (reason == null) {
            throw new BusinessException(404, "驳回模板不存在");
        }
        reason.setContent(dto.getContent().trim());
        if (dto.getCategory() != null) {
            reason.setCategory(dto.getCategory());
        }
        if (dto.getSort() != null) {
            reason.setSort(dto.getSort());
        }
        if (dto.getStatus() != null) {
            reason.setStatus(dto.getStatus());
        }
        reason.setUpdatedAt(LocalDateTime.now());
        auditRejectReasonMapper.updateById(reason);
        return reason;
    }

    @Override
    public Map<String, List<AuditRejectReason>> listRejectReasonsByCategory(boolean includeDisabled) {
        List<AuditRejectReason> all = listRejectReasons(includeDisabled);
        Map<String, List<AuditRejectReason>> grouped = new LinkedHashMap<>();
        for (AuditRejectReason r : all) {
            String key = categoryLabel(r.getCategory());
            grouped.computeIfAbsent(key, k -> new ArrayList<>()).add(r);
        }
        return grouped;
    }

    @Override
    public void setRejectReasonStatus(Long id, Integer status, Long adminUserId) {
        requirePermission(adminUserId, "admin:audit:reasons:edit");
        if (status == null || (status != 0 && status != 1)) {
            throw new BusinessException(400, "状态值无效");
        }
        AuditRejectReason reason = auditRejectReasonMapper.selectById(id);
        if (reason == null) {
            throw new BusinessException(404, "驳回模板不存在");
        }
        reason.setStatus(status);
        reason.setUpdatedAt(LocalDateTime.now());
        auditRejectReasonMapper.updateById(reason);
    }

    @Override
    public void deleteRejectReason(Long id, Long adminUserId) {
        requirePermission(adminUserId, "admin:audit:reasons:edit");
        AuditRejectReason reason = auditRejectReasonMapper.selectById(id);
        if (reason == null) {
            throw new BusinessException(404, "驳回模板不存在");
        }
        auditRejectReasonMapper.deleteById(id);
    }

    @Override
    public void appendAuditLog(Long resourceId, Long auditorId, String auditorName,
                               String action, int beforeStatus, int afterStatus, String reason) {
        ResourceAuditLog log = new ResourceAuditLog();
        log.setResourceId(resourceId);
        log.setAuditorId(auditorId);
        log.setAuditorName(auditorName);
        log.setAction(action);
        log.setBeforeStatus(beforeStatus);
        log.setAfterStatus(afterStatus);
        log.setReason(reason);
        log.setCreatedAt(LocalDateTime.now());
        resourceAuditLogMapper.insert(log);
    }

    private void requirePermission(Long userId, String permission) {
        if (userId == null) {
            throw new BusinessException(401, "未登录或Token已过期");
        }
        if (!adminPermissionService.hasPermission(userId, permission)) {
            throw new BusinessException(403, "无权限访问");
        }
    }

    private String categoryLabel(Integer category) {
        if (category == null) return "通用";
        return switch (category) {
            case 1 -> "内容质量";
            case 2 -> "格式规范";
            case 3 -> "版权合规";
            case 4 -> "分类挂载";
            case 5 -> "其他";
            default -> "通用";
        };
    }

    private ResourceAuditLog mapToLog(Map<String, Object> row) {
        ResourceAuditLog log = new ResourceAuditLog();
        log.setId(toLong(row.get("id")));
        log.setResourceId(toLong(row.get("resourceId")));
        log.setAuditorId(toLong(row.get("auditorId")));
        log.setAuditorName(row.get("auditorName") != null ? String.valueOf(row.get("auditorName")) : null);
        log.setAction(row.get("action") != null ? String.valueOf(row.get("action")) : null);
        log.setBeforeStatus(toInt(row.get("beforeStatus")));
        log.setAfterStatus(toInt(row.get("afterStatus")));
        log.setReason(row.get("reason") != null ? String.valueOf(row.get("reason")) : null);
        Object createdAt = row.get("createdAt");
        if (createdAt instanceof LocalDateTime ldt) {
            log.setCreatedAt(ldt);
        } else if (createdAt instanceof java.sql.Timestamp ts) {
            log.setCreatedAt(ts.toLocalDateTime());
        } else if (createdAt != null) {
            String raw = String.valueOf(createdAt);
            if (raw.length() >= 19) {
                log.setCreatedAt(LocalDateTime.parse(raw.substring(0, 19).replace(' ', 'T')));
            }
        }
        return log;
    }

    private Long toLong(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number n) {
            return n.longValue();
        }
        return Long.parseLong(String.valueOf(value));
    }

    private Integer toInt(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number n) {
            return n.intValue();
        }
        return Integer.parseInt(String.valueOf(value));
    }
}
