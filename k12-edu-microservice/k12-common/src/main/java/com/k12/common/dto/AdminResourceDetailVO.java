package com.k12.common.dto;

import com.k12.common.constant.ResourceStatusConstants;
import com.k12.common.entity.PrimaryChineseResource;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AdminResourceDetailVO {

    private Long id;
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
    private String fileExt;
    private String originalFilename;
    private String ossUrl;
    private Integer fileSizeKb;
    private Integer status;
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
    private Integer allowPreview;
    private Integer previewStatus;
    private Integer fileSafetyStatus;

    public static AdminResourceDetailVO fromEntity(PrimaryChineseResource e) {
        AdminResourceDetailVO vo = new AdminResourceDetailVO();
        vo.setId(e.getId());
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
        vo.setOriginalFilename(e.getOriginalFilename());
        vo.setOssUrl(e.getOssUrl());
        vo.setFileSizeKb(e.getFileSizeKb());
        int status = e.getStatus() != null ? e.getStatus() : 0;
        int auditStatus = ResourceStatusConstants.resolveAuditStatus(e.getAuditStatus(), status);
        int publishStatus = ResourceStatusConstants.resolvePublishStatus(e.getPublishStatus(), status);
        vo.setStatus(status);
        vo.setAuditStatus(auditStatus);
        vo.setPublishStatus(publishStatus);
        vo.setAuditStatusLabel(ResourceStatusConstants.auditStatusLabel(e.getAuditStatus(), status));
        vo.setShelfStatusLabel(ResourceStatusConstants.publishStatusLabel(e.getPublishStatus(), status));
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
        vo.setAllowPreview(e.getAllowPreview());
        vo.setPreviewStatus(e.getPreviewStatus());
        vo.setFileSafetyStatus(e.getFileSafetyStatus());
        return vo;
    }
}

