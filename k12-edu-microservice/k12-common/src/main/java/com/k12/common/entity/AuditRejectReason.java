package com.k12.common.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 审核驳回原因模板
 */
@Data
@TableName("audit_reject_reason")
public class AuditRejectReason {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String content;

    /** 问题分类: 0-通用, 1-内容质量, 2-格式规范, 3-版权合规, 4-分类挂载, 5-其他 */
    private Integer category;

    private Integer sort;

    /** 0-禁用 1-启用 */
    private Integer status;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
