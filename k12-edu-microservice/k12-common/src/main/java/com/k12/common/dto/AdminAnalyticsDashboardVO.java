package com.k12.common.dto;

import lombok.Data;

import java.util.List;

@Data
public class AdminAnalyticsDashboardVO {

    private int days;
    private boolean scoped;
    private String scopeHint;
    private AdminAnalyticsSummaryVO summary;
    private AdminAnalyticsAuditVO audit;
    private List<AdminAnalyticsDailyPointVO> resourceUploadTrend;
    private List<AdminAnalyticsActionDailyVO> actionTrend;
    private List<AdminAnalyticsDistributionItemVO> stageDistribution;
    private List<AdminAnalyticsDistributionItemVO> subjectDistribution;
    private List<AdminAnalyticsTopResourceVO> topByDownload;
    private List<AdminAnalyticsTopResourceVO> topByView;
    private List<AdminAnalyticsTopResourceVO> topByCollect;
    private List<AdminAnalyticsUploaderStatVO> topUploaders;
}
