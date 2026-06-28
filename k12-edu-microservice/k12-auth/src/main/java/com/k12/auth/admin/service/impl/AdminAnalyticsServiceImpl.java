package com.k12.auth.admin.service.impl;

import com.k12.auth.admin.service.AdminAnalyticsService;
import com.k12.auth.mapper.AdminAnalyticsUserMapper;
import com.k12.common.dto.AdminAnalyticsDailyPointVO;
import com.k12.common.dto.AdminAnalyticsUserVO;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AdminAnalyticsServiceImpl implements AdminAnalyticsService {

    private final AdminAnalyticsUserMapper adminAnalyticsUserMapper;
    public AdminAnalyticsServiceImpl(AdminAnalyticsUserMapper adminAnalyticsUserMapper) {
        this.adminAnalyticsUserMapper = adminAnalyticsUserMapper;
    }


    @Override
    public AdminAnalyticsUserVO getUserAnalytics(int days) {
        int safeDays = clampDays(days);
        LocalDate start = LocalDate.now().minusDays(safeDays - 1L);
        LocalDateTime since = start.atStartOfDay();

        AdminAnalyticsUserVO vo = new AdminAnalyticsUserVO();
        vo.setDays(safeDays);
        vo.setTotalUsers(adminAnalyticsUserMapper.countTotalUsers());
        vo.setNewUsersInPeriod(adminAnalyticsUserMapper.countNewUsers(since));

        Map<String, Long> daily = new HashMap<>();
        for (Map<String, Object> row : adminAnalyticsUserMapper.dailyRegistrations(since)) {
            daily.put(String.valueOf(row.get("day")), toLong(row.get("cnt")));
        }
        vo.setRegistrationTrend(fillDailyPoints(daily, start, safeDays, false));
        return vo;
    }

    private List<AdminAnalyticsDailyPointVO> fillDailyPoints(
            Map<String, Long> daily, LocalDate start, int days, boolean cumulative) {
        List<AdminAnalyticsDailyPointVO> list = new ArrayList<>(days);
        long running = 0L;
        for (int i = 0; i < days; i++) {
            LocalDate day = start.plusDays(i);
            String key = day.toString();
            long count = daily.getOrDefault(key, 0L);
            AdminAnalyticsDailyPointVO point = new AdminAnalyticsDailyPointVO();
            point.setDate(key);
            point.setCount(count);
            if (cumulative) {
                running += count;
                point.setCumulative(running);
            }
            list.add(point);
        }
        return list;
    }

    private int clampDays(int days) {
        if (days < 7) {
            return 7;
        }
        return Math.min(days, 90);
    }

    private long toLong(Object value) {
        if (value == null) {
            return 0L;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        return Long.parseLong(String.valueOf(value));
    }
}
