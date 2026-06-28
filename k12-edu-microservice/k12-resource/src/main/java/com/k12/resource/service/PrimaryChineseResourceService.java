package com.k12.resource.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.k12.common.BusinessException;
import com.k12.common.constant.ResourceStatusConstants;
import com.k12.common.dto.MyUploadStatsVO;
import com.k12.common.dto.PrimaryChineseResourceQueryDTO;
import com.k12.common.dto.PrimaryChineseResourceVO;
import com.k12.common.dto.ResourceRejectInfoVO;
import com.k12.common.dto.ResourceSuiteVO;
import com.k12.common.dto.UnitTreeNodeVO;
import com.k12.common.entity.PrimaryChineseResource;
import com.k12.common.entity.ResourceAudit;
import com.k12.common.entity.ResourceAuditLog;
import com.k12.resource.config.ResourceUploadProperties;
import com.k12.resource.mapper.PrimaryChineseResourceMapper;
import com.k12.resource.mapper.ResourceAuditLogMapper;
import com.k12.resource.mapper.ResourceAuditMapper;
import com.k12.resource.search.SearchIndexSyncHook;
import com.k12.resource.util.OssMetadataHelper;
import com.k12.resource.util.PublicResourceQuerySupport;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 小学语文学科资源Service
 */
@Slf4j
@Service
public class PrimaryChineseResourceService {

    private final PrimaryChineseResourceMapper primaryChineseResourceMapper;
    private final ResourceAuditLogMapper resourceAuditLogMapper;
    private final ResourceAuditMapper resourceAuditMapper;
    private final UnitCatalogService unitCatalogService;
    private final OssMetadataHelper ossMetadataHelper;
    private final ResourceUploadProperties resourceUploadProperties;
    private final ResourceBrowseService resourceBrowseService;
    private final ResourcePlacementService resourcePlacementService;
    private final ResourceWriteService resourceWriteService;
    private final RemoteFileProxyService remoteFileProxyService;
    private final SearchIndexSyncHook searchIndexSyncHook;
    private final UploadPlacementValidator uploadPlacementValidator;
    private final ResourcePreviewSafetySync resourcePreviewSafetySync;
    public PrimaryChineseResourceService(PrimaryChineseResourceMapper primaryChineseResourceMapper, ResourceAuditLogMapper resourceAuditLogMapper, ResourceAuditMapper resourceAuditMapper, UnitCatalogService unitCatalogService, OssMetadataHelper ossMetadataHelper, ResourceUploadProperties resourceUploadProperties, ResourceBrowseService resourceBrowseService, ResourcePlacementService resourcePlacementService, ResourceWriteService resourceWriteService, RemoteFileProxyService remoteFileProxyService, SearchIndexSyncHook searchIndexSyncHook, UploadPlacementValidator uploadPlacementValidator, ResourcePreviewSafetySync resourcePreviewSafetySync) {
        this.primaryChineseResourceMapper = primaryChineseResourceMapper;
        this.resourceAuditLogMapper = resourceAuditLogMapper;
        this.resourceAuditMapper = resourceAuditMapper;
        this.unitCatalogService = unitCatalogService;
        this.ossMetadataHelper = ossMetadataHelper;
        this.resourceUploadProperties = resourceUploadProperties;
        this.resourceBrowseService = resourceBrowseService;
        this.resourcePlacementService = resourcePlacementService;
        this.resourceWriteService = resourceWriteService;
        this.remoteFileProxyService = remoteFileProxyService;
        this.searchIndexSyncHook = searchIndexSyncHook;
        this.uploadPlacementValidator = uploadPlacementValidator;
        this.resourcePreviewSafetySync = resourcePreviewSafetySync;
    }


    private static final Map<String, String> SUITE_TYPE_ICONS = Map.of(
            "课件", "📚", "教案", "📖", "练习", "✏️", "学案", "📘",
            "试卷", "📝", "视频", "🎬", "音频/朗读", "🎧", "知识点", "📋"
    );

    // ===================== 查询 =====================

    /**
     * 根据条件查询资源列表（不分页，兼容旧接口）
     */
    public List<PrimaryChineseResource> findByCondition(
            String stage, String subject, String module, String type,
            String gradeName, String edition, String unitName) {
        return primaryChineseResourceMapper.findByCondition(
                stage, subject, module, type, gradeName, edition, null, null,
                unitName, null, null, null, null, null, null, null);
    }

    /**
     * 根据DTO查询资源列表（不分页，支持关键词）
     * edition 使用模糊匹配逻辑
     */
    public List<PrimaryChineseResource> listAll(PrimaryChineseResourceQueryDTO dto) {
        PublicResourceQuerySupport.applyPublicStatusDefault(dto);
        if (resourceBrowseService.shouldUseBrowse(dto)) {
            return resourceBrowseService.listAll(dto);
        }
        resolveEdition(dto);
        return primaryChineseResourceMapper.findByCondition(
                dto.getStage(), dto.getSubject(), dto.getModule(), dto.getType(),
                dto.getGradeName(), dto.getEdition(), dto.getBrandCode(), dto.getSubType(),
                dto.getUnitName(), dto.getLessonName(),
                dto.getFileExt(), dto.getStatus(), dto.getAuditStatus(), dto.getPublishStatus(),
                dto.getUploaderId(), dto.getKeyword());
    }

    /**
     * 分页查询资源列表
     * 支持功能：
     *   - edition 模糊匹配（前端"统编版" → DB"统编版(2024)"）
     *   - unitName 包含匹配（前端子单元名 → DB父级名）
     *   - status/fileExt 精确筛选
     *   - 排序字段白名单防注入
     */
    public Map<String, Object> listByPage(PrimaryChineseResourceQueryDTO dto) {
        PublicResourceQuerySupport.applyPublicStatusDefault(dto);
        if (resourceBrowseService.shouldUseBrowse(dto)) {
            return resourceBrowseService.listByPage(dto);
        }
        // 安全处理排序字段，防止SQL注入
        String sortField = getSafeSortField(dto.getSortField());
        String sortOrder = "asc".equalsIgnoreCase(dto.getSortOrder()) ? "asc" : "desc";

        // edition 模糊匹配
        resolveEdition(dto);

        // unitName 包含匹配（支持跨格式：前端"第一单元·识字" ↔ DB"第一单元 识字（一）"）
        String unitName = dto.getUnitName();
        if (unitName != null && !unitName.isEmpty()) {
            List<String> allUnits = primaryChineseResourceMapper.findDistinctUnitNames(
                    dto.getGradeName(), dto.getEdition());
            boolean exactMatch = allUnits.contains(unitName);
            if (!exactMatch) {
                // 标准化比较 key：去掉分隔符差异（·/空格/括号），提取核心单元编号
                String normalizedInput = normalizeUnitKey(unitName);
                for (String u : allUnits) {
                    String normalizedDb = normalizeUnitKey(u);
                    // 1. 标准化后完全一致
                    if (normalizedInput.equals(normalizedDb)) {
                        log.info("unitName 标准化匹配: '{}' -> '{}'", unitName, u);
                        dto.setUnitName(u);
                        break;
                    }
                    // 2. 单元编号前缀匹配（如都含 "第一单元" 或 "第1单元"）
                    if (normalizedDb.contains(normalizedInput) || normalizedInput.contains(normalizedDb)) {
                        log.info("unitName 包含匹配: '{}' -> '{}'", unitName, u);
                        dto.setUnitName(u);
                        break;
                    }
                    // 3. 原始 contains 兜底
                    if (u.contains(unitName) || unitName.contains(u)) {
                        log.info("unitName 原始匹配: '{}' -> '{}'", unitName, u);
                        dto.setUnitName(u);
                        break;
                    }
                }
                if (!allUnits.contains(dto.getUnitName())) {
                    // 保留前端传入的单元名做精确筛选，避免误返回其他单元资源
                    dto.setUnitName(unitName);
                    log.info("unitName '{}' 在资源库无别名记录，按精确条件查询（共{}个历史单元）",
                            unitName, allUnits.size());
                }
            }
        }

        Page<PrimaryChineseResource> page = new Page<>(dto.getCurrent(), dto.getSize());
        primaryChineseResourceMapper.findByConditionPage(
                page,
                dto.getStage(), dto.getSubject(), dto.getModule(), dto.getType(),
                dto.getGradeName(), dto.getEdition(), dto.getBrandCode(), dto.getSubType(),
                dto.getUnitName(), dto.getLessonName(),
                dto.getFileExt(), dto.getStatus(), dto.getAuditStatus(), dto.getPublishStatus(),
                dto.getUploaderId(), dto.getKeyword(),
                sortField, sortOrder
        );

        Map<String, Object> result = new HashMap<>();
        result.put("records", page.getRecords());
        result.put("total", page.getTotal());
        result.put("current", page.getCurrent());
        result.put("size", page.getSize());
        result.put("pages", page.getPages());
        return result;
    }

    /**
     * 根据ID查询
     */
    public PrimaryChineseResource getById(Long id) {
        return primaryChineseResourceMapper.selectById(id);
    }

    /** C 端公开详情：仅返回已上架资源 */
    public PrimaryChineseResource getPublicById(Long id) {
        PrimaryChineseResource resource = getById(id);
        if (!PublicResourceQuerySupport.isPubliclyPublished(resource)) {
            return null;
        }
        return resource;
    }

    /**
     * 上传者查看资源最新驳回信息（仅 status=2 且本人上传）
     */
    public ResourceRejectInfoVO getMyResourceRejectInfo(Long resourceId, Long userId) {
        if (userId == null) {
            throw new BusinessException(401, "请先登录");
        }
        PrimaryChineseResource resource = primaryChineseResourceMapper.selectById(resourceId);
        if (resource == null || (resource.getIsDeleted() != null && resource.getIsDeleted() == 1)) {
            throw new BusinessException(404, "资源不存在");
        }
        if (resource.getUploaderId() == null || !resource.getUploaderId().equals(userId)) {
            throw new BusinessException(403, "无权查看该资源的审核信息");
        }
        if (resource.getStatus() == null || resource.getStatus() != ResourceStatusConstants.REJECTED) {
            return null;
        }
        ResourceAuditLog log = resourceAuditLogMapper.selectLatestRejectByResourceId(resourceId);
        ResourceRejectInfoVO vo = ResourceRejectInfoVO.fromLog(resourceId, log);
        if (!StringUtils.hasText(vo.getReason())) {
            ResourceAudit legacy = resourceAuditMapper.selectLatestRejectByResourceId(resourceId);
            if (legacy != null && StringUtils.hasText(legacy.getReason())) {
                vo.setReason(legacy.getReason());
                vo.setAuditorName(legacy.getAuditorName());
                vo.setRejectedAt(legacy.getCreateTime());
            }
        }
        if (!StringUtils.hasText(vo.getReason())) {
            vo.setReason("管理员未填写具体原因");
        }
        return vo;
    }

    /**
     * 未通过资源复制为新草稿，供重新上传
     */
    public PrimaryChineseResource cloneRejectedToDraft(Long resourceId, Long userId) {
        if (userId == null) {
            throw new BusinessException(401, "请先登录");
        }
        PrimaryChineseResource source = primaryChineseResourceMapper.selectById(resourceId);
        if (source == null || (source.getIsDeleted() != null && source.getIsDeleted() == 1)) {
            throw new BusinessException(404, "资源不存在");
        }
        if (source.getUploaderId() == null || !source.getUploaderId().equals(userId)) {
            throw new BusinessException(403, "无权操作该资源");
        }
        if (source.getStatus() == null || source.getStatus() != ResourceStatusConstants.REJECTED) {
            throw new BusinessException(400, "仅未通过资源可重新上传");
        }

        PrimaryChineseResource draft = new PrimaryChineseResource();
        draft.setStage(source.getStage());
        draft.setSubject(source.getSubject());
        draft.setModule(source.getModule());
        draft.setType(source.getType());
        draft.setGradeName(source.getGradeName());
        draft.setEdition(source.getEdition());
        draft.setBrandCode(source.getBrandCode());
        draft.setCatalogNodeId(source.getCatalogNodeId());
        draft.setCatalogPath(source.getCatalogPath());
        draft.setSubType(source.getSubType());
        draft.setUnitName(source.getUnitName());
        draft.setLessonName(source.getLessonName());
        draft.setTitle(source.getTitle());
        draft.setOriginalFilename(source.getOriginalFilename());
        draft.setFileExt(source.getFileExt());
        draft.setOssBucket(source.getOssBucket());
        draft.setOssObjectKey(source.getOssObjectKey());
        draft.setOssUrl(source.getOssUrl());
        draft.setFileSizeKb(source.getFileSizeKb());
        draft.setRemark(source.getRemark());
        draft.setAllowPreview(source.getAllowPreview());
        draft.setIsFree(source.getIsFree());
        draft.setSort(source.getSort());
        return saveDraft(draft, userId);
    }

    /**
     * 我的上传统计
     */
    public MyUploadStatsVO getMyUploadStats(Long uploaderId) {
        MyUploadStatsVO vo = new MyUploadStatsVO();
        if (uploaderId == null) {
            vo.setTotal(0L);
            vo.setPublished(0L);
            vo.setPending(0L);
            vo.setDraft(0L);
            vo.setRejected(0L);
            vo.setOffline(0L);
            vo.setTotalViews(0L);
            vo.setTotalDownloads(0L);
            return vo;
        }
        vo.setTotal(primaryChineseResourceMapper.countByUploader(uploaderId));
        vo.setPublished(primaryChineseResourceMapper.countPublishedByUploader(uploaderId));
        vo.setPending(primaryChineseResourceMapper.countPendingByUploader(uploaderId));
        vo.setDraft(primaryChineseResourceMapper.countDraftByUploader(uploaderId));
        vo.setRejected(primaryChineseResourceMapper.countRejectedByUploader(uploaderId));
        vo.setOffline(primaryChineseResourceMapper.countOfflineByUploader(uploaderId));
        vo.setTotalViews(primaryChineseResourceMapper.sumViewsByUploader(uploaderId));
        vo.setTotalDownloads(primaryChineseResourceMapper.sumDownloadsByUploader(uploaderId));
        return vo;
    }

    // ===================== 枚举/筛选项 =====================

    /**
     * 获取所有年级列表（去重，供前端下拉）
     */
    public List<String> getGradeNames() {
        return primaryChineseResourceMapper.findDistinctGradeNames();
    }

    /**
     * 获取所有版本列表（去重）
     */
    public List<String> getEditions() {
        return primaryChineseResourceMapper.findDistinctEditions();
    }

    /**
     * 获取所有栏目列表（去重）
     */
    public List<String> getModules() {
        return primaryChineseResourceMapper.findDistinctModules();
    }

    /**
     * 获取所有资源类型列表（去重）
     */
    public List<String> getTypes() {
        return primaryChineseResourceMapper.findDistinctTypes();
    }

    /**
     * 获取指定年级+版本下的单元列表（供侧边树使用）
     */
    public List<String> getUnitNames(String gradeName, String edition) {
        return primaryChineseResourceMapper.findDistinctUnitNames(gradeName, edition);
    }

    /**
     * 获取所有筛选枚举项（一次性返回，减少请求）
     */
    public Map<String, Object> getFilterOptions() {
        Map<String, Object> options = new HashMap<>();
        options.put("gradeNames", primaryChineseResourceMapper.findDistinctGradeNames());
        options.put("editions", primaryChineseResourceMapper.findDistinctEditions());
        options.put("modules", primaryChineseResourceMapper.findDistinctModules());
        options.put("types", primaryChineseResourceMapper.findDistinctTypes());
        return options;
    }

    // ===================== 写操作 =====================

    /**
     * C 端 POST /save 已禁用；batch 等内部场景仍调用 {@link #saveLegacyWrite}
     */
    public PrimaryChineseResource save(PrimaryChineseResource resource) {
        throw new BusinessException(400, "请使用 /draft/save 保存草稿，或通过 /draft/{id}/submit 或 /draft/submit 提交审核");
    }

    /** 内部写入（batch-save 等），不供 C 端直接提交审核 */
    public PrimaryChineseResource saveLegacyWrite(PrimaryChineseResource resource) {
        applyInsertDefaults(resource);
        if (resource.getStatus() == null) {
            resource.setStatus(resolveDefaultUploadStatus());
        }
        uploadPlacementValidator.validateForSave(resource);
        syncLifecycleFields(resource);
        resourcePreviewSafetySync.sync(resource);
        ossMetadataHelper.fillOssFields(resource);
        Long id = resourceWriteService.persistFromLegacy(resource);
        resource.setId(id);
        resourceWriteService.syncLegacyToWide(resource);
        resourcePlacementService.syncPrimaryPlacement(resource);
        searchIndexSyncHook.afterPrimaryChanged(id);
        return getById(id);
    }

    /**
     * 保存或更新草稿（不要求已上传文件）
     */
    public PrimaryChineseResource saveDraft(PrimaryChineseResource resource, Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("请先登录");
        }
        uploadPlacementValidator.validateForSave(resource);
        resource.setUploaderId(userId);
        resource.setStatus(ResourceStatusConstants.DRAFT);
        syncLifecycleFields(resource);
        resourcePreviewSafetySync.sync(resource);
        if (resource.getTitle() == null || resource.getTitle().isBlank()) {
            resource.setTitle("未命名草稿");
        }
        if (resource.getId() != null) {
            PrimaryChineseResource existing = getById(resource.getId());
            if (existing == null || existing.getStatus() != ResourceStatusConstants.DRAFT) {
                throw new IllegalStateException("草稿不存在");
            }
            if (!userId.equals(existing.getUploaderId())) {
                throw new IllegalStateException("无权修改该草稿");
            }
            resource.setUpdateTime(LocalDateTime.now());
            ossMetadataHelper.fillOssFields(resource);
            resourceWriteService.persistFromLegacy(resource);
            resourceWriteService.syncLegacyToWide(resource);
            resourcePlacementService.syncPrimaryPlacement(resource);
            searchIndexSyncHook.afterPrimaryChanged(resource.getId());
            return getById(resource.getId());
        }
        applyInsertDefaults(resource);
        ossMetadataHelper.fillOssFields(resource);
        Long id = resourceWriteService.persistFromLegacy(resource);
        resource.setId(id);
        resourceWriteService.syncLegacyToWide(resource);
        resourcePlacementService.syncPrimaryPlacement(resource);
        searchIndexSyncHook.afterPrimaryChanged(id);
        return getById(id);
    }

    public List<PrimaryChineseResource> listDrafts(Long userId) {
        if (userId == null) {
            return List.of();
        }
        return primaryChineseResourceMapper.findDraftsByUploader(userId);
    }

    public PrimaryChineseResource getDraft(Long id, Long userId) {
        PrimaryChineseResource resource = getById(id);
        if (resource == null || resource.getStatus() != ResourceStatusConstants.DRAFT) {
            return null;
        }
        if (userId == null || !userId.equals(resource.getUploaderId())) {
            return null;
        }
        return resource;
    }

    /**
     * 草稿提交审核（status: -1 -> 0）
     */
    public PrimaryChineseResource submitDraft(Long id, Long userId) {
        PrimaryChineseResource resource = getDraft(id, userId);
        if (resource == null) {
            throw new IllegalStateException("草稿不存在或无权操作");
        }
        if (resource.getOssUrl() == null || resource.getOssUrl().isBlank()) {
            throw new IllegalStateException("请先上传文件后再提交");
        }
        if (resource.getTitle() == null || resource.getTitle().isBlank()) {
            throw new IllegalStateException("请填写资源标题");
        }
        uploadPlacementValidator.validateForSubmit(resource);
        resource.setStatus(resolveDefaultUploadStatus());
        syncLifecycleFields(resource);
        resourcePreviewSafetySync.sync(resource);
        resource.setUpdateTime(LocalDateTime.now());
        resourceWriteService.persistFromLegacy(resource);
        resourceWriteService.syncLegacyToWide(resource);
        resourcePlacementService.syncPrimaryPlacement(resource);
        searchIndexSyncHook.afterPrimaryChanged(id);
        return getById(id);
    }

    /**
     * 保存草稿并提交审核（聚合接口，一次请求完成 saveDraft + submitDraft）
     */
    public PrimaryChineseResource submitFromPayload(PrimaryChineseResource resource, Long userId) {
        PrimaryChineseResource saved = saveDraft(resource, userId);
        return submitDraft(saved.getId(), userId);
    }

    /**
     * 删除草稿（软删）
     */
    public void deleteDraft(Long id, Long userId) {
        PrimaryChineseResource draft = getDraft(id, userId);
        if (draft == null) {
            throw new IllegalStateException("草稿不存在或无权操作");
        }
        draft.setIsDeleted(1);
        draft.setUpdateTime(LocalDateTime.now());
                primaryChineseResourceMapper.updateById(draft);
    }

    /**
     * 撤回待审核资源（status: 0 → -1 草稿）
     */
    public PrimaryChineseResource withdrawPending(Long id, Long userId) {
        if (userId == null) {
            throw new BusinessException(401, "请先登录");
        }
        PrimaryChineseResource resource = primaryChineseResourceMapper.selectById(id);
        if (resource == null || (resource.getIsDeleted() != null && resource.getIsDeleted() == 1)) {
            throw new BusinessException(404, "资源不存在");
        }
        if (resource.getUploaderId() == null || !resource.getUploaderId().equals(userId)) {
            throw new BusinessException(403, "无权操作该资源");
        }
        if (resource.getStatus() == null || resource.getStatus() != ResourceStatusConstants.PENDING) {
            throw new BusinessException(400, "仅待审核资源可撤回");
        }
        resource.setStatus(ResourceStatusConstants.DRAFT);
        resource.setAuditStatus(ResourceStatusConstants.AUDIT_DRAFT);
        resource.setPublishStatus(ResourceStatusConstants.PUBLISH_UNPUBLISHED);
        resource.setUpdateTime(LocalDateTime.now());
        primaryChineseResourceMapper.updateById(resource);
        resourceWriteService.syncLegacyToWide(resource);
        resourcePlacementService.syncPrimaryPlacement(resource);
        searchIndexSyncHook.afterPrimaryChanged(id);
        return getById(id);
    }


    private void syncLifecycleFields(PrimaryChineseResource resource) {
        int status = resource.getStatus() != null ? resource.getStatus() : ResourceStatusConstants.PENDING;
        resource.setAuditStatus(ResourceStatusConstants.resolveAuditStatus(null, status));
        resource.setPublishStatus(ResourceStatusConstants.resolvePublishStatus(null, status));
    }
    private int resolveDefaultUploadStatus() {
        int configured = resourceUploadProperties.getDefaultStatus();
        if (configured == ResourceStatusConstants.PUBLISHED
                || configured == ResourceStatusConstants.PENDING
                || configured == ResourceStatusConstants.REJECTED
                || configured == ResourceStatusConstants.OFFLINE) {
            return configured;
        }
        return ResourceStatusConstants.PENDING;
    }

    public List<UnitTreeNodeVO> getUnitTree(String volumeKey, String gradeName, String edition, String subject) {
        return unitCatalogService.getUnitTree(volumeKey, gradeName, edition, subject);
    }

    /**
     * 成套资源列表（按资源类型聚合）
     */
    public List<ResourceSuiteVO> listSuites(PrimaryChineseResourceQueryDTO dto) {
        if (resourceBrowseService.shouldUseBrowse(dto)) {
            return resourceBrowseService.listSuites(dto);
        }
        resolveEdition(dto);
        List<PrimaryChineseResource> records = listAll(dto);
        if (records.isEmpty()) {
            return List.of();
        }

        Map<String, List<PrimaryChineseResource>> typeMap = new LinkedHashMap<>();
        for (PrimaryChineseResource item : records) {
            String type = StringUtils.hasText(item.getType()) ? item.getType() : "其他";
            typeMap.computeIfAbsent(type, k -> new ArrayList<>()).add(item);
        }

        String gradeLabel = dto.getGradeName() != null ? dto.getGradeName() : "";
        String editionLabel = dto.getEdition() != null ? dto.getEdition() : "";

        List<ResourceSuiteVO> suites = new ArrayList<>();
        for (Map.Entry<String, List<PrimaryChineseResource>> entry : typeMap.entrySet()) {
            String type = entry.getKey();
            List<PrimaryChineseResource> items = entry.getValue();
            ResourceSuiteVO suite = new ResourceSuiteVO();
            suite.setKey(type);
            suite.setIcon(SUITE_TYPE_ICONS.getOrDefault(type, "📄"));
            suite.setTitle("成套" + type + "资源");
            suite.setSub(gradeLabel + editionLabel + type + "合集（共" + items.size() + "份）");
            suite.setTag("配套资源");
            suite.setFileCount(items.size());
            String updateTime = items.stream()
                    .map(PrimaryChineseResource::getUploadTime)
                    .filter(Objects::nonNull)
                    .map(t -> t.toLocalDate().toString())
                    .findFirst()
                    .orElse(LocalDateTime.now().toLocalDate().toString());
            suite.setUpdateTime(updateTime);
            suite.setItems(items.stream()
                    .map(PrimaryChineseResourceVO::fromEntity)
                    .collect(Collectors.toList()));
            suites.add(suite);
        }
        return suites;
    }

    public List<Map<String, Object>> countByModule(String stage, String subject, String gradeName, String edition) {
        return primaryChineseResourceMapper.countByModule(stage, subject, gradeName, edition);
    }

    private void applyInsertDefaults(PrimaryChineseResource resource) {
        if (!StringUtils.hasText(resource.getBrandCode())) {
            resource.setBrandCode("qicai");
        }
        if (resource.getUploadTime() == null) {
            resource.setUploadTime(LocalDateTime.now());
        }
        resource.setUpdateTime(LocalDateTime.now());
        if (resource.getAllowPreview() == null) {
            resource.setAllowPreview(1);
        }
        if (resource.getDownloadCount() == null) {
            resource.setDownloadCount(0);
        }
        if (resource.getViewCount() == null) {
            resource.setViewCount(0);
        }
    }

    /**
     * 代理 OSS 文件流（预览 inline / 下载 attachment），支持 Range 断点续传
     */
    public void streamFile(
            Long id,
            String disposition,
            HttpServletRequest request,
            HttpServletResponse response) {
        PrimaryChineseResource resource = getById(id);
        if (resource == null) {
            writePlainError(response, HttpServletResponse.SC_NOT_FOUND, "资源不存在");
            return;
        }
        if (!PublicResourceQuerySupport.isPublicFileAccessible(resource)) {
            writePlainError(response, HttpServletResponse.SC_FORBIDDEN, "资源未上架或文件不可访问");
            return;
        }
        String ossUrl = resource.getOssUrl();
        if (!StringUtils.hasText(ossUrl)) {
            writePlainError(response, HttpServletResponse.SC_NOT_FOUND, "文件地址为空");
            return;
        }

        boolean attachment = "attachment".equalsIgnoreCase(disposition);
        if (attachment) {
            incrementDownloadCount(id);
        }

        String filename = resource.getOriginalFilename();
        if (!StringUtils.hasText(filename)) {
            String ext = StringUtils.hasText(resource.getFileExt()) ? resource.getFileExt() : "bin";
            filename = resource.getTitle() + "." + ext;
        }

        remoteFileProxyService.streamUrl(
                ossUrl,
                filename,
                mimeFromExt(resource.getFileExt()),
                attachment,
                request,
                response);
    }

    private void writePlainError(HttpServletResponse response, int status, String message) {
        try {
            response.setStatus(status);
            response.setContentType("text/plain;charset=UTF-8");
            response.getWriter().write(message);
        } catch (Exception ignored) {
            // ignore
        }
    }

    private String mimeFromExt(String ext) {
        if (!StringUtils.hasText(ext)) {
            return "application/octet-stream";
        }
        return switch (ext.toLowerCase(Locale.ROOT)) {
            case "mp4" -> "video/mp4";
            case "webm" -> "video/webm";
            case "mov" -> "video/quicktime";
            case "avi" -> "video/x-msvideo";
            case "mp3" -> "audio/mpeg";
            case "wav" -> "audio/wav";
            case "m4a" -> "audio/mp4";
            case "jpg", "jpeg" -> "image/jpeg";
            case "png" -> "image/png";
            case "gif" -> "image/gif";
            case "webp" -> "image/webp";
            case "pdf" -> "application/pdf";
            default -> "application/octet-stream";
        };
    }

    /**
     * 增加下载次数
     */
    public void incrementDownloadCount(Long id) {
        PrimaryChineseResource resource = getById(id);
        if (resource != null) {
            resource.setDownloadCount((resource.getDownloadCount() == null ? 0 : resource.getDownloadCount()) + 1);
            primaryChineseResourceMapper.updateById(resource);
        }
    }

    /**
     * 增加浏览次数
     */
    public void incrementViewCount(Long id) {
        PrimaryChineseResource resource = getById(id);
        if (resource != null) {
            resource.setViewCount((resource.getViewCount() == null ? 0 : resource.getViewCount()) + 1);
            primaryChineseResourceMapper.updateById(resource);
        }
    }

    // ===================== 私有工具 =====================

    /**
     * edition 模糊匹配：前端版本名可能和数据库不完全一致
     * 如 "统编版" → 匹配 "统编版(2024)"，或 "人教版" → 精确匹配 "人教版"
     */
    private void resolveEdition(PrimaryChineseResourceQueryDTO dto) {
        String edition = dto.getEdition();
        if (edition == null || edition.isEmpty()) return;
        List<String> allEditions = primaryChineseResourceMapper.findDistinctEditions();
        boolean exactMatch = allEditions.contains(edition);
        if (!exactMatch) {
            for (String e : allEditions) {
                if (e.contains(edition) || edition.contains(e)) {
                    log.info("edition 模糊匹配: '{}' -> '{}'", edition, e);
                    dto.setEdition(e);
                    return;
                }
            }
            log.warn("edition '{}' 在数据库中无匹配记录，将忽略此条件", edition);
            dto.setEdition(null);
        }
    }

    /**
     * 白名单校验排序字段，防止SQL注入
     */
    private String getSafeSortField(String sortField) {
        if (sortField == null) return "upload_time";
        switch (sortField) {
            case "uploadTime":
            case "upload_time":
                return "upload_time";
            case "fileSizeKb":
            case "file_size_kb":
                return "file_size_kb";
            case "downloadCount":
            case "download_count":
                return "download_count";
            case "viewCount":
            case "view_count":
                return "view_count";
            case "sort":
                return "sort";
            default:
                return "upload_time";
        }
    }

    /**
     * 标准化单元名称 key，用于跨格式模糊匹配
     * 将 "第一单元·识字" 和 "第一单元 识字（一）" 归一化为可比较的格式
     *
     * 规则：
     * - 统一分隔符：· 空格 → 删除
     * - 中文括号 → 英文括号 → 删除
     * - 提取核心标识：第X单元 / 第X部分
     */
    private String normalizeUnitKey(String unitName) {
        if (unitName == null || unitName.isEmpty()) return "";
        String s = unitName;
        // 去掉所有分隔符
        s = s.replace("·", "").replace(" ", "").replace("（", "(").replace("）", ")");
        // 去掉英文括号及其内容（如 "(一)" "(1)" "（课文单元1）"）
        s = s.replaceAll("\\(.*?\\)", "");
        // 全角转半角数字（① → 1 等）
        s = s.replace("①", "1").replace("②", "2").replace("③", "3")
             .replace("④", "4").replace("⑤", "5").replace("⑥", "6");
        return s.trim();
    }
}



