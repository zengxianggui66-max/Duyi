package com.k12.resource.service;

import com.k12.common.dto.UnitDocQueryDTO;
import com.k12.common.entity.UnitDoc;

import java.util.List;
import java.util.Map;

/**
 * 单元文档Service接口
 */
public interface UnitDocService {
    
    /**
     * 创建单元文档记录
     */
    UnitDoc create(UnitDoc unitDoc);
    
    /**
     * 根据ID查询
     */
    UnitDoc getById(Integer id);
    
    /**
     * 根据单元名称查询
     */
    List<UnitDoc> getByUnitName(String unitName);
    
    /**
     * 分页查询
     */
    Map<String, Object> listByPage(UnitDocQueryDTO query);
    
    /**
     * 根据条件查询所有
     */
    List<UnitDoc> listAll(UnitDocQueryDTO query);
    
    /**
     * 更新单元文档
     */
    boolean update(UnitDoc unitDoc);
    
    /**
     * 删除单元文档（逻辑删除）
     */
    boolean deleteById(Integer id);
    
    /**
     * 根据文件名模糊查询
     */
    List<UnitDoc> searchByFilename(String filename);
    
    /**
     * 根据文件类型查询
     */
    List<UnitDoc> getByFileType(String fileType);
    
    /**
     * 根据文件大小范围查询
     */
    List<UnitDoc> getBySizeRange(Integer minKb, Integer maxKb);
    
    /**
     * 更新OSS URL
     */
    boolean updateOssUrl(Integer id, String ossUrl);
    
    /**
     * 批量更新OSS URL
     */
    int batchUpdateOssUrl(List<UnitDoc> unitDocs);
    
    /**
     * 获取统计信息
     */
    Map<String, Object> getStatistics();
    
    /**
     * 获取所有单元名称（去重）
     */
    List<String> getAllUnitNames();
}
