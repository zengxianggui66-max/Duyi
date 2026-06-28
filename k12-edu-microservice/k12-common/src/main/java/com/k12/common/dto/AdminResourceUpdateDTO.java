package com.k12.common.dto;

import lombok.Data;

@Data
public class AdminResourceUpdateDTO {

    private String title;
    private String module;
    private String type;
    private String gradeName;
    private String edition;
    private String unitName;
    private String lessonName;
    private String remark;
    private Integer isFree;
    private Integer sort;

    /** 目录挂载节点（3E 内容中心统一挂载） */
    private Long catalogNodeId;

    /** 目录路径冗余（兼容旧字段） */
    private String catalogPath;
}
