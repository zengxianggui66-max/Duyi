package com.k12.common.dto;

import lombok.Data;

@Data
public class ResourceCatalogAliasQueryDTO {

    private String sourceType;

    private String keyword;

    /** 0=禁用 1=启用 */
    private Integer status;

    private Integer current = 1;

    private Integer size = 20;
}
