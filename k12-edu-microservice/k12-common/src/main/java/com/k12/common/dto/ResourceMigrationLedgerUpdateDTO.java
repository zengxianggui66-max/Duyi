package com.k12.common.dto;

import lombok.Data;

/**
 * Phase 3-P0: 台账更新 DTO
 */
@Data
public class ResourceMigrationLedgerUpdateDTO {

    private String legacySourceTable;

    private String targetMainChain;

    /** 0=未开始 1=进行中 2=已完成 */
    private Integer backfillStatus;

    /** 0=未切换 1=灰度中 2=全量 */
    private Integer frontendSwitchStatus;

    /** 0=保留 1=兼容期 2=已下线 */
    private Integer legacyApiOfflineStatus;

    /** 0=未知 1=通过 2=失败 */
    private Integer compareStatus;

    /** 0-100 */
    private Integer comparePassRate;

    private String notes;
}
