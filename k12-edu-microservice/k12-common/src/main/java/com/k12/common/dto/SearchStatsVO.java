package com.k12.common.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class SearchStatsVO {
    private int days;
    private long totalQueries;
    private long zeroResultQueries;
    private double zeroResultRate;
    private long totalClicks;
    private double clickThroughRate;
    private Map<String, Long> clicksByType;
    private List<SearchQueryRankVO> topQueries;
    private List<SearchQueryRankVO> topZeroQueries;
}
