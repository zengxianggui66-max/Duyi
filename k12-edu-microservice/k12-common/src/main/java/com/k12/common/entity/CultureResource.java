package com.k12.common.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("culture_resource")
public class CultureResource {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String title;
    private String summary;
    private String category;
    private String region;
    private String durationType;
    private String durationLabel;
    private String suitableAudience;
    private String location;
    private String resourceKind;
    private String fileFormat;
    private String fileUrl;
    private String coverUrl;
    private String icon;
    private String sourceName;
    private String externalUrl;
    private String tags;
    private Integer downloadCount;
    private Integer viewCount;
    private Integer isFree;
    private Integer isElite;
    private Integer status;
    private Integer sort;
    @TableLogic
    @TableField("is_deleted")
    private Integer deleted;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
