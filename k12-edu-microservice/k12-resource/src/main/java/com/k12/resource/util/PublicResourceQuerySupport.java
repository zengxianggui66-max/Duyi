package com.k12.resource.util;

import com.k12.common.constant.ResourcePreviewSafetyConstants;
import com.k12.common.constant.ResourceStatusConstants;
import com.k12.common.dto.PrimaryChineseResourceQueryDTO;
import com.k12.common.entity.PrimaryChineseResource;

/**
 * 公开资源查询：无上传者筛选时默认仅返回「审核通过 + 已上架」资源。
 */
public final class PublicResourceQuerySupport {

    private PublicResourceQuerySupport() {
    }

    /**
     * 当未指定 uploaderId 且未指定 status/auditStatus/publishStatus 时，默认只查公开资源。
     * 「我的资源」传 uploaderId 时不改动，可查询草稿/待审等全状态。
     */
    public static void applyPublicStatusDefault(PrimaryChineseResourceQueryDTO dto) {
        if (dto == null) {
            return;
        }
        if (dto.getUploaderId() != null) {
            return;
        }
        if (dto.getStatus() == null) {
            dto.setStatus(ResourceStatusConstants.PUBLISHED);
        }
        if (dto.getAuditStatus() == null) {
            dto.setAuditStatus(ResourceStatusConstants.AUDIT_APPROVED);
        }
        if (dto.getPublishStatus() == null) {
            dto.setPublishStatus(ResourceStatusConstants.PUBLISH_PUBLISHED);
        }
    }

    /** 是否对 C 端公开展示（已上架且未删除） */
    public static boolean isPubliclyPublished(PrimaryChineseResource resource) {
        if (resource == null) {
            return false;
        }
        if (resource.getIsDeleted() != null && resource.getIsDeleted() == 1) {
            return false;
        }
        Integer publishStatus = resource.getPublishStatus();
        if (publishStatus != null) {
            return publishStatus == ResourceStatusConstants.PUBLISH_PUBLISHED;
        }
        return resource.getStatus() != null
                && resource.getStatus() == ResourceStatusConstants.PUBLISHED;
    }

    /** 是否处于待审核队列 */
    public static boolean isPendingAudit(PrimaryChineseResource resource) {
        if (resource == null) {
            return false;
        }
        Integer auditStatus = resource.getAuditStatus();
        if (auditStatus != null) {
            return auditStatus == ResourceStatusConstants.AUDIT_PENDING;
        }
        return resource.getStatus() != null
                && resource.getStatus() == ResourceStatusConstants.PENDING;
    }

    /** 公开文件流是否允许（已上架且非风险文件） */
    public static boolean isPublicFileAccessible(PrimaryChineseResource resource) {
        if (!isPubliclyPublished(resource)) {
            return false;
        }
        Integer safety = resource.getFileSafetyStatus();
        return safety == null || safety != ResourcePreviewSafetyConstants.SAFETY_RISK;
    }
}
