package com.k12.common.dto;

import lombok.Data;

import java.time.LocalDate;

/**
 * Phase 8：审核员工作量 VO
 */
@Data
public class AuditWorkloadVO {

    /** 统计日期 */
    private LocalDate statDate;

    /** 审核员用户ID */
    private Long auditorId;

    /** 审核员姓名 */
    private String auditorName;

    /** 审核通过数 */
    private Integer approveCount;

    /** 驳回数 */
    private Integer rejectCount;

    /** 审核总数 */
    private Integer totalCount;

    /** 平均审核时长(秒) */
    private Integer avgDurationSec;

    /** 平均审核时长(分钟，格式化) */
    private String avgDurationFormatted;

    /** 最长审核时长(秒) */
    private Integer maxDurationSec;

    /** 超时审核数(>24h) */
    private Integer overtimeCount;
}
