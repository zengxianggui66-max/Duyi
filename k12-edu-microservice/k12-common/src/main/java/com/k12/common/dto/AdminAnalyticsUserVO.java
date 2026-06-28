package com.k12.common.dto;

import lombok.Data;

import java.util.List;

@Data
public class AdminAnalyticsUserVO {

    private int days;
    private long totalUsers;
    private long newUsersInPeriod;
    private List<AdminAnalyticsDailyPointVO> registrationTrend;
}
