package com.k12.resource.search;

import com.k12.common.dto.SearchP3ReadinessVO;
import com.k12.resource.mapper.SearchQueryLogMapper;
import com.k12.resource.mapper.SearchReadinessMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * P3 搜索引擎影子接入：量化就绪评估
 */
@Service
public class SearchP3ReadinessService {

    private final SearchReadinessMapper readinessMapper;
    private final SearchQueryLogMapper searchQueryLogMapper;
    public SearchP3ReadinessService(SearchReadinessMapper readinessMapper, SearchQueryLogMapper searchQueryLogMapper) {
        this.readinessMapper = readinessMapper;
        this.searchQueryLogMapper = searchQueryLogMapper;
    }


    public SearchP3ReadinessVO evaluate(int days) {
        int safeDays = Math.max(days, 1);
        LocalDateTime since = LocalDateTime.now().minusDays(safeDays);

        SearchP3ReadinessVO vo = new SearchP3ReadinessVO();
        vo.setDays(safeDays);
        List<String> notes = new ArrayList<>();

        // 1. 规模
        long totalDocs = readinessMapper.countActiveDocuments();
        vo.setTotalDocs(totalDocs);
        vo.setDocTypeDistribution(readinessMapper.docTypeDistribution());
        vo.setChannelDistribution(readinessMapper.channelDistribution());
        vo.setScaleVerdict(judgeScale(totalDocs));
        vo.setScaleRecommendation(scaleRecommendation(totalDocs));

        // 2. 耗时
        long totalQueries = searchQueryLogMapper.countSince(since);
        vo.setTotalQueries(totalQueries);
        Map<String, Object> costSummary = readinessMapper.queryCostSummary(since);
        if (costSummary != null) {
            vo.setAvgCostMs(asDouble(costSummary.get("avgCostMs")));
            vo.setMaxCostMs(asInt(costSummary.get("maxCostMs")));
        }
        vo.setP95CostMs(readinessMapper.p95CostMs(since));
        vo.setP99CostMs(readinessMapper.p99CostMs(since));
        vo.setSlowestKeywords(readinessMapper.slowestKeywords(since, 30));
        vo.setLatencyVerdict(judgeLatency(vo.getP95CostMs(), vo.getP99CostMs()));

        // 3. 零结果
        long zeroQueries = searchQueryLogMapper.countZeroSince(since);
        vo.setZeroResultQueries(zeroQueries);
        vo.setZeroResultRate(totalQueries == 0 ? 0 : round4(zeroQueries * 1.0 / totalQueries));
        vo.setTopZeroKeywords(readinessMapper.topZeroKeywords(since, 50));
        vo.setZeroResultVerdict(judgeZeroResultRate(vo.getZeroResultRate()));

        // 4. CTR
        Double ctr = readinessMapper.overallCtr(since);
        vo.setOverallCtr(ctr == null ? 0 : ctr);
        vo.setCtrByKeyword(readinessMapper.ctrByKeyword(since, 50));
        vo.setCtrVerdict(judgeCtr(vo.getOverallCtr()));

        // 5. 点击位置
        Map<String, Object> pos = readinessMapper.overallClickPosition(since);
        if (pos != null) {
            vo.setOverallAvgClickPosition(asDouble(pos.get("avgClickPosition")));
            vo.setOverallTop3ClickRate(asDouble(pos.get("top3ClickRate")));
        }
        vo.setPositionByKeyword(readinessMapper.positionByKeyword(since, 50));
        vo.setPositionVerdict(judgeTop3Rate(vo.getOverallTop3ClickRate()));

        // 综合
        vo.setP3Mode(resolveP3Mode(totalDocs, vo));
        vo.setOverallRecommendation(buildOverallRecommendation(vo));
        if (totalQueries < 100) {
            notes.add("近 " + safeDays + " 天查询样本仅 " + totalQueries + " 次，阈值判定仅供参考，建议上线后积累真实流量再评估。");
        }
        if (vo.getZeroResultRate() > 0.08) {
            notes.add("零结果率偏高可能含测试词（如 xyz_no_result_*），生产报表建议排除测试 client_key。");
        }
        if (vo.getOverallCtr() == 0 && totalQueries > 0) {
            notes.add("CTR 为 0 多因点击日志与查询词未同窗关联；需前端点击上报携带与搜索一致的 keyword。");
        }
        vo.setNotes(notes);
        return vo;
    }

    private String judgeScale(long totalDocs) {
        if (totalDocs < 10_000) {
            return "MYSQL_SUFFICIENT";
        }
        if (totalDocs < 50_000) {
            return "START_P3_RECOMMENDED";
        }
        if (totalDocs < 100_000) {
            return "P3_SHOULD_EXECUTE";
        }
        return "P3_MANDATORY";
    }

    private String scaleRecommendation(long totalDocs) {
        if (totalDocs < 10_000) {
            return "索引 < 1 万：MySQL 足够，P3 建议影子接入（双写/对比，不替换主路径）";
        }
        if (totalDocs < 50_000) {
            return "索引 1 万–5 万：建议开始 P3 影子接入并逐步灰度";
        }
        if (totalDocs < 100_000) {
            return "索引 5 万–10 万：应执行 P3，MySQL 仅保留兜底";
        }
        return "索引 > 10 万：必须 P3 为主引擎";
    }

    private String judgeLatency(Integer p95, Integer p99) {
        if (p95 == null && p99 == null) {
            return "NO_DATA";
        }
        int p95v = p95 == null ? 0 : p95;
        int p99v = p99 == null ? 0 : p99;
        if (p99v > 1000) {
            return "P3_MANDATORY";
        }
        if (p95v > 500) {
            return "P3_NEEDED";
        }
        if (p95v > 200) {
            return "WATCH_SUGGEST";
        }
        return "MYSQL_OK";
    }

    private String judgeZeroResultRate(double rate) {
        if (rate < 0.03) {
            return "HEALTHY";
        }
        if (rate <= 0.08) {
            return "IMPROVE_LEXICON";
        }
        return "QUALITY_ISSUE";
    }

    private String judgeCtr(double ctr) {
        if (ctr > 0.35) {
            return "GOOD";
        }
        if (ctr >= 0.15) {
            return "ACCEPTABLE";
        }
        return "NEEDS_RELEVANCE_TUNING";
    }

    private String judgeTop3Rate(Double rate) {
        if (rate == null) {
            return "NO_CLICK_DATA";
        }
        if (rate > 0.60) {
            return "RANKING_GOOD";
        }
        if (rate >= 0.40) {
            return "RANKING_OK";
        }
        return "RANKING_NEEDS_WORK";
    }

    private String resolveP3Mode(long totalDocs, SearchP3ReadinessVO vo) {
        if (totalDocs >= 50_000 || "P3_MANDATORY".equals(vo.getLatencyVerdict())) {
            return "GRAYSCALE_PRIMARY";
        }
        return "SHADOW_ONLY";
    }

    private String buildOverallRecommendation(SearchP3ReadinessVO vo) {
        if ("P3_MANDATORY".equals(vo.getScaleVerdict()) || "P3_MANDATORY".equals(vo.getLatencyVerdict())) {
            return "必须推进 P3 搜索引擎，MySQL 保留降级兜底";
        }
        if ("MYSQL_SUFFICIENT".equals(vo.getScaleVerdict())
                && ("MYSQL_OK".equals(vo.getLatencyVerdict()) || "WATCH_SUGGEST".equals(vo.getLatencyVerdict()))) {
            return "当前可继续 MySQL 主搜；建议启动 P3 影子接入（双路召回对比、不写主路径）";
        }
        if ("QUALITY_ISSUE".equals(vo.getZeroResultVerdict())) {
            return "P3 前先治理词典/索引；影子接入同步验证相关性";
        }
        return "建议 P3 影子接入 + 持续监控 5 类报表";
    }

    private double round4(double v) {
        return Math.round(v * 10000.0) / 10000.0;
    }

    private double asDouble(Object v) {
        if (v instanceof Number n) {
            return n.doubleValue();
        }
        try {
            return v == null ? 0 : Double.parseDouble(String.valueOf(v));
        } catch (Exception e) {
            return 0;
        }
    }

    private Integer asInt(Object v) {
        if (v instanceof Number n) {
            return n.intValue();
        }
        try {
            return v == null ? null : Integer.parseInt(String.valueOf(v));
        } catch (Exception e) {
            return null;
        }
    }
}
