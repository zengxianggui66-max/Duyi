package com.k12.common.dto;

import lombok.Data;

/**
 * 审核流水查询
 */
@Data
public class ResourceAuditLogQueryDTO {
    private Integer current = 1;
    private Integer size = 20;
    private Long resourceId;
    private Long auditorId;
    private String action;
    private String keyword;
    /** 起止时间筛选（ISO 格式 yyyy-MM-dd HH:mm:ss） */
    private String startTime;
    private String endTime;
}
