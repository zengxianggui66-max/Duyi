package com.k12.resource.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.k12.common.entity.SysConfig;
import com.k12.common.mapper.SysConfigMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UnifiedReadFeatureFlagService {

    private static final String KEY_UNIFIED_READ = "feature.resourceUnifiedRead.enabled";
    private static final String KEY_PRIMARY_READ = "feature.primaryChineseUnifiedRead.enabled";
    private static final String KEY_TOPIC_READ = "feature.topicUnifiedRead.enabled";
    private static final String KEY_CULTURE_READ = "feature.cultureUnifiedRead.enabled";
    private static final String KEY_COMPETITION_READ = "feature.competitionUnifiedRead.enabled";

    private final SysConfigMapper sysConfigMapper;

    public UnifiedReadFeatureFlagService(SysConfigMapper sysConfigMapper) {
        this.sysConfigMapper = sysConfigMapper;
    }

    public boolean isUnifiedReadEnabled() {
        return getFlagValueMap().getOrDefault(KEY_UNIFIED_READ, false);
    }

    public boolean isSourceEnabled(String sourceType) {
        if (!StringUtils.hasText(sourceType)) {
            return isUnifiedReadEnabled();
        }
        if (!isUnifiedReadEnabled()) {
            return false;
        }
        String key = switch (sourceType) {
            case "primary_chinese", "edu_resource" -> KEY_PRIMARY_READ;
            case "topic_resource" -> KEY_TOPIC_READ;
            case "culture_resource" -> KEY_CULTURE_READ;
            case "competition_resource" -> KEY_COMPETITION_READ;
            default -> KEY_UNIFIED_READ;
        };
        return getFlagValueMap().getOrDefault(key, false);
    }

    private Map<String, Boolean> getFlagValueMap() {
        List<SysConfig> rows = sysConfigMapper.selectList(new QueryWrapper<SysConfig>()
                .in("config_key", List.of(
                        KEY_UNIFIED_READ,
                        KEY_PRIMARY_READ,
                        KEY_TOPIC_READ,
                        KEY_CULTURE_READ,
                        KEY_COMPETITION_READ
                )));
        Map<String, Boolean> map = new HashMap<>();
        for (SysConfig row : rows) {
            if (row == null || !StringUtils.hasText(row.getConfigKey())) {
                continue;
            }
            map.put(row.getConfigKey(), Boolean.parseBoolean(String.valueOf(row.getConfigValue()).trim()));
        }
        return map;
    }
}
