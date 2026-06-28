package com.k12.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("xinketang.home_quick_entry")
public class HomeQuickEntry {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("entry_key")
    private String entryKey;

    private String title;

    private String description;

    private String icon;

    @TableField("accent_color")
    private String accentColor;

    @TableField("nav_target")
    private String navTarget;

    @TableField("stage_keys")
    private String stageKeys;

    private Integer sort;

    private Integer status;

    private String remark;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;
}
