package com.k12.resource.search;

import com.k12.common.dto.SearchQueryRankVO;
import com.k12.common.dto.SearchStatsVO;
import com.k12.resource.mapper.SearchClickLogMapper;
import com.k12.resource.mapper.SearchQueryLogMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 搜索统计复盘（P2/P3）
 */
@Service
public class SearchAnalyticsService {

    private final SearchQueryLogMapper searchQueryLogMapper;
    private final SearchClickLogMapper searchClickLogMapper;
    public SearchAnalyticsService(SearchQueryLogMapper searchQueryLogMapper, SearchClickLogMapper searchClickLogMapper) {
        this.searchQueryLogMapper = searchQueryLogMapper;
        this.searchClickLogMapper = searchClickLogMapper;
    }


    public SearchStatsVO stats(int days) {
        int safeDays = Math.max(days, 1);
        LocalDateTime since = LocalDateTime.now().minusDays(safeDays);

        long totalQueries = searchQueryLogMapper.countSince(since);
        long zeroQueries = searchQueryLogMapper.countZeroSince(since);
        long totalClicks = searchClickLogMapper.selectCount(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.k12.resource.entity.SearchClickLog>()
                        .ge(com.k12.resource.entity.SearchClickLog::getCreateTime, since));

        SearchStatsVO vo = new SearchStatsVO();
        vo.setDays(safeDays);
        vo.setTotalQueries(totalQueries);
        vo.setZeroResultQueries(zeroQueries);
        vo.setZeroResultRate(totalQueries == 0 ? 0 : round4(zeroQueries * 1.0 / totalQueries));
        vo.setTotalClicks(totalClicks);
        vo.setClickThroughRate(totalQueries == 0 ? 0 : round4(totalClicks * 1.0 / totalQueries));
        vo.setClicksByType(toClickTypeMap(searchClickLogMapper.countByClickType(since)));
        vo.setTopQueries(toRankList(searchQueryLogMapper.topQueries(since, 10), false));
        vo.setTopZeroQueries(toRankList(searchQueryLogMapper.topZeroQueries(since, 10), true));
        return vo;
    }

    private Map<String, Long> toClickTypeMap(List<Map<String, Object>> rows) {
        Map<String, Long> map = new HashMap<>();
        if (rows == null) {
            return map;
        }
        for (Map<String, Object> row : rows) {
            String type = String.valueOf(row.getOrDefault("clickType", "result"));
            long cnt = asLong(row.get("cnt"));
            map.put(type, cnt);
        }
        return map;
    }

    private List<SearchQueryRankVO> toRankList(List<Map<String, Object>> rows, boolean zeroFocus) {
        if (rows == null) {
            return List.of();
        }
        return rows.stream().map(row -> {
            SearchQueryRankVO item = new SearchQueryRankVO();
            item.setKeyword(String.valueOf(row.getOrDefault("keyword", "")));
            item.setQueryCount(asLong(row.get("queryCount")));
            if (zeroFocus) {
                item.setZeroCount(asLong(row.get("zeroCount")));
            } else {
                item.setAvgHits(asDouble(row.get("avgHits")));
            }
            return item;
        }).collect(Collectors.toList());
    }

    private long asLong(Object value) {
        if (value instanceof Number n) {
            return n.longValue();
        }
        try {
            return Long.parseLong(String.valueOf(value));
        } catch (Exception e) {
            return 0L;
        }
    }

    private double asDouble(Object value) {
        if (value instanceof Number n) {
            return n.doubleValue();
        }
        try {
            return Double.parseDouble(String.valueOf(value));
        } catch (Exception e) {
            return 0;
        }
    }

    private double round4(double value) {
        return Math.round(value * 10000.0) / 10000.0;
    }
}
