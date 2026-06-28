package com.k12.common.dto;

import lombok.Data;

@Data
public class AdminAnalyticsSummaryVO {

    private long totalResources;
    private long pendingResources;
    private long publishedResources;
    private long totalDownloads;
    private long totalViews;
    private long totalCollects;
    private long newResourcesInPeriod;
}
