package com.k12.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Phase 8：资源日统计快照
 */
@Data
@TableName("resource_daily_stats")
public class ResourceDailyStats {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 统计日期 */
    private LocalDate statDate;

    /** 资源来源: ALL=全部 / primary_chinese 等 */
    private String sourceType;

    /** 资源总数 */
    private Integer totalCount;

    /** 当日新增数 */
    private Integer newCount;

    /** 待审数(截至当日) */
    private Integer pendingCount;

    /** 当日审核通过数 */
    private Integer approvedCount;

    /** 当日驳回数 */
    private Integer rejectedCount;

    /** 已上架数(截至当日) */
    private Integer publishedCount;

    /** 当日下架数 */
    private Integer offlineCount;

    /** 当日删除数 */
    private Integer deletedCount;

    /** 当日下载次数 */
    private Integer downloadCount;

    /** 当日浏览次数 */
    private Integer viewCount;

    private LocalDateTime createTime;
}
