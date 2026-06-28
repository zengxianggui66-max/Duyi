package com.k12.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("xinketang.ops_channel_tab")
public class OpsChannelTab {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("channel_code")
    private String channelCode;

    @TableField("tab_key")
    private String tabKey;

    @TableField("tab_name")
    private String tabName;

    private String icon;

    @TableField("search_keyword")
    private String searchKeyword;

    private Integer sort;

    private Integer status;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;
}
