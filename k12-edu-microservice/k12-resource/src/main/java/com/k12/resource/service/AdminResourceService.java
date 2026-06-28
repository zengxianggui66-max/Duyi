package com.k12.resource.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.k12.common.dto.ResourceQueryDTO;
import com.k12.common.entity.Resource;

import java.util.List;
import java.util.Map;

public interface AdminResourceService {
    
    // ========== 资源管理 ==========
    Page<Resource> listResources(ResourceQueryDTO query, Long userId);
    
    void createResource(Resource resource);
    
    void updateResource(Resource resource);
    
    void deleteResource(Long id);
    
    // ========== 资源审核 ==========
    Page<Resource> listPendingResources(ResourceQueryDTO query, Long userId);
    
    void auditResource(Long resourceId, Integer status, String reason, Long auditorId, String auditorName);
    
    // ========== 收藏管理 ==========
    List<com.k12.common.entity.Collection> getUserCollections(Long userId);
    
    void collect(Long userId, Long resourceId);
    
    void uncollect(Long userId, Long resourceId);
    
    boolean checkCollected(Long userId, Long resourceId);
    
    // ========== 统计 ==========
    Map<String, Object> getResourceStats();
    
    Map<String, Object> getUserStats();
    
    // ========== 下载/浏览记录 ==========
    void recordDownload(Long resourceId);
    
    void recordView(Long resourceId);
}
