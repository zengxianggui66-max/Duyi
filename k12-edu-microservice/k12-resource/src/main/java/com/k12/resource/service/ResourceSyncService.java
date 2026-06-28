package com.k12.resource.service;

import com.k12.common.dto.ResourceFileDTO;
import com.k12.common.dto.ResourceWriteDTO;
import com.k12.common.entity.EduCatalogNode;
import com.k12.common.entity.EduResource;
import com.k12.common.entity.PrimaryChineseResource;
import com.k12.resource.mapper.EduCatalogNodeMapper;
import com.k12.resource.mapper.PrimaryChineseResourceMapper;
import com.k12.resource.util.ResourceStatusMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

/**
 * edu_resource → oss_primary_chinese_resource 同 ID 双写（M4）
 */
@Slf4j
@Service
public class ResourceSyncService {

    private final PrimaryChineseResourceMapper primaryChineseResourceMapper;
    private final EduCatalogNodeMapper eduCatalogNodeMapper;
    public ResourceSyncService(PrimaryChineseResourceMapper primaryChineseResourceMapper, EduCatalogNodeMapper eduCatalogNodeMapper) {
        this.primaryChineseResourceMapper = primaryChineseResourceMapper;
        this.eduCatalogNodeMapper = eduCatalogNodeMapper;
    }


    /**
     * 将宽表实体同步到 oss 宽表（同 id upsert）
     */
    public void syncToWideTable(PrimaryChineseResource resource) {
        if (resource == null || resource.getId() == null) {
            return;
        }
        if (!StringUtils.hasText(resource.getTitle())) {
            resource.setTitle("未命名资源");
        }
        if (resource.getUploadTime() == null) {
            resource.setUploadTime(LocalDateTime.now());
        }
        resource.setUpdateTime(LocalDateTime.now());
        if (resource.getIsDeleted() == null) {
            resource.setIsDeleted(0);
        }

        PrimaryChineseResource existing = primaryChineseResourceMapper.selectById(resource.getId());
        if (existing == null) {
            primaryChineseResourceMapper.insert(resource);
            log.info("宽表同步 insert id={}", resource.getId());
        } else {
            primaryChineseResourceMapper.updateById(resource);
            log.debug("宽表同步 update id={}", resource.getId());
        }
    }

    /**
     * 从主表 + 写入上下文构建宽表并同步
     */
    public PrimaryChineseResource buildWideFromMaster(
            EduResource master,
            LegacyWriteContext ctx) {
        PrimaryChineseResource wide = new PrimaryChineseResource();
        wide.setId(master.getId());
        wide.setTitle(master.getTitle());
        wide.setRemark(StringUtils.hasText(master.getDescription()) ? master.getDescription() : master.getRemark());
        wide.setOriginalFilename(master.getOriginalFilename());
        wide.setFileExt(master.getFileExt());
        wide.setOssBucket(master.getOssBucket());
        wide.setOssObjectKey(master.getOssObjectKey());
        wide.setOssUrl(master.getOssUrl());
        wide.setFileSizeKb(master.getFileSizeKb());
        wide.setDownloadCount(master.getDownloadCount() != null ? master.getDownloadCount() : 0);
        wide.setViewCount(master.getViewCount() != null ? master.getViewCount() : 0);
        wide.setStatus(ResourceStatusMapper.eduToWide(master.getStatus()));
        wide.setUploaderId(master.getUploaderId());
        wide.setUploadTime(master.getUploadTime());
        wide.setUpdateTime(master.getUpdateTime());
        wide.setSort(master.getSort());
        wide.setAllowPreview(ctx.getAllowPreview() != null ? ctx.getAllowPreview() : 1);
        wide.setIsFree(ctx.getIsFree() != null ? ctx.getIsFree() : 0);
        wide.setLessonPlanJson(ctx.getLessonPlanJson());

        wide.setStage(ctx.getStage());
        wide.setSubject(ctx.getSubject());
        wide.setModule(ctx.getModule());
        wide.setType(ctx.getType());
        wide.setGradeName(ctx.getGradeName());
        wide.setEdition(ctx.getEdition());
        wide.setUnitName(ctx.getUnitName());
        wide.setLessonName(ctx.getLessonName());
        wide.setBrandCode(ctx.getBrandCode());
        wide.setSubType(ctx.getSubType());
        wide.setCatalogNodeId(ctx.getCatalogNodeId());
        wide.setCatalogPath(resolveCatalogPath(ctx));
        wide.setIsDeleted(0);
        return wide;
    }

    public void syncMasterToWide(EduResource master, LegacyWriteContext ctx) {
        PrimaryChineseResource wide = buildWideFromMaster(master, ctx);
        syncToWideTable(wide);
    }

    private String resolveCatalogPath(LegacyWriteContext ctx) {
        if (StringUtils.hasText(ctx.getCatalogPath())) {
            return ctx.getCatalogPath();
        }
        if (ctx.getCatalogNodeId() != null && ctx.getCatalogNodeId() > 0) {
            EduCatalogNode node = eduCatalogNodeMapper.findActiveById(ctx.getCatalogNodeId());
            if (node != null) {
                return node.getNamePath();
            }
        }
        return null;
    }

    /**
     * 宽表写入上下文（文本维度 + 目录）
     */
    @lombok.Data
    public static class LegacyWriteContext {
        private String stage;
        private String subject;
        private String module;
        private String type;
        private String gradeName;
        private String edition;
        private String unitName;
        private String lessonName;
        private String brandCode;
        private Long catalogNodeId;
        private String catalogPath;
        private String subType;
        private Integer allowPreview;
        private Integer isFree;
        private String lessonPlanJson;
        private java.util.List<ResourceFileDTO> files;

        public static LegacyWriteContext fromPrimaryChinese(PrimaryChineseResource r) {
            LegacyWriteContext ctx = new LegacyWriteContext();
            ctx.setStage(r.getStage());
            ctx.setSubject(r.getSubject());
            ctx.setModule(r.getModule());
            ctx.setType(r.getType());
            ctx.setGradeName(r.getGradeName());
            ctx.setEdition(r.getEdition());
            ctx.setUnitName(r.getUnitName());
            ctx.setLessonName(r.getLessonName());
            ctx.setBrandCode(r.getBrandCode());
            ctx.setCatalogNodeId(r.getCatalogNodeId());
            ctx.setCatalogPath(r.getCatalogPath());
            ctx.setSubType(r.getSubType());
            ctx.setAllowPreview(r.getAllowPreview());
            ctx.setIsFree(r.getIsFree());
            ctx.setLessonPlanJson(r.getLessonPlanJson());
            return ctx;
        }

        public static LegacyWriteContext fromWriteDto(ResourceWriteDTO dto) {
            LegacyWriteContext ctx = new LegacyWriteContext();
            if (dto.getFiles() != null) {
                ctx.setFiles(dto.getFiles());
            }
            ctx.setStage(dto.getStage());
            ctx.setSubject(dto.getSubject());
            ctx.setModule(dto.getModule());
            ctx.setType(dto.getType());
            ctx.setGradeName(dto.getGradeName());
            ctx.setEdition(dto.getEdition());
            ctx.setUnitName(dto.getUnitName());
            ctx.setLessonName(dto.getLessonName());
            ctx.setBrandCode(dto.getBrandCode());
            ctx.setCatalogNodeId(dto.getCatalogNodeId());
            ctx.setCatalogPath(dto.getCatalogPath());
            ctx.setSubType(dto.getSubType());
            ctx.setAllowPreview(dto.getAllowPreview());
            ctx.setIsFree(dto.getIsFree());
            ctx.setLessonPlanJson(dto.getLessonPlanJson());
            return ctx;
        }
    }
}
