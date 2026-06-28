package com.k12.resource.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.k12.common.entity.CompetitionResource;
import com.k12.common.entity.CultureResource;
import com.k12.common.entity.EduResource;
import com.k12.common.entity.EduResourceDimension;
import com.k12.common.entity.ResourceMain;
import com.k12.common.entity.TopicResource;
import com.k12.resource.adapter.CompetitionSourceAdapter;
import com.k12.resource.adapter.CultureSourceAdapter;
import com.k12.resource.adapter.EduResourceSourceAdapter;
import com.k12.resource.adapter.TopicSourceAdapter;
import com.k12.resource.mapper.CompetitionResourceMapper;
import com.k12.resource.mapper.CultureResourceMapper;
import com.k12.resource.mapper.EduResourceDimensionMapper;
import com.k12.resource.mapper.EduResourceMapper;
import com.k12.resource.mapper.ResourceMainMapper;
import com.k12.resource.mapper.TopicResourceMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Phase 3K-β：各 COMPAT/PRIMARY 源写入后 upsert resource_main 映射行
 */
@Slf4j
@Service
public class ResourceMainUpsertService {

    private final EduResourceMapper eduResourceMapper;
    private final EduResourceDimensionMapper eduResourceDimensionMapper;
    private final TopicResourceMapper topicResourceMapper;
    private final CultureResourceMapper cultureResourceMapper;
    private final CompetitionResourceMapper competitionResourceMapper;
    private final ResourceMainMapper resourceMainMapper;

    public ResourceMainUpsertService(EduResourceMapper eduResourceMapper,
                                     EduResourceDimensionMapper eduResourceDimensionMapper,
                                     TopicResourceMapper topicResourceMapper,
                                     CultureResourceMapper cultureResourceMapper,
                                     CompetitionResourceMapper competitionResourceMapper,
                                     ResourceMainMapper resourceMainMapper) {
        this.eduResourceMapper = eduResourceMapper;
        this.eduResourceDimensionMapper = eduResourceDimensionMapper;
        this.topicResourceMapper = topicResourceMapper;
        this.cultureResourceMapper = cultureResourceMapper;
        this.competitionResourceMapper = competitionResourceMapper;
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
        upsertRow(
                EduResourceSourceAdapter.SOURCE_TYPE,
                "edu_resource",
                eduResourceId,
                resource.getTitle(),
                dim != null && dim.getStageId() != null ? String.valueOf(dim.getStageId()) : null,
                dim != null && dim.getSubjectId() != null ? String.valueOf(dim.getSubjectId()) : null,
                mapEduAuditStatus(status),
                mapEduPublishStatus(status),
                status != null ? status : 0,
                resource.getUploaderId(),
                resource.getUploadTime(),
                resource.getIsDeleted() != null ? resource.getIsDeleted() : 0
        );
    }

    @Transactional(rollbackFor = Exception.class)
    public void upsertFromTopicResource(Long topicResourceId) {
        if (topicResourceId == null) {
            return;
        }
        TopicResource resource = topicResourceMapper.selectById(topicResourceId);
        if (resource == null) {
            log.debug("resource_main upsert skip: topic_resource {} not found", topicResourceId);
            return;
        }
        StatusTriple triple = mapCompatLegacyStatus(resource.getStatus());
        upsertRow(
                TopicSourceAdapter.SOURCE_TYPE,
                "topic_resource",
                topicResourceId,
                resource.getTitle(),
                resource.getGradeStage(),
                resource.getSubject(),
                triple.auditStatus(),
                triple.publishStatus(),
                triple.legacyStatus(),
                null,
                resource.getCreateTime(),
                resource.getDeleted() != null ? resource.getDeleted() : 0
        );
    }

    @Transactional(rollbackFor = Exception.class)
    public void upsertFromCultureResource(Long cultureResourceId) {
        if (cultureResourceId == null) {
            return;
        }
        CultureResource resource = cultureResourceMapper.selectById(cultureResourceId);
        if (resource == null) {
            log.debug("resource_main upsert skip: culture_resource {} not found", cultureResourceId);
            return;
        }
        StatusTriple triple = mapCompatLegacyStatus(resource.getStatus());
        upsertRow(
                CultureSourceAdapter.SOURCE_TYPE,
                "culture_resource",
                cultureResourceId,
                resource.getTitle(),
                null,
                null,
                triple.auditStatus(),
                triple.publishStatus(),
                triple.legacyStatus(),
                null,
                resource.getCreateTime(),
                resource.getDeleted() != null ? resource.getDeleted() : 0
        );
    }

    @Transactional(rollbackFor = Exception.class)
    public void upsertFromCompetitionResource(Long competitionResourceId) {
        if (competitionResourceId == null) {
            return;
        }
        CompetitionResource resource = competitionResourceMapper.selectById(competitionResourceId);
        if (resource == null) {
            log.debug("resource_main upsert skip: competition_resource {} not found", competitionResourceId);
            return;
        }
        StatusTriple triple = mapCompatLegacyStatus(resource.getStatus());
        upsertRow(
                CompetitionSourceAdapter.SOURCE_TYPE,
                "competition_resource",
                competitionResourceId,
                resource.getTitle(),
                resource.getGradeStage(),
                resource.getSubject(),
                triple.auditStatus(),
                triple.publishStatus(),
                triple.legacyStatus(),
                null,
                resource.getCreateTime(),
                resource.getDeleted() != null ? resource.getDeleted() : 0
        );
    }

    private void upsertRow(String sourceType,
                           String sourceTable,
                           Long sourceId,
                           String title,
                           String stageCode,
                           String subjectCode,
                           Integer auditStatus,
                           Integer publishStatus,
                           Integer legacyStatus,
                           Long uploaderId,
                           LocalDateTime uploadTime,
                           Integer isDeleted) {
        Long globalId = resourceMainMapper.findGlobalId(sourceType, sourceId);
        ResourceMain row = globalId != null ? resourceMainMapper.selectById(globalId) : new ResourceMain();
        if (row == null) {
            row = new ResourceMain();
            globalId = null;
        }

        row.setSourceType(sourceType);
        row.setSourceTable(sourceTable);
        row.setSourceId(sourceId);
        row.setTitle(title);
        row.setStageCode(stageCode);
        row.setSubjectCode(subjectCode);
        row.setAuditStatus(auditStatus);
        row.setPublishStatus(publishStatus);
        row.setLegacyStatus(legacyStatus);
        row.setUploaderId(uploaderId);
        row.setUploadTime(uploadTime);
        row.setIsDeleted(isDeleted);
        row.setUpdateTime(LocalDateTime.now());

        if (globalId == null) {
            row.setCreateTime(LocalDateTime.now());
            resourceMainMapper.insert(row);
            log.debug("resource_main upsert insert {} {} -> global {}", sourceType, sourceId, row.getId());
        } else {
            row.setId(globalId);
            resourceMainMapper.updateById(row);
            log.debug("resource_main upsert update {} {} -> global {}", sourceType, sourceId, globalId);
        }
    }

    /** topic/culture/competition：与 sql/86 回填一致 */
    static StatusTriple mapCompatLegacyStatus(Integer status) {
        int legacy = status != null ? status : 0;
        int normalized = status != null && status == 1 ? 1 : 0;
        return new StatusTriple(normalized, normalized, legacy);
    }

    /** 与 sql/86 edu_resource 回填逻辑一致 */
    static Integer mapEduAuditStatus(Integer status) {
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

    static Integer mapEduPublishStatus(Integer status) {
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

    record StatusTriple(Integer auditStatus, Integer publishStatus, Integer legacyStatus) {
    }
}
