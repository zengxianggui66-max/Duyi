package com.k12.resource.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.k12.common.dto.ResourceMainDetailVO;
import com.k12.common.dto.ResourceMainQueryDTO;
import com.k12.common.dto.ResourceMainVO;

import java.util.Map;

/**
 * Phase 7：统一资源主域服务（管理端资源列表 / 审核中心共用）
 * 基于 v_admin_resource_main 视图，不绑定具体学科源表
 */
public interface AdminResourceMainService {

    /** 资源列表（分页，支持 source_type 筛选） */
    Page<ResourceMainVO> listPage(ResourceMainQueryDTO query, Long adminUserId);

    /** 待审队列（audit_status = 0） */
    Page<ResourceMainVO> listPending(ResourceMainQueryDTO query, Long adminUserId);

    /** 按 global_id 查详情 */
    ResourceMainDetailVO getDetail(Long globalId, Long adminUserId);

    /** 资源统计（按 source_type 分组） */
    Map<String, Object> getStats(Long adminUserId);
}
