package com.k12.resource.adapter;

import com.k12.common.constant.ResourceTypeConstants;
import com.k12.common.dto.AdminResourceAuditInsightsVO;
import com.k12.common.dto.AdminResourceUpdateDTO;
import com.k12.resource.service.AdminPrimaryResourceService;
import com.k12.resource.service.PrimaryChineseResourceService;
import org.springframework.stereotype.Component;

@Component
public class PrimaryChineseSourceAdapter implements ResourceSourceAdapter {

    public static final String SOURCE_TYPE = "primary_chinese";

    private final PrimaryChineseResourceService primaryChineseResourceService;
    private final AdminPrimaryResourceService adminPrimaryResourceService;

    public PrimaryChineseSourceAdapter(PrimaryChineseResourceService primaryChineseResourceService,
                                       AdminPrimaryResourceService adminPrimaryResourceService) {
        this.primaryChineseResourceService = primaryChineseResourceService;
        this.adminPrimaryResourceService = adminPrimaryResourceService;
    }

    @Override
    public String sourceType() {
        return SOURCE_TYPE;
    }

    @Override
    public void incrementView(Long sourceId) {
        primaryChineseResourceService.incrementViewCount(sourceId);
    }

    @Override
    public void incrementDownload(Long sourceId) {
        primaryChineseResourceService.incrementDownloadCount(sourceId);
    }

    @Override
    public String collectResourceType() {
        return ResourceTypeConstants.PRIMARY_CHINESE;
    }

    @Override
    public boolean supportsUpdate() {
        return true;
    }

    @Override
    public boolean supportsPlacement() {
        return true;
    }

    @Override
    public boolean supportsTop() {
        return true;
    }

    @Override
    public boolean supportsFullAudit() {
        return true;
    }

    @Override
    public void update(Long sourceId, AdminResourceUpdateDTO dto, Long userId) {
        adminPrimaryResourceService.update(sourceId, dto, userId);
    }

    @Override
    public void audit(Long sourceId, Integer status, String reason, Long auditorId, String auditorName) {
        adminPrimaryResourceService.auditResource(sourceId, status, reason, auditorId, auditorName);
    }

    @Override
    public void publish(Long sourceId, Long userId) {
        adminPrimaryResourceService.publish(sourceId, userId);
    }

    @Override
    public void offline(Long sourceId, Long userId) {
        adminPrimaryResourceService.offline(sourceId, userId);
    }

    @Override
    public void recycle(Long sourceId, Long userId) {
        adminPrimaryResourceService.moveToRecycle(sourceId, userId);
    }

    @Override
    public void restore(Long sourceId, Long userId) {
        adminPrimaryResourceService.restoreFromRecycle(sourceId, userId);
    }

    @Override
    public void setRecommend(Long sourceId, boolean enabled, Long userId) {
        adminPrimaryResourceService.setRecommend(sourceId, enabled, userId);
    }

    @Override
    public void setTop(Long sourceId, boolean enabled, Integer topSort, Long userId) {
        adminPrimaryResourceService.setTop(sourceId, enabled, topSort, userId);
    }

    @Override
    public AdminResourceAuditInsightsVO getAuditInsights(Long sourceId, Long userId) {
        return adminPrimaryResourceService.getAuditInsights(sourceId, userId);
    }
}
