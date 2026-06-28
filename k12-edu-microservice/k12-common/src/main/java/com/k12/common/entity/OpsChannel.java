package com.k12.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("xinketang.ops_channel")
public class OpsChannel {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String code;

    private String name;

    private String icon;

    private String description;

    @TableField("bg_gradient")
    private String bgGradient;

    @TableField("route_path")
    private String routePath;

    @TableField("stats_json")
    private String statsJson;

    @TableField("ui_json")
    private String uiJson;

    private Integer sort;

    private Integer status;

    private String remark;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;
}
