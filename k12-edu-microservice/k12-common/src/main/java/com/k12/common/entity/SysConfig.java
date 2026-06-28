package com.k12.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_config")
public class SysConfig {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String configKey;

    private String configValue;

    /** string / int / bool / json / secret */
    private String valueType;

    private String groupCode;

    private String description;

    private Integer requiresRestart;

    private Long updatedBy;

    private LocalDateTime updatedAt;
}
