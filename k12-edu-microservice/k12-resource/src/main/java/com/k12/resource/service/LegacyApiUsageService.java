package com.k12.resource.service;

import com.k12.common.dto.LegacyApiUsageVO;

import java.util.List;

public interface LegacyApiUsageService {
    void record(String apiPath, String sampleQuery);

    List<LegacyApiUsageVO> listRecent(Integer days);
}
