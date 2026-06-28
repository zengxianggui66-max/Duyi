package com.k12.resource.adapter;

import com.k12.common.BusinessException;
import com.k12.common.dto.AdminResourceUpdateDTO;

/**
 * 专题 / 传统文化 / 竞赛等 legacy status 资源共用管理端行为。
 */
public abstract class AbstractDedicatedZoneSourceAdapter implements ResourceSourceAdapter {

    protected abstract void doIncrementView(Long sourceId);

    protected abstract void doIncrementDownload(Long sourceId);

    protected abstract void doApplyGenericUpdate(Long sourceId, AdminResourceUpdateDTO dto);

    protected abstract void doUpdateStatus(Long sourceId, int status);

    protected abstract void doUpdateElite(Long sourceId, int isElite);

    @Override
    public void incrementView(Long sourceId) {
        doIncrementView(sourceId);
    }

    @Override
    public void incrementDownload(Long sourceId) {
        doIncrementDownload(sourceId);
    }

    @Override
    public boolean supportsUpdate() {
        return true;
    }

    @Override
    public void update(Long sourceId, AdminResourceUpdateDTO dto, Long userId) {
        doApplyGenericUpdate(sourceId, dto);
    }

    @Override
    public void audit(Long sourceId, Integer status, String reason, Long auditorId, String auditorName) {
        if (status == null) {
            throw new BusinessException(400, "审核状态不能为空");
        }
        updateLegacyStatus(sourceId, status == 1 ? 1 : 2);
    }

    @Override
    public void publish(Long sourceId, Long userId) {
        updateLegacyStatus(sourceId, 1);
    }

    @Override
    public void offline(Long sourceId, Long userId) {
        updateLegacyStatus(sourceId, 3);
    }

    @Override
    public void recycle(Long sourceId, Long userId) {
        updateLegacyStatus(sourceId, 4);
    }

    @Override
    public void restore(Long sourceId, Long userId) {
        updateLegacyStatus(sourceId, 1);
    }

    @Override
    public void setRecommend(Long sourceId, boolean enabled, Long userId) {
        setElite(sourceId, enabled ? 1 : 0);
    }

    @Override
    public void updateLegacyStatus(Long sourceId, int status) {
        doUpdateStatus(sourceId, status);
    }

    @Override
    public void setElite(Long sourceId, int isElite) {
        doUpdateElite(sourceId, isElite);
    }
}
