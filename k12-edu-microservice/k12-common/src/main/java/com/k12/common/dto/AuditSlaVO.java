package com.k12.common.dto;

import lombok.Data;

/**
 * Phase 8：审核 SLA 指标 VO
 */
@Data
public class AuditSlaVO {

    /** 平均审核时长(秒) */
    private Long avgAuditDurationSec;

    /** 平均审核时长(格式化: "2h 35m") */
    private String avgAuditDurationFormatted;

    /** 超时待审数(>24h) */
    private Integer overtime24hCount;

    /** 超时待审数(>48h) */
    private Integer overtime48hCount;

    /** 超时待审数(>72h) */
    private Integer overtime72hCount;

    /** 当前待审总数 */
    private Integer totalPendingCount;

    /** 当日审核完成数 */
    private Integer todayCompletedCount;

    /** 当日驳回数 */
    private Integer todayRejectedCount;
}
