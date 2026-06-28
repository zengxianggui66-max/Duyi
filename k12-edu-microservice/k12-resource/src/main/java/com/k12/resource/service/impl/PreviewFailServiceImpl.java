package com.k12.resource.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.k12.common.entity.PreviewFailQueue;
import com.k12.resource.mapper.PreviewFailQueueMapper;
import com.k12.resource.service.PreviewFailService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Phase 8：预览失败队列 Service 实现
 */
@Service
@SuppressWarnings("null")
public class PreviewFailServiceImpl implements PreviewFailService {

    private final PreviewFailQueueMapper previewFailQueueMapper;
    public PreviewFailServiceImpl(PreviewFailQueueMapper previewFailQueueMapper) {
        this.previewFailQueueMapper = previewFailQueueMapper;
    }


    @Override
    public Page<PreviewFailQueue> listPage(int pageNum, int pageSize, Integer status,
                                            String sourceType, String keyword) {
        Page<PreviewFailQueue> page = new Page<>(pageNum, pageSize);
        return previewFailQueueMapper.findPage(page, status, sourceType, keyword);
    }

    @Override
    public void markHandled(Long id, Long handlerId, String handlerName, String note) {
        int rows = previewFailQueueMapper.markHandled(id, handlerId, handlerName, note);
        if (rows == 0) {
            throw new RuntimeException("预览失败记录不存在: id=" + id);
        }
    }

    @Override
    public void markIgnored(Long id, Long handlerId, String handlerName, String note) {
        int rows = previewFailQueueMapper.markIgnored(id, handlerId, handlerName, note);
        if (rows == 0) {
            throw new RuntimeException("预览失败记录不存在: id=" + id);
        }
    }

    @Override
    public int countPending() {
        return previewFailQueueMapper.countPending();
    }

    @Override
    public List<Map<String, Object>> countByStatus() {
        return previewFailQueueMapper.countByStatus();
    }
}
