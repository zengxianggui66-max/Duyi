package com.k12.resource.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("resource_search_index")
public class ResourceSearchIndex {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String docId;
    private Long resourceId;
    private String resourceType;
    private String title;
    private String summary;
    private String searchText;
    private String stageKey;
    private String stageName;
    private String channelKey;
    private String channelName;
    private String subject;
    private String gradeName;
    private String editionName;
    private String moduleName;
    private String catalogPath;
    private String lessonName;
    private String teachingType;
    private String fileExt;
    private String detailRoute;
    private Integer downloadCount;
    private Integer viewCount;
    private Double hotScore;
    private Integer vipFlag;
    private Integer status;
    private LocalDateTime publishTime;
    private LocalDateTime updateTime;
}
