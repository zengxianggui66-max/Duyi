package com.k12.common.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("topic_resource")
public class TopicResource {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String title;
    private String summary;
    private String category;
    private String region;
    private String gradeStage;
    private String subject;
    private String resourceForm;
    private String topicLabel;
    private String schoolYear;
    private String fileFormat;
    private String fileUrl;
    private String coverUrl;
    private String icon;
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
