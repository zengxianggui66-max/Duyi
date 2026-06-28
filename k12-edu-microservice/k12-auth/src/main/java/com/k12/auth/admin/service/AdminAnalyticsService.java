package com.k12.auth.admin.service;

import com.k12.common.dto.AdminAnalyticsUserVO;

public interface AdminAnalyticsService {

    AdminAnalyticsUserVO getUserAnalytics(int days);
}
