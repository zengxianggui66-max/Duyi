package com.k12.common.dto;

import com.k12.common.constant.ResourceStatusConstants;
import com.k12.common.entity.PrimaryChineseResource;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AdminResourceListVO {

    private Long id;
    private String title;
    private String stage;
    private String subject;
    private String module;
    private String type;
    private String gradeName;
    private String edition;
    private String fileExt;
    private Integer status;
    private Integer auditStatus;
    private Integer publishStatus;
    private String auditStatusLabel;
    private String shelfStatusLabel;
    private Long uploaderId;
    private LocalDateTime uploadTime;
    private Integer isRecommend;
    private Integer isTop;
    private Integer isFree;
    private Integer downloadCount;
    private Integer viewCount;

    public static AdminResourceListVO fromEntity(PrimaryChineseResource e) {
        AdminResourceListVO vo = new AdminResourceListVO();
        vo.setId(e.getId());
        vo.setTitle(e.getTitle());
        vo.setStage(e.getStage());
        vo.setSubject(e.getSubject());
        vo.setModule(e.getModule());
        vo.setType(e.getType());
        vo.setGradeName(e.getGradeName());
        vo.setEdition(e.getEdition());
        vo.setFileExt(e.getFileExt());
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
        vo.setIsRecommend(e.getIsRecommend());
        vo.setIsTop(e.getIsTop());
        vo.setIsFree(e.getIsFree());
        vo.setDownloadCount(e.getDownloadCount());
        vo.setViewCount(e.getViewCount());
        return vo;
    }
}

