package com.k12.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("xinketang.ops_channel_featured_album")
public class OpsChannelFeaturedAlbum {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("channel_code")
    private String channelCode;

    private String title;

    private String icon;

    private String meta;

    @TableField("resource_count")
    private Integer resourceCount;

    @TableField("download_count")
    private Integer downloadCount;

    @TableField("cover_gradient")
    private String coverGradient;

    @TableField("link_path")
    private String linkPath;

    private Integer sort;

    private Integer status;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;
}
