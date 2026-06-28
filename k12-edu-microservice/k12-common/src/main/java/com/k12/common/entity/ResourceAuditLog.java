package com.k12.common.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 资源审核流水（Phase 4-B）
 */
@Data
@TableName("resource_audit_log")
public class ResourceAuditLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long resourceId;

    private Long auditorId;

    private String auditorName;

    /** approve | reject */
    private String action;

    private Integer beforeStatus;

    private Integer afterStatus;

    private String reason;

    @TableField("created_at")
    private LocalDateTime createdAt;
}
