package com.k12.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("xinketang.home_hot_word")
public class HomeHotWord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String label;

    @TableField("action_type")
    private String actionType;

    @TableField("nav_target")
    private String navTarget;

    private String badge;

    @TableField("stage_keys")
    private String stageKeys;

    @TableField("start_time")
    private LocalDateTime startTime;

    @TableField("end_time")
    private LocalDateTime endTime;

    private Integer sort;

    private Integer status;

    private String remark;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;
}
