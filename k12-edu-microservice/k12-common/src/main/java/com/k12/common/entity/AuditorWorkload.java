package com.k12.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Phase 8：审核员工作量日汇总
 */
@Data
@TableName("auditor_workload")
public class AuditorWorkload {

    @TableId(type = IdType.AUTO)
    private Long id;

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

    /** 最长审核时长(秒) */
    private Integer maxDurationSec;

    /** 超时审核数(>24h) */
    private Integer overtimeCount;

    private LocalDateTime createTime;
}
