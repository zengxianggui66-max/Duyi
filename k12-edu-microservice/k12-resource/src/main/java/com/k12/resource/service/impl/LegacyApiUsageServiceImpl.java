package com.k12.resource.service.impl;

import com.k12.common.dto.LegacyApiUsageVO;
import com.k12.resource.mapper.LegacyApiUsageMapper;
import com.k12.resource.service.LegacyApiUsageService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class LegacyApiUsageServiceImpl implements LegacyApiUsageService {

    private final LegacyApiUsageMapper legacyApiUsageMapper;

    public LegacyApiUsageServiceImpl(LegacyApiUsageMapper legacyApiUsageMapper) {
        this.legacyApiUsageMapper = legacyApiUsageMapper;
    }

    @Override
    public void record(String apiPath, String sampleQuery) {
        if (!StringUtils.hasText(apiPath)) {
            return;
        }
        String normalized = apiPath.trim();
        String query = StringUtils.hasText(sampleQuery)
                ? sampleQuery.substring(0, Math.min(sampleQuery.length(), 240))
                : null;
        legacyApiUsageMapper.upsertHit(normalized, query);
    }

    @Override
    public List<LegacyApiUsageVO> listRecent(Integer days) {
        int safeDays = days == null || days < 1 ? 7 : Math.min(days, 90);
        return legacyApiUsageMapper.listRecent(safeDays);
    }
}
