package com.k12.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Phase 3-P0: 资源来源治理台账
 */
@Data
@TableName("resource_migration_ledger")
public class ResourceMigrationLedger {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 来源类型：primary_chinese / topic_resource / culture_resource / competition_resource / article_attachment */
    private String sourceType;

    /** 旧表名 */
    private String legacySourceTable;

    /** 新主链路目标（如 resource_main + edu_resource） */
    private String targetMainChain;

    /** 回填状态：0=未开始 1=进行中 2=已完成 */
    private Integer backfillStatus;

    /** 前台切换状态：0=未切换 1=灰度中 2=全量 */
    private Integer frontendSwitchStatus;

    /** 旧接口下线状态：0=保留 1=兼容期 2=已下线 */
    private Integer legacyApiOfflineStatus;

    /** 新旧结果比对状态：0=未知 1=通过 2=失败 */
    private Integer compareStatus;

    /** 最近一次比对通过率，0-100 */
    private Integer comparePassRate;

    private LocalDateTime lastComparedAt;

    private String notes;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
