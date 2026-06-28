package com.k12.resource.search;

import com.k12.resource.entity.SearchClickLog;
import com.k12.resource.mapper.SearchClickLogMapper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 异步写入搜索点击日志（P3/P2.5）
 */
@Service
public class SearchClickLogService {

    private final SearchClickLogMapper searchClickLogMapper;
    public SearchClickLogService(SearchClickLogMapper searchClickLogMapper) {
        this.searchClickLogMapper = searchClickLogMapper;
    }


    @Async
    public void logClick(
            Long userId,
            String keyword,
            String normalizedKeyword,
            String contentDomain,
            String docId,
            Long resourceId,
            String resourceType,
            String clickType,
            Integer position,
            String detailRoute) {
        if (!StringUtils.hasText(keyword)) {
            return;
        }
        SearchClickLog row = new SearchClickLog();
        row.setUserId(userId);
        row.setKeyword(keyword.trim());
        row.setNormalizedKeyword(normalizedKeyword);
        row.setContentDomain(contentDomain);
        row.setDocId(docId);
        row.setResourceId(resourceId);
        row.setResourceType(resourceType);
        row.setClickType(StringUtils.hasText(clickType) ? clickType : "result");
        row.setPosition(position);
        row.setDetailRoute(detailRoute);
        searchClickLogMapper.insert(row);
    }
}
