package com.k12.common.dto;

import lombok.Data;

@Data
public class BrowseTypeStatVO {

    /** 展示类型（与前端 Tab 名一致，优先使用） */
    private String displayType;

    /** 兼容旧字段，与 displayType 相同 */
    private String type;

    private Long count;
}
