package com.k12.common.dto;

import lombok.Data;

import java.util.List;

/**
 * 资源浏览类型统计（含与列表同 scope 的总数）
 */
@Data
public class BrowseStatsResultVO {

    private List<BrowseTypeStatVO> types;
    private Long total;
    /** 各 displayType 计数之和，用于与 total 对账 */
    private Long typeSum;
}
