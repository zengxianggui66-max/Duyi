package com.k12.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Phase 3C: 目录别名映射表
 */
@Data
@TableName("resource_catalog_alias")
public class ResourceCatalogAlias {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String sourceType;

    private String legacyTitle;

    private String aliasTitle;

    private Long catalogNodeId;

    /** 0-100 */
    private Integer confidence;

    /** 0=禁用 1=启用 */
    private Integer status;

    private String notes;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
