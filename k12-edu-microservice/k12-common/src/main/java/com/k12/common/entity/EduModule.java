package com.k12.common.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

/**
 * 资源栏目实体
 * 对应数据库表：xinketang.edu_module
 */
@Data
@TableName("xinketang.edu_module")
public class EduModule {

    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 栏目名称
     */
    private String name;

    /**
     * 图标（emoji）
     */
    private String icon;

    private String code;

    @com.baomidou.mybatisplus.annotation.TableField("module_category")
    private String moduleCategory;

    private String description;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 状态：0=禁用 1=启用
     */
    private Integer status;
}
