package com.k12.common.dto;

import com.k12.common.constant.ResourceStatusConstants;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ResourceAuditLogVO {
    private Long id;
    private Long resourceId;
    private String resourceTitle;
    private String stage;
    private String subject;
    private Long auditorId;
    private String auditorName;
    private String action;
    private String actionLabel;
    private Integer beforeStatus;
    private String beforeStatusLabel;
    private Integer afterStatus;
    private String afterStatusLabel;
    private String reason;
    private LocalDateTime createdAt;

    public static ResourceAuditLogVO of(
            Long id,
            Long resourceId,
            String resourceTitle,
            String stage,
            String subject,
            Long auditorId,
            String auditorName,
            String action,
            Integer beforeStatus,
            Integer afterStatus,
            String reason,
            LocalDateTime createdAt) {
        ResourceAuditLogVO vo = new ResourceAuditLogVO();
        vo.setId(id);
        vo.setResourceId(resourceId);
        vo.setResourceTitle(resourceTitle);
        vo.setStage(stage);
        vo.setSubject(subject);
        vo.setAuditorId(auditorId);
        vo.setAuditorName(auditorName);
        vo.setAction(action);
        vo.setActionLabel(resolveActionLabel(action));
        vo.setBeforeStatus(beforeStatus);
        vo.setBeforeStatusLabel(ResourceStatusConstants.auditStatusLabel(null, beforeStatus != null ? beforeStatus : 0));
        vo.setAfterStatus(afterStatus);
        vo.setAfterStatusLabel(ResourceStatusConstants.auditStatusLabel(null, afterStatus != null ? afterStatus : 0));
        vo.setReason(reason);
        vo.setCreatedAt(createdAt);
        return vo;
    }

    private static String resolveActionLabel(String action) {
        if (action == null) {
            return "";
        }
        return switch (action) {
            case "approve", "approve_publish" -> "审核通过并上架";
            case "approve_audit" -> "审核通过";
            case "reject" -> "驳回";
            default -> action;
        };
    }
}
