package com.k12.auth.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_data_scope")
public class SysDataScope {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long roleId;
    private String scopeType;
    private String scopeValue;
    private Integer status;
    private LocalDateTime createTime;
}
