package com.k12.resource.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.k12.common.BusinessException;
import com.k12.common.constant.ResourceStatusConstants;
import com.k12.common.dto.AdminDataScopeVO;
import com.k12.common.dto.AdminPrimaryResourceQueryDTO;
import com.k12.common.dto.AdminResourceAuditInsightsVO;
import com.k12.common.dto.AdminResourceBatchResultVO;
import com.k12.common.dto.AdminResourceDetailVO;
import com.k12.common.dto.AdminResourceListVO;
import com.k12.common.dto.AdminResourceUpdateDTO;
import com.k12.common.dto.DuplicateResourceHintVO;
import com.k12.common.entity.PrimaryChineseResource;
import com.k12.common.entity.ResourceAudit;
import com.k12.common.service.AdminPermissionService;
import com.k12.resource.mapper.PrimaryChineseResourceMapper;
import com.k12.resource.mapper.ResourceAuditMapper;
import com.k12.resource.search.SearchIndexSyncHook;
import com.k12.resource.service.AdminAuditService;
import com.k12.resource.service.AdminPrimaryResourceService;
import com.k12.resource.service.AuditContentAnalyzer;
import com.k12.resource.service.ResourceLifecycleService;
import com.k12.resource.util.PublicResourceQuerySupport;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@SuppressWarnings("null")
public class AdminPrimaryResourceServiceImpl implements AdminPrimaryResourceService {

    private static final Map<String, String> STAGE_MAP = Map.of(
            "primary", "小学",
            "junior", "初中",
            "senior", "高中"
    );
    private static final Map<String, String> SUBJECT_MAP = Map.of(
            "chinese", "语文",
            "math", "数学",
            "english", "英语"
    );
    private static final Set<String> SORT_FIELDS = Set.of(
            "upload_time", "update_time", "download_count", "view_count", "sort", "top_sort"
    );

    private final PrimaryChineseResourceMapper primaryChineseResourceMapper;
    private final ResourceAuditMapper resourceAuditMapper;
    private final AdminAuditService adminAuditService;
    private final AdminPermissionService adminPermissionService;
    private final ResourceLifecycleService resourceLifecycleService;
    private final SearchIndexSyncHook searchIndexSyncHook;
    private final AuditContentAnalyzer auditContentAnalyzer;
    public AdminPrimaryResourceServiceImpl(PrimaryChineseResourceMapper primaryChineseResourceMapper, ResourceAuditMapper resourceAuditMapper, AdminAuditService adminAuditService, AdminPermissionService adminPermissionService, ResourceLifecycleService resourceLifecycleService, SearchIndexSyncHook searchIndexSyncHook, AuditContentAnalyzer auditContentAnalyzer) {
        this.primaryChineseResourceMapper = primaryChineseResourceMapper;
        this.resourceAuditMapper = resourceAuditMapper;
        this.adminAuditService = adminAuditService;
        this.adminPermissionService = adminPermissionService;
        this.resourceLifecycleService = resourceLifecycleService;
        this.searchIndexSyncHook = searchIndexSyncHook;
        this.auditContentAnalyzer = auditContentAnalyzer;
    }


    /** 审核通过后是否自动上架，默认 false（审核与发布解耦） */
    @Value("${k12.audit.auto-publish:false}")
    private boolean auditAutoPublish;

    @Override
    public Page<AdminResourceListVO> listPage(AdminPrimaryResourceQueryDTO query, Long adminUserId) {
        DataScopeParams scope = resolveScopeParams(adminUserId);
        Page<PrimaryChineseResource> page = new Page<>(safePage(query.getCurrent()), safeSize(query.getSize()));
        String sortField = resolveSortField(query.getSortField());
        String sortOrder = "asc".equalsIgnoreCase(query.getSortOrder()) ? "ASC" : "DESC";

        IPage<PrimaryChineseResource> result = primaryChineseResourceMapper.adminFindPage(
                page,
                query.getStage(),
                query.getSubject(),
                query.getModule(),
                query.getType(),
                query.getGradeName(),
                query.getEdition(),
                query.getStatus(),
                query.getAuditStatus(),
                query.getIsRecommend(),
                query.getIsTop(),
                query.getIsFree(),
                query.getUploaderId(),
                query.getKeyword(),
                query.getStatus() == null,
                scope.stages(),
                scope.subjects(),
                scope.uploaderId(),
                sortField,
                sortOrder
        );

        Page<AdminResourceListVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        voPage.setRecords(result.getRecords().stream().map(AdminResourceListVO::fromEntity).toList());
        return voPage;
    }

    @Override
    public Page<AdminResourceListVO> listPending(AdminPrimaryResourceQueryDTO query, Long adminUserId) {
        query.setStatus(null);
        query.setAuditStatus(ResourceStatusConstants.AUDIT_PENDING);
        return listPage(query, adminUserId);
    }

    @Override
    public Map<String, Object> getResourceStats(Long adminUserId) {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalResources", primaryChineseResourceMapper.selectCount(scopedWrapper(adminUserId)));
        stats.put("pendingResources", primaryChineseResourceMapper.selectCount(
                scopedWrapper(adminUserId).eq(PrimaryChineseResource::getAuditStatus, ResourceStatusConstants.AUDIT_PENDING)));
        stats.put("approvedResources", primaryChineseResourceMapper.selectCount(
                scopedWrapper(adminUserId).eq(PrimaryChineseResource::getAuditStatus, ResourceStatusConstants.AUDIT_APPROVED)));
        List<PrimaryChineseResource> metrics = primaryChineseResourceMapper.selectList(
                scopedWrapper(adminUserId).select(PrimaryChineseResource::getDownloadCount, PrimaryChineseResource::getViewCount));
        stats.put("totalDownloads", metrics.stream().mapToLong(r -> nvl(r.getDownloadCount())).sum());
        stats.put("totalViews", metrics.stream().mapToLong(r -> nvl(r.getViewCount())).sum());
        return stats;
    }

    @Override
    public AdminResourceDetailVO getDetail(Long id, Long adminUserId) {
        PrimaryChineseResource resource = requireAccessible(id, adminUserId);
        return AdminResourceDetailVO.fromEntity(resource);
    }

    @Override
    @Transactional
    public void update(Long id, AdminResourceUpdateDTO dto, Long adminUserId) {
        PrimaryChineseResource resource = requireAccessible(id, adminUserId);
        if (StringUtils.hasText(dto.getTitle())) {
            resource.setTitle(dto.getTitle());
        }
        if (dto.getModule() != null) {
            resource.setModule(dto.getModule());
        }
        if (dto.getType() != null) {
            resource.setType(dto.getType());
        }
        if (dto.getGradeName() != null) {
            resource.setGradeName(dto.getGradeName());
        }
        if (dto.getEdition() != null) {
            resource.setEdition(dto.getEdition());
        }
        if (dto.getUnitName() != null) {
            resource.setUnitName(dto.getUnitName());
        }
        if (dto.getLessonName() != null) {
            resource.setLessonName(dto.getLessonName());
        }
        if (dto.getRemark() != null) {
            resource.setRemark(dto.getRemark());
        }
        if (dto.getIsFree() != null) {
            resource.setIsFree(dto.getIsFree());
        }
        if (dto.getSort() != null) {
            resource.setSort(dto.getSort());
        }
        if (dto.getCatalogNodeId() != null) {
            resource.setCatalogNodeId(dto.getCatalogNodeId());
        }
        if (dto.getCatalogPath() != null) {
            resource.setCatalogPath(dto.getCatalogPath());
        }

                primaryChineseResourceMapper.updateById(resource);
        if (Integer.valueOf(ResourceStatusConstants.PUBLISHED).equals(resource.getStatus())) {
            syncSearchIndex(resource.getId());
        }
    }

    @Override
    @Transactional
    public void publish(Long id, Long adminUserId) {
        PrimaryChineseResource resource = requireAccessible(id, adminUserId);
        resourceLifecycleService.validatePublish(resource);
        resource.setStatus(ResourceStatusConstants.PUBLISHED);
        resource.setPublishStatus(ResourceStatusConstants.PUBLISH_PUBLISHED);
        syncLifecycleFields(resource);

                primaryChineseResourceMapper.updateById(resource);
        syncSearchIndex(id);
    }

    @Override
    @Transactional
    public void offline(Long id, Long adminUserId) {
        PrimaryChineseResource resource = requireAccessible(id, adminUserId);
        resourceLifecycleService.validateOffline(resource);
        resource.setStatus(ResourceStatusConstants.OFFLINE);
        syncLifecycleFields(resource);

                primaryChineseResourceMapper.updateById(resource);
        syncSearchIndex(id);
    }

    @Override
    @Transactional
    public void setRecommend(Long id, boolean recommend, Long adminUserId) {
        PrimaryChineseResource resource = requireAccessible(id, adminUserId);
        resourceLifecycleService.validateRecommend(resource);
        resource.setIsRecommend(recommend ? 1 : 0);

                primaryChineseResourceMapper.updateById(resource);
    }

    @Override
    @Transactional
    public void setTop(Long id, boolean top, Integer topSort, Long adminUserId) {
        PrimaryChineseResource resource = requireAccessible(id, adminUserId);
        resourceLifecycleService.validateTop(resource);
        resource.setIsTop(top ? 1 : 0);
        if (topSort != null) {
            resource.setTopSort(topSort);
        }

                primaryChineseResourceMapper.updateById(resource);
    }

    @Override
    @Transactional
    public void moveToRecycle(Long id, Long adminUserId) {
        PrimaryChineseResource resource = requireAccessible(id, adminUserId);
        resourceLifecycleService.validateMoveToRecycle(resource);
        resource.setStatus(ResourceStatusConstants.DELETED);
        syncLifecycleFields(resource);

                primaryChineseResourceMapper.updateById(resource);
        syncSearchIndex(id);
    }

    @Override
    @Transactional
    public void auditResource(Long id, Integer status, String reason, Long auditorId, String auditorName) {
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
            if (!StringUtils.hasText(reason)) {
                throw new BusinessException(400, "驳回时必须填写原因");
            }
        } else {
            throw new BusinessException(400, "无效的审核状态");
        }

        PrimaryChineseResource resource = requireAccessible(id, auditorId);
        resourceLifecycleService.validateAudit(resource);
        int beforeStatus = resource.getStatus() != null ? resource.getStatus() : ResourceStatusConstants.PENDING;

        String action;
        if (status == ResourceStatusConstants.PUBLISHED) {
            // 审核通过 — 半分离：auditStatus 和 publishStatus 独立管理
            resource.setAuditStatus(ResourceStatusConstants.AUDIT_APPROVED);
            if (auditAutoPublish) {
                resource.setStatus(ResourceStatusConstants.PUBLISHED);
                resource.setPublishStatus(ResourceStatusConstants.PUBLISH_PUBLISHED);
                action = "approve_publish";
            } else {
                resource.setStatus(ResourceStatusConstants.OFFLINE);
                resource.setPublishStatus(ResourceStatusConstants.PUBLISH_UNPUBLISHED);
                action = "approve_audit";
            }
        } else {
            // 驳回
            resource.setStatus(ResourceStatusConstants.REJECTED);
            resource.setAuditStatus(ResourceStatusConstants.AUDIT_REJECTED);
            resource.setPublishStatus(ResourceStatusConstants.PUBLISH_UNPUBLISHED);
            action = "reject";
        }
        primaryChineseResourceMapper.updateById(resource);

        ResourceAudit audit = new ResourceAudit();
        audit.setResourceId(id);
        audit.setStatus(status);
        audit.setReason(reason);
        audit.setAuditorId(auditorId);
        audit.setAuditorName(auditorName);
        resourceAuditMapper.insert(audit);

        int afterStatus = resource.getStatus() != null ? resource.getStatus() : ResourceStatusConstants.PENDING;
        adminAuditService.appendAuditLog(id, auditorId, auditorName, action, beforeStatus, afterStatus, reason);

        if (ResourceStatusConstants.PUBLISH_PUBLISHED == resource.getPublishStatus()) {
            syncSearchIndex(id);
        }
    }

    @Override
    @Transactional
    public AdminResourceBatchResultVO batchAudit(List<Long> ids, String action, String reason,
                                                 Long auditorId, String auditorName) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException(400, "请选择资源");
        }
        if (!StringUtils.hasText(action)) {
            throw new BusinessException(400, "请指定审核操作");
        }
        int targetStatus;
        switch (action.toLowerCase()) {
            case "approve" -> targetStatus = ResourceStatusConstants.PUBLISHED;
            case "reject" -> {
                targetStatus = ResourceStatusConstants.REJECTED;
                if (!StringUtils.hasText(reason)) {
                    throw new BusinessException(400, "批量驳回时必须填写原因");
                }
            }
            default -> throw new BusinessException(400, "不支持的审核操作: " + action);
        }

        int successCount = 0;
        int skipCount = 0;
        List<String> skipReasons = new ArrayList<>();
        for (Long id : ids) {
            try {
                PrimaryChineseResource resource = requireAccessible(id, auditorId);
                if (!PublicResourceQuerySupport.isPendingAudit(resource)) {
                    skipCount++;
                    skipReasons.add(String.format("#%d（非待审核）", id));
                    continue;
                }
                auditResource(id, targetStatus, reason, auditorId, auditorName);
                successCount++;
            } catch (BusinessException ex) {
                skipCount++;
                skipReasons.add(String.format("#%d（%s）", id, ex.getMessage()));
            }
        }

        if (successCount == 0) {
            throw new BusinessException(400, "所选资源均无法审核：" + String.join("、",
                    skipReasons.subList(0, Math.min(3, skipReasons.size()))));
        }
        String message = skipCount > 0
                ? String.format("成功 %d 条，跳过 %d 条", successCount, skipCount)
                : String.format("成功 %d 条", successCount);
        return AdminResourceBatchResultVO.of(successCount, skipCount, skipReasons, message);
    }

    @Override
    public AdminResourceAuditInsightsVO getAuditInsights(Long id, Long adminUserId) {
        PrimaryChineseResource resource = requireAccessible(id, adminUserId);
        AdminResourceAuditInsightsVO vo = new AdminResourceAuditInsightsVO();
        vo.setResourceId(id);
        vo.setSensitiveWords(auditContentAnalyzer.detectSensitiveWords(resource));
        AuditContentAnalyzer.FileSafety safety = auditContentAnalyzer.assessFileSafety(resource);
        vo.setFileSafetyStatus(safety.status());
        vo.setFileSafetyMessage(safety.message());

        List<Map<String, Object>> duplicates = primaryChineseResourceMapper.findDuplicateCandidates(
                id, resource.getTitle(), resource.getOriginalFilename(), resource.getOssObjectKey());
        List<DuplicateResourceHintVO> hints = new ArrayList<>();
        for (Map<String, Object> row : duplicates) {
            DuplicateResourceHintVO hint = new DuplicateResourceHintVO();
            hint.setId(toLong(row.get("id")));
            hint.setTitle(row.get("title") != null ? String.valueOf(row.get("title")) : null);
            hint.setStatus(toInt(row.get("status")));
            if (hint.getStatus() != null) {
                hint.setStatusLabel(ResourceStatusConstants.auditStatusLabel(null, hint.getStatus())
                        + " / " + ResourceStatusConstants.publishStatusLabel(null, hint.getStatus()));
            }
            hint.setMatchType(row.get("matchType") != null ? String.valueOf(row.get("matchType")) : null);
            hints.add(hint);
        }
        vo.setDuplicateHints(hints);
        return vo;
    }

    @Override
    @Transactional
    public void restoreFromRecycle(Long id, Long adminUserId) {
        PrimaryChineseResource resource = requireAccessible(id, adminUserId);
        resourceLifecycleService.validateRestore(resource);
        resource.setStatus(ResourceStatusConstants.OFFLINE);
        syncLifecycleFields(resource);

                primaryChineseResourceMapper.updateById(resource);
        syncSearchIndex(id);
    }

    @Override
    @Transactional
    public AdminResourceBatchResultVO batchAction(List<Long> ids, String action, Long adminUserId) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException(400, "请选择资源");
        }
        if (!StringUtils.hasText(action)) {
            throw new BusinessException(400, "请指定批量操作类型");
        }

        int successCount = 0;
        int skipCount = 0;
        List<String> skipReasons = new ArrayList<>();

        for (Long id : ids) {
            PrimaryChineseResource resource = requireAccessible(id, adminUserId);
            if (!isBatchApplicable(action, resource)) {
                skipCount++;
                int status = resource.getStatus() != null ? resource.getStatus() : 0;
                skipReasons.add(String.format("#%d（%s）", id,
                        ResourceStatusConstants.auditStatusLabel(resource.getAuditStatus(), status) + " / "
                                + ResourceStatusConstants.publishStatusLabel(resource.getPublishStatus(), status)));
                continue;
            }
            applyBatchAction(action, resource);
            if (needsSearchSync(action)) {
                syncSearchIndex(resource.getId());
            }
            successCount++;
        }

        if (successCount == 0) {
            throw new BusinessException(400, buildBatchFailMessage(action, skipReasons));
        }

        String message = skipCount > 0
                ? String.format("成功 %d 条，跳过 %d 条", successCount, skipCount)
                : String.format("成功 %d 条", successCount);
        return AdminResourceBatchResultVO.of(successCount, skipCount, skipReasons, message);
    }

    private boolean isBatchApplicable(String action, PrimaryChineseResource resource) {
        int status = resource.getStatus() != null ? resource.getStatus() : 0;
        return switch (action) {
            case "publish" -> resourceLifecycleService.canPublish(resource);
            case "offline" -> resourceLifecycleService.canOffline(status);
            case "recommend", "unrecommend", "top", "untop" -> resourceLifecycleService.canRecommend(status);
            case "recycle" -> resourceLifecycleService.canMoveToRecycle(status);
            case "restore" -> resourceLifecycleService.canRestore(status);
            default -> throw new BusinessException(400, "不支持的批量操作: " + action);
        };
    }

    private void applyBatchAction(String action, PrimaryChineseResource resource) {
        switch (action) {
            case "publish" -> {
                resource.setStatus(ResourceStatusConstants.PUBLISHED);

                syncLifecycleFields(resource);
                primaryChineseResourceMapper.updateById(resource);
            }
            case "offline" -> {
                resource.setStatus(ResourceStatusConstants.OFFLINE);

                syncLifecycleFields(resource);
                primaryChineseResourceMapper.updateById(resource);
            }
            case "recommend" -> {
                resource.setIsRecommend(1);

                primaryChineseResourceMapper.updateById(resource);
            }
            case "unrecommend" -> {
                resource.setIsRecommend(0);

                primaryChineseResourceMapper.updateById(resource);
            }
            case "top" -> {
                resource.setIsTop(1);

                primaryChineseResourceMapper.updateById(resource);
            }
            case "untop" -> {
                resource.setIsTop(0);

                primaryChineseResourceMapper.updateById(resource);
            }
            case "recycle" -> {
                resource.setStatus(ResourceStatusConstants.DELETED);

                syncLifecycleFields(resource);
                primaryChineseResourceMapper.updateById(resource);
            }
            case "restore" -> {
                resource.setStatus(ResourceStatusConstants.OFFLINE);

                syncLifecycleFields(resource);
                primaryChineseResourceMapper.updateById(resource);
            }
            default -> throw new BusinessException(400, "不支持的批量操作: " + action);
        }
    }

    private String buildBatchFailMessage(String action, List<String> skipReasons) {
        String requirement = switch (action) {
            case "publish" -> "批量发布仅适用于「审核通过且未上架」资源";
            case "offline" -> "批量下架仅适用于「已上架」资源";
            case "recommend", "unrecommend", "top", "untop" -> "该操作仅适用于「已上架」资源";
            case "recycle" -> "移入回收站不适用于「已上架」资源，请先下架";
            case "restore" -> "仅「回收站」资源可恢复";
            default -> "所选资源均无法执行该操作";
        };
        String samples = String.join("、", skipReasons.subList(0, Math.min(3, skipReasons.size())));
        if (skipReasons.size() > 3) {
            samples += " 等";
        }
        return requirement + "：" + samples;
    }

    private boolean needsSearchSync(String action) {
        return "publish".equals(action) || "offline".equals(action) || "recycle".equals(action)
                || "restore".equals(action);
    }


    private void syncLifecycleFields(PrimaryChineseResource resource) {
        int status = resource.getStatus() != null ? resource.getStatus() : ResourceStatusConstants.PENDING;
        resource.setAuditStatus(ResourceStatusConstants.resolveAuditStatus(resource.getAuditStatus(), status));
        if (status == ResourceStatusConstants.PUBLISHED) {
            resource.setPublishStatus(ResourceStatusConstants.PUBLISH_PUBLISHED);
            return;
        }
        if (status == ResourceStatusConstants.OFFLINE
                && Integer.valueOf(ResourceStatusConstants.PUBLISH_PUBLISHED).equals(resource.getPublishStatus())) {
            resource.setPublishStatus(ResourceStatusConstants.PUBLISH_OFFLINE);
            return;
        }
        if (resource.getPublishStatus() == null) {
            resource.setPublishStatus(ResourceStatusConstants.resolvePublishStatus(null, status));
        }
    }
    private void syncSearchIndex(Long id) {
        searchIndexSyncHook.afterPrimaryChanged(id);
    }

    private LambdaQueryWrapper<PrimaryChineseResource> scopedWrapper(Long adminUserId) {
        LambdaQueryWrapper<PrimaryChineseResource> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PrimaryChineseResource::getIsDeleted, 0);
        wrapper.ne(PrimaryChineseResource::getStatus, ResourceStatusConstants.DELETED);
        applyScopeToWrapper(wrapper, resolveScopeParams(adminUserId));
        return wrapper;
    }

    private void applyScopeToWrapper(LambdaQueryWrapper<PrimaryChineseResource> wrapper, DataScopeParams scope) {
        if (scope.uploaderId() != null) {
            wrapper.eq(PrimaryChineseResource::getUploaderId, scope.uploaderId());
        }
        if (!scope.stages().isEmpty()) {
            wrapper.in(PrimaryChineseResource::getStage, scope.stages());
        }
        if (!scope.subjects().isEmpty()) {
            wrapper.in(PrimaryChineseResource::getSubject, scope.subjects());
        }
    }

    private long nvl(Integer value) {
        return value == null ? 0L : value.longValue();
    }

    private PrimaryChineseResource requireAccessible(Long id, Long adminUserId) {
        PrimaryChineseResource resource = primaryChineseResourceMapper.selectById(id);
        resourceLifecycleService.assertExists(resource);
        assertInDataScope(resource, adminUserId);
        return resource;
    }

    private void assertInDataScope(PrimaryChineseResource resource, Long adminUserId) {
        DataScopeParams scope = resolveScopeParams(adminUserId);
        if (scope.uploaderId() != null && !scope.uploaderId().equals(resource.getUploaderId())) {
            throw new BusinessException(403, "无权访问该资源");
        }
        if (!scope.stages().isEmpty() && !scope.stages().contains(resource.getStage())) {
            throw new BusinessException(403, "无权访问该资源");
        }
        if (!scope.subjects().isEmpty() && !scope.subjects().contains(resource.getSubject())) {
            throw new BusinessException(403, "无权访问该资源");
        }
    }

    private DataScopeParams resolveScopeParams(Long adminUserId) {
        AdminDataScopeVO scope = adminPermissionService.resolveDataScope(adminUserId);
        if ("ALL".equals(scope.getScopeType())) {
            return new DataScopeParams(List.of(), List.of(), null);
        }
        if ("SELF".equals(scope.getScopeType())) {
            return new DataScopeParams(List.of(), List.of(), adminUserId);
        }
        List<String> stages = new ArrayList<>();
        List<String> subjects = new ArrayList<>();
        if ("STAGE_SUBJECT".equals(scope.getScopeType())) {
            for (String code : scope.getStages()) {
                stages.add(STAGE_MAP.getOrDefault(code, code));
            }
            for (String code : scope.getSubjects()) {
                subjects.add(SUBJECT_MAP.getOrDefault(code, code));
            }
        }
        return new DataScopeParams(stages, subjects, null);
    }

    private String resolveSortField(String sortField) {
        if (!StringUtils.hasText(sortField)) {
            return "upload_time";
        }
        String normalized = sortField.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
        if (!SORT_FIELDS.contains(normalized)) {
            return "upload_time";
        }
        return normalized;
    }

    private int safePage(Integer current) {
        return current == null || current < 1 ? 1 : current;
    }

    private int safeSize(Integer size) {
        if (size == null || size < 1) {
            return 15;
        }
        return Math.min(size, 100);
    }

    private Long toLong(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number n) {
            return n.longValue();
        }
        return Long.parseLong(String.valueOf(value));
    }

    private Integer toInt(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number n) {
            return n.intValue();
        }
        return Integer.parseInt(String.valueOf(value));
    }

    private record DataScopeParams(List<String> stages, List<String> subjects, Long uploaderId) {
    }
}



