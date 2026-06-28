package com.k12.resource.service;

import com.k12.common.constant.ResourcePreviewSafetyConstants;
import com.k12.common.entity.PrimaryChineseResource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 同步 preview_status / file_safety_status 到资源记录（保存草稿与提交审核时调用）
 */
@Component
public class ResourcePreviewSafetySync {

    private final AuditContentAnalyzer auditContentAnalyzer;
    public ResourcePreviewSafetySync(AuditContentAnalyzer auditContentAnalyzer) {
        this.auditContentAnalyzer = auditContentAnalyzer;
    }


    public void sync(PrimaryChineseResource resource) {
        if (resource == null) {
            return;
        }
        resource.setPreviewStatus(resolvePreviewStatus(resource));
        resource.setFileSafetyStatus(resolveFileSafetyStatus(resource));
    }

    private int resolvePreviewStatus(PrimaryChineseResource resource) {
        if (!StringUtils.hasText(resource.getOssUrl())) {
            return ResourcePreviewSafetyConstants.PREVIEW_PENDING;
        }
        if (resource.getPreviewStatus() != null
                && resource.getPreviewStatus() == ResourcePreviewSafetyConstants.PREVIEW_FAILED) {
            return ResourcePreviewSafetyConstants.PREVIEW_FAILED;
        }
        Integer allow = resource.getAllowPreview();
        if (allow != null) {
            return allow == 1
                    ? ResourcePreviewSafetyConstants.PREVIEW_AVAILABLE
                    : ResourcePreviewSafetyConstants.PREVIEW_NOT_AVAILABLE;
        }
        return ResourcePreviewSafetyConstants.PREVIEW_PENDING;
    }

    private int resolveFileSafetyStatus(PrimaryChineseResource resource) {
        AuditContentAnalyzer.FileSafety safety = auditContentAnalyzer.assessFileSafety(resource);
        return ResourcePreviewSafetyConstants.mapFileSafetyString(safety.status());
    }
}
