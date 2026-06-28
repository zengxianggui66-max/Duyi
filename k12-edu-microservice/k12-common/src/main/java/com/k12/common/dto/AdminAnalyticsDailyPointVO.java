package com.k12.common.dto;

import lombok.Data;

@Data
public class AdminAnalyticsDailyPointVO {

    /** yyyy-MM-dd */
    private String date;
    private long count;
    /** 累计（资源增长趋势用） */
    private Long cumulative;
}
