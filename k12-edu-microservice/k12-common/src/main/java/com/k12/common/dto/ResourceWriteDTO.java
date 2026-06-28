package com.k12.common.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * M4 主表写入请求（与宽表/学科页字段对齐）
 */
@Data
public class ResourceWriteDTO {

    private Long id;
    private String title;
    private String description;
    private String remark;

    private String stage;
    private String subject;
    private String module;
    private String type;
    private String gradeName;
    private String edition;
    private String unitName;
    private String lessonName;

    private String brandCode;
    private Long catalogNodeId;
    private String catalogPath;
    private String subType;

    private Integer status;
    private Long uploaderId;
    private Integer allowPreview = 1;
    private Integer isFree = 0;
    private Integer sort = 0;
    private String lessonPlanJson;

    /** 主文件（兼容单文件上传） */
    private String originalFilename;
    private String fileExt;
    private String ossBucket;
    private String ossObjectKey;
    private String ossUrl;
    private Integer fileSizeKb;

    /** 多文件（可选，含 answer/seewo 等） */
    private List<ResourceFileDTO> files = new ArrayList<>();
}
