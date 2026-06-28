package com.k12.resource.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.k12.common.dto.FilePreviewInfoDTO;
import com.k12.common.dto.ResourceMainDetailVO;
import com.k12.common.dto.ResourceMainQueryDTO;
import com.k12.common.dto.ResourceMainVO;

import java.util.List;
import java.util.Map;

/**
 * Phase 3D: 前台统一资源主链服务
 */
public interface ResourceMainService {

    Page<ResourceMainVO> page(ResourceMainQueryDTO query);

    Long resolveGlobalId(String sourceType, Long sourceId);

    ResourceMainDetailVO detail(Long resourceId);

    Map<String, Object> stats(ResourceMainQueryDTO query);

    List<Map<String, Object>> types(ResourceMainQueryDTO query);

    FilePreviewInfoDTO preview(Long resourceId);

    Map<String, Object> download(Long resourceId, Long userId);

    void view(Long resourceId, Long userId);

    void collect(Long resourceId, Long userId);
}
