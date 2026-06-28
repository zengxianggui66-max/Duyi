package com.k12.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("edu_catalog_node")
public class EduCatalogNode {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Integer schemeId;

    private Long parentId;

    private String code;

    private String name;

    private String namePath;

    private Integer depth;

    private String nodeType;

    private Integer sort;

    private String icon;

    private String meta;

    private Integer status;
}
