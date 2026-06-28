package com.k12.resource.service;

import com.k12.common.dto.ResourceFileDTO;
import com.k12.common.dto.ResourceWriteDTO;
import com.k12.common.dto.ResourceWriteResultVO;
import com.k12.common.entity.EduResource;
import com.k12.common.entity.EduResourceDimension;
import com.k12.common.entity.EduResourceFile;
import com.k12.common.entity.PrimaryChineseResource;
import com.k12.resource.mapper.DimensionLookupMapper;
import com.k12.resource.mapper.EduResourceDimensionMapper;
import com.k12.resource.mapper.EduResourceFileMapper;
import com.k12.resource.mapper.EduResourceMapper;
import com.k12.resource.search.SearchIndexSyncHook;
import com.k12.resource.service.ResourceSyncService.LegacyWriteContext;
import com.k12.resource.util.ResourceStatusMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * M4：edu_resource 主表写入（文件 + 维度 + 挂载）
 */
@Slf4j
@Service
public class ResourceWriteService {

    private final EduResourceMapper eduResourceMapper;
    private final EduResourceFileMapper eduResourceFileMapper;
    private final EduResourceDimensionMapper eduResourceDimensionMapper;
    private final DimensionLookupMapper dimensionLookupMapper;
    private final ResourceSyncService resourceSyncService;
    private final ResourcePlacementService resourcePlacementService;
    private final SearchIndexSyncHook searchIndexSyncHook;

    public ResourceWriteService(EduResourceMapper eduResourceMapper,
                                EduResourceFileMapper eduResourceFileMapper,
                                EduResourceDimensionMapper eduResourceDimensionMapper,
                                DimensionLookupMapper dimensionLookupMapper,
                                ResourceSyncService resourceSyncService,
                                ResourcePlacementService resourcePlacementService,
                                SearchIndexSyncHook searchIndexSyncHook) {
        this.eduResourceMapper = eduResourceMapper;
        this.eduResourceFileMapper = eduResourceFileMapper;
        this.eduResourceDimensionMapper = eduResourceDimensionMapper;
        this.dimensionLookupMapper = dimensionLookupMapper;
        this.resourceSyncService = resourceSyncService;
        this.resourcePlacementService = resourcePlacementService;
        this.searchIndexSyncHook = searchIndexSyncHook;
    }


    @Transactional(rollbackFor = Exception.class)
    public ResourceWriteResultVO create(ResourceWriteDTO dto, Long uploaderId) {
        if (!StringUtils.hasText(dto.getTitle())) {
            throw new IllegalArgumentException("请填写资源标题");
        }
        LegacyWriteContext ctx = LegacyWriteContext.fromWriteDto(dto);
        EduResource master = toEduResource(dto, uploaderId);
        Long id = persistMaster(master, ctx, null);
        return finishWrite(id, master, ctx);
    }

    @Transactional(rollbackFor = Exception.class)
    public ResourceWriteResultVO update(Long id, ResourceWriteDTO dto, Long uploaderId) {
        EduResource existing = eduResourceMapper.selectById(id);
        if (existing == null) {
            throw new IllegalStateException("资源不存在");
        }
        LegacyWriteContext ctx = LegacyWriteContext.fromWriteDto(dto);
        EduResource master = toEduResource(dto, uploaderId);
        master.setId(id);
        master.setUploaderId(existing.getUploaderId() != null ? existing.getUploaderId() : uploaderId);
        persistMaster(master, ctx, id);
        return finishWrite(id, master, ctx);
    }

    /**
     * 兼容小学语文宽表写入：先写主表，再双写宽表（同 ID）
     */
    @Transactional(rollbackFor = Exception.class)
    public Long persistFromLegacy(PrimaryChineseResource legacy) {
        LegacyWriteContext ctx = LegacyWriteContext.fromPrimaryChinese(legacy);
        EduResource master = toEduResourceFromLegacy(legacy);
        Long id = persistMaster(master, ctx, legacy.getId());
        legacy.setId(id);
        return id;
    }

    public void syncLegacyToWide(PrimaryChineseResource legacy) {
        LegacyWriteContext ctx = LegacyWriteContext.fromPrimaryChinese(legacy);
        EduResource master = eduResourceMapper.selectById(legacy.getId());
        if (master == null) {
            master = toEduResourceFromLegacy(legacy);
            master.setId(legacy.getId());
        }
        resourceSyncService.syncMasterToWide(master, ctx);
    }

    private ResourceWriteResultVO finishWrite(Long id, EduResource master, LegacyWriteContext ctx) {
        resourceSyncService.syncMasterToWide(master, ctx);
        PrimaryChineseResource wide = resourceSyncService.buildWideFromMaster(master, ctx);
        wide.setId(id);
        resourcePlacementService.syncPrimaryPlacement(wide);

        searchIndexSyncHook.afterEduResourceChanged(id);

        ResourceWriteResultVO result = new ResourceWriteResultVO();
        result.setId(id);
        result.setMasterWritten(true);
        result.setWideSynced(true);
        result.setPlacementSynced(wide.getCatalogNodeId() != null && wide.getCatalogNodeId() > 0);
        return result;
    }

    private Long persistMaster(EduResource master, LegacyWriteContext ctx, Long presetId) {
        Long id = presetId;
        if (id != null && eduResourceMapper.selectById(id) != null) {
            master.setId(id);
            master.setUpdateTime(LocalDateTime.now());
            eduResourceMapper.updateById(master);
        } else if (id != null) {
            master.setId(id);
            if (master.getUploadTime() == null) {
                master.setUploadTime(LocalDateTime.now());
            }
            eduResourceMapper.insert(master);
        } else {
            if (master.getUploadTime() == null) {
                master.setUploadTime(LocalDateTime.now());
            }
            eduResourceMapper.insert(master);
            id = master.getId();
        }

        eduResourceFileMapper.deleteByResourceId(id);
        saveFiles(id, master, ctx);
        eduResourceDimensionMapper.deleteByResourceId(id);
        saveDimension(id, ctx);
        return id;
    }

    private void saveFiles(Long resourceId, EduResource master, LegacyWriteContext ctx) {
        List<EduResourceFile> files = new ArrayList<>();
        if (!CollectionUtils.isEmpty(ctx.getFiles())) {
            for (ResourceFileDTO dto : ctx.getFiles()) {
                files.add(toFileEntity(resourceId, dto));
            }
        } else if (StringUtils.hasText(master.getOssUrl()) || StringUtils.hasText(master.getOssObjectKey())) {
            EduResourceFile main = new EduResourceFile();
            main.setResourceId(resourceId);
            main.setFileRole("main");
            main.setOriginalFilename(master.getOriginalFilename());
            main.setFileExt(master.getFileExt());
            main.setOssBucket(master.getOssBucket());
            main.setOssObjectKey(master.getOssObjectKey());
            main.setOssUrl(master.getOssUrl());
            main.setFileSizeBytes(master.getFileSizeKb() != null ? master.getFileSizeKb() * 1024L : 0L);
            main.setAllowPreview(ctx.getAllowPreview() != null ? ctx.getAllowPreview() : 1);
            main.setSort(0);
            main.setStatus(1);
            files.add(main);
        }
        for (EduResourceFile file : files) {
            eduResourceFileMapper.insert(file);
        }
    }

    private void saveDimension(Long resourceId, LegacyWriteContext ctx) {
        EduResourceDimension dim = new EduResourceDimension();
        dim.setResourceId(resourceId);
        Integer stageId = dimensionLookupMapper.findStageIdByName(ctx.getStage());
        dim.setStageId(stageId);
        if (stageId != null && StringUtils.hasText(ctx.getSubject())) {
            dim.setSubjectId(dimensionLookupMapper.findSubjectIdByNameAndStage(ctx.getSubject(), stageId));
        }
        if (StringUtils.hasText(ctx.getEdition())) {
            dim.setEditionId(dimensionLookupMapper.findEditionIdByName(ctx.getEdition()));
        }
        if (StringUtils.hasText(ctx.getGradeName()) && stageId != null) {
            dim.setGradeId(resolveGradeId(ctx.getGradeName(), stageId));
        }
        if (StringUtils.hasText(ctx.getModule())) {
            dim.setModuleId(dimensionLookupMapper.findModuleIdByName(ctx.getModule()));
        }
        if (StringUtils.hasText(ctx.getType())) {
            dim.setResourceTypeId(dimensionLookupMapper.findResourceTypeIdByName(ctx.getType()));
        }
        eduResourceDimensionMapper.insert(dim);
    }

    private Integer resolveGradeId(String gradeName, Integer stageId) {
        Integer id = dimensionLookupMapper.findGradeIdByName(gradeName, stageId);
        if (id != null) {
            return id;
        }
        if (gradeName.contains("一年级")) {
            return dimensionLookupMapper.findGradeIdByName("一年级", stageId);
        }
        if (gradeName.contains("二年级")) {
            return dimensionLookupMapper.findGradeIdByName("二年级", stageId);
        }
        return null;
    }

    private EduResourceFile toFileEntity(Long resourceId, ResourceFileDTO dto) {
        EduResourceFile file = new EduResourceFile();
        file.setResourceId(resourceId);
        file.setFileRole(StringUtils.hasText(dto.getFileRole()) ? dto.getFileRole() : "main");
        file.setOriginalFilename(dto.getOriginalFilename());
        file.setFileExt(dto.getFileExt());
        file.setOssBucket(dto.getOssBucket());
        file.setOssObjectKey(dto.getOssObjectKey());
        file.setOssUrl(dto.getOssUrl());
        file.setFileSizeBytes(dto.getFileSizeBytes() != null ? dto.getFileSizeBytes() : 0L);
        file.setAllowPreview(dto.getAllowPreview() != null ? dto.getAllowPreview() : 1);
        file.setSort(dto.getSort() != null ? dto.getSort() : 0);
        file.setStatus(1);
        return file;
    }

    private EduResource toEduResource(ResourceWriteDTO dto, Long uploaderId) {
        EduResource r = new EduResource();
        r.setTitle(dto.getTitle());
        r.setDescription(dto.getDescription());
        r.setRemark(dto.getRemark());
        r.setOriginalFilename(dto.getOriginalFilename());
        r.setFileExt(dto.getFileExt());
        r.setOssBucket(dto.getOssBucket());
        r.setOssObjectKey(dto.getOssObjectKey());
        r.setOssUrl(dto.getOssUrl());
        r.setFileSizeKb(dto.getFileSizeKb());
        r.setStatus(ResourceStatusMapper.wideToEdu(dto.getStatus()));
        r.setUploaderId(dto.getUploaderId() != null ? dto.getUploaderId() : uploaderId);
        r.setSort(dto.getSort());
        r.setUploadTime(LocalDateTime.now());
        r.setUpdateTime(LocalDateTime.now());
        r.setDownloadCount(0);
        r.setViewCount(0);
        r.setIsDeleted(0);
        return r;
    }

    private EduResource toEduResourceFromLegacy(PrimaryChineseResource legacy) {
        EduResource r = new EduResource();
        r.setId(legacy.getId());
        r.setTitle(legacy.getTitle());
        r.setDescription(legacy.getRemark());
        r.setRemark(legacy.getRemark());
        r.setOriginalFilename(legacy.getOriginalFilename());
        r.setFileExt(legacy.getFileExt());
        r.setOssBucket(legacy.getOssBucket());
        r.setOssObjectKey(legacy.getOssObjectKey());
        r.setOssUrl(legacy.getOssUrl());
        r.setFileSizeKb(legacy.getFileSizeKb());
        r.setStatus(ResourceStatusMapper.wideToEdu(legacy.getStatus()));
        r.setUploaderId(legacy.getUploaderId());
        r.setSort(legacy.getSort());
        r.setUploadTime(legacy.getUploadTime() != null ? legacy.getUploadTime() : LocalDateTime.now());
        r.setUpdateTime(LocalDateTime.now());
        r.setDownloadCount(legacy.getDownloadCount() != null ? legacy.getDownloadCount() : 0);
        r.setViewCount(legacy.getViewCount() != null ? legacy.getViewCount() : 0);
        r.setIsDeleted(0);
        return r;
    }

}
