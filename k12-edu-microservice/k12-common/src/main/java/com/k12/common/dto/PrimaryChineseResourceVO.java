package com.k12.common.dto;

import com.k12.common.entity.PrimaryChineseResource;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 小学语文资源 API 视图（对外暴露 description，与前端 PrimaryChineseItem 对齐）
 */
@Data
public class PrimaryChineseResourceVO {

    private Long id;
    private String stage;
    private String subject;
    private String module;
    private String type;
    private String gradeName;
    private String edition;
    private String brandCode;
    private Long catalogNodeId;
    private String catalogPath;
    private String subType;
    private String unitName;
    private String lessonName;
    private String title;
    private String originalFilename;
    private String fileExt;
    private String ossBucket;
    private String ossObjectKey;
    private String ossUrl;
    private Integer fileSizeKb;
    private Integer downloadCount;
    private Integer viewCount;
    private Integer status;
    private Integer auditStatus;
    private Integer publishStatus;
    private Long uploaderId;
    private LocalDateTime uploadTime;
    private LocalDateTime updateTime;
    private Integer sort;
    /** 简介（API 字段，库表存 remark） */
    private String description;
    private String remark;
    private Integer allowPreview;
    private Integer previewStatus;
    private Integer fileSafetyStatus;
    private Integer isFree;
    private String lessonPlanJson;

    public static PrimaryChineseResourceVO fromEntity(PrimaryChineseResource entity) {
        if (entity == null) {
            return null;
        }
        PrimaryChineseResourceVO vo = new PrimaryChineseResourceVO();
        vo.setId(entity.getId());
        vo.setStage(entity.getStage());
        vo.setSubject(entity.getSubject());
        vo.setModule(entity.getModule());
        vo.setType(entity.getType());
        vo.setGradeName(entity.getGradeName());
        vo.setEdition(entity.getEdition());
        vo.setBrandCode(entity.getBrandCode());
        vo.setCatalogNodeId(entity.getCatalogNodeId());
        vo.setCatalogPath(entity.getCatalogPath());
        vo.setSubType(entity.getSubType());
        vo.setUnitName(entity.getUnitName());
        vo.setLessonName(entity.getLessonName());
        vo.setTitle(entity.getTitle());
        vo.setOriginalFilename(entity.getOriginalFilename());
        vo.setFileExt(entity.getFileExt());
        vo.setOssBucket(entity.getOssBucket());
        vo.setOssObjectKey(entity.getOssObjectKey());
        vo.setOssUrl(entity.getOssUrl());
        vo.setFileSizeKb(entity.getFileSizeKb());
        vo.setDownloadCount(entity.getDownloadCount());
        vo.setViewCount(entity.getViewCount());
        vo.setStatus(entity.getStatus());
        vo.setAuditStatus(entity.getAuditStatus());
        vo.setPublishStatus(entity.getPublishStatus());
        vo.setUploaderId(entity.getUploaderId());
        vo.setUploadTime(entity.getUploadTime());
        vo.setUpdateTime(entity.getUpdateTime());
        vo.setSort(entity.getSort());
        vo.setRemark(entity.getRemark());
        vo.setDescription(entity.getRemark());
        vo.setAllowPreview(entity.getAllowPreview());
        vo.setPreviewStatus(entity.getPreviewStatus());
        vo.setFileSafetyStatus(entity.getFileSafetyStatus());
        vo.setIsFree(entity.getIsFree());
        vo.setLessonPlanJson(entity.getLessonPlanJson());
        return vo;
    }

    public PrimaryChineseResource toEntity() {
        PrimaryChineseResource entity = new PrimaryChineseResource();
        entity.setId(id);
        entity.setStage(stage);
        entity.setSubject(subject);
        entity.setModule(module);
        entity.setType(type);
        entity.setGradeName(gradeName);
        entity.setEdition(edition);
        entity.setBrandCode(brandCode);
        entity.setCatalogNodeId(catalogNodeId);
        entity.setCatalogPath(catalogPath);
        entity.setSubType(subType);
        entity.setUnitName(unitName);
        entity.setLessonName(lessonName);
        entity.setTitle(title);
        entity.setOriginalFilename(originalFilename);
        entity.setFileExt(fileExt);
        entity.setOssBucket(ossBucket);
        entity.setOssObjectKey(ossObjectKey);
        entity.setOssUrl(ossUrl);
        entity.setFileSizeKb(fileSizeKb);
        entity.setDownloadCount(downloadCount);
        entity.setViewCount(viewCount);
        entity.setStatus(status);
        entity.setAuditStatus(auditStatus);
        entity.setPublishStatus(publishStatus);
        entity.setUploaderId(uploaderId);
        entity.setUploadTime(uploadTime);
        entity.setUpdateTime(updateTime);
        entity.setSort(sort);
        entity.setAllowPreview(allowPreview);
        entity.setPreviewStatus(previewStatus);
        entity.setFileSafetyStatus(fileSafetyStatus);
        entity.setIsFree(isFree);
        entity.setLessonPlanJson(lessonPlanJson);
        if (description != null && !description.isBlank()) {
            entity.setRemark(description);
        } else {
            entity.setRemark(remark);
        }
        return entity;
    }
}
