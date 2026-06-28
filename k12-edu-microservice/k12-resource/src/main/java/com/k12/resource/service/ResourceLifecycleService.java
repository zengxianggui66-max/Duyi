package com.k12.resource.service;

import com.k12.common.BusinessException;
import com.k12.common.constant.ResourceStatusConstants;
import com.k12.common.entity.PrimaryChineseResource;
import org.springframework.stereotype.Service;

import java.util.Arrays;

/**
 * 主资源状态流转校验（Phase 3 骨架，后续 Phase 接搜索索引联动）
 */
@Service
public class ResourceLifecycleService {

    public void assertExists(PrimaryChineseResource resource) {
        if (resource == null) {
            throw new BusinessException("资源不存在");
        }
    }

    public void assertStatus(PrimaryChineseResource resource, int... allowed) {
        assertExists(resource);
        int status = resource.getStatus() != null ? resource.getStatus() : 0;
        if (Arrays.stream(allowed).noneMatch(a -> a == status)) {
            throw new BusinessException(400, String.format(
                    "当前状态「%s」不允许此操作",
                    ResourceStatusConstants.auditStatusLabel(null, status) + " / "
                            + ResourceStatusConstants.publishStatusLabel(null, status)));
        }
    }

    public boolean canPublish(PrimaryChineseResource resource) {
        if (resource == null) {
            return false;
        }
        int audit = ResourceStatusConstants.resolveAuditStatus(
                resource.getAuditStatus(), resource.getStatus());
        int publish = ResourceStatusConstants.resolvePublishStatus(
                resource.getPublishStatus(), resource.getStatus());
        if (audit == ResourceStatusConstants.AUDIT_APPROVED
                && publish != ResourceStatusConstants.PUBLISH_PUBLISHED) {
            return true;
        }
        int status = resource.getStatus() != null ? resource.getStatus() : ResourceStatusConstants.PENDING;
        return status == ResourceStatusConstants.PENDING || status == ResourceStatusConstants.OFFLINE;
    }

    /** @deprecated use {@link #canPublish(PrimaryChineseResource)} */
    @Deprecated
    public boolean canPublish(int status) {
        return status == ResourceStatusConstants.PENDING || status == ResourceStatusConstants.OFFLINE;
    }

    public boolean canOffline(int status) {
        return status == ResourceStatusConstants.PUBLISHED;
    }

    public boolean canRecommend(int status) {
        return status == ResourceStatusConstants.PUBLISHED;
    }

    public boolean canMoveToRecycle(int status) {
        return status == ResourceStatusConstants.DRAFT
                || status == ResourceStatusConstants.PENDING
                || status == ResourceStatusConstants.REJECTED
                || status == ResourceStatusConstants.OFFLINE;
    }

    public void validatePublish(PrimaryChineseResource resource) {
        assertExists(resource);
        if (canPublish(resource)) {
            return;
        }
        int audit = ResourceStatusConstants.resolveAuditStatus(
                resource.getAuditStatus(), resource.getStatus());
        int publish = ResourceStatusConstants.resolvePublishStatus(
                resource.getPublishStatus(), resource.getStatus());
        throw new BusinessException(400, String.format(
                "当前状态「%s / %s」不允许发布",
                ResourceStatusConstants.auditStatusCodeLabel(audit),
                ResourceStatusConstants.publishStatusLabel(publish)));
    }

    public void validateOffline(PrimaryChineseResource resource) {
        assertStatus(resource, ResourceStatusConstants.PUBLISHED);
    }

    public void validateRecommend(PrimaryChineseResource resource) {
        assertStatus(resource, ResourceStatusConstants.PUBLISHED);
    }

    public void validateTop(PrimaryChineseResource resource) {
        assertStatus(resource, ResourceStatusConstants.PUBLISHED);
    }

    public void validateMoveToRecycle(PrimaryChineseResource resource) {
        assertStatus(resource,
                ResourceStatusConstants.DRAFT,
                ResourceStatusConstants.PENDING,
                ResourceStatusConstants.REJECTED,
                ResourceStatusConstants.OFFLINE);
    }

    public void validateAudit(PrimaryChineseResource resource) {
        assertExists(resource);
        if (resource.getAuditStatus() != null) {
            if (resource.getAuditStatus() != ResourceStatusConstants.AUDIT_PENDING) {
                throw new BusinessException(400, String.format(
                        "当前状态「%s」不允许审核",
                        ResourceStatusConstants.auditStatusLabel(
                                resource.getAuditStatus(), resource.getStatus())));
            }
            return;
        }
        assertStatus(resource, ResourceStatusConstants.PENDING);
    }

    public boolean canRestore(int status) {
        return status == ResourceStatusConstants.DELETED;
    }

    public void validateRestore(PrimaryChineseResource resource) {
        assertStatus(resource, ResourceStatusConstants.DELETED);
    }
}
