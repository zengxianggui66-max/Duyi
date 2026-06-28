package com.k12.resource.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("sys_search_document")
public class SysSearchDocument {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String docId;
    private String docType;
    private String bizId;
    private String title;
    private String subtitle;
    private String summary;
    private String contentText;
    private String keywordText;
    private String stageKey;
    private String stageName;
    private String subjectKey;
    private String subjectName;
    private String gradeKey;
    private String gradeName;
    private String channelKey;
    private String channelName;
    private String moduleKey;
    private String moduleName;
    private String resourceTypeKey;
    private String resourceTypeName;
    private String routePath;
    private String coverUrl;
    private LocalDateTime publishTime;
    private Integer viewCount;
    private Integer downloadCount;
    private BigDecimal qualityScore;
    private Double hotScore;
    private Integer vipFlag;
    private Integer status;
    private Integer isDeleted;
    private LocalDateTime updateTime;
    private LocalDateTime createTime;
}
