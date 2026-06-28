package com.k12.resource.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.k12.common.dto.AuditRejectReasonWriteDTO;
import com.k12.common.dto.ResourceAuditLogQueryDTO;
import com.k12.common.dto.ResourceAuditLogVO;
import com.k12.common.entity.AuditRejectReason;

import java.util.List;
import java.util.Map;

public interface AdminAuditService {

    Page<ResourceAuditLogVO> listLogs(ResourceAuditLogQueryDTO query, Long adminUserId);

    List<AuditRejectReason> listRejectReasons(boolean includeDisabled);

    /** 按分类分组返回驳回模板，供前端 el-optgroup 使用 */
    Map<String, List<AuditRejectReason>> listRejectReasonsByCategory(boolean includeDisabled);

    AuditRejectReason createRejectReason(AuditRejectReasonWriteDTO dto, Long adminUserId);

    AuditRejectReason updateRejectReason(Long id, AuditRejectReasonWriteDTO dto, Long adminUserId);

    void setRejectReasonStatus(Long id, Integer status, Long adminUserId);

    void deleteRejectReason(Long id, Long adminUserId);

    void appendAuditLog(Long resourceId, Long auditorId, String auditorName,
                        String action, int beforeStatus, int afterStatus, String reason);
}
