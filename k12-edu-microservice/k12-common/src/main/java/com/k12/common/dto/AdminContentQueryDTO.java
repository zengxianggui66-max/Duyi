package com.k12.common.dto;

import lombok.Data;

/**
 * 内容中心 Admin 列表查询（专题专辑 / 文化研学包 / 竞赛套卷共用）
 */
@Data
public class AdminContentQueryDTO {
    private String keyword;
    private String category;
    private String region;
    private String gradeStage;
    private Integer status;
    /** 是否包含禁用/下线项，默认 true */
    private Boolean includeDisabled = true;
    private Integer current = 1;
    private Integer size = 15;
}
