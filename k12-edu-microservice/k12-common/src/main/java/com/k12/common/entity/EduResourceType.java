package com.k12.common.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

/**
 * 资源类型实体
 * 对应数据库表：xinketang.edu_resource_type
 */
@Data
@TableName("xinketang.edu_resource_type")
public class EduResourceType {

    @TableId(type = IdType.AUTO)
    private Integer id;

    @com.baomidou.mybatisplus.annotation.TableField("parent_id")
    private Integer parentId;

    private String code;

    /**
     * 类型名称
     */
    private String name;

    /**
     * 图标（emoji）
     */
    private String icon;

    @com.baomidou.mybatisplus.annotation.TableField("group_code")
    private String groupCode;

    @com.baomidou.mybatisplus.annotation.TableField("group_name")
    private String groupName;

    @com.baomidou.mybatisplus.annotation.TableField("allow_preview")
    private Integer allowPreview;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 状态：0=禁用 1=启用
     */
    private Integer status;
}
