package com.k12.resource.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_search_intent_rule")
public class SysSearchIntentRule {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String pattern;
    private String intentType;
    private String targetKey;
    private String targetValue;
    private String targetPayload;
    private Integer priority;
    private Integer status;
    private LocalDateTime updateTime;
}
