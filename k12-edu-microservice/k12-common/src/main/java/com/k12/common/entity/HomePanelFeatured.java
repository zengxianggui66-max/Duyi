package com.k12.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("xinketang.home_panel_featured")
public class HomePanelFeatured {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("panel_code")
    private String panelCode;

    @TableField("tab_key")
    private String tabKey;

    @TableField("filter_key")
    private String filterKey;

    @TableField("stage_key")
    private String stageKey;

    @TableField("subject_name")
    private String subjectName;

    @TableField("grade_name")
    private String gradeName;

    @TableField("resource_id")
    private Long resourceId;

    @TableField("resource_source")
    private String resourceSource;

    private Integer sort;

    @TableField("expire_time")
    private LocalDateTime expireTime;

    private Integer status;
}
