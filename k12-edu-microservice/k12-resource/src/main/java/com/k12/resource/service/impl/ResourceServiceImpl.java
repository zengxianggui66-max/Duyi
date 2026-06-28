package com.k12.resource.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.k12.common.BusinessException;
import com.k12.common.PageResult;
import com.k12.common.dto.ResourceQueryDTO;
import com.k12.common.entity.Resource;
import com.k12.resource.mapper.ResourceMapper;
import com.k12.resource.service.ResourceService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;

@Service
@SuppressWarnings("null")
public class ResourceServiceImpl implements ResourceService {

    private final ResourceMapper resourceMapper;
    public ResourceServiceImpl(ResourceMapper resourceMapper) {
        this.resourceMapper = resourceMapper;
    }


    @Override
    public PageResult<Resource> listResources(ResourceQueryDTO query) {
        LambdaQueryWrapper<Resource> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(query.getGradeLevel())) {
            wrapper.eq(Resource::getGradeLevel, query.getGradeLevel());
        }
        if (StringUtils.hasText(query.getSubject())) {
            wrapper.eq(Resource::getSubject, query.getSubject());
        }
        if (StringUtils.hasText(query.getGrade())) {
            wrapper.like(Resource::getGrade, query.getGrade());
        }
        if (StringUtils.hasText(query.getVersion())) {
            wrapper.eq(Resource::getVersion, query.getVersion());
        }
        if (StringUtils.hasText(query.getResourceType())) {
            wrapper.eq(Resource::getResourceType, query.getResourceType());
        }
        if (StringUtils.hasText(query.getExamType())) {
            wrapper.eq(Resource::getExamType, query.getExamType());
        }

        // 特色频道：竞赛专区（标签含「学科竞赛」或上传标记 competition）
        if ("jingsai".equals(query.getChannelType())) {
            wrapper.and(w -> w.like(Resource::getTags, "学科竞赛")
                    .or().like(Resource::getTags, "competition"));
        }

        // 特色频道：专题资源（标签含「专题资源」或上传标记 topic）
        if ("zhuanti".equals(query.getChannelType())) {
            wrapper.and(w -> w.like(Resource::getTags, "专题资源")
                    .or().like(Resource::getTags, "topic"));
        }

        if (query.getIsFree() != null) {
            wrapper.eq(Resource::getIsFree, query.getIsFree());
        }

        // 媒体类型筛选（video, audio, document, image）
        if (StringUtils.hasText(query.getMediaType())) {
            String mediaType = query.getMediaType();
            if ("video".equals(mediaType)) {
                wrapper.and(w -> w
                    .like(Resource::getContentType, "video")
                    .or().like(Resource::getContentType, "mp4")
                    .or().like(Resource::getContentType, "avi")
                    .or().like(Resource::getContentType, "mov")
                );
            } else if ("audio".equals(mediaType)) {
                wrapper.and(w -> w
                    .like(Resource::getContentType, "audio")
                    .or().like(Resource::getContentType, "mp3")
                    .or().like(Resource::getContentType, "wav")
                );
            } else if ("image".equals(mediaType)) {
                wrapper.and(w -> w
                    .like(Resource::getContentType, "image")
                    .or().like(Resource::getContentType, "jpg")
                    .or().like(Resource::getContentType, "png")
                );
            } else if ("document".equals(mediaType)) {
                wrapper.and(w -> w
                    .like(Resource::getContentType, "pdf")
                    .or().like(Resource::getContentType, "word")
                    .or().like(Resource::getContentType, "document")
                    .or().like(Resource::getContentType, "ppt")
                    .or().like(Resource::getContentType, "excel")
                );
            }
        }

        // 关键词搜索
        if (StringUtils.hasText(query.getKeyword())) {
            String[] keywords = query.getKeyword().split("[,，]");
            for (int i = 0; i < keywords.length; i++) {
                String kw = keywords[i].trim();
                if (!kw.isEmpty()) {
                    if (i == 0) {
                        wrapper.and(w -> w
                            .like(Resource::getTitle, kw)
                            .or().like(Resource::getTags, kw)
                            .or().like(Resource::getDescription, kw)
                        );
                    } else {
                        wrapper.or(w -> w
                            .like(Resource::getTitle, kw)
                            .or().like(Resource::getTags, kw)
                            .or().like(Resource::getDescription, kw)
                        );
                    }
                }
            }
        }
        wrapper.eq(Resource::getStatus, 1);

        // 多维度排序支持
        String sortField = query.getSortField();
        String sortOrder = query.getSortOrder();
        boolean isDesc = !"asc".equalsIgnoreCase(sortOrder);

        if ("downloadCount".equals(sortField)) {
            wrapper.orderBy(true, isDesc, Resource::getDownloadCount);
        } else if ("viewCount".equals(sortField)) {
            wrapper.orderBy(true, isDesc, Resource::getViewCount);
        } else if ("rating".equals(sortField)) {
            // 好评优先：先按评分，再按评分次数
            wrapper.orderBy(true, isDesc, Resource::getRating)
                   .orderBy(true, isDesc, Resource::getRatingCount);
        } else if ("collectCount".equals(sortField)) {
            wrapper.orderBy(true, isDesc, Resource::getCollectCount);
        } else {
            // 默认按创建时间
            wrapper.orderBy(true, isDesc, Resource::getCreateTime);
        }

        Page<Resource> page = new Page<>(query.getCurrent(), query.getSize());
        Page<Resource> result = resourceMapper.selectPage(page, wrapper);
        return new PageResult<>(result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize());
    }

    @Override
    public List<Resource> getHotResources(String gradeLevel) {
        LambdaQueryWrapper<Resource> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(gradeLevel) && !"all".equals(gradeLevel)) {
            wrapper.eq(Resource::getGradeLevel, gradeLevel);
        }
        wrapper.eq(Resource::getStatus, 1)
               .orderByDesc(Resource::getDownloadCount)
               .last("LIMIT 8");
        return resourceMapper.selectList(wrapper);
    }

    @Override
    public List<Resource> getRecommendResources() {
        LambdaQueryWrapper<Resource> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Resource::getStatus, 1)
               .orderByDesc(Resource::getCreateTime)
               .last("LIMIT 6");
        return resourceMapper.selectList(wrapper);
    }

    @Override
    public Resource getDetail(Long id) {
        Resource resource = resourceMapper.selectById(id);
        if (resource == null || resource.getStatus() == 0) {
            throw new BusinessException("资源不存在或已下架");
        }
        resource.setViewCount(resource.getViewCount() + 1);
        resourceMapper.updateById(resource);
        return resource;
    }

    @Override
    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalResources", resourceMapper.selectCount(
                new LambdaQueryWrapper<Resource>().eq(Resource::getStatus, 1)));
        stats.put("totalDownloads", resourceMapper.selectList(null).stream()
                .mapToInt(Resource::getDownloadCount).sum());
        stats.put("totalExams", resourceMapper.selectCount(
                new LambdaQueryWrapper<Resource>().eq(Resource::getResourceType, "exam").eq(Resource::getStatus, 1)));
        stats.put("totalCoursewares", resourceMapper.selectCount(
                new LambdaQueryWrapper<Resource>().eq(Resource::getResourceType, "courseware").eq(Resource::getStatus, 1)));
        return stats;
    }

    @Override
    public void incrementDownloadCount(Long id) {
        Resource resource = resourceMapper.selectById(id);
        if (resource != null) {
            resource.setDownloadCount(resource.getDownloadCount() + 1);
            resourceMapper.updateById(resource);
        }
    }

    @Override
    public String getDownloadUrl(Long id) {
        Resource resource = resourceMapper.selectById(id);
        if (resource == null || resource.getStatus() == 0) {
            throw new BusinessException("资源不存在或已下架");
        }
        return resource.getFileUrl();
    }
    
    @Override
    public Long create(Resource resource) {
        // 设置默认值
        if (resource.getStatus() == null) {
            resource.setStatus(1);
        }
        if (resource.getDownloadCount() == null) {
            resource.setDownloadCount(0);
        }
        if (resource.getViewCount() == null) {
            resource.setViewCount(0);
        }
        if (resource.getCollectCount() == null) {
            resource.setCollectCount(0);
        }
        if (resource.getRating() == null) {
            resource.setRating(java.math.BigDecimal.ZERO);
        }
        if (resource.getRatingCount() == null) {
            resource.setRatingCount(0);
        }
        resourceMapper.insert(resource);
        return resource.getId();
    }
}
