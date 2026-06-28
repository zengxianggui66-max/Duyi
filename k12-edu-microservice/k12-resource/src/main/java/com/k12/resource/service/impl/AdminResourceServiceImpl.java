package com.k12.resource.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.k12.common.BusinessException;
import com.k12.common.constant.ResourceStatusConstants;
import com.k12.common.dto.AdminDataScopeVO;
import com.k12.common.dto.ResourceQueryDTO;
import com.k12.common.entity.Resource;
import com.k12.common.entity.ResourceAudit;
import com.k12.common.service.AdminPermissionService;
import com.k12.resource.mapper.CollectionMapper;
import com.k12.resource.mapper.ResourceAuditMapper;
import com.k12.resource.mapper.ResourceMapper;
import com.k12.resource.service.AdminResourceService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@SuppressWarnings("null")
public class AdminResourceServiceImpl implements AdminResourceService {

    private final ResourceMapper resourceMapper;
    private final CollectionMapper collectionMapper;
    private final ResourceAuditMapper resourceAuditMapper;
    private final AdminPermissionService adminPermissionService;
    public AdminResourceServiceImpl(ResourceMapper resourceMapper, CollectionMapper collectionMapper, ResourceAuditMapper resourceAuditMapper, AdminPermissionService adminPermissionService) {
        this.resourceMapper = resourceMapper;
        this.collectionMapper = collectionMapper;
        this.resourceAuditMapper = resourceAuditMapper;
        this.adminPermissionService = adminPermissionService;
    }


    // ========== 资源管理 ==========

    @Override
    public Page<Resource> listResources(ResourceQueryDTO query, Long userId) {
        LambdaQueryWrapper<Resource> wrapper = new LambdaQueryWrapper<>();
        
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.and(w -> w
                .like(Resource::getTitle, query.getKeyword())
                .or().like(Resource::getTags, query.getKeyword())
            );
        }
        
        if (StringUtils.hasText(query.getGradeLevel())) {
            wrapper.eq(Resource::getGradeLevel, query.getGradeLevel());
        }
        if (StringUtils.hasText(query.getSubject())) {
            wrapper.eq(Resource::getSubject, query.getSubject());
        }
        if (StringUtils.hasText(query.getResourceType())) {
            wrapper.eq(Resource::getResourceType, query.getResourceType());
        }
        if (query.getCategoryId() != null) {
            wrapper.eq(Resource::getCategoryId, query.getCategoryId());
        }
        
        if (query.getStatus() != null) {
            wrapper.eq(Resource::getStatus, query.getStatus());
        }

        applyDataScope(wrapper, userId);
        
        wrapper.orderByDesc(Resource::getCreateTime);
        
        Page<Resource> page = new Page<>(query.getCurrent(), query.getSize());
        return resourceMapper.selectPage(page, wrapper);
    }

    @Override
    @Transactional
    public void createResource(Resource resource) {
        resource.setStatus(0); // 待审核
        resource.setDownloadCount(0);
        resource.setViewCount(0);
        resource.setCollectCount(0);
        resourceMapper.insert(resource);
    }

    @Override
    @Transactional
    public void updateResource(Resource resource) {
        Resource existing = resourceMapper.selectById(resource.getId());
        if (existing == null) {
            throw new BusinessException("资源不存在");
        }
        // 如果修改了文件，更新文件信息
        if (resource.getFileUrl() != null) {
            existing.setFileUrl(resource.getFileUrl());
            existing.setFileSize(resource.getFileSize());
            existing.setFileFormat(resource.getFileFormat());
        }
        existing.setTitle(resource.getTitle());
        existing.setDescription(resource.getDescription());
        existing.setCoverUrl(resource.getCoverUrl());
        existing.setGradeLevel(resource.getGradeLevel());
        existing.setSubject(resource.getSubject());
        existing.setGrade(resource.getGrade());
        existing.setVersion(resource.getVersion());
        existing.setResourceType(resource.getResourceType());
        existing.setExamType(resource.getExamType());
        existing.setCategoryId(resource.getCategoryId());
        existing.setTags(resource.getTags());
        existing.setIsFree(resource.getIsFree());
        
        resourceMapper.updateById(existing);
    }

    @Override
    @Transactional
    public void deleteResource(Long id) {
        resourceMapper.deleteById(id);
    }

    // ========== 资源审核 ==========

    @Override
    public Page<Resource> listPendingResources(ResourceQueryDTO query, Long userId) {
        LambdaQueryWrapper<Resource> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Resource::getStatus, 0);
        
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.like(Resource::getTitle, query.getKeyword());
        }

        applyDataScope(wrapper, userId);
        
        wrapper.orderByDesc(Resource::getCreateTime);
        
        Page<Resource> page = new Page<>(query.getCurrent(), query.getSize());
        return resourceMapper.selectPage(page, wrapper);
    }

    @Override
    @Transactional
    public void auditResource(Long resourceId, Integer status, String reason, Long auditorId, String auditorName) {
        if (auditorId == null) {
            throw new BusinessException(401, "未登录或Token已过期");
        }
        if (status != null && status == ResourceStatusConstants.PUBLISHED) {
            if (!adminPermissionService.hasPermission(auditorId, "admin:audit:approve")) {
                throw new BusinessException(403, "无审核通过权限");
            }
        } else if (status != null && status == ResourceStatusConstants.REJECTED) {
            if (!adminPermissionService.hasPermission(auditorId, "admin:audit:reject")) {
                throw new BusinessException(403, "无审核驳回权限");
            }
        } else {
            throw new BusinessException(400, "无效的审核状态");
        }
        Resource resource = resourceMapper.selectById(resourceId);
        if (resource == null) {
            throw new BusinessException("资源不存在");
        }
        
        // 更新资源状态
        resource.setStatus(status);
        resourceMapper.updateById(resource);
        
        // 记录审核历史
        ResourceAudit audit = new ResourceAudit();
        audit.setResourceId(resourceId);
        audit.setStatus(status);
        audit.setReason(reason);
        audit.setAuditorId(auditorId);
        audit.setAuditorName(auditorName);
        resourceAuditMapper.insert(audit);
    }

    // ========== 收藏管理 ==========

    @Override
    public List<com.k12.common.entity.Collection> getUserCollections(Long userId) {
        return collectionMapper.selectList(
            new LambdaQueryWrapper<com.k12.common.entity.Collection>()
                .eq(com.k12.common.entity.Collection::getUserId, userId)
        );
    }

    @Override
    @Transactional
    public void collect(Long userId, Long resourceId) {
        // 检查是否已收藏
        LambdaQueryWrapper<com.k12.common.entity.Collection> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(com.k12.common.entity.Collection::getUserId, userId)
                .eq(com.k12.common.entity.Collection::getResourceId, resourceId)
                .eq(com.k12.common.entity.Collection::getResourceType, com.k12.common.constant.ResourceTypeConstants.RESOURCE);
        if (collectionMapper.selectCount(wrapper) > 0) {
            return; // 已收藏
        }
        
        com.k12.common.entity.Collection collection = new com.k12.common.entity.Collection();
        collection.setUserId(userId);
        collection.setResourceId(resourceId);
        collection.setResourceType(com.k12.common.constant.ResourceTypeConstants.RESOURCE);
        collectionMapper.insert(collection);
        
        // 更新资源的收藏数
        Resource resource = resourceMapper.selectById(resourceId);
        if (resource != null) {
            resource.setCollectCount(resource.getCollectCount() + 1);
            resourceMapper.updateById(resource);
        }
    }

    @Override
    @Transactional
    public void uncollect(Long userId, Long resourceId) {
        LambdaQueryWrapper<com.k12.common.entity.Collection> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(com.k12.common.entity.Collection::getUserId, userId)
                .eq(com.k12.common.entity.Collection::getResourceId, resourceId)
                .eq(com.k12.common.entity.Collection::getResourceType, com.k12.common.constant.ResourceTypeConstants.RESOURCE);
        collectionMapper.delete(wrapper);
        
        // 更新资源的收藏数
        Resource resource = resourceMapper.selectById(resourceId);
        if (resource != null && resource.getCollectCount() > 0) {
            resource.setCollectCount(resource.getCollectCount() - 1);
            resourceMapper.updateById(resource);
        }
    }

    @Override
    public boolean checkCollected(Long userId, Long resourceId) {
        LambdaQueryWrapper<com.k12.common.entity.Collection> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(com.k12.common.entity.Collection::getUserId, userId)
                .eq(com.k12.common.entity.Collection::getResourceId, resourceId)
                .eq(com.k12.common.entity.Collection::getResourceType, com.k12.common.constant.ResourceTypeConstants.RESOURCE);
        return collectionMapper.selectCount(wrapper) > 0;
    }

    // ========== 统计 ==========

    @Override
    public Map<String, Object> getResourceStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // 资源总数
        long totalResources = resourceMapper.selectCount(null);
        stats.put("totalResources", totalResources);
        
        // 待审核
        long pendingResources = resourceMapper.selectCount(
            new LambdaQueryWrapper<Resource>().eq(Resource::getStatus, 0)
        );
        stats.put("pendingResources", pendingResources);
        
        // 已通过
        long approvedResources = resourceMapper.selectCount(
            new LambdaQueryWrapper<Resource>().eq(Resource::getStatus, 1)
        );
        stats.put("approvedResources", approvedResources);
        
        // 总下载量
        List<Resource> allResources = resourceMapper.selectList(null);
        int totalDownloads = allResources.stream().mapToInt(Resource::getDownloadCount).sum();
        stats.put("totalDownloads", totalDownloads);
        
        // 总收藏数
        int totalCollects = allResources.stream().mapToInt(Resource::getCollectCount).sum();
        stats.put("totalCollects", totalCollects);
        
        // 按学科统计
        Map<String, Long> bySubject = new HashMap<>();
        for (Resource r : allResources) {
            String subject = r.getSubject();
            if (subject != null) {
                bySubject.put(subject, bySubject.getOrDefault(subject, 0L) + 1);
            }
        }
        stats.put("bySubject", bySubject);
        
        // 按类型统计
        Map<String, Long> byType = new HashMap<>();
        for (Resource r : allResources) {
            String type = r.getResourceType();
            if (type != null) {
                byType.put(type, byType.getOrDefault(type, 0L) + 1);
            }
        }
        stats.put("byType", byType);
        
        // 下载量 TOP 10
        List<Resource> topDownloads = resourceMapper.selectList(
            new LambdaQueryWrapper<Resource>()
                .eq(Resource::getStatus, 1)
                .orderByDesc(Resource::getDownloadCount)
                .last("LIMIT 10")
        );
        stats.put("topDownloads", topDownloads);
        
        return stats;
    }

    @Override
    public Map<String, Object> getUserStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // 用户总数（需要从 user 服务获取，这里先返回预估数据）
        stats.put("totalUsers", 0);
        
        // 收藏总数
        long totalCollections = collectionMapper.selectCount(null);
        stats.put("totalCollections", totalCollections);
        
        // 活跃用户数（今天有操作的用户，这里简化为收藏用户数）
        stats.put("activeUsers", 0);
        
        return stats;
    }

    // ========== 下载/浏览记录 ==========

    @Override
    public void recordDownload(Long resourceId) {
        Resource resource = resourceMapper.selectById(resourceId);
        if (resource != null) {
            resource.setDownloadCount(resource.getDownloadCount() + 1);
            resourceMapper.updateById(resource);
        }
    }

    @Override
    public void recordView(Long resourceId) {
        Resource resource = resourceMapper.selectById(resourceId);
        if (resource != null) {
            resource.setViewCount(resource.getViewCount() + 1);
            resourceMapper.updateById(resource);
        }
    }

    private void applyDataScope(LambdaQueryWrapper<Resource> wrapper, Long userId) {
        if (userId == null) {
            return;
        }
        AdminDataScopeVO scope = adminPermissionService.resolveDataScope(userId);
        if ("ALL".equals(scope.getScopeType())) {
            return;
        }
        if ("SELF".equals(scope.getScopeType())) {
            wrapper.eq(Resource::getAuthorId, userId);
            return;
        }
        if ("STAGE_SUBJECT".equals(scope.getScopeType())) {
            if (!scope.getStages().isEmpty()) {
                wrapper.in(Resource::getGradeLevel, scope.getStages());
            }
            if (!scope.getSubjects().isEmpty()) {
                wrapper.in(Resource::getSubject, scope.getSubjects());
            }
        }
    }
}
