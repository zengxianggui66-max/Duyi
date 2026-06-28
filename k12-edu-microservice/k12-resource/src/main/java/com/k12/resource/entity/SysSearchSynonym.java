package com.k12.resource.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_search_synonym")
public class SysSearchSynonym {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String word;
    private String synonyms;
    private String domain;
    private String canonical;
    private Integer status;
    private LocalDateTime updateTime;
}
