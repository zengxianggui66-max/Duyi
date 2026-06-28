package com.k12.resource.service.impl;

import com.k12.common.dto.AdminAnalyticsActionDailyVO;
import com.k12.common.dto.AdminAnalyticsAuditVO;
import com.k12.common.dto.AdminAnalyticsDailyPointVO;
import com.k12.common.dto.AdminAnalyticsDashboardVO;
import com.k12.common.dto.AdminAnalyticsSummaryVO;
import com.k12.common.dto.AdminDataScopeVO;
import com.k12.common.service.AdminPermissionService;
import com.k12.resource.mapper.AdminAnalyticsMapper;
import com.k12.resource.service.AdminAnalyticsService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@SuppressWarnings("null")
public class AdminAnalyticsServiceImpl implements AdminAnalyticsService {

    private static final Map<String, String> STAGE_MAP = Map.of(
            "primary", "小学",
            "junior", "初中",
            "senior", "高中"
    );
    private static final Map<String, String> SUBJECT_MAP = Map.of(
            "chinese", "语文",
            "math", "数学",
            "english", "英语"
    );
    private static final int TOP_LIMIT = 10;

    private final AdminAnalyticsMapper adminAnalyticsMapper;
    private final AdminPermissionService adminPermissionService;
    public AdminAnalyticsServiceImpl(AdminAnalyticsMapper adminAnalyticsMapper, AdminPermissionService adminPermissionService) {
        this.adminAnalyticsMapper = adminAnalyticsMapper;
        this.adminPermissionService = adminPermissionService;
    }


    @Override
    public AdminAnalyticsDashboardVO getDashboard(Long adminUserId, int days) {
        int safeDays = clampDays(days);
        LocalDate start = LocalDate.now().minusDays(safeDays - 1L);
        LocalDateTime since = start.atStartOfDay();
        ScopeParams scope = resolveScopeParams(adminUserId);

        Map<String, Object> rawSummary = adminAnalyticsMapper.selectSummary(
                since, scope.stages(), scope.subjects(), scope.uploaderId());
        AdminAnalyticsSummaryVO summary = mapSummary(rawSummary);

        long baseCount = adminAnalyticsMapper.countResourcesBefore(
                since, scope.stages(), scope.subjects(), scope.uploaderId());

        AdminAnalyticsDashboardVO vo = new AdminAnalyticsDashboardVO();
        vo.setDays(safeDays);
        vo.setScoped(scope.scoped());
        vo.setScopeHint(scope.hint());
        vo.setSummary(summary);
        vo.setResourceUploadTrend(buildUploadTrend(
                adminAnalyticsMapper.selectDailyUpload(since, scope.stages(), scope.subjects(), scope.uploaderId()),
                start, safeDays, baseCount));
        vo.setActionTrend(buildActionTrend(
                adminAnalyticsMapper.selectDailyViews(since, scope.stages(), scope.subjects(), scope.uploaderId()),
                adminAnalyticsMapper.selectDailyDownloads(since, scope.stages(), scope.subjects(), scope.uploaderId()),
                adminAnalyticsMapper.selectDailyCollects(since, scope.stages(), scope.subjects(), scope.uploaderId()),
                start, safeDays));
        vo.setAudit(buildAudit(adminAnalyticsMapper.selectAuditStats(
                since, scope.stages(), scope.subjects(), scope.uploaderId())));
        vo.setStageDistribution(adminAnalyticsMapper.selectStageDistribution(
                scope.stages(), scope.subjects(), scope.uploaderId()));
        vo.setSubjectDistribution(adminAnalyticsMapper.selectSubjectDistribution(
                scope.stages(), scope.subjects(), scope.uploaderId()));
        vo.setTopByDownload(adminAnalyticsMapper.selectTopByDownload(
                TOP_LIMIT, scope.stages(), scope.subjects(), scope.uploaderId()));
        vo.setTopByView(adminAnalyticsMapper.selectTopByView(
                TOP_LIMIT, scope.stages(), scope.subjects(), scope.uploaderId()));
        vo.setTopByCollect(adminAnalyticsMapper.selectTopByCollect(
                TOP_LIMIT, scope.stages(), scope.subjects(), scope.uploaderId()));
        vo.setTopUploaders(adminAnalyticsMapper.selectTopUploaders(
                since, TOP_LIMIT, scope.stages(), scope.subjects(), scope.uploaderId()));
        return vo;
    }

    private AdminAnalyticsSummaryVO mapSummary(Map<String, Object> raw) {
        AdminAnalyticsSummaryVO summary = new AdminAnalyticsSummaryVO();
        if (raw == null) {
            return summary;
        }
        summary.setTotalResources(toLong(raw.get("totalResources")));
        summary.setPendingResources(toLong(raw.get("pendingResources")));
        summary.setPublishedResources(toLong(raw.get("publishedResources")));
        summary.setTotalDownloads(toLong(raw.get("totalDownloads")));
        summary.setTotalViews(toLong(raw.get("totalViews")));
        summary.setTotalCollects(toLong(raw.get("totalCollects")));
        summary.setNewResourcesInPeriod(toLong(raw.get("newResourcesInPeriod")));
        return summary;
    }

    private List<AdminAnalyticsDailyPointVO> buildUploadTrend(
            List<Map<String, Object>> rows, LocalDate start, int days, long baseCount) {
        Map<String, Long> daily = toDailyMap(rows);
        List<AdminAnalyticsDailyPointVO> list = new ArrayList<>(days);
        long cumulative = baseCount;
        for (int i = 0; i < days; i++) {
            LocalDate day = start.plusDays(i);
            String key = day.toString();
            long count = daily.getOrDefault(key, 0L);
            cumulative += count;
            AdminAnalyticsDailyPointVO point = new AdminAnalyticsDailyPointVO();
            point.setDate(key);
            point.setCount(count);
            point.setCumulative(cumulative);
            list.add(point);
        }
        return list;
    }

    private List<AdminAnalyticsActionDailyVO> buildActionTrend(
            List<Map<String, Object>> views,
            List<Map<String, Object>> downloads,
            List<Map<String, Object>> collects,
            LocalDate start,
            int days) {
        Map<String, Long> viewMap = toDailyMap(views);
        Map<String, Long> downloadMap = toDailyMap(downloads);
        Map<String, Long> collectMap = toDailyMap(collects);
        List<AdminAnalyticsActionDailyVO> list = new ArrayList<>(days);
        for (int i = 0; i < days; i++) {
            String key = start.plusDays(i).toString();
            AdminAnalyticsActionDailyVO row = new AdminAnalyticsActionDailyVO();
            row.setDate(key);
            row.setViews(viewMap.getOrDefault(key, 0L));
            row.setDownloads(downloadMap.getOrDefault(key, 0L));
            row.setCollects(collectMap.getOrDefault(key, 0L));
            list.add(row);
        }
        return list;
    }

    private AdminAnalyticsAuditVO buildAudit(AdminAnalyticsAuditVO raw) {
        if (raw == null) {
            raw = new AdminAnalyticsAuditVO();
        }
        long approved = raw.getApproved();
        long rejected = raw.getRejected();
        long total = approved + rejected;
        if (total > 0) {
            raw.setPassRate(Math.round(approved * 10000.0 / total) / 100.0);
        }
        return raw;
    }

    private Map<String, Long> toDailyMap(List<Map<String, Object>> rows) {
        Map<String, Long> daily = new HashMap<>();
        if (rows == null) {
            return daily;
        }
        for (Map<String, Object> row : rows) {
            daily.put(String.valueOf(row.get("day")), toLong(row.get("cnt")));
        }
        return daily;
    }

    private ScopeParams resolveScopeParams(Long adminUserId) {
        AdminDataScopeVO scope = adminPermissionService.resolveDataScope(adminUserId);
        if ("ALL".equals(scope.getScopeType())) {
            return new ScopeParams(List.of(), List.of(), null, false, null);
        }
        if ("SELF".equals(scope.getScopeType())) {
            return new ScopeParams(List.of(), List.of(), adminUserId, true, "仅统计本人上传的资源");
        }
        List<String> stages = new ArrayList<>();
        List<String> subjects = new ArrayList<>();
        if ("STAGE_SUBJECT".equals(scope.getScopeType())) {
            for (String code : scope.getStages()) {
                stages.add(STAGE_MAP.getOrDefault(code, code));
            }
            for (String code : scope.getSubjects()) {
                subjects.add(SUBJECT_MAP.getOrDefault(code, code));
            }
        }
        String hint = "统计范围："
                + (stages.isEmpty() ? "全部学段" : String.join("、", stages))
                + " / "
                + (subjects.isEmpty() ? "全部学科" : String.join("、", subjects));
        return new ScopeParams(stages, subjects, null, true, hint);
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

    private record ScopeParams(List<String> stages, List<String> subjects, Long uploaderId, boolean scoped, String hint) {
    }
}
