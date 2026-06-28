package com.k12.resource.service;

import com.k12.common.dto.BehaviorQueryDTO;
import com.k12.common.dto.BehaviorStatsVO;

import java.util.List;
import java.util.Map;

public interface ViewRecordService {
    Map<String, Object> listByPage(Long userId, BehaviorQueryDTO dto);

    BehaviorStatsVO getStats(Long userId);

    void upsertView(Long userId, Long resourceId, String resourceType, Map<String, Object> snapshot);

    void remove(Long userId, Long id);

    void batchRemove(Long userId, List<Long> ids);

    void clearAll(Long userId);
}
