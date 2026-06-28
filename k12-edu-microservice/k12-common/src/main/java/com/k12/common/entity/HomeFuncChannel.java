package com.k12.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 首页顶栏功能入口配置
 */
@Data
@TableName("xinketang.home_func_channel")
public class HomeFuncChannel {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("func_key")
    private String funcKey;

    private String name;

    @TableField("exam_type")
    private String examType;

    @TableField("default_topic")
    private String defaultTopic;

    @TableField("stage_key")
    private String stageKey;

    @TableField("paper_tab")
    private String paperTab;

    @TableField("paper_default_grade")
    private String paperDefaultGrade;

    @TableField("scroll_target")
    private String scrollTarget;

    @TableField("exam_tab_label")
    private String examTabLabel;

    @TableField("browse_module")
    private String browseModule;

    @TableField("browse_stage_key")
    private String browseStageKey;

    @TableField("browse_default_volume")
    private String browseDefaultVolume;

    private Integer sort;

    private Integer status;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;
}
