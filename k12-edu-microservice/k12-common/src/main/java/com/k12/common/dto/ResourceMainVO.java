package com.k12.common.dto;

import com.k12.common.constant.ResourceStatusConstants;
import com.k12.common.entity.VAdminResourceMain;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Phase 7：统一资源列表 VO（用于管理端列表 / 审核队列）
 * 不绑定具体源表类型，所有学科共用
 */
@Data
public class ResourceMainVO {

    /** 全局统一 ID（resource_main.id） */
    private Long globalId;

    /** 源类型 */
    private String sourceType;

    /** 源表 ID（用于回查详情） */
    private Long sourceId;

    private String title;

    // ---- 分类维度 ----
    private String stage;
    private String subject;
    private String module;
    private String type;
    private String gradeName;
    private String edition;
    private String brandCode;
    private String subType;
    private String unitName;
    private String lessonName;

    // ---- 文件信息 ----
    private String fileExt;
    private String ossUrl;
    private Integer fileSizeKb;

    // ---- 状态 ----
    private Integer legacyStatus;
    private Integer auditStatus;
    private Integer publishStatus;
    private String auditStatusLabel;
    private String shelfStatusLabel;

    // ---- 上传 ----
    private Long uploaderId;
    private LocalDateTime uploadTime;

    // ---- 运营 ----
    private Integer isRecommend;
    private Integer isTop;
    private Integer isFree;
    private Integer downloadCount;
    private Integer viewCount;

    // ---- 安全 ----
    private Integer allowPreview;
    private Integer previewStatus;
    private Integer fileSafetyStatus;

    // ---- 目录 ----
    private Long catalogNodeId;

    /**
     * 从统一视图实体转换
     */
    public static ResourceMainVO fromEntity(VAdminResourceMain e) {
        ResourceMainVO vo = new ResourceMainVO();
        vo.setGlobalId(e.getGlobalId());
        vo.setSourceType(e.getSourceType());
        vo.setSourceId(e.getSourceId());
        vo.setTitle(e.getTitle());
        vo.setStage(e.getStage());
        vo.setSubject(e.getSubject());
        vo.setModule(e.getModule());
        vo.setType(e.getType());
        vo.setGradeName(e.getGradeName());
        vo.setEdition(e.getEdition());
        vo.setBrandCode(e.getBrandCode());
        vo.setSubType(e.getSubType());
        vo.setUnitName(e.getUnitName());
        vo.setLessonName(e.getLessonName());
        vo.setFileExt(e.getFileExt());
        vo.setOssUrl(e.getOssUrl());
        vo.setFileSizeKb(e.getFileSizeKb());

        int legacy = e.getLegacyStatus() != null ? e.getLegacyStatus() : 0;
        int audit = ResourceStatusConstants.resolveAuditStatus(e.getAuditStatus(), legacy);
        int publish = ResourceStatusConstants.resolvePublishStatus(e.getPublishStatus(), legacy);
        vo.setLegacyStatus(legacy);
        vo.setAuditStatus(audit);
        vo.setPublishStatus(publish);
        vo.setAuditStatusLabel(ResourceStatusConstants.auditStatusLabel(e.getAuditStatus(), legacy));
        vo.setShelfStatusLabel(ResourceStatusConstants.publishStatusLabel(e.getPublishStatus(), legacy));

        vo.setUploaderId(e.getUploaderId());
        vo.setUploadTime(e.getUploadTime());
        vo.setIsRecommend(e.getIsRecommend());
        vo.setIsTop(e.getIsTop());
        vo.setIsFree(e.getIsFree());
        vo.setDownloadCount(e.getDownloadCount());
        vo.setViewCount(e.getViewCount());
        vo.setAllowPreview(e.getAllowPreview());
        vo.setPreviewStatus(e.getPreviewStatus());
        vo.setFileSafetyStatus(e.getFileSafetyStatus());
        vo.setCatalogNodeId(e.getCatalogNodeId());
        return vo;
    }
}
