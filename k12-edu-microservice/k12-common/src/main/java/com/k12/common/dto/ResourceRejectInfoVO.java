package com.k12.common.dto;

import com.k12.common.entity.ResourceAuditLog;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * C 端：上传者查看资源最新驳回信息
 */
@Data
public class ResourceRejectInfoVO {
    private Long resourceId;
    private String reason;
    private String auditorName;
    private LocalDateTime rejectedAt;

    public static ResourceRejectInfoVO fromLog(Long resourceId, ResourceAuditLog log) {
        ResourceRejectInfoVO vo = new ResourceRejectInfoVO();
        vo.setResourceId(resourceId);
        if (log != null) {
            vo.setReason(log.getReason());
            vo.setAuditorName(log.getAuditorName());
            vo.setRejectedAt(log.getCreatedAt());
        }
        return vo;
    }
}
