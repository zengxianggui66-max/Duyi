package com.k12.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Phase 8：敏感词库
 */
@Data
@TableName("sys_sensitive_word")
public class SysSensitiveWord {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 敏感词 */
    private String word;

    /** 分类: 0-通用 1-政治 2-色情 3-广告 4-暴力 5-其他 */
    private Integer category;

    /** 严重级别: 1-警告 2-阻断 */
    private Integer level;

    /** 1-启用 0-禁用 */
    private Integer status;

    /** 备注 */
    private String remark;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
