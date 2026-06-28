package com.k12.resource.service;

import com.k12.common.dto.SearchAllResultVO;
import com.k12.common.dto.SearchClickRequest;
import com.k12.common.dto.SearchRedirectVO;
import com.k12.common.dto.SearchStatsVO;
import com.k12.common.dto.SearchSuggestItemVO;

import java.util.List;
import java.util.Map;

/**
 * 搜索服务接口
 */
public interface SearchService {

    /**
     * 获取热搜关键词列表
     * @param limit 返回数量
     * @return 热搜关键词列表
     */
    List<Map<String, Object>> getHotKeywords(Integer limit);

    /**
     * 记录搜索关键词
     * @param keyword 关键词
     * @param userId 用户ID（可为null）
     */
    void recordSearchKeyword(String keyword, Long userId);

    /**
     * 获取用户搜索历史
     * @param userId 用户ID
     * @param limit 返回数量
     * @return 搜索历史列表
     */
    List<String> getSearchHistory(Long userId, Integer limit);

    /**
     * 清空用户搜索历史
     * @param userId 用户ID
     */
    void clearSearchHistory(Long userId);

    /**
     * 全站搜索
     */
    SearchAllResultVO searchAll(
            String q,
            Integer page,
            Integer size,
            String stage,
            String channel,
            String type,
            String domain,
            String sort,
            Long userId,
            String clientKey,
            String searchEngine);

    /**
     * 全量重建搜索索引（管理）
     */
    int rebuildSearchIndex();

    /**
     * 搜索建议词
     */
    List<SearchSuggestItemVO> suggest(String q, Integer limit, Long userId);

    /**
     * 命中直跳判定
     */
    SearchRedirectVO redirect(String q);

    /**
     * 记录搜索结果点击（P3）
     */
    void recordClick(SearchClickRequest request, Long userId);

    /**
     * 搜索统计复盘（P3）
     */
    SearchStatsVO searchStats(Integer days);

    /** P3 量化就绪评估（5 类报表） */
    com.k12.common.dto.SearchP3ReadinessVO searchP3Readiness(Integer days);

    /** P3 OpenSearch 健康检查 */
    Map<String, Object> searchEngineHealth();

    /** P3 全量同步到搜索引擎 */
    int syncSearchEngine();
}
