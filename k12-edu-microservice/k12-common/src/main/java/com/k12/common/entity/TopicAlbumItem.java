package com.k12.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("topic_album_item")
public class TopicAlbumItem {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long albumId;
    private Long resourceId;
    private Integer sort;
}
