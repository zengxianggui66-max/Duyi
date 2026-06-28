package com.k12.resource.service;

import com.k12.common.dto.AdminAnalyticsDashboardVO;

public interface AdminAnalyticsService {

    AdminAnalyticsDashboardVO getDashboard(Long adminUserId, int days);
}
