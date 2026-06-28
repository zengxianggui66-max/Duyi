package com.k12.common.dto;

import lombok.Data;

/**
 * 资讯 Admin 列表查询（含草稿/下线）
 */
@Data
public class AdminArticleQueryDTO {
    private String keyword;
    private String category;
    /** null=全部，0=草稿，1=已发布 */
    private Integer status;
    private Integer current = 1;
    private Integer size = 15;
}
