package com.k12.resource.service;

import com.k12.common.PageResult;
import com.k12.common.dto.ResourceQueryDTO;
import com.k12.common.entity.Resource;
import java.util.List;
import java.util.Map;

public interface ResourceService {
    PageResult<Resource> listResources(ResourceQueryDTO query);
    List<Resource> getHotResources(String gradeLevel);
    List<Resource> getRecommendResources();
    Resource getDetail(Long id);
    Map<String, Object> getStats();
    
    /**
     * 增加下载次数
     */
    void incrementDownloadCount(Long id);
    
    /**
     * 获取下载URL
     */
    String getDownloadUrl(Long id);
    
    /**
     * 创建资源（用于文件上传后创建资源记录）
     */
    Long create(Resource resource);
}
