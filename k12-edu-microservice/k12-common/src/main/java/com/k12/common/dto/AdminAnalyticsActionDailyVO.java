package com.k12.common.dto;

import lombok.Data;

@Data
public class AdminAnalyticsActionDailyVO {

    private String date;
    private long views;
    private long downloads;
    private long collects;
}
