package com.k12.resource.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("search_query_log")
public class SearchQueryLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String clientKey;
    private String keyword;
    private String normalizedKeyword;
    private String intentJson;
    private String contentDomain;
    private Integer hitCount;
    private Integer page;
    private String stage;
    private String channel;
    private String type;
    private String sort;
    private String blockedCode;
    private Integer costMs;
    /** API 路径：all / suggest */
    private String apiPath;
    private LocalDateTime createTime;
}
