package com.k12.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("xinketang.home_latest_item")
public class HomeLatestItem {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("column_id")
    private Long columnId;

    private String title;

    @TableField("item_date")
    private LocalDate itemDate;

    @TableField("resource_id")
    private Long resourceId;

    @TableField("resource_source")
    private String resourceSource;

    @TableField("link_path")
    private String linkPath;

    @TableField("article_id")
    private Long articleId;

    private Integer sort;

    private Integer status;

    private String remark;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;
}
