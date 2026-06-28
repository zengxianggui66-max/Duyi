package com.k12.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 资源品牌/系列
 */
@Data
@TableName("edu_resource_brand")
public class EduResourceBrand {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** qicai | zhuangyuan | platform */
    private String code;

    private String name;

    private String publisher;

    private String logoUrl;

    private Integer sort;

    /** 1=启用 0=停用 */
    private Integer status;
}
