package com.k12.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("xinketang.home_banner")
public class HomeBanner {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("slot_code")
    private String slotCode;

    private String title;

    private String subtitle;

    @TableField("cta_text")
    private String ctaText;

    private String icon;

    @TableField("bg_color_start")
    private String bgColorStart;

    @TableField("bg_color_end")
    private String bgColorEnd;

    @TableField("image_url")
    private String imageUrl;

    @TableField("nav_target")
    private String navTarget;

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
