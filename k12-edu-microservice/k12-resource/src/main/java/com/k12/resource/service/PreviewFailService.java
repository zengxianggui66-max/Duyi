package com.k12.resource.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.k12.common.entity.PreviewFailQueue;

import java.util.List;
import java.util.Map;

/**
 * Phase 8：预览失败队列 Service
 */
public interface PreviewFailService {

    /**
     * 分页查询
     */
    Page<PreviewFailQueue> listPage(int pageNum, int pageSize, Integer status,
                                     String sourceType, String keyword);

    /**
     * 标记已处理
     */
    void markHandled(Long id, Long handlerId, String handlerName, String note);

    /**
     * 标记已忽略
     */
    void markIgnored(Long id, Long handlerId, String handlerName, String note);

    /**
     * 待处理数
     */
    int countPending();

    /**
     * 按状态分组统计
     */
    List<Map<String, Object>> countByStatus();
}
