package com.k12.resource.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.k12.common.dto.SensitiveWordDTO;
import com.k12.common.entity.SysSensitiveWord;
import com.k12.resource.mapper.SysSensitiveWordMapper;
import com.k12.resource.service.SensitiveWordService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Phase 8：敏感词管理 Service 实现
 */
@Service

public class SensitiveWordServiceImpl implements SensitiveWordService {

    private final SysSensitiveWordMapper sysSensitiveWordMapper;
    public SensitiveWordServiceImpl(SysSensitiveWordMapper sysSensitiveWordMapper) {
        this.sysSensitiveWordMapper = sysSensitiveWordMapper;
    }


    @Override
    public Page<SysSensitiveWord> listPage(int pageNum, int pageSize, String keyword,
                                            Integer category, Integer level, Integer status) {
        Page<SysSensitiveWord> page = new Page<>(pageNum, pageSize);
        return sysSensitiveWordMapper.findPage(page, keyword, category, level, status);
    }

    @Override
    public SysSensitiveWord create(SensitiveWordDTO dto) {
        SysSensitiveWord entity = new SysSensitiveWord();
        entity.setWord(dto.getWord());
        entity.setCategory(dto.getCategory() != null ? dto.getCategory() : 0);
        entity.setLevel(dto.getLevel() != null ? dto.getLevel() : 1);
        entity.setStatus(1);
        entity.setRemark(dto.getRemark());
        sysSensitiveWordMapper.insert(entity);
        return entity;
    }

    @Override
    public SysSensitiveWord update(Long id, SensitiveWordDTO dto) {
        SysSensitiveWord entity = sysSensitiveWordMapper.selectById(id);
        if (entity == null) {
            throw new RuntimeException("敏感词不存在: id=" + id);
        }
        entity.setWord(dto.getWord());
        entity.setCategory(dto.getCategory() != null ? dto.getCategory() : 0);
        entity.setLevel(dto.getLevel() != null ? dto.getLevel() : 1);
        entity.setRemark(dto.getRemark());
        sysSensitiveWordMapper.updateById(entity);
        return entity;
    }

    @Override
    public void delete(Long id) {
        sysSensitiveWordMapper.deleteById(id);
    }

    @Override
    public void batchUpdateStatus(List<Long> ids, Integer status) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        sysSensitiveWordMapper.batchUpdateStatus(ids, status);
    }

    @Override
    public List<String> getAllEnabledWords() {
        return sysSensitiveWordMapper.findAllEnabledWords();
    }

    @Override
    public List<Map<String, Object>> countByCategory() {
        return sysSensitiveWordMapper.countByCategory();
    }
}
