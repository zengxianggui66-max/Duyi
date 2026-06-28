package com.k12.resource.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("search_click_log")
public class SearchClickLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String keyword;
    private String normalizedKeyword;
    private String contentDomain;
    private String docId;
    private Long resourceId;
    private String resourceType;
    private String clickType;
    private Integer position;
    private String detailRoute;
    private LocalDateTime createTime;
}
