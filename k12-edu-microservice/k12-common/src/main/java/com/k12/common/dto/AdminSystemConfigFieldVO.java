package com.k12.common.dto;

import lombok.Data;

@Data
public class AdminSystemConfigFieldVO {

    private String key;
    private String valueType;
    private String description;
    private Boolean requiresRestart;
    /** secret 类型：是否已配置非空值 */
    private Boolean configured;
}
