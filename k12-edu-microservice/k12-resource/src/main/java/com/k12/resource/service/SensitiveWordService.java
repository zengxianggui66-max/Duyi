package com.k12.resource.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.k12.common.dto.SensitiveWordDTO;
import com.k12.common.entity.SysSensitiveWord;

import java.util.List;

/**
 * Phase 8：敏感词管理 Service
 */
public interface SensitiveWordService {

    /**
     * 分页查询
     */
    Page<SysSensitiveWord> listPage(int pageNum, int pageSize, String keyword,
                                     Integer category, Integer level, Integer status);

    /**
     * 新增敏感词
     */
    SysSensitiveWord create(SensitiveWordDTO dto);

    /**
     * 编辑敏感词
     */
    SysSensitiveWord update(Long id, SensitiveWordDTO dto);

    /**
     * 删除敏感词
     */
    void delete(Long id);

    /**
     * 批量启用/禁用
     */
    void batchUpdateStatus(List<Long> ids, Integer status);

    /**
     * 获取所有启用的敏感词（用于内容检测）
     */
    List<String> getAllEnabledWords();

    /**
     * 按分类统计
     */
    List<java.util.Map<String, Object>> countByCategory();
}
