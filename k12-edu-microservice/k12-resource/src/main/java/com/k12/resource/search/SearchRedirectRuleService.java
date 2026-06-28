package com.k12.resource.search;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.k12.resource.entity.SysSearchRedirect;
import com.k12.resource.mapper.SysSearchRedirectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * DB 搜索重定向规则（启用项，按 priority 降序加载）
 */
@Service
public class SearchRedirectRuleService {

    private final SysSearchRedirectMapper redirectMapper;
    public SearchRedirectRuleService(SysSearchRedirectMapper redirectMapper) {
        this.redirectMapper = redirectMapper;
    }


    private volatile Map<String, SysSearchRedirect> rulesByKeyword = Map.of();

    @PostConstruct
    public void init() {
        refreshFromDb();
    }

    public void refreshFromDb() {
        List<SysSearchRedirect> rows = redirectMapper.selectList(
                new LambdaQueryWrapper<SysSearchRedirect>()
                        .eq(SysSearchRedirect::getStatus, 1)
                        .orderByDesc(SysSearchRedirect::getPriority)
                        .orderByAsc(SysSearchRedirect::getId));
        Map<String, SysSearchRedirect> map = new LinkedHashMap<>();
        if (rows != null) {
            for (SysSearchRedirect row : rows) {
                if (row == null || !StringUtils.hasText(row.getKeyword())) {
                    continue;
                }
                String key = row.getKeyword().trim();
                map.putIfAbsent(key, row);
            }
        }
        rulesByKeyword = Map.copyOf(map);
    }

    public Optional<SysSearchRedirect> findRule(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return Optional.empty();
        }
        SysSearchRedirect rule = rulesByKeyword.get(keyword.trim());
        return Optional.ofNullable(rule);
    }

    public Optional<String> findRoutePath(String keyword) {
        return findRule(keyword)
                .map(SysSearchRedirect::getRoutePath)
                .filter(StringUtils::hasText);
    }
}
