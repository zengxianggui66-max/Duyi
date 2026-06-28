package com.k12.common.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * P3 搜索引擎影子接入：量化就绪评估（5 类报表 + 阈值判定）
 */
@Data
public class SearchP3ReadinessVO {
    private int days;

    /** 1. 数据规模 */
    private long totalDocs;
    private List<Map<String, Object>> docTypeDistribution;
    private List<Map<String, Object>> channelDistribution;
    private String scaleVerdict;
    private String scaleRecommendation;

    /** 2. 耗时 */
    private long totalQueries;
    private Double avgCostMs;
    private Integer maxCostMs;
    private Integer p95CostMs;
    private Integer p99CostMs;
    private List<Map<String, Object>> slowestKeywords;
    private String latencyVerdict;

    /** 3. 零结果 */
    private long zeroResultQueries;
    private double zeroResultRate;
    private List<Map<String, Object>> topZeroKeywords;
    private String zeroResultVerdict;

    /** 4. CTR */
    private double overallCtr;
    private List<Map<String, Object>> ctrByKeyword;
    private String ctrVerdict;

    /** 5. 点击位置 */
    private Double overallTop3ClickRate;
    private Double overallAvgClickPosition;
    private List<Map<String, Object>> positionByKeyword;
    private String positionVerdict;

    /** 综合 */
    private String overallRecommendation;
    private String p3Mode;
    private List<String> notes;
}
