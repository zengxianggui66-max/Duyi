package com.k12.common.dto;

import lombok.Data;

/**
 * 我的收藏统计
 */
@Data
public class CollectStatsVO {

    private Long total;
    private Long primaryCount;
    private Long juniorCount;
    private Long seniorCount;
    private Long artCount;
    private Long danceCount;
}
