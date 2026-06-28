package com.k12.resource.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_search_redirect")
public class SysSearchRedirect {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String keyword;
    private String title;
    private String routePath;
    private String navTarget;
    private Integer priority;
    private Integer status;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
