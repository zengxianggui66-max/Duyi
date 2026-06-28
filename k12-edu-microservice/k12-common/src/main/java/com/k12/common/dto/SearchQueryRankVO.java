package com.k12.common.dto;

import lombok.Data;

@Data
public class SearchQueryRankVO {
    private String keyword;
    private long queryCount;
    private long zeroCount;
    private double avgHits;
}
