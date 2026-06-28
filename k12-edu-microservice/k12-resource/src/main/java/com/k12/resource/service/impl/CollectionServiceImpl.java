package com.k12.resource.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.k12.common.constant.ResourceTypeConstants;
import com.k12.common.dto.CollectItemVO;
import com.k12.common.dto.CollectQueryDTO;
import com.k12.common.dto.CollectStatsVO;
import com.k12.common.entity.Collection;
import com.k12.common.entity.EduResource;
import com.k12.common.entity.PrimaryChineseResource;
import com.k12.resource.mapper.CollectionMapper;
import com.k12.resource.mapper.EduResourceMapper;
import com.k12.resource.mapper.PrimaryChineseResourceMapper;
import com.k12.resource.service.CollectionService;
import com.k12.resource.util.StageKeyHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 收藏服务实现
 */
@Service
@SuppressWarnings("null")
public class CollectionServiceImpl extends ServiceImpl<CollectionMapper, Collection> implements CollectionService {

    private final CollectionMapper collectionMapper;
    private final PrimaryChineseResourceMapper primaryChineseResourceMapper;
    private final EduResourceMapper eduResourceMapper;
    public CollectionServiceImpl(CollectionMapper collectionMapper, PrimaryChineseResourceMapper primaryChineseResourceMapper, EduResourceMapper eduResourceMapper) {
        this.collectionMapper = collectionMapper;
        this.primaryChineseResourceMapper = primaryChineseResourceMapper;
        this.eduResourceMapper = eduResourceMapper;
    }


    @Override
    public List<Collection> getUserCollections(Long userId) {
        QueryWrapper<Collection> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        wrapper.orderByDesc("create_time");
        return list(wrapper);
    }

    @Override
    public void collect(Long userId, Long resourceId, String resourceType) {
        collectWithSnapshot(userId, resourceId, resourceType, Map.of());
    }

    @Override
    public void collectWithSnapshot(Long userId, Long resourceId, String resourceType, Map<String, Object> snapshot) {
        String type = ResourceTypeConstants.normalize(resourceType);
        if (isCollected(userId, resourceId, type)) {
            return;
        }
        Collection collection = new Collection();
        collection.setUserId(userId);
        collection.setResourceId(resourceId);
        collection.setResourceType(type);
        applySnapshotFromRequest(collection, snapshot);
        if (!StringUtils.hasText(collection.getTitle())) {
            fillSnapshotFromResource(collection, resourceId, type);
        }
        save(collection);
    }

    @Override
    public void uncollect(Long userId, Long resourceId, String resourceType) {
        String type = ResourceTypeConstants.normalize(resourceType);
        QueryWrapper<Collection> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        wrapper.eq("resource_id", resourceId);
        wrapper.eq("resource_type", type);
        remove(wrapper);
    }

    @Override
    public boolean isCollected(Long userId, Long resourceId, String resourceType) {
        String type = ResourceTypeConstants.normalize(resourceType);
        QueryWrapper<Collection> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        wrapper.eq("resource_id", resourceId);
        wrapper.eq("resource_type", type);
        return count(wrapper) > 0;
    }

    @Override
    public Map<String, Object> listByPage(Long userId, CollectQueryDTO dto) {
        int current = dto.getCurrent() != null && dto.getCurrent() > 0 ? dto.getCurrent() : 1;
        int size = dto.getSize() != null && dto.getSize() > 0 ? dto.getSize() : 20;
        Page<CollectItemVO> page = new Page<>(current, size);
        IPage<CollectItemVO> result = collectionMapper.selectCollectPage(
                page,
                userId,
                dto.getStageKey(),
                dto.getSubject(),
                dto.getTeachingType());
        Map<String, Object> map = new HashMap<>();
        map.put("records", result.getRecords());
        map.put("total", result.getTotal());
        map.put("current", result.getCurrent());
        map.put("size", result.getSize());
        map.put("pages", result.getPages());
        return map;
    }

    @Override
    public CollectStatsVO getStats(Long userId) {
        CollectStatsVO vo = new CollectStatsVO();
        vo.setTotal(collectionMapper.countByUser(userId));
        vo.setPrimaryCount(0L);
        vo.setJuniorCount(0L);
        vo.setSeniorCount(0L);
        vo.setArtCount(0L);
        vo.setDanceCount(0L);
        for (Map<String, Object> row : collectionMapper.countGroupByStageKey(userId)) {
            String sk = String.valueOf(row.get("sk"));
            long cnt = ((Number) row.get("cnt")).longValue();
            switch (sk) {
                case "primary" -> vo.setPrimaryCount(cnt);
                case "junior" -> vo.setJuniorCount(cnt);
                case "senior" -> vo.setSeniorCount(cnt);
                case "art" -> vo.setArtCount(cnt);
                case "dance" -> vo.setDanceCount(cnt);
                default -> { }
            }
        }
        return vo;
    }

    private void applySnapshotFromRequest(Collection collection, Map<String, Object> snapshot) {
        if (snapshot == null || snapshot.isEmpty()) {
            return;
        }
        setStr(collection::setTitle, snapshot.get("title"));
        setStr(collection::setStage, snapshot.get("stage"));
        setStr(collection::setStageKey, snapshot.get("stageKey"));
        setStr(collection::setSubject, snapshot.get("subject"));
        setStr(collection::setSubjectKey, snapshot.get("subjectKey"));
        setStr(collection::setModule, snapshot.get("module"));
        setStr(collection::setTeachingType, snapshot.get("teachingType"));
        if (snapshot.get("type") != null && !StringUtils.hasText(collection.getTeachingType())) {
            setStr(collection::setTeachingType, snapshot.get("type"));
        }
        setStr(collection::setGradeName, snapshot.get("gradeName"));
        setStr(collection::setFileExt, snapshot.get("fileExt"));
        setStr(collection::setOssUrl, snapshot.get("ossUrl"));
        if (!StringUtils.hasText(collection.getStageKey()) && StringUtils.hasText(collection.getStage())) {
            collection.setStageKey(StageKeyHelper.toStageKey(collection.getStage()));
        }
    }

    private void setStr(java.util.function.Consumer<String> setter, Object value) {
        if (value != null) {
            String s = String.valueOf(value).trim();
            if (!s.isEmpty() && !"null".equals(s)) {
                setter.accept(s);
            }
        }
    }

    private void fillSnapshotFromResource(Collection collection, Long resourceId, String type) {
        if (ResourceTypeConstants.PRIMARY_CHINESE.equals(type)) {
            PrimaryChineseResource r = primaryChineseResourceMapper.selectById(resourceId);
            if (r == null) {
                return;
            }
            collection.setTitle(r.getTitle());
            collection.setStage(r.getStage());
            collection.setStageKey(StageKeyHelper.toStageKey(r.getStage()));
            collection.setSubject(r.getSubject());
            collection.setModule(r.getModule());
            collection.setTeachingType(r.getType());
            collection.setGradeName(r.getGradeName());
            collection.setFileExt(r.getFileExt());
            collection.setOssUrl(r.getOssUrl());
            return;
        }
        EduResource r = eduResourceMapper.selectById(resourceId);
        if (r == null) {
            return;
        }
        collection.setTitle(r.getTitle());
        collection.setFileExt(r.getFileExt());
        collection.setOssUrl(r.getOssUrl());
        collection.setGradeName(r.getGradeName());
    }
}
