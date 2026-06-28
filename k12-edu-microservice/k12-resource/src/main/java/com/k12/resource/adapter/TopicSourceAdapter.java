package com.k12.resource.adapter;

import com.k12.common.BusinessException;
import com.k12.common.dto.AdminResourceUpdateDTO;
import com.k12.common.entity.TopicResource;
import com.k12.resource.mapper.TopicResourceMapper;
import com.k12.resource.service.TopicZoneService;
import org.springframework.stereotype.Component;

@Component
public class TopicSourceAdapter extends AbstractDedicatedZoneSourceAdapter {

    public static final String SOURCE_TYPE = "topic_resource";

    private final TopicZoneService topicZoneService;
    private final TopicResourceMapper topicResourceMapper;

    public TopicSourceAdapter(TopicZoneService topicZoneService, TopicResourceMapper topicResourceMapper) {
        this.topicZoneService = topicZoneService;
        this.topicResourceMapper = topicResourceMapper;
    }

    @Override
    public String sourceType() {
        return SOURCE_TYPE;
    }

    @Override
    protected void doIncrementView(Long sourceId) {
        topicZoneService.incrementResourceView(sourceId);
    }

    @Override
    protected void doIncrementDownload(Long sourceId) {
        topicZoneService.incrementResourceDownload(sourceId);
    }

    @Override
    protected void doApplyGenericUpdate(Long sourceId, AdminResourceUpdateDTO dto) {
        TopicResource entity = requireEntity(sourceId);
        if (dto.getTitle() != null) {
            entity.setTitle(dto.getTitle());
        }
        if (dto.getSort() != null) {
            entity.setSort(dto.getSort());
        }
        if (dto.getIsFree() != null) {
            entity.setIsFree(dto.getIsFree());
        }
        topicResourceMapper.updateById(entity);
    }

    @Override
    protected void doUpdateStatus(Long sourceId, int status) {
        TopicResource entity = requireEntity(sourceId);
        entity.setStatus(status);
        topicResourceMapper.updateById(entity);
    }

    @Override
    protected void doUpdateElite(Long sourceId, int isElite) {
        TopicResource entity = requireEntity(sourceId);
        entity.setIsElite(isElite);
        topicResourceMapper.updateById(entity);
    }

    private TopicResource requireEntity(Long sourceId) {
        TopicResource entity = topicResourceMapper.selectById(sourceId);
        if (entity == null) {
            throw new BusinessException(404, "专题资源不存在");
        }
        return entity;
    }
}
