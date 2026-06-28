package com.k12.resource.search;

import com.k12.resource.entity.SearchQueryLog;
import com.k12.resource.mapper.SearchQueryLogMapper;
import com.k12.resource.service.UserResourceActionService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 异步写入搜索查询日志（P1/P2/P2.5）
 */
@Service
public class SearchLogService {

    private final SearchQueryLogMapper searchQueryLogMapper;
    private final UserResourceActionService userResourceActionService;
    public SearchLogService(SearchQueryLogMapper searchQueryLogMapper, UserResourceActionService userResourceActionService) {
        this.searchQueryLogMapper = searchQueryLogMapper;
        this.userResourceActionService = userResourceActionService;
    }


    @Async
    public void logQuery(
            Long userId,
            String clientKey,
            String keyword,
            String normalizedKeyword,
            String intentJson,
            String contentDomain,
            int hitCount,
            int page,
            String stage,
            String channel,
            String type,
            String sort,
            String blockedCode,
            int costMs,
            String apiPath) {
        if (!StringUtils.hasText(keyword)) {
            return;
        }
        SearchQueryLog row = new SearchQueryLog();
        row.setUserId(userId);
        row.setClientKey(clientKey);
        row.setKeyword(keyword.trim());
        row.setNormalizedKeyword(normalizedKeyword);
        row.setIntentJson(intentJson);
        row.setContentDomain(contentDomain);
        row.setHitCount(hitCount);
        row.setPage(page);
        row.setStage(stage);
        row.setChannel(channel);
        row.setType(type);
        row.setSort(sort);
        row.setBlockedCode(blockedCode);
        row.setCostMs(costMs);
        row.setApiPath(apiPath);
        searchQueryLogMapper.insert(row);
        userResourceActionService.recordSearch(userId, keyword.trim(), hitCount,
                apiPath != null ? apiPath : "/api/search/all");
    }
}
