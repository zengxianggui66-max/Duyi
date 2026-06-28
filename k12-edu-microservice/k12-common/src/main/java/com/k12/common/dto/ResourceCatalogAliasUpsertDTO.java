package com.k12.common.dto;

import lombok.Data;

@Data
public class ResourceCatalogAliasUpsertDTO {

    private String sourceType;

    private String legacyTitle;

    private String aliasTitle;

    private Long catalogNodeId;

    /** 0-100 */
    private Integer confidence;

    /** 0=禁用 1=启用 */
    private Integer status;

    private String notes;
}
