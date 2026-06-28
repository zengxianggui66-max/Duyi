package com.k12.common.dto;

import lombok.Data;

@Data
public class AdminUserStatsVO {

    private Long uploadCount;
    private Long collectionCount;

    /** Phase 6-C 行为流水统计 */
    private Long viewCount;
    private Long downloadCount;
    private Long searchCount;
    private Long loginCount;
}
