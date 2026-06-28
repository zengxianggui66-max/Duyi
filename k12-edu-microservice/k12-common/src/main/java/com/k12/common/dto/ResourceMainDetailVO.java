package com.k12.common.dto;

import com.k12.common.constant.ResourceStatusConstants;
import com.k12.common.entity.VAdminResourceMain;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Phase 7：统一资源详情 VO
 */
@Data
public class ResourceMainDetailVO {

    private Long globalId;
    private String sourceType;
    private Long sourceId;

    private String title;
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

    private String originalFilename;
    private String fileExt;
    private String ossUrl;
    private Integer fileSizeKb;

    private Integer legacyStatus;
    private Integer auditStatus;
    private Integer publishStatus;
    private String auditStatusLabel;
    private String shelfStatusLabel;

    private Long uploaderId;
    private LocalDateTime uploadTime;
    private LocalDateTime updateTime;

    private Integer isRecommend;
    private Integer isTop;
    private Integer topSort;
    private Integer isFree;
    private Integer downloadCount;
    private Integer viewCount;
    private Integer sort;
    private String remark;

    private Long catalogNodeId;
    private String catalogPath;

    public static ResourceMainDetailVO fromEntity(VAdminResourceMain e) {
        ResourceMainDetailVO vo = new ResourceMainDetailVO();
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
        vo.setOriginalFilename(e.getOriginalFilename());
        vo.setFileExt(e.getFileExt());
        vo.setOssUrl(e.getOssUrl());
        vo.setFileSizeKb(e.getFileSizeKb());

        int legacy = e.getLegacyStatus() != null ? e.getLegacyStatus() : 0;
        vo.setLegacyStatus(legacy);
        vo.setAuditStatus(ResourceStatusConstants.resolveAuditStatus(e.getAuditStatus(), legacy));
        vo.setPublishStatus(ResourceStatusConstants.resolvePublishStatus(e.getPublishStatus(), legacy));
        vo.setAuditStatusLabel(ResourceStatusConstants.auditStatusLabel(e.getAuditStatus(), legacy));
        vo.setShelfStatusLabel(ResourceStatusConstants.publishStatusLabel(e.getPublishStatus(), legacy));

        vo.setUploaderId(e.getUploaderId());
        vo.setUploadTime(e.getUploadTime());
        vo.setUpdateTime(e.getUpdateTime());
        vo.setIsRecommend(e.getIsRecommend());
        vo.setIsTop(e.getIsTop());
        vo.setTopSort(e.getTopSort());
        vo.setIsFree(e.getIsFree());
        vo.setDownloadCount(e.getDownloadCount());
        vo.setViewCount(e.getViewCount());
        vo.setSort(e.getSort());
        vo.setRemark(e.getRemark());
        vo.setCatalogNodeId(e.getCatalogNodeId());
        vo.setCatalogPath(e.getCatalogPath());
        return vo;
    }
}
