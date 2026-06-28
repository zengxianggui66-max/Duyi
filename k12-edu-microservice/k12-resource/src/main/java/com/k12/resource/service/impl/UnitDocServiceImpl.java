package com.k12.resource.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.k12.common.dto.UnitDocQueryDTO;
import com.k12.common.entity.UnitDoc;
import com.k12.resource.mapper.UnitDocMapper;
import com.k12.resource.service.UnitDocService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 单元文档Service实现类
 */
@Slf4j
@Service
@SuppressWarnings("null")
public class UnitDocServiceImpl implements UnitDocService {
    
    private final UnitDocMapper unitDocMapper;
    public UnitDocServiceImpl(UnitDocMapper unitDocMapper) {
        this.unitDocMapper = unitDocMapper;
    }

    
    @Override
    public UnitDoc create(UnitDoc unitDoc) {
        // 设置默认值
        if (unitDoc.getOssBucket() == null) {
            unitDoc.setOssBucket("qier-duuyi");
        }
        if (unitDoc.getIsDeleted() == null) {
            unitDoc.setIsDeleted(0);
        }
        
        unitDocMapper.insert(unitDoc);
        log.info("创建单元文档成功，ID: {}", unitDoc.getId());
        return unitDoc;
    }
    
    @Override
    public UnitDoc getById(Integer id) {
        return unitDocMapper.selectById(id);
    }
    
    @Override
    public List<UnitDoc> getByUnitName(String unitName) {
        return unitDocMapper.findByUnitName(unitName);
    }
    
    @Override
    public Map<String, Object> listByPage(UnitDocQueryDTO query) {
        Map<String, Object> result = new HashMap<>();
        
        // 构建查询条件
        LambdaQueryWrapper<UnitDoc> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UnitDoc::getIsDeleted, 0);
        
        // 单元名称模糊查询
        if (StringUtils.isNotBlank(query.getUnitName())) {
            wrapper.like(UnitDoc::getUnitName, query.getUnitName());
        }
        
        // 文件名模糊查询
        if (StringUtils.isNotBlank(query.getOriginalFilename())) {
            wrapper.like(UnitDoc::getOriginalFilename, query.getOriginalFilename());
        }
        
        // 文件类型查询
        if (StringUtils.isNotBlank(query.getFileType())) {
            List<UnitDoc> allDocs = unitDocMapper.selectList(wrapper);
            // 在内存中过滤文件类型
            List<UnitDoc> filtered = allDocs.stream()
                    .filter(doc -> query.getFileType().equalsIgnoreCase(doc.getFileType()))
                    .collect(Collectors.toList());
            
            // 手动分页
            int total = filtered.size();
            int startIndex = (query.getCurrent() - 1) * query.getSize();
            int endIndex = Math.min(startIndex + query.getSize(), total);
            
            if (startIndex < total) {
                filtered = filtered.subList(startIndex, endIndex);
            } else {
                filtered = new ArrayList<>();
            }
            
            result.put("list", filtered);
            result.put("total", (long) total);
            result.put("current", query.getCurrent());
            result.put("size", query.getSize());
            return result;
        }
        
        // 排序
        if ("asc".equalsIgnoreCase(query.getSortOrder())) {
            wrapper.orderByAsc(UnitDoc::getUploadTime);
        } else {
            wrapper.orderByDesc(UnitDoc::getUploadTime);
        }
        
        // 分页查询
        Page<UnitDoc> page = new Page<>(query.getCurrent(), query.getSize());
        IPage<UnitDoc> pageResult = unitDocMapper.selectPage(page, wrapper);
        
        result.put("list", pageResult.getRecords());
        result.put("total", pageResult.getTotal());
        result.put("current", query.getCurrent());
        result.put("size", query.getSize());
        
        return result;
    }
    
    @Override
    public List<UnitDoc> listAll(UnitDocQueryDTO query) {
        LambdaQueryWrapper<UnitDoc> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UnitDoc::getIsDeleted, 0);
        
        if (StringUtils.isNotBlank(query.getUnitName())) {
            wrapper.like(UnitDoc::getUnitName, query.getUnitName());
        }
        
        wrapper.orderByDesc(UnitDoc::getUploadTime);
        
        return unitDocMapper.selectList(wrapper);
    }
    
    @Override
    public boolean update(UnitDoc unitDoc) {
        int rows = unitDocMapper.updateById(unitDoc);
        return rows > 0;
    }
    
    @Override
    public boolean deleteById(Integer id) {
        UnitDoc unitDoc = new UnitDoc();
        unitDoc.setId(id);
        unitDoc.setIsDeleted(1);
        int rows = unitDocMapper.updateById(unitDoc);
        log.info("删除单元文档，ID: {}, 结果: {}", id, rows > 0);
        return rows > 0;
    }
    
    @Override
    public List<UnitDoc> searchByFilename(String filename) {
        return unitDocMapper.findByFilenameLike(filename);
    }
    
    @Override
    public List<UnitDoc> getByFileType(String fileType) {
        String condition = getFileTypeCondition(fileType);
        return unitDocMapper.findByFileType(condition);
    }
    
    @Override
    public List<UnitDoc> getBySizeRange(Integer minKb, Integer maxKb) {
        return unitDocMapper.findBySizeRange(minKb, maxKb);
    }
    
    @Override
    public boolean updateOssUrl(Integer id, String ossUrl) {
        UnitDoc unitDoc = new UnitDoc();
        unitDoc.setId(id);
        unitDoc.setOssUrl(ossUrl);
        int rows = unitDocMapper.updateById(unitDoc);
        return rows > 0;
    }
    
    @Override
    public int batchUpdateOssUrl(List<UnitDoc> unitDocs) {
        return unitDocMapper.batchUpdateOssUrl(unitDocs);
    }
    
    @Override
    public Map<String, Object> getStatistics() {
        return unitDocMapper.getStatistics();
    }
    
    @Override
    public List<String> getAllUnitNames() {
        LambdaQueryWrapper<UnitDoc> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UnitDoc::getIsDeleted, 0)
               .select(UnitDoc::getUnitName)
               .groupBy(UnitDoc::getUnitName);
        
        List<UnitDoc> docs = unitDocMapper.selectList(wrapper);
        return docs.stream()
                .map(UnitDoc::getUnitName)
                .distinct()
                .collect(Collectors.toList());
    }
    
    /**
     * 根据文件类型获取SQL条件
     */
    private String getFileTypeCondition(String fileType) {
        String lowerType = fileType.toLowerCase();
        switch (lowerType) {
            case "doc":
                return "original_filename LIKE '%.doc' AND original_filename NOT LIKE '%.docx'";
            case "docx":
                return "original_filename LIKE '%.docx'";
            case "pdf":
                return "original_filename LIKE '%.pdf'";
            case "ppt":
                return "original_filename LIKE '%.ppt' AND original_filename NOT LIKE '%.pptx'";
            case "pptx":
                return "original_filename LIKE '%.pptx'";
            default:
                return "1=1";
        }
    }
}
