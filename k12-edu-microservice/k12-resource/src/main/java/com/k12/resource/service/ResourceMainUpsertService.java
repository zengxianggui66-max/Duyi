package com.k12.resource.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.k12.common.entity.EduResource;
import com.k12.common.entity.EduResourceDimension;
import com.k12.common.entity.ResourceMain;
import com.k12.resource.adapter.EduResourceSourceAdapter;
import com.k12.resource.mapper.EduResourceDimensionMapper;
import com.k12.resource.mapper.EduResourceMapper;
import com.k12.resource.mapper.ResourceMainMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Phase 3K-β：edu_resource 写入后 upsert resource_main 映射行
 */
@Slf4j
@Service
public class ResourceMainUpsertService {

    private static final String SOURCE_TABLE = "edu_resource";

    private final EduResourceMapper eduResourceMapper;
    private final EduResourceDimensionMapper eduResourceDimensionMapper;
    private final ResourceMainMapper resourceMainMapper;

    public ResourceMainUpsertService(EduResourceMapper eduResourceMapper,
                                     EduResourceDimensionMapper eduResourceDimensionMapper,
                                     ResourceMainMapper resourceMainMapper) {
        this.eduResourceMapper = eduResourceMapper;
        this.eduResourceDimensionMapper = eduResourceDimensionMapper;
        this.resourceMainMapper = resourceMainMapper;
    }

    @Transactional(rollbackFor = Exception.class)
    public void upsertFromEduResource(Long eduResourceId) {
        if (eduResourceId == null) {
            return;
        }
        EduResource resource = eduResourceMapper.selectById(eduResourceId);
        if (resource == null) {
            log.debug("resource_main upsert skip: edu_resource {} not found", eduResourceId);
            return;
        }

        EduResourceDimension dim = eduResourceDimensionMapper.selectOne(
                new LambdaQueryWrapper<EduResourceDimension>()
                        .eq(EduResourceDimension::getResourceId, eduResourceId)
                        .last("LIMIT 1"));

        Integer status = resource.getStatus();
        Integer auditStatus = mapAuditStatus(status);
        Integer publishStatus = mapPublishStatus(status);
        Integer legacyStatus = status != null ? status : 0;

        Long globalId = resourceMainMapper.findGlobalId(EduResourceSourceAdapter.SOURCE_TYPE, eduResourceId);
        ResourceMain row = globalId != null ? resourceMainMapper.selectById(globalId) : new ResourceMain();
        if (row == null) {
            row = new ResourceMain();
            globalId = null;
        }

        row.setSourceType(EduResourceSourceAdapter.SOURCE_TYPE);
        row.setSourceTable(SOURCE_TABLE);
        row.setSourceId(eduResourceId);
        row.setTitle(resource.getTitle());
        row.setStageCode(dim != null && dim.getStageId() != null ? String.valueOf(dim.getStageId()) : null);
        row.setSubjectCode(dim != null && dim.getSubjectId() != null ? String.valueOf(dim.getSubjectId()) : null);
        row.setAuditStatus(auditStatus);
        row.setPublishStatus(publishStatus);
        row.setLegacyStatus(legacyStatus);
        row.setUploaderId(resource.getUploaderId());
        row.setUploadTime(resource.getUploadTime());
        row.setIsDeleted(resource.getIsDeleted() != null ? resource.getIsDeleted() : 0);
        row.setUpdateTime(LocalDateTime.now());

        if (globalId == null) {
            row.setCreateTime(LocalDateTime.now());
            resourceMainMapper.insert(row);
            log.debug("resource_main upsert insert edu_resource {} -> global {}", eduResourceId, row.getId());
        } else {
            row.setId(globalId);
            resourceMainMapper.updateById(row);
            log.debug("resource_main upsert update edu_resource {} -> global {}", eduResourceId, globalId);
        }
    }

    /** 与 sql/86_phase3b_resource_main_chain.sql edu_resource 回填逻辑一致 */
    static Integer mapAuditStatus(Integer status) {
        if (status == null) {
            return 0;
        }
        if (status == 2) {
            return 2;
        }
        if (status == -1 || status == 0) {
            return status == -1 ? -1 : 0;
        }
        if (status == 1 || status == 3 || status == 4) {
            return 1;
        }
        return 0;
    }

    static Integer mapPublishStatus(Integer status) {
        if (status == null) {
            return 0;
        }
        return switch (status) {
            case 1 -> 1;
            case 3 -> 2;
            case 4 -> 4;
            default -> 0;
        };
    }
}
