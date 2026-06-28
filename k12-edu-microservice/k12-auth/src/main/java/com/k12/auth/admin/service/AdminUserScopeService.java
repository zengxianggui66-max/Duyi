package com.k12.auth.admin.service;

import com.k12.common.BusinessException;
import com.k12.common.dto.AdminDataScopeVO;
import com.k12.common.service.AdminPermissionService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Phase 6-D：用户列表/详情数据范围（auditor 仅见审核过的上传者）
 */
@Service
public class AdminUserScopeService {

    private final AdminPermissionService adminPermissionService;
    private final JdbcTemplate jdbcTemplate;
    public AdminUserScopeService(AdminPermissionService adminPermissionService, JdbcTemplate jdbcTemplate) {
        this.adminPermissionService = adminPermissionService;
        this.jdbcTemplate = jdbcTemplate;
    }


    public boolean isAuditUploaderScope(Long operatorId) {
        if (operatorId == null) {
            return false;
        }
        AdminDataScopeVO scope = adminPermissionService.resolveDataScope(operatorId);
        return "AUDIT_UPLOADER".equals(scope.getScopeType());
    }

    public List<Long> listVisibleUploaderUserIds(Long auditorId) {
        if (auditorId == null) {
            return List.of();
        }
        return jdbcTemplate.queryForList("""
                SELECT DISTINCT r.uploader_id
                FROM resource_audit_log l
                INNER JOIN oss_primary_chinese_resource r ON r.id = l.resource_id
                WHERE l.auditor_id = ?
                  AND r.uploader_id IS NOT NULL
                  AND r.is_deleted = 0
                """, Long.class, auditorId);
    }

    public void assertCanViewUser(Long operatorId, Long targetUserId) {
        if (!isAuditUploaderScope(operatorId)) {
            return;
        }
        List<Long> allowed = listVisibleUploaderUserIds(operatorId);
        if (targetUserId == null || !allowed.contains(targetUserId)) {
            throw new BusinessException(403, "无权查看该用户（仅可见您审核过的资源上传者）");
        }
    }
}
