package com.k12.resource.adapter;

import com.k12.common.BusinessException;
import com.k12.common.constant.ResourceTypeConstants;
import com.k12.common.dto.AdminResourceAuditInsightsVO;
import com.k12.common.dto.AdminResourceUpdateDTO;

/**
 * Phase 3I-C：资源来源适配器 — 按 sourceType 隔离读写与状态变更。
 */
public interface ResourceSourceAdapter {

    String sourceType();

    void incrementView(Long sourceId);

    void incrementDownload(Long sourceId);

    default String collectResourceType() {
        return ResourceTypeConstants.RESOURCE;
    }

    default boolean isExternalPreview(Integer allowPreview, String url) {
        return false;
    }

    default boolean supportsUpdate() {
        return false;
    }

    default boolean supportsPlacement() {
        return false;
    }

    default boolean supportsTop() {
        return false;
    }

    default boolean supportsFullAudit() {
        return false;
    }

    default void update(Long sourceId, AdminResourceUpdateDTO dto, Long userId) {
        throw unsupported("更新");
    }

    default void audit(Long sourceId, Integer status, String reason, Long auditorId, String auditorName) {
        throw unsupported("审核");
    }

    default void publish(Long sourceId, Long userId) {
        updateLegacyStatus(sourceId, 1);
    }

    default void offline(Long sourceId, Long userId) {
        updateLegacyStatus(sourceId, 3);
    }

    default void recycle(Long sourceId, Long userId) {
        updateLegacyStatus(sourceId, 4);
    }

    default void restore(Long sourceId, Long userId) {
        updateLegacyStatus(sourceId, 1);
    }

    default void setRecommend(Long sourceId, boolean enabled, Long userId) {
        setElite(sourceId, enabled ? 1 : 0);
    }

    default void setTop(Long sourceId, boolean enabled, Integer topSort, Long userId) {
        throw unsupported("置顶");
    }

    default AdminResourceAuditInsightsVO getAuditInsights(Long sourceId, Long userId) {
        AdminResourceAuditInsightsVO vo = new AdminResourceAuditInsightsVO();
        vo.setFileSafetyStatus("unknown");
        vo.setFileSafetyMessage("当前资源类型暂未接入审核洞察");
        return vo;
    }

    default void updateLegacyStatus(Long sourceId, int status) {
        throw unsupported("状态更新");
    }

    default void setElite(Long sourceId, int isElite) {
        throw unsupported("推荐更新");
    }

    private static BusinessException unsupported(String action) {
        return new BusinessException(400, "当前资源类型暂不支持" + action);
    }
}
